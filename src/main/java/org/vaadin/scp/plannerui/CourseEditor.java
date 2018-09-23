package org.vaadin.scp.plannerui;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.data.Binder;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.Registration;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.vaadin.addon.leaflet.LMarker;
import org.vaadin.addon.leaflet.LPolyline;
import org.vaadin.addon.leaflet.LeafletClickEvent;
import org.vaadin.addon.leaflet.LeafletClickListener;
import org.vaadin.addon.leaflet.shared.TooltipState;
import org.vaadin.addon.leaflet.util.JTSUtil;
import org.vaadin.scp.CourseService;
import org.vaadin.scp.jpa.HelperBuoy;
import org.vaadin.scp.jpa.MainBuoy;
import org.vaadin.scp.jpa.SailingCourse;
import org.vaadin.scp.jpa.SailingCourseRepository;
import org.vaadin.viritin.button.ConfirmButton;
import org.vaadin.viritin.button.PrimaryButton;
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
@StyleSheet("vaadin://customstyles.css")
public class CourseEditor extends MHorizontalLayout {

    private final CourseService service;
    private Registration mapClickListener;
    private SailingCourse course;

    SailingBaseMap map = new SailingBaseMap();

    MVerticalLayout details = new MVerticalLayout().withWidth("400px");

    Label courseLenght = new MLabel().withCaption("Course lenght");

    RouteEditor routePoints = new RouteEditor(this);

    NativeSelect<MainBuoy> addBuoyToRoute = new NativeSelect<>();

    MGrid<MainBuoy> mGrid = new MGrid<>(MainBuoy.class).withCaption("RoutePoints (d'n'd to re-order)")
            .withProperties("name", "location")
            .withWidth("300px");

    TextField courseName = new TextField("Course name");

    Binder<SailingCourse> binder = new Binder<SailingCourse>(SailingCourse.class);
    
    public CourseEditor(CourseService service, SailingCourseRepository courseRepository) {
        this.service = service;
        setSizeFull();
        expand(map);

        PrimaryButton save = new PrimaryButton("Save", e -> {
            service.save(course);
            editCourse(course);
        });
        ConfirmButton restore = new ConfirmButton("Restore", "Are you sure you want to restore your unsaved changes?", e -> {
            editCourse(course);
        });

        ConfirmButton close = new ConfirmButton("Close course", "Are you sure you want to close this course, unsaved changes will be lost?", e -> {
            MainUI.showListing();
        });

        details.add(new MHorizontalLayout(save, restore, close));

        details.addComponents(courseName, courseLenght, routePoints, addBuoyToRoute);

        addBuoyToRoute.setCaption("Add existing buoy to route...");
        addBuoyToRoute.setItemCaptionGenerator(b -> b.getName());
        addBuoyToRoute.setEmptySelectionAllowed(false);
        addBuoyToRoute.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                course.getCoursePoints().add(e.getValue());
                drawCourse();
                addBuoyToRoute.setValue(null);
            }
        });

        Button addNewMainBuoy = new Button("New main buoy...");
        addNewMainBuoy.addClickListener(e -> {
            Notification.show("Click on map to set location...");
            map.addStyleName("crosshair");
            mapClickListener = map.addClickListener(new LeafletClickListener() {
                @Override
                public void onClick(LeafletClickEvent event) {
                    MainBuoy mainBuoy = service.addBuoy(course, JTSUtil.toPoint(event.getPoint()).getCoordinate());
                    course.getCoursePoints().add(mainBuoy);
                    drawCourse();
                    mapClickListener.remove();
                    map.removeStyleName("crosshair");
                    updateMainBuoys();
                }
            });

        });
        Button addNewHelperBuoy = new Button("New helper buoy...");
        addNewHelperBuoy.addClickListener(e -> {
            Notification.show("Click on map to set location...");
            map.addStyleName("crosshair");
            mapClickListener = map.addClickListener(new LeafletClickListener() {
                @Override
                public void onClick(LeafletClickEvent event) {
                    HelperBuoy helperBuoy = service.addHelperBuoy(course, null, JTSUtil.toPoint(event.getPoint()).getCoordinate());
                    drawCourse();
                    mapClickListener.remove();
                    map.removeStyleName("crosshair");
                }
            });

        });

        details.addComponents(addNewMainBuoy, addNewHelperBuoy);

        add(details);

    }

    private void updateMainBuoys() {
        addBuoyToRoute.setItems(course.getMainBuoys());
    }

    public void editCourse(SailingCourse c) {
        this.course = service.findAttached(c);
        addBuoyToRoute.setItems(course.getMainBuoys());
        mGrid.setItems(new ArrayList<>(this.course.getCoursePoints()));
        drawCourse();
        map.zoomToContent();
        binder.setBean(course);
    }

    void drawCourse() {
        routePoints.setValue(course.getCoursePoints());
        map.clear();
        Set<MainBuoy> mainBuoys = course.getMainBuoys();
        mainBuoys.forEach(b -> {
            Point location = b.getLocation();
            LMarker marker = new LMarker(location);
            marker.setIcon(b.getName());
            marker.addDragEndListener(e -> {
                Point geometry = (Point) marker.getGeometry();
                b.setLocation(geometry);
                drawCourse();
            });
            marker.addClickListener(e -> {
                MainBuoyEditor editor = new MainBuoyEditor();
                editor.setDeleteHandler(buoy -> {
                    service.removeBuoy(buoy);
                    drawCourse();
                    editor.closePopup();
                });
                editor.setSavedHandler(buoy -> {
                    drawCourse();
                    updateMainBuoys();
                    editor.closePopup();
                });
                editor.setEntity(b);
                editor.openInModalPopup();
            });
            map.addLayer(marker);
        });

        drawRouteLine();

        for (HelperBuoy hb : course.getHelperBuoys()) {
            LMarker marker = new LMarker(hb.getLocation());
            marker.setTooltip("G");
            TooltipState tooltipState = new TooltipState();
            tooltipState.sticky = true;
            marker.setTooltipState(tooltipState);
            marker.setIcon(FontAwesome.BULLSEYE);
            marker.setIconPathFill("gray");
            marker.addDragEndListener(e -> {
                Point geometry = (Point) marker.getGeometry();
                hb.setLocation(geometry);
                drawCourse();
            });
            marker.addClickListener(e -> {
                HelperBuoyEditor editor = new HelperBuoyEditor();
                editor.setDeleteHandler(buoy -> {
                    service.removeBuoy(buoy);
                    drawCourse();
                    editor.closePopup();
                });
                editor.setSavedHandler(buoy -> {
                    drawCourse();
                    editor.closePopup();
                });
                editor.setEntity(hb);
                editor.openInModalPopup();
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

    private void drawRouteLine() {
        try {
            double distance = 0;
            CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");
            GeodeticCalculator gc = new GeodeticCalculator(crs);

            GeometryFactory factory = new GeometryFactory();
            Coordinate[] coordinates = new Coordinate[course.getCoursePoints().size()];
            for (int i = 0; i < course.getCoursePoints().size(); i++) {
                final boolean first = i == 0;
                if (!first) {
                    gc.setStartingPosition(gc.getDestinationPosition());
                }
                coordinates[i] = course.getCoursePoints().get(i).getLocation().getCoordinate();
                gc.setDestinationPosition(JTS.toDirectPosition(coordinates[i], crs));
                if (!first) {
                    distance += gc.getOrthodromicDistance();
                }
            }
            if (coordinates.length > 2) {
                LPolyline routeLine = new LPolyline(factory.createLineString(coordinates));
                map.addLayer(routeLine);
            }
            courseLenght.setValue(((int) distance) + "m");
        } catch (FactoryException e) {
            e.printStackTrace();
        } catch (TransformException e) {
            e.printStackTrace();
        }

    }

}
