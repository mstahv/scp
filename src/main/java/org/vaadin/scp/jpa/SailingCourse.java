package org.vaadin.scp.jpa;

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

@Entity
public class SailingCourse extends AbstractEntity {

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
    
    public SailingCourse() {

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
    
}
