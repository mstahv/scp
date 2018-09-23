package org.vaadin.scp.plannerui;

import com.vaadin.data.HasValue;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.Registration;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import fi.jasoft.dragdroplayouts.DDVerticalLayout;
import fi.jasoft.dragdroplayouts.client.ui.LayoutDragMode;
import fi.jasoft.dragdroplayouts.drophandlers.DefaultVerticalLayoutDropHandler;
import org.vaadin.scp.jpa.MainBuoy;
import org.vaadin.viritin.label.MLabel;
import org.vaadin.viritin.layouts.MHorizontalLayout;

import java.util.List;

/**
 * Created by mstahv
 */
public class RouteEditor extends VerticalLayout implements HasValue<List<MainBuoy>> {

    private final CourseEditor editor;
    private List<MainBuoy> mainBuoys;

    DDVerticalLayout buoyLayout = new DDVerticalLayout();

    public RouteEditor(CourseEditor editor) {
        setCaption("Route points (drag to re-order)");
        this.editor = editor;
        addComponent(buoyLayout);
        buoyLayout.setMargin(false);
        setMargin(false);
    }

    @Override
    public void setValue(List<MainBuoy> mainBuoys) {
        buoyLayout.removeAllComponents();
        this.mainBuoys = mainBuoys;
        mainBuoys.forEach(this::addBuoy);

        buoyLayout.setComponentVerticalDropRatio(0.5f);
        buoyLayout.setDragMode(LayoutDragMode.CLONE);
        buoyLayout.setDropHandler(new DefaultVerticalLayoutDropHandler() {
            @Override
            protected void handleComponentReordering(DragAndDropEvent event) {
                super.handleComponentReordering(event);
                updateStateFromComponentTree();
            }
        });

    }

    private void updateStateFromComponentTree() {
        // update order from the component structure
        mainBuoys.clear();
        for(Component c : buoyLayout) {
            BuoyRow r = (BuoyRow) c;
            mainBuoys.add(r.buoy);
        }
        editor.drawCourse();
    }

    private void addBuoy(MainBuoy buoy) {
        buoyLayout.addComponent(new BuoyRow(buoy));
    }

    @Override
    public List<MainBuoy> getValue() {
        return mainBuoys;
    }

    @Override
    public void setRequiredIndicatorVisible(boolean b) {

    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return false;
    }

    @Override
    public void setReadOnly(boolean b) {

    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<List<MainBuoy>> valueChangeListener) {
        return null;
    }

    class BuoyRow extends MHorizontalLayout {
        private final MainBuoy buoy;

        public BuoyRow(MainBuoy buoy) {
            this.buoy = buoy;
            Button removeRoutePoint = new Button(VaadinIcons.TRASH);
            removeRoutePoint.setStyleName(ValoTheme.BUTTON_BORDERLESS);
            removeRoutePoint.addClickListener(e->{
                buoyLayout.removeComponent(this);
                updateStateFromComponentTree();
            });
            expand(new MLabel(buoy.getName() + " " + buoy.getLocation()).withUndefinedWidth());
            add(removeRoutePoint);
        }

    }

}
