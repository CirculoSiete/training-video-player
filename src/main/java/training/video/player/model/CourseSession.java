package training.video.player.model;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Builder
public class CourseSession {
  private String name;
  private String videoUrl;

  @Tolerate
  public CourseSession() {
    log.info("Creating CourseSession..");
  }
}
