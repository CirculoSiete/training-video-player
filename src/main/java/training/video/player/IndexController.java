package training.video.player;

import io.micronaut.context.annotation.Value;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.views.ModelAndView;
import lombok.extern.slf4j.Slf4j;
import training.video.player.model.BreadCrumItem;
import training.video.player.model.Breadcrum;
import training.video.player.model.Page;
import training.video.player.model.User;
import training.video.player.service.GitLabService;

import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static io.micronaut.core.util.StringUtils.hasText;
import static java.util.Optional.ofNullable;
import static java.util.stream.Stream.of;
import static org.apache.commons.validator.routines.EmailValidator.getInstance;

@Slf4j
@Controller("/")
public class IndexController {
  public static final String OAUTH_2_PROXY = "_oauth2_proxy";
  private final GitLabService gitLabService;
  @Value("${defaultemail:''}")
  private String defaultEmail;
  @Value("${testing:false}")
  private Boolean testing;


  public IndexController(GitLabService gitLabService) {
    this.gitLabService = gitLabService;
  }

  private static boolean isEmail(String s) {
    return getInstance().isValid(s);
  }

  @Get("/test")
  public ModelAndView layout() {
    var page = Page.builder()
      .name("Nombre de la página")
      .title("Título de la página")
      .build();
    var user = User.builder()
      .name("Círculo Siete")
      .avatarUrl("https://es.gravatar.com/userimage/2127112/4267fe3a6281a375329f061798691634.jpeg")
      .build();

    var breadcrum = Breadcrum.from(
      Map.of(
        "Index", "/",
        "Cursos 3", "/courses",
        "Cursos", "/courses"
      ));

    var data = Map.of(
      "user", user,
      "page", page,
      "breadcrum", breadcrum
    );

    return new ModelAndView("test", data);
  }


  @Get("/courses")
  public ModelAndView courses(HttpRequest<?> request) {
    return index(request);
  }

  @Get
  public ModelAndView index(HttpRequest<?> request) {

    var user = this.getUser(request);
    var groups = gitLabService.foo(user.getId());

    log.info("User is member of {} groups", groups.size());

    var page = Page.builder()
      .name("Cursos")
      .title("Título de la página")
      .build();

    var userToRender = gitLabService.from(user);

    var breadcrum = Breadcrum.from(Stream.of(
      BreadCrumItem.from("Home", "/"),
      BreadCrumItem.from("Cursos", "/courses")
    ));

    var dataToRender = Map.of(
      "user", userToRender,
      "groups", gitLabService.from(groups),
      "page", page,
      "breadcrum", breadcrum
    );

    return new ModelAndView("index", dataToRender);
  }

  private org.gitlab4j.api.models.User getUser(HttpRequest<?> request) {
    var data = Map.of("name", "Círculo Siete", "avatar", "https://es.gravatar.com/userimage/2127112/4267fe3a6281a375329f061798691634.jpeg");

    Optional<Cookie> cookie = request.getCookies().findCookie(OAUTH_2_PROXY);
    if (cookie.isEmpty() && !testing) {
      log.info("No se tiene acceso. TESTING: {}", testing);
      //TODO: lanzar una excepcion adecuada
      throw new RuntimeException("No acceso");
      //return new ModelAndView("no_access", data);
    }

    String cookieValue = cookie.isPresent() ? cookie.get().getValue() : "";

    log.info("Valor obtenido de la cookie de email [{}]", cookieValue);

    var first = ofNullable(
      of(cookieValue.split("\\|"))
        .filter(StringUtils::hasText)
        .map(s -> {
          try {
            log.info("Intentando decodear [{}]", s);
            String s1 = new String(Base64.getDecoder().decode(s));
            log.info("Se obtuvo valor decodeado [{}]", s1);
            return s1;
          } catch (Throwable t) {
            log.info("Fallando al decodear [{}]", s);
            return null;
          }
        })
        .filter(Objects::nonNull)
        .filter(IndexController::isEmail)
        .findFirst()
        .orElse(emailForTesting()));

    if (first.isEmpty()) {
      log.info("No se encontro email.");
      //TODO: lanzar una excepcion adecuada
      throw new RuntimeException("No acceso");
      //return new ModelAndView("no_access", data);
    }

    var userEmail = first.get();

    log.info("Email del usuario [{}]", userEmail);

    var optionalUserByEmail = gitLabService.findUser(userEmail);

    if (optionalUserByEmail.isEmpty()) {
      log.info("No se encontro al usuario con email [{}]", userEmail);
      //TODO: lanzar una excepcion adecuada
      throw new RuntimeException("user_not_found");
      //return new ModelAndView("user_not_found", data);
    }

    return optionalUserByEmail.get();
  }

  private String emailForTesting() {
    log.info("DefaultEmail: {}", defaultEmail);
    return testing ? (hasText(defaultEmail) ? defaultEmail : null) : null;
  }


  @Get("/courses/{courseId}")
  public ModelAndView courseDetail(HttpRequest<?> request, @PathVariable String courseId) {
    var user = getUser(request);
    var membership = gitLabService.findMembership(courseId, user.getId());

    var group = gitLabService.getGroup(courseId);

    if (membership.isEmpty()) {
      //TODO: mejorar esto
      return index(request);
    }

    var page = Page.builder()
      .name("Cursos")
      .title(group.getDescription())
      .build();

    var userToRender = gitLabService.from(user);

    var breadcrum = Breadcrum.from(Stream.of(
      BreadCrumItem.from("Home", "/"),
      BreadCrumItem.from("Cursos", "/courses"),
      BreadCrumItem.from(group.getDescription(), "/courses/" + courseId)
    ));

    var dataToRender = Map.of(
      "user", userToRender,
      "page", page,
      "breadcrum", breadcrum
    );

    return new ModelAndView("course_detail", dataToRender);
  }
}
