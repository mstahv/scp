package org.vaadin.scp.plannerui;

import org.vaadin.addon.leaflet.LLayerGroup;
import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.LOpenStreetMapLayer;
import org.vaadin.addon.leaflet.LeafletLayer;

/**
 * Created by mstahv
 */
public class SailingBaseMap extends LMap {

    private LOpenStreetMapLayer baseLayer = new LOpenStreetMapLayer();

    private LLayerGroup group = new LLayerGroup();

    @Override
    public void addLayer(LeafletLayer layer) {
        group.addComponent(layer);
    }

    public SailingBaseMap() {
        super.addLayer(baseLayer);
        super.addLayer(group);
    }

    public final void clear() {
        group.removeAllComponents();
    }


}
