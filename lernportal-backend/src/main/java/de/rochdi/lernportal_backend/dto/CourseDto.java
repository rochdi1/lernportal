package de.rochdi.lernportal_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseDto {

    private Long id;
    private String title;
    private String description;

    public static CourseDto fromEntity(CourseEntity entity) {
        CourseDto dto = new CourseDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        return dto;
    }
}