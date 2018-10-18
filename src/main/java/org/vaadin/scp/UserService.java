/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vaadin.scp;

import com.vaadin.spring.annotation.VaadinSessionScope;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.vaadin.scp.jpa.SailingCourse;
import org.vaadin.scp.jpa.SailingCourseRepository;

/**
 *
 * @author mstahv
 */
@Component
@VaadinSessionScope
public class UserService {
    
    private String email;
    private final SailingCourseRepository sailingCourseRepository;
    
    @Value("${devmode:false}")
    private boolean devmode;

    public UserService(SailingCourseRepository repo) {
        this.sailingCourseRepository = repo;
    }

    public String getEmail() {
        if(email == null && isDevMode()) {
            return "matti.tahvonen@gmail.com";
        }
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    public List<SailingCourse> findCourses() {
        return sailingCourseRepository.retrieveByEmail(getEmail());
    }

    public boolean isDevMode() {
        return devmode;
    }

    public boolean isAuthtenticated() {
        return getEmail() != null;
    }

    public SailingCourse newCourse() {
        SailingCourse sailingCourse = new SailingCourse();
        sailingCourse.getAdminEmails().add(getEmail());
        sailingCourse.setCourseName("new course " + LocalDate.now());
        return sailingCourseRepository.save(sailingCourse);
    }

    public void delete(SailingCourse sc) {
        sailingCourseRepository.delete(sc);
    }
    
}
