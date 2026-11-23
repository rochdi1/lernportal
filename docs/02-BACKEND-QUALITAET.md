# 02 – Backend Qualität (Phase 2)

## Ziel
Backend robust machen: Service-Layer, DTOs, Error-Handling, Unit-Tests für Controller & Service.

---

## Voraussetzungen
- Phase 1 (Frontend Sichtbarkeit) erledigt
- Backend läuft (`./gradlew bootRun` im `lernportal-backend/` Ordner)
- `CourseController`, `CourseRepository`, `Course`, `CorsConfig` bereits vorhanden

---

## Betroffene Dateien

| Datei | Aktion |
|-------|--------|
| `lernportal-backend/src/main/java/de/rochdi/lernportal_backend/service/CourseService.java` | **neu** |
| `lernportal-backend/src/main/java/de/rochdi/lernportal_backend/dto/CourseDto.java` | **neu** |
| `lernportal-backend/src/main/java/de/rochdi/lernportal_backend/controller/CourseController.java` | **ändern** – nutzt DTOs & Service |
| `lernportal-backend/src/test/java/de/rochdi/lernportal_backend/service/CourseServiceTest.java` | **neu** |
| `lernportal-backend/src/test/java/de/rochdi/lernportal_backend/controller/CourseControllerTest.java` | **neu** |

---

## 2.1 Service-Layer anlegen

**Ziel:** Business-Logik (z. B. Validierung, Fehler beim Nicht-Vorhandensein) vom Controller trennen.

**Datei:** `lernportal-backend/src/main/java/de/rochdi/lernportal_backend/service/CourseService.java`

```java
package de.rochdi.lernportal_backend.service;

import de.rochdi.lernportal_backend.dto.CourseDto;
import de.rochdi.lernportal_backend.entity.CourseEntity;
import de.rochdi.lernportal_backend.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

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
```

---

## 2.2 DTO einführen

**Ziel:** API gibt nicht direkt Entities zurück (saubere Trennung).

**Datei:** `lernportal-backend/src/main/java/de/rochdi/lernportal_backend/dto/CourseDto.java`

```java
package de.rochdi.lernportal_backend.dto;

import de.rochdi.lernportal_backend.entity.CourseEntity;
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
```

---

## 2.3 Entity anpassen (mit Lombok @Getter/@Setter)

**Datei:** `lernportal-backend/src/main/java/de/rochdi/lernportal_backend/entity/CourseEntity.java`

> Falls `Course.java` noch als Model existiert: umbenennen in `CourseEntity.java` oder neu anlegen mit `@Entity`.

```java
package de.rochdi.lernportal_backend.entity;

import de.rochdi.lernportal_backend.dto.CourseDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "course")
public class CourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
}
```

---

## 2.4 Custom Exception

**Datei:** `lernportal-backend/src/main/java/de/rochdi/lernportal_backend/exception/CourseNotFoundException.java`

```java
package de.rochdi.lernportal_backend.exception;

public class CourseNotFoundException extends RuntimeException {

    public CourseNotFoundException(String message) {
        super(message);
    }
}
```

**Global Exception Handler:**

**Datei:** `lernportal-backend/src/main/java/de/rochdi/lernportal_backend/exception/GlobalExceptionHandler.java`

```java
package de.rochdi.lernportal_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CourseNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(CourseNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}
```

---

## 2.5 Controller umstellen auf DTOs + Service

**Datei:** `lernportal-backend/src/main/java/de/rochdi/lernportal_backend/controller/CourseController.java`

```java
package de.rochdi.lernportal_backend.controller;

import de.rochdi.lernportal_backend.dto.CourseDto;
import de.rochdi.lernportal_backend.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService service;

    public CourseController(CourseService service) {
        this.service = service;
    }

    @GetMapping
    public List<CourseDto> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<CourseDto> create(@RequestBody CourseDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseDto> update(@PathVariable Long id, @RequestBody CourseDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## Unit-Tests

| Testdatei | Was wird getestet | Testfälle |
|-----------|-------------------|-----------|
| `CourseServiceTest.java` | Service-Logik (mit Mockito Mock für Repository) | 1. `findAll()` ruft Repository auf<br>2. `findById()` wirft `CourseNotFoundException`<br>3. `create()` speichert Entity |
| `CourseControllerTest.java` | Controller-Endpunkte (`@WebMvcTest`) | 1. GET `/api/courses` → 200 + Liste<br>2. GET `/api/courses/{id}` nicht gefunden → 404<br>3. POST `/api/courses` → 201<br>4. DELETE `/api/courses/{id}` → 204 |

**Beispiel `CourseServiceTest.java`:**

```java
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
```

**Beispiel `CourseControllerTest.java`:**

```java
package de.rochdi.lernportal_backend.controller;

import de.rochdi.lernportal_backend.dto.CourseDto;
import de.rochdi.lernportal_backend.service.CourseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService service;

    @Test
    void getAll_shouldReturnCourses() throws Exception {
        CourseDto dto = new CourseDto();
        dto.setId(1L);
        dto.setTitle("Java");

        when(service.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Java"));
    }
}
```

**Backend-Test-Befehl:**
```bash
cd lernportal-backend
./gradlew test
```

---

## Definition of Done

- [ ] `CourseService` (mit `@Transactional`) angelegt
- [ ] `CourseDto` (mit Lombok) angelegt
- [ ] `CourseEntity` (zuvor `Course`) vorhanden
- [ ] `CourseNotFoundException` + `GlobalExceptionHandler` angelegt
- [ ] `CourseController` nutzt DTOs + Service
- [ ] `CourseServiceTest` grün
- [ ] `CourseControllerTest` grün (`@WebMvcTest`)
- [ ] `./gradlew test` erfolgreich
- [ ] In `00-UEBERSICHT.md` Status Task 2.1–2.2 auf ✅ gesetzt

---

## Nächster Schritt
→ Phase 3: `03-QUIZ-MODUL.md` (Question/Answer Backend + Quiz-Frontend)