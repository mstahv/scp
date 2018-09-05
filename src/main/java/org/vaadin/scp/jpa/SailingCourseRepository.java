package org.vaadin.scp.jpa;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.vaadin.scp.jpa.SailingCourse;

/**
 * Created by mstahv
 */
public interface SailingCourseRepository extends JpaRepository<SailingCourse,Long> {

    @EntityGraph(attributePaths = {"mainBuoys", "helperBuoys", "coursePoints"})
    public SailingCourse findOneWithDetailsById(Long id);
}
