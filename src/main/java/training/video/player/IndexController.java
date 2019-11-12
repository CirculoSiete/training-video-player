package training.video.player;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.views.ModelAndView;
import lombok.extern.slf4j.Slf4j;
import training.video.player.service.GitLabService;

import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.apache.commons.validator.routines.EmailValidator.getInstance;

@Slf4j
@Controller("/")
public class IndexController {
  public static final String OAUTH_2_PROXY = "_oauth2_proxy";
  private final GitLabService gitLabService;

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
    if (cookie.isEmpty()) {
      return new ModelAndView("no_access", data);
    }

    var value = cookie.get().getValue();
    log.info("Valor obtenido de la cookie de email [{}]", value);

    Optional<String> first = Stream.of(value.split("\\|"))
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
      .findFirst();

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

    var optionalMember = gitLabService.findDevOpsMembership(user.getId());

    if (optionalMember.isEmpty()) {
      log.info("El usuario [{}], no se encuentra en el grupo [{}]", user.getId());
      return new ModelAndView("no_access", data);
    }

    var foo = Map.of("name", user.getName(), "avatar", user.getAvatarUrl());

    return new ModelAndView("index", foo);
  }
}
