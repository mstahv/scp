package org.vaadin.scp.plannerui;

import com.vaadin.annotations.Widgetset;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.scp.SessionService;
import org.vaadin.scp.auth.LoginView;
import org.vaadin.scp.jpa.SailingCourse;
import org.vaadin.scp.jpa.SailingCourseRepository;
import org.vaadin.viritin.button.ConfirmButton;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.grid.MGrid;
import org.vaadin.viritin.label.Header;
import org.vaadin.viritin.layouts.MVerticalLayout;

/**
 * Created by mstahv
 */
@SpringUI
@Widgetset("org.vaadin.scp.AppWidgetSet")
public class MainUI extends UI {

    private final CourseEditor courseEditor;
    private MGrid<SailingCourse> coursesGrid = new MGrid<>();
    private final SessionService sessionService;
    private final LoginView loginView;
    private final Button addCourse = new MButton("New course");

    public MainUI(CourseEditor view, SessionService sessionService, LoginView loginView) {
        this.courseEditor = view;
        this.sessionService = sessionService;
        this.loginView = loginView;

        coursesGrid.withFullWidth();
        coursesGrid.setSelectionMode(Grid.SelectionMode.NONE);
        coursesGrid.addColumn(SailingCourse::getCourseName).setCaption("Course name");
        coursesGrid.addComponentColumn(sc -> {

            Button edit = new MButton(VaadinIcons.EDIT).withStyleName(ValoTheme.BUTTON_BORDERLESS);
            edit.addClickListener(e -> {
                courseEditor.editCourse(sc);
                setContent(courseEditor);
            });
            
            ConfirmButton delete = new ConfirmButton(VaadinIcons.TRASH, "Are you really sure you want to delete this course?",
            () -> {
                sessionService.delete(sc);
                init();
            }).withStyleName(ValoTheme.BUTTON_BORDERLESS, ValoTheme.BUTTON_DANGER);

            return new HorizontalLayout(edit, delete);

        });

        coursesGrid.addComponentColumn(sc -> {
            return new Link("Course setup tool", new ExternalResource("/mobile/?uuid=" + sc.getUuid()));
        });

        addCourse.addClickListener(e -> {
            courseEditor.editCourse(sessionService.newCourse());
            setContent(courseEditor);
        });

        //testEditingFirstRoute(repo);
    }

    public void testEditingFirstRoute(SailingCourseRepository repo1) {
        courseEditor.editCourse(repo1.findAll().get(0));
        setContent(courseEditor);

    }

    public void init() {

        coursesGrid.setRows(sessionService.findCourses());

        setContent(
                new MVerticalLayout(new Header("Your courses or courses shared with you"))
                        .expand(coursesGrid)
                        .add(addCourse)
        );
    }

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        if (!sessionService.isAuthtenticated()) {
            setContent(loginView);
            return;
        }
        init();
    }

    public static void showListing() {
        ((MainUI) UI.getCurrent()).init();
    }
}
