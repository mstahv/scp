package org.vaadin.scp.plannerui;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.vaadin.addon.leaflet.util.PointField;
import org.vaadin.scp.jpa.HelperBuoy;
import org.vaadin.scp.jpa.MainBuoy;
import org.vaadin.viritin.fields.IntegerField;
import org.vaadin.viritin.form.AbstractForm;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

/**
 * Created by mstahv
 */
public class HelperBuoyEditor extends AbstractForm<HelperBuoy> {

    private TextField name = new TextField();
    private PointField location = new PointField();
    private NativeSelect<MainBuoy> mainBuoy = new NativeSelect<>("Main buoy");

    IntegerField distance = new IntegerField();
    IntegerField angle = new IntegerField();
    NativeSelect<MainBuoy> referencePoint = new NativeSelect<>();

    public HelperBuoyEditor() {
        super(HelperBuoy.class);
    }

    @Override
    public void setEntity(HelperBuoy entity) {
        mainBuoy.setItems(entity.getCourse().getMainBuoys());
        referencePoint.setItems(entity.getCourse().getMainBuoys());
        super.setEntity(entity);
    }

    @Override
    protected Component createContent() {
        mainBuoy.setItemCaptionGenerator(MainBuoy::getName);
        referencePoint.setItemCaptionGenerator(MainBuoy::getName);
        location.setHeight("200px");
        MHorizontalLayout reposition = new MHorizontalLayout().withCaption("Reposition bouy").withMargin(true);
        Button adjust = new Button(VaadinIcons.CHECK_CIRCLE);
        adjust.setEnabled(false);
        adjust.setDescription("Set main buoy, reference point and details to continue.");
        distance.setValue(100);
        angle.setValue(90);
        referencePoint.addValueChangeListener(e -> {
            adjust.setEnabled(e.getValue() != null && mainBuoy.getValue() != null);
        });
        adjust.addClickListener( e-> {
            try {
                CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");
                GeodeticCalculator gc = new GeodeticCalculator(crs);
                gc.setStartingPosition(JTS.toDirectPosition(mainBuoy.getValue().getLocation().getCoordinate(), crs));
                gc.setDestinationPosition(JTS.toDirectPosition(referencePoint.getValue().getLocation().getCoordinate(), crs));
                double targetAngle = gc.getAzimuth() + angle.getValue();
                if(targetAngle > 180) {
                    targetAngle -= 360;
                } else if (targetAngle < -180) {
                    targetAngle += 360;
                }
                gc.setDirection(targetAngle, distance.getValue());
                location.setValue(JTS.toGeometry(gc.getDestinationPosition()));
            } catch (TransformException e1) {
                e1.printStackTrace();
            } catch (NoSuchAuthorityCodeException e1) {
                e1.printStackTrace();
            } catch (FactoryException e1) {
                e1.printStackTrace();
            }

        });
        distance.setWidth("6em");
        angle.setWidth("5em");
        reposition.add(distance, new Label("m and"), angle, new Label("° from"), referencePoint, adjust);
        reposition.alignAll(Alignment.MIDDLE_CENTER);

        return new VerticalLayout(name, mainBuoy, location, reposition, getToolbar());
    }
}
