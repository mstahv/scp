package org.vaadin.scp.plannerui;

import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.UI;
import org.vaadin.scp.jpa.SailingCourse;
import org.vaadin.scp.jpa.SailingCourseRepository;

import javax.annotation.PostConstruct;

/**
 * Created by mstahv
 */
@SpringUI
public class MainUI extends UI {

    public MainUI(CourseEditor view, SailingCourseRepository repo) {
        SailingCourse sailingCourse = repo.findAll().get(0);
        view.editCourse(sailingCourse);
        setContent(view);
    }

    @Override
    protected void init(VaadinRequest vaadinRequest) {

    }
}
