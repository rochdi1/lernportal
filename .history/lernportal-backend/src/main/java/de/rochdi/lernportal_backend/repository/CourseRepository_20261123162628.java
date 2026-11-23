package de.rochdi.lernportal_backend.repository;

import de.rochdi.lernportal.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
