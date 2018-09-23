package org.vaadin.scp.plannerui;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Created by mstahv
 */
@SpringComponent
@UIScope
public class TestView extends VerticalLayout {

    public TestView() {
        addComponent(new Label("It works!?"));
    }

}
