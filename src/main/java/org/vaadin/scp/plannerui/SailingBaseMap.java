package org.vaadin.scp.plannerui;

import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.LOpenStreetMapLayer;

/**
 * Created by mstahv
 */
public class SailingBaseMap extends LMap {

    public SailingBaseMap() {
    }

    public final void clear() {
        removeAllComponents();
        addLayer(new LOpenStreetMapLayer());
    }
}
