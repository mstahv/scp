package org.vaadin.scp.jpa;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.ElementCollection;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.vaadin.scp.CourseService;
import org.vaadin.scp.plannerui.CourseEditor;

@Entity
public class SailingCourse extends AbstractEntity {

    private String uuid = UUID.randomUUID().toString();

    private String courseName;

    @Temporal(TemporalType.DATE)
    private Date date;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<MainBuoy> mainBuoys = new HashSet<>();
    @OneToMany(cascade = CascadeType.ALL)
    private Set<HelperBuoy> helperBuoys = new HashSet<>();

    @ManyToMany(cascade = CascadeType.ALL)
    @OrderColumn
    private List<MainBuoy> coursePoints = new ArrayList<>();

    @ElementCollection
    private Set<String> adminEmails = new HashSet<>();

    public SailingCourse() {

    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Set<MainBuoy> getMainBuoys() {
        return mainBuoys;
    }

    public void setMainBuoys(Set<MainBuoy> mainBuoys) {
        this.mainBuoys = mainBuoys;
    }

    public Set<HelperBuoy> getHelperBuoys() {
        return helperBuoys;
    }

    public void setHelperBuoys(Set<HelperBuoy> helperBuoys) {
        this.helperBuoys = helperBuoys;
    }

    public List<MainBuoy> getCoursePoints() {
        return coursePoints;
    }

    public void setCoursePoints(List<MainBuoy> coursePoints) {
        this.coursePoints = coursePoints;
    }

    public Set<String> getAdminEmails() {
        return adminEmails;
    }

    public void setAdminEmails(Set<String> adminEmails) {
        this.adminEmails = adminEmails;
    }

    /**
     * @return the center point of all buoys in the course
     */
    public Point getCentroid() {
        GeometryFactory geometryFactory = new GeometryFactory();
        ArrayList<Geometry> geoms = new ArrayList<>();
        getMainBuoys().forEach(g -> geoms.add(g.getLocation()));
        getHelperBuoys().forEach(g -> geoms.add(g.getLocation()));
        GeometryCollection gc = geometryFactory.createGeometryCollection(geoms.toArray(new Geometry[]{}));
        Point centroid = gc.getCentroid();
        return centroid;
    }

    public void rotate(int degrees, double scale) {
        CoordinateReferenceSystem crs;
        try {
            crs = CRS.decode("EPSG:4326");
            GeodeticCalculator gc = new GeodeticCalculator(crs);
            gc.setStartingPosition(JTS.toDirectPosition(getCentroid().getCoordinate(), crs));

            for (MainBuoy mb : getMainBuoys()) {
                final Point location = mb.getLocation();
                Point rotatedPoint = rotatePoint(gc, location, degrees, scale);
                mb.setLocation(rotatedPoint);
            }
            for (HelperBuoy b : getHelperBuoys()) {
                Point location = b.getLocation();
                Point rotated = rotatePoint(gc, location, degrees, scale);
                b.setLocation(rotated);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private Point rotatePoint(GeodeticCalculator gc, final Point location, Integer degrees, Double scale) throws TransformException, IllegalArgumentException, IllegalStateException {
        gc.setDestinationGeographicPoint(location.getX(), location.getY());
        double azimuth = gc.getAzimuth();
        final double orthodromicDistance = gc.getOrthodromicDistance();
        azimuth = CourseService.normalizeAzimuth(azimuth + degrees);
        gc.setDirection(azimuth, orthodromicDistance * scale);
        DirectPosition destinationPosition = gc.getDestinationPosition();
        Point rotatedPoint = JTS.toGeometry(destinationPosition);
        return rotatedPoint;
    }

}
