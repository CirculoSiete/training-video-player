package training.video.player.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Course {
  private Integer id;
  private String name;
  private String path;
  private String description;
  //private Visibility visibility;
  private Boolean lfsEnabled;
  private String avatarUrl;
  private String webUrl;
  private List<CourseSession> sessions;
}
