package org.vaadin.scp;

import com.vaadin.server.VaadinServlet;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.util.Arrays;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.vaadin.scp.jpa.HelperBuoy;
import org.vaadin.scp.jpa.HelperBuoyRepository;
import org.vaadin.scp.jpa.MainBuoy;
import org.vaadin.scp.jpa.MainBuoyRepository;
import org.vaadin.scp.jpa.SailingCourse;
import org.vaadin.scp.jpa.SailingCourseRepository;

@SpringBootApplication
public class SailingCoursePlannerApplication {

    public static void main(String[] args) {
        System.setProperty("org.geotools.referencing.forceXY", "true");
        SpringApplication.run(SailingCoursePlannerApplication.class, args);
    }

    @Bean
    public VaadinServlet vaadinServlet() {
        return new SpringAwareTouchKitServlet();
    }

    @Bean
    CommandLineRunner init(CourseService service, SailingCourseRepository repo, MainBuoyRepository buoyRepository, HelperBuoyRepository helperBuoyRepository) {
        return (String... strings) -> {
            reallyBasicCourse(service, repo);
            //finland(service,repo);
        };
    }

    private void finland(CourseService service, SailingCourseRepository repo) {
        // Insert some demo data
        SailingCourse course = new SailingCourse();
        course.setCourseName("Test course");

        // course.getEditorEmails().add("matti@vaadin.com");
        course = repo.save(course);

        MainBuoy buoy1 = service.addBuoy(course, new Coordinate(22.9193536, 59.8295091));
        MainBuoy buoy2 = service.addBuoy(course, new Coordinate(27.0197533, 69.9090459));

        course.getCoursePoints().add(buoy1);
        course.getCoursePoints().add(buoy2);

        repo.save(course);
    }

    private void reallyBasicCourse(CourseService service, SailingCourseRepository repo) {
        if (repo.count() == 0) {
            // Insert some demo data
            SailingCourse course = new SailingCourse();
            course.setCourseName("Test course");
            course.getAdminEmails().add("matti.tahvonen@gmail.com");

            course = repo.save(course);

            MainBuoy buoy1 = service.addBuoy(course, new Coordinate(0, 0));
            MainBuoy buoy2 = service.addBuoy(course, new Coordinate(0, 2));
            MainBuoy buoy3 = service.addBuoy(course, new Coordinate(-1, 1));
            service.addBuoy(course, new Coordinate(-1, 2));

            service.addHelperBuoy(course, buoy1, new Coordinate(0.5, 0));
            service.addHelperBuoy(course, buoy2, new Coordinate(0.5, 2));

            course.getCoursePoints().add(buoy1);
            course.getCoursePoints().add(buoy2);
            course.getCoursePoints().add(buoy3);
            course.getCoursePoints().add(buoy1);
            course.getCoursePoints().add(buoy2);

            repo.save(course);
        }

    }
}
