package training.video.player.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Group {
  private Integer id;
  private String name;
  private String path;
  private String description;
  private String avatarUrl;
  private String webUrl;
  private String fullName;
  private String fullPath;
}
