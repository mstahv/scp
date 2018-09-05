package org.vaadin.scp;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
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
		SpringApplication.run(SailingCoursePlannerApplication.class, args);
	}


	@Bean
	CommandLineRunner init(CourseService service, SailingCourseRepository repo, MainBuoyRepository buoyRepository, HelperBuoyRepository helperBuoyRepository) {
		return (String... strings) -> {
			// Insert some demo data
			SailingCourse course = new SailingCourse();

			course = repo.save(course);

			MainBuoy buoy1 = service.addBuoy(course, new Coordinate(0,0));
			MainBuoy buoy2 = service.addBuoy(course, new Coordinate(0,2));
			MainBuoy buoy3 = service.addBuoy(course, new Coordinate(-1,1));

			service.addHelperBuoy(buoy1, new Coordinate(0.5,0));
			service.addHelperBuoy(buoy2, new Coordinate(0.5,2));

			course.getCoursePoints().add(buoy1);
			course.getCoursePoints().add(buoy2);
			course.getCoursePoints().add(buoy3);
			course.getCoursePoints().add(buoy1);
			course.getCoursePoints().add(buoy2);

			repo.save(course);

		};
	}
}
