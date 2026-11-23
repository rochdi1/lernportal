package de.rochdi.lernportal_backend.service;

import de.rochdi.lernportal_backend.dto.CourseDto;
import de.rochdi.lernportal_backend.entity.CourseEntity;
import de.rochdi.lernportal_backend.exception.CourseNotFoundException;
import de.rochdi.lernportal_backend.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository repository;

    @InjectMocks
    private CourseService service;

    @Test
    void findAll_shouldReturnMappedDtos() {
        CourseEntity entity = new CourseEntity();
        entity.setId(1L);
        entity.setTitle("Java");
        entity.setDescription("Einstieg");

        when(repository.findAll()).thenReturn(List.of(entity));

        List<CourseDto> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Java");
        verify(repository).findAll();
    }

    @Test
    void findById_whenNotFound_shouldThrow() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(CourseNotFoundException.class);
    }
}