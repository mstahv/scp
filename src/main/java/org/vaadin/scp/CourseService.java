package org.vaadin.scp;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vaadin.scp.jpa.HelperBuoy;
import org.vaadin.scp.jpa.HelperBuoyRepository;
import org.vaadin.scp.jpa.MainBuoy;
import org.vaadin.scp.jpa.MainBuoyRepository;
import org.vaadin.scp.jpa.SailingCourse;
import org.vaadin.scp.jpa.SailingCourseRepository;

/**
 * Created by mstahv
 */

@Service
public class CourseService {

    private static String[] mainBouyLabels = new String[] {"A", "B", "C", "D", "E", "F","G","H","I","J","K","L","M","N", "O","P","Q","R", "S","T","U"};

    private HelperBuoyRepository helperBuoyRepository;
    private MainBuoyRepository mainBuoyRepository;
    private SailingCourseRepository sailingCourseRepository;
    GeometryFactory geometryFactory = new GeometryFactory();

    public CourseService(HelperBuoyRepository helperBuoyRepository, MainBuoyRepository mainBuoyRepository, SailingCourseRepository sailingCourseRepository) {
        this.helperBuoyRepository = helperBuoyRepository;
        this.mainBuoyRepository = mainBuoyRepository;
        this.sailingCourseRepository = sailingCourseRepository;
    }

    public MainBuoy addBuoy(SailingCourse course, Coordinate location) {
        MainBuoy mainBuoy = new MainBuoy();
        mainBuoy.setCourse(course);
        mainBuoy.setLocation(geometryFactory.createPoint(location));
        int nextLabelIndex = course.getMainBuoys().size();
        mainBuoy.setName(mainBouyLabels[nextLabelIndex%mainBouyLabels.length]);
        //mainBuoy = mainBuoyRepository.save(mainBuoy);
        course.getMainBuoys().add(mainBuoy);
        return mainBuoy;
    }

    public HelperBuoy addHelperBuoy(SailingCourse course, MainBuoy mainBuoy, Coordinate location) {
        HelperBuoy helperBuoy = new HelperBuoy();
        helperBuoy.setCourse(course);
        if(mainBuoy != null) {
            helperBuoy.setMainBuoy(mainBuoy);
            helperBuoy.setName(mainBuoy.getName() + ".1");
        } else {
            helperBuoy.setName("" + (course.getHelperBuoys().size() + 1 ));
        }
        helperBuoy.setLocation(geometryFactory.createPoint(location));
        //helperBuoy = helperBuoyRepository.save(helperBuoy);
        course.getHelperBuoys().add(helperBuoy);
        return helperBuoy;
    }

    @Transactional
    public SailingCourse findAttached(SailingCourse detached) {
        return sailingCourseRepository.findOneWithDetailsById(detached.getId());
    }

    public void removeBuoy(MainBuoy buoy) {
        SailingCourse course = buoy.getCourse();
        while(course.getCoursePoints().remove(buoy)) {
        }
        course.getMainBuoys().remove(buoy);
        buoy.setCourse(null);
    }

    public void saveBuoy(MainBuoy buoy) {
        mainBuoyRepository.save(buoy);
    }

    public void removeBuoy(HelperBuoy buoy) {
        SailingCourse course = buoy.getCourse();
        course.getHelperBuoys().remove(buoy);
    }

    public void saveBuoy(HelperBuoy buoy) {
        helperBuoyRepository.save(buoy);
    }

    public void save(SailingCourse course) {
        sailingCourseRepository.save(course);
    }
}
