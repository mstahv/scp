package org.vaadin.scp.plannerui;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import org.vaadin.scp.jpa.SailingCourse;
import org.vaadin.scp.jpa.SailingCourseRepository;
import org.vaadin.viritin.grid.MGrid;
import org.vaadin.viritin.layouts.MVerticalLayout;


/**
 * Created by mstahv
 */
@SpringUI
public class MainUI extends UI {

    private final CourseEditor courseEditor;
    private MGrid<SailingCourse> coursesGrid = new MGrid<>();
    private final SailingCourseRepository repo;

    public MainUI(CourseEditor view, SailingCourseRepository repo) {
        courseEditor = view;
        this.repo = repo;
        coursesGrid.addColumn(SailingCourse::getCourseName).setCaption("Course name");
        coursesGrid.addComponentColumn( sc -> {

            Button edit = new Button(VaadinIcons.EDIT);
            edit.addClickListener(e -> {
                courseEditor.editCourse(sc);
                setContent(courseEditor);
            });
            
            return new HorizontalLayout(edit);
            
        });
        
        init();
    }

    public void init() {
        
        coursesGrid.setRows(repo.findAll());
        
        setContent(
                new MVerticalLayout()
                        .expand(coursesGrid)
        );
    }

    @Override
    protected void init(VaadinRequest vaadinRequest) {

    }
    
    public static void showListing() {
        ((MainUI)UI.getCurrent()).init();
    }
}
