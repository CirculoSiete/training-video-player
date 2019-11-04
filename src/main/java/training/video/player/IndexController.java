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

    var data = CollectionUtils.mapOf("name", "Attendee", "avatar", "https://www.gravatar.com/avatar/f2f6aa8f9b52814cd7c2eb7eecb9cda1.png");

    return new ModelAndView("index", data);
  }
}
