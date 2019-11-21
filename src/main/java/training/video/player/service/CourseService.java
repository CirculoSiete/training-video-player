package training.video.player.service;

import io.micronaut.context.annotation.Value;
import io.micronaut.discovery.event.ServiceStartedEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.scheduling.annotation.Async;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;
import training.video.player.model.Course;
import training.video.player.model.CourseSession;

import javax.inject.Singleton;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;

@Slf4j
@Singleton
public class CourseService {
  @Value("${coursesdata}")
  private String coursesData;
  private Map<String, Course> courses;

  public Optional<Course> findCourse(String id) {
    return Optional.ofNullable(courses.get(id));
  }

  public Boolean hasVideos(String id) {
    return findCourse(id).isPresent();
  }

  @Async
  @EventListener
  public void init(ServiceStartedEvent event) {
    log.info("Courses file {}", coursesData);
    courses = new HashMap<>();

    Yaml yaml = new Yaml();

    try {
      File file = new File(coursesData);
      InputStream ios = new FileInputStream(file);
      Map<String, List<CourseSession>> load = yaml.load(ios);

      load.forEach((path, courseSessions) -> {
        var course = Course.builder().path(path).build();
        course.setSessions(courseSessions);

        courses.put(path, course);
      });


      ios.close();
    } catch (FileNotFoundException e) {
      log.error(format("Can't load courses file. '%s'", coursesData), e);
      throw new IllegalStateException(format("Can't load courses file. '%s'", coursesData), e);
    } catch (IOException e) {
      log.warn(e.getMessage(), e);
    }
  }
}
