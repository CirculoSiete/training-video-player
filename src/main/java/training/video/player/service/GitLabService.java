package training.video.player.service;

import io.micronaut.context.annotation.Value;
import io.micronaut.discovery.event.ServiceStartedEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.scheduling.annotation.Async;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Member;
import org.gitlab4j.api.models.User;

import javax.inject.Singleton;
import java.util.Optional;

import static org.gitlab4j.api.Constants.TokenType.ACCESS;

@Slf4j
@Singleton
public class GitLabService {
  @Value("${gitlaburl}")
  private String gitlabUrl;

  @Value("${accesstoken}")
  private String accesstoken;

  @Value("${devopsgroup}")
  private String devopsgroup;

  private GitLabApi gitLabApi;

  @Async
  @EventListener
  public void init(ServiceStartedEvent event) {
    log.info("Using GitLab at '{}'", gitlabUrl);
    gitLabApi = new GitLabApi(gitlabUrl, ACCESS, accesstoken);
  }

  public Optional<User> findUser(String email) {
    return gitLabApi.getUserApi().getOptionalUserByEmail(email);
  }

  public Optional<Member> findDevOpsMembership(Integer userId) {
    return findMembership(devopsgroup, userId);
  }

  public Optional<Member> findMembership(String groupIdOrPath, Integer userId) {
    log.info("Trying to find membership of {} in {}", userId, groupIdOrPath);
    return gitLabApi.getGroupApi().getOptionalMember(groupIdOrPath, userId);
  }
}
