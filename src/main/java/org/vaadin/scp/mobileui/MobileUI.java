package org.vaadin.scp.mobileui;

import com.vaadin.annotations.Widgetset;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.LMarker;
import org.vaadin.addon.leaflet.LOpenStreetMapLayer;
import org.vaadin.addon.leaflet.LPolyline;
import org.vaadin.addon.leaflet.shared.TooltipState;
import org.vaadin.scp.CourseService;
import org.vaadin.scp.jpa.AbstractBuoy;
import org.vaadin.scp.jpa.HelperBuoy;
import org.vaadin.scp.jpa.MainBuoy;
import org.vaadin.scp.jpa.SailingCourse;
import org.vaadin.touchkit.ui.NavigationView;

/**
 * The UI for setting up the course using mobile device.
 *
 * Shows course based on the secret url and then guides personnel to the correct
 * places to set drop buoys.
 *
 * @author mstahv
 */
@SpringUI(path = "mobile/*")
@Widgetset("org.vaadin.scp.MobileAppWidgetSet")
public class MobileUI extends UI {

    private final CourseService service;
    private SailingCourse course;
    LMap map = new LMap();
    Label targetLabel = new Label("");
    private org.vaadin.addon.leaflet.shared.Point lastKnownPoint;
    private AbstractBuoy targetBuoy;

    MyPositionMarker myPositionMarker;
    LPolyline directionPolyline;

    GeodeticCalculator gc;
    private CoordinateReferenceSystem crs;
    private Double lastKnownSpeed;
    private Double lastKnownAccuracy;

    public MobileUI(CourseService service, MyPositionMarker myPositionMarker) {
        this.service = service;
        this.myPositionMarker = myPositionMarker;
        try {
            crs = CRS.decode("EPSG:4326");
            gc = new GeodeticCalculator(crs);
        } catch (FactoryException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected void init(VaadinRequest request) {
        final String uuid = request.getParameter("uuid");

        try {
            course = service.findAttachedByUUID(uuid);
        } catch (Exception e) {
            reportUnknownCourse();
            return;
        }
        if (course == null) {
            reportUnknownCourse();
            return;
        }

        NavigationView view = new NavigationView(course.getCourseName());

        AbsoluteLayout absoluteLayout = new AbsoluteLayout();
        map.addLayer(new LOpenStreetMapLayer());
        myPositionMarker.setMap(map);

        drawMainBuoys();

        drawRouteLine();

        drawHelperBuoys();

        map.zoomToContent();

        Notification usageTip = Notification.show("Click on buoys to change the navigation target.");
        usageTip.setDelayMsec(3000);

        absoluteLayout.addComponent(map);
        absoluteLayout.addComponent(targetLabel, "right:5px;top:5px;z-index:1000000;");

        view.setContent(absoluteLayout);
        setContent(view);

        map.locate(true, true, false);
        map.addLocateListener(e -> {
            lastKnownPoint = e.getPoint();
            lastKnownSpeed = e.getSpeed();
            lastKnownAccuracy = e.getAccuracy();
            if (targetBuoy != null) {
                updateTargetLabel();
            }
        });
    }

    public void drawHelperBuoys() {
        for (HelperBuoy hb : course.getHelperBuoys()) {
            LMarker marker = new LMarker(hb.getLocation());
            marker.setTooltip("G");
            TooltipState tooltipState = new TooltipState();
            tooltipState.sticky = true;
            marker.setTooltipState(tooltipState);
            marker.setIcon(VaadinIcons.BULLSEYE);
            marker.setIconPathFill("gray");
            marker.addClickListener(e -> {
                navigateTo(hb);
            });
            map.addLayer(marker);
            if (hb.getMainBuoy() != null) {
                LPolyline connector = new LPolyline(hb.getConnectionLine());
                connector.setDashArray("8, 4");
                connector.setColor("gray");
                map.addLayer(connector);
            }
        }
    }

    public void drawMainBuoys() {
        Set<MainBuoy> mainBuoys = course.getMainBuoys();
        mainBuoys.forEach(b -> {
            Point location = b.getLocation();
            LMarker marker = new LMarker(location);
            marker.setIcon(b.getName());
            marker.addClickListener(e -> {
                navigateTo(b);
            });
            map.addLayer(marker);
        });
    }

    public void reportUnknownCourse() {
        Notification.show("The course was not found :-(", Notification.Type.ERROR_MESSAGE);
    }

    public void updateTargetLabel() throws IllegalStateException, IllegalArgumentException {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='color: white; font-weight: bold; font-size: large; text-shadow: 1px 1px 2px black;'>Buoy ");
        sb.append(targetBuoy.getName());
        try {
            gc = new GeodeticCalculator();
            gc.setStartingGeographicPoint(lastKnownPoint.getLon(), lastKnownPoint.getLat());
            gc.setDestinationPosition(JTS.toDirectPosition(targetBuoy.getLocation().getCoordinate(), crs));
            int azimuth = (int) gc.getAzimuth();
            if (azimuth < 0) {
                // convert to "compass angle"
                azimuth = azimuth + 360;
            }
            double orthodromicDistance = gc.getOrthodromicDistance();
            sb.append(" is ");
            sb.append(CourseService.formatDistance(orthodromicDistance));
            sb.append(" to ");
            sb.append(azimuth);
            sb.append("°");
            if (lastKnownSpeed == null || lastKnownSpeed == 0 || lastKnownAccuracy > 40) {
            } else {
                sb.append("<br/>");
                sb.append((int) (orthodromicDistance / lastKnownSpeed)); //TODO
                sb.append(" seconds with current speed");
            }
            sb.append("</div>");
        } catch (TransformException ex) {
            Logger.getLogger(MobileUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        targetLabel.setContentMode(ContentMode.HTML);
        targetLabel.setValue(sb.toString());

        org.vaadin.addon.leaflet.shared.Point[] points = new org.vaadin.addon.leaflet.shared.Point[]{
            new org.vaadin.addon.leaflet.shared.Point(lastKnownPoint.getLat(), lastKnownPoint.getLon()),
            new org.vaadin.addon.leaflet.shared.Point(targetBuoy.getLocation().getY(), targetBuoy.getLocation().getX())};
        if (directionPolyline == null) {
            directionPolyline = new LPolyline(points);
            directionPolyline.setDashArray("4, 2");
            directionPolyline.setColor("magenta");
            directionPolyline.setWeight(1);

            map.addLayer(directionPolyline);
        } else {
            directionPolyline.setPoints(points);
        }

    }

    private void navigateTo(AbstractBuoy buoy) {
        Notification.show("Now navigating towards " + buoy.getName());
        targetBuoy = buoy;
        updateTargetLabel();
    }

    private void drawRouteLine() {
        GeometryFactory factory = new GeometryFactory();
        Coordinate[] coordinates = new Coordinate[course.getCoursePoints().size()];
        for (int i = 0; i < course.getCoursePoints().size(); i++) {
            coordinates[i] = course.getCoursePoints().get(i).getLocation().getCoordinate();
        }
        if (coordinates.length > 2) {
            LPolyline routeLine = new LPolyline(factory.createLineString(coordinates));
            map.addLayer(routeLine);
        }

    }


}
