package org.vaadin.scp.plannerui;

import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.VerticalLayout;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.vaadin.addon.leaflet.LMarker;
import org.vaadin.addon.leaflet.LPolyline;
import org.vaadin.scp.jpa.HelperBuoy;
import org.vaadin.scp.jpa.MainBuoy;
import org.vaadin.scp.jpa.SailingCourse;
import org.vaadin.scp.jpa.SailingCourseRepository;
import org.vaadin.viritin.grid.MGrid;
import org.vaadin.viritin.label.MLabel;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by mstahv
 */
@SpringComponent
@UIScope
public class CourseEditor extends MHorizontalLayout {

    private final SailingCourseRepository courseRepo;
    private SailingCourse course;

    SailingBaseMap map = new SailingBaseMap();

    VerticalLayout details = new MVerticalLayout().withUndefinedWidth();

    MGrid<MainBuoy> buoyTable = new MGrid<>(MainBuoy.class).withCaption("MainBuoy")
            .withWidth("300px");

    public CourseEditor(SailingCourseRepository courseRepository) {
        this.courseRepo = courseRepository;
        setSizeFull();
        expand(map);

        details.addComponent(new MLabel("Edit course").withUndefinedWidth());
        details.addComponents(buoyTable);
        add(details);

    }

    public void editCourse(SailingCourse course) {
        this.course = courseRepo.findOneWithDetailsById(course.getId());
        System.err.println(this.course.getCoursePoints());
        buoyTable.setItems(new ArrayList<>(this.course.getCoursePoints()));
        drawCourse();
    }

    private void drawCourse() {
        map.clear();
        Set<MainBuoy> mainBuoys = course.getMainBuoys();
        mainBuoys.forEach(b -> {
            Point location = b.getLocation();
            LMarker marker = new LMarker(location);
            map.addLayer(marker);
        });

        drawRouteLine();

        for(HelperBuoy hb : course.getHelperBuoys()) {
            LMarker helperBuoy = new LMarker(hb.getLocation());
            helperBuoy.setIcon(FontAwesome.BULLSEYE);
            helperBuoy.setIconPathFill("gray");
            map.addLayer(helperBuoy);
            LPolyline connector = new LPolyline(hb.getConnectionLine());
            connector.setDashArray("8, 4");
            connector.setColor("gray");
            map.addLayer(connector);
        }

        map.zoomToContent();
    }

    private void drawRouteLine() {
        GeometryFactory factory = new GeometryFactory();
        Coordinate[] coordinates = new Coordinate[course.getCoursePoints().size()];
        for(int i = 0; i < course.getCoursePoints().size();i++) {
            coordinates[i] = course.getCoursePoints().get(i).getLocation().getCoordinate();
        }
        LPolyline routeLine = new LPolyline(factory.createLineString(coordinates));
        map.addLayer(routeLine);
    }

}
