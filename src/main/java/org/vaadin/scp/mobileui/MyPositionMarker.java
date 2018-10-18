
package org.vaadin.scp.mobileui;

import com.vaadin.spring.annotation.UIScope;
import org.springframework.stereotype.Component;
import org.vaadin.addon.leaflet.*;
import org.vaadin.addon.leaflet.shared.Point;

/**
 * @author mstahv
 */
@Component
@UIScope
public class MyPositionMarker implements LeafletLocateListener {

    private LMap map;
    private LCircle accuracy = new LCircle();
    private LCircleMarker dot = new LCircleMarker();
    private LMarker you = new LMarker();
    private LPolyline snake; 

    public void setMap(LMap map) {
        if (this.map == null) {
            this.map = map;
            map.addLocateListener(this);
        }
    }

    public void locate() {
        map.locate(true, true, false);
    }

    public void stopLocate() {
        map.stopLocate();
    }

    @Override
    public void onLocate(LeafletLocateEvent event) {
        Point myloc = event.getPoint();
        if (you.getParent() == null) {
            you.setPopup("You");
            dot.setColor("red");
            dot.setRadius(1);
            accuracy.setPoint(myloc);
            accuracy.setColor("yellow");
            accuracy.setStroke(false);
            snake = new LPolyline(myloc);
            snake.setColor("red");
            snake.getStyle().setWeight(1);
            
            you.setPoint(myloc);
            dot.setPoint(myloc);
            accuracy.setRadius(event.getAccuracy());
            map.addComponents(accuracy, dot, snake, you);
            map.setLayersToUpdateOnLocate(accuracy, dot, snake, you);
        }
    }

}
