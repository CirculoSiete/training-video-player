package training.video.player.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Data
@Builder
public class Breadcrum {
  private List<BreadCrumItem> items;

  public static Breadcrum from(Map<String, String> data) {
    List<BreadCrumItem> items = data.entrySet().stream()
      .map(entry -> BreadCrumItem.from(
        entry.getKey(),
        entry.getValue()))
      .collect(toList());

    return Breadcrum.builder().items(items).build();
  }

  public static Breadcrum from(Stream<BreadCrumItem> items) {
    return Breadcrum.builder()
      .items(items.collect(toList()))
      .build();
  }
}
