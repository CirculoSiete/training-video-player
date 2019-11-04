package training.video.player;

import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.views.ModelAndView;

@Controller("/")
public class IndexController {

  @Get
  public ModelAndView index() {
    //GitLabApi gitLabApi = new GitLabApi(GitLabApi.ApiVersion.V3, "http://your.gitlab.server.com", "YOUR_ACCESS_TOKEN");

    var data = CollectionUtils.mapOf("name", "Círculo Siete", "avatar", "https://es.gravatar.com/userimage/2127112/4267fe3a6281a375329f061798691634.jpeg");

    return new ModelAndView("index", data);
  }
}
