package de.rochdi.lernportal_backend.service;

import de.rochdi.lernportal_backend.dto.CourseDto;
import de.rochdi.lernportal_backend.entity.CourseEntity;
import de.rochdi.lernportal_backend.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import de.rochdi.lernportal_backend.exception.CourseNotFoundException;


@Service
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository repository;

    public CourseService(CourseRepository repository) {
        this.repository = repository;
    }

    public List<CourseDto> findAll() {
        return repository.findAll().stream()
                .map(CourseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public CourseDto findById(Long id) {
        CourseEntity entity = repository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Kurs mit ID " + id + " nicht gefunden"));
        return CourseDto.fromEntity(entity);
    }

    @Transactional
    public CourseDto create(CourseDto dto) {
        CourseEntity entity = new CourseEntity();
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        return CourseDto.fromEntity(repository.save(entity));
    }

    @Transactional
    public CourseDto update(Long id, CourseDto dto) {
        CourseEntity entity = repository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Kurs mit ID " + id + " nicht gefunden"));
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        return CourseDto.fromEntity(repository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new CourseNotFoundException("Kurs mit ID " + id + " nicht gefunden");
        }
        repository.deleteById(id);
    }
}
