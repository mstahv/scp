package org.vaadin.scp.plannerui;

import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.addon.leaflet.util.PointField;
import org.vaadin.scp.jpa.MainBuoy;
import org.vaadin.viritin.form.AbstractForm;

/**
 * Created by mstahv
 */
public class MainBuoyEditor extends AbstractForm<MainBuoy> {

    private TextField name = new TextField();
    private PointField location = new PointField();

    public MainBuoyEditor() {
        super(MainBuoy.class);
    }

    @Override
    protected Component createContent() {
        location.setHeight("200px");
        return new VerticalLayout(name, location, getToolbar());
    }
}
