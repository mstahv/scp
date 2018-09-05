package org.vaadin.scp.jpa;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
public class MainBuoy extends AbstractBuoy {

    public MainBuoy() {
    }

}
