package de.rochdi.lernportal_backend.controller;

import org.springframework.web.bind.annotation.*;

import de.rochdi.lernportal_backend.model.Course;
import de.rochdi.lernportal_backend.repository.CourseRepository;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseRepository repository;

    public CourseController(CourseRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Course> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public Course create(@RequestBody Course course) {
        return repository.save(course);
    }

    @PutMapping("/{id}")
    public Course update(@PathVariable Long id, @RequestBody Course course) {
        course.setId(id);
        return repository.save(course);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
