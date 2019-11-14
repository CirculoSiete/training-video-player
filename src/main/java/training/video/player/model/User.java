package training.video.player.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
  private String name;
  private String username;
  private Long id;
  private String email;
  private String avatarUrl;
  private Boolean isAdmin;
}
