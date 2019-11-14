package training.video.player.service;

import io.micronaut.context.annotation.Value;
import io.micronaut.discovery.event.ServiceStartedEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.scheduling.annotation.Async;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Group;
import org.gitlab4j.api.models.ImpersonationToken;
import org.gitlab4j.api.models.Member;
import org.gitlab4j.api.models.User;

import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.gitlab4j.api.Constants.TokenType.ACCESS;
import static org.gitlab4j.api.models.ImpersonationToken.Scope.API;
import static org.gitlab4j.api.models.ImpersonationToken.Scope.READ_USER;

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

  public List<Group> foo(Integer id) {
    try {
      LocalDateTime ahorita = LocalDateTime.now().plusDays(1);
      Date date = Date.from(ahorita.atZone(ZoneId.systemDefault()).toInstant());

      ImpersonationToken ahorita_nomas = gitLabApi.getUserApi()
        .createImpersonationToken(id, "ahorita nomas", date,
          new ImpersonationToken.Scope[]{API, READ_USER});

      var gitLabApi2 = new GitLabApi(gitlabUrl, ahorita_nomas.getToken());
      List<Group> groups = gitLabApi2.getGroupApi().getGroups();

      gitLabApi.getUserApi().revokeImpersonationToken(id, ahorita_nomas.getId());
      return groups;
    } catch (GitLabApiException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public training.video.player.model.Group from(Group source) {
    return training.video.player.model.Group.builder()
      .avatarUrl(source.getAvatarUrl())
      .description(source.getDescription())
      .fullName(source.getFullName())
      .fullPath(source.getFullPath())
      .id(source.getId())
      .name(source.getName())
      .path(source.getPath())
      .webUrl(source.getWebUrl())
      .build();
  }

  public List<training.video.player.model.Group> from(List<Group> source) {
    return source.stream()
      .map(this::from)
      .collect(toList());
  }

  public training.video.player.model.User from(User source) {
    return training.video.player.model.User.builder()
      .avatarUrl(source.getAvatarUrl())
      .email(source.getEmail())
      .id(source.getId())
      .isAdmin(source.getIsAdmin())
      .name(source.getName())
      .username(source.getUsername())
      .build();
  }

  public Optional<User> findUser(String email) {
    return gitLabApi.getUserApi().getOptionalUserByEmail(email);
  }

  public Optional<Member> findDevOpsMembership(Integer userId) {
    return findMembership(devopsgroup, userId);
  }

  public Optional<Member> findMembership(String groupIdOrPath, Integer userId) {
    //TODO: validar que exista el grupo del curso
    log.info("Trying to find membership of {} in {}", userId, groupIdOrPath);
    return gitLabApi.getGroupApi().getOptionalMember(groupIdOrPath, userId);
  }

  public Group getGroup(String id) {
    try {
      return gitLabApi.getGroupApi().getGroup(id);
    } catch (GitLabApiException e) {
      //TODO: mejorar esto
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
