package training.video.player.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BreadCrumItem {
  private String name;
  private String uri;

  public static BreadCrumItem from(String name, String uri) {
    return BreadCrumItem.builder()
      .name(name)
      .uri(uri)
      .build();
  }
}
