package de.rochdi.lernportal_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import de.rochdi.lernportal_backend.entity.CourseEntity;

public interface CourseRepository extends JpaRepository<CourseEntity, Long> {
}
