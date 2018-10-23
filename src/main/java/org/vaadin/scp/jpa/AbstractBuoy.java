package org.vaadin.scp.jpa;

import com.vividsolutions.jts.geom.Point;

import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

/**
 * Created by mstahv
 */
@MappedSuperclass
public class AbstractBuoy extends AbstractEntity {

    private String name;

    @Column(columnDefinition = "GEOGRAPHY")
    private Point location;

    @NotNull
    @ManyToOne
    private SailingCourse course;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public SailingCourse getCourse() {
        return course;
    }

    public void setCourse(SailingCourse course) {
        this.course = course;
    }

}
