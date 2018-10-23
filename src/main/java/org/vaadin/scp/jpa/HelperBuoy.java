package org.vaadin.scp.jpa;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

@Entity
public class HelperBuoy extends AbstractBuoy {

    @ManyToOne(fetch = FetchType.EAGER)
    private MainBuoy mainBuoy;

    public HelperBuoy() {
    }

    public MainBuoy getMainBuoy() {
        return mainBuoy;
    }

    public void setMainBuoy(MainBuoy mainBuoy) {
        this.mainBuoy = mainBuoy;
    }

    public LineString getConnectionLine() {
        GeometryFactory factory = new GeometryFactory();
        return factory.createLineString(new Coordinate[]{mainBuoy.getLocation().getCoordinate(), getLocation().getCoordinate()});
    }
}
