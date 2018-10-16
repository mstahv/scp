package org.vaadin.scp.jpa;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Created by mstahv
 */
public interface SailingCourseRepository extends JpaRepository<SailingCourse,Long> {

    @EntityGraph(attributePaths = {"mainBuoys", "helperBuoys", "coursePoints", "adminEmails"})
    public SailingCourse findOneWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"mainBuoys", "helperBuoys", "coursePoints", "adminEmails"})
    public SailingCourse findByUuid(String uuid);

    @Query("SELECT DISTINCT s FROM SailingCourse s JOIN s.adminEmails e WHERE e = :email")
    public List<SailingCourse> retrieveByEmail(@Param("email")String email);
    
}
