package training.video.player;

import io.micronaut.context.annotation.Value;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.views.ModelAndView;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.models.Group;
import org.gitlab4j.api.models.Project;
import training.video.player.service.GitLabService;

import java.util.*;

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

  @Get
  public ModelAndView index(HttpRequest<?> request) {
    var data = Map.of("name", "Círculo Siete", "avatar", "https://es.gravatar.com/userimage/2127112/4267fe3a6281a375329f061798691634.jpeg");

    Optional<Cookie> cookie = request.getCookies().findCookie(OAUTH_2_PROXY);
    if (cookie.isEmpty() && !testing) {
      log.info("No se tiene acceso. TESTING: {}", testing);
      return new ModelAndView("no_access", data);
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
      return new ModelAndView("no_access", data);
    }

    var userEmail = first.get();

    log.info("Email del usuario [{}]", userEmail);

    var optionalUserByEmail = gitLabService.findUser(userEmail);

    if (optionalUserByEmail.isEmpty()) {
      log.info("No se encontro al usuario con email [{}]", userEmail);
      return new ModelAndView("user_not_found", data);
    }

    var user = optionalUserByEmail.get();
    var groups = gitLabService.foo(user.getId());

    log.info("User is member of {} groups", groups.size());

    var optionalMember = gitLabService.findDevOpsMembership(user.getId());

    if (optionalMember.isEmpty()) {
      log.info("El usuario [{}], no se encuentra en el grupo", user.getId());
      return new ModelAndView("no_access", data);
    }

    var foo = Map.of("name", user.getName(), "avatar", user.getAvatarUrl());

    return new ModelAndView("index", foo);
  }

  private String emailForTesting() {
    log.info("DefaultEmail: {}", defaultEmail);
    return testing ? (hasText(defaultEmail) ? defaultEmail : null) : null;
  }
}
