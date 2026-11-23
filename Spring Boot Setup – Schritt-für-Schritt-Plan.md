# Spring Boot Setup – Schritt-für-Schritt-Plan

Backend-Setup für das Lernportal-Projekt (Java/Spring Boot/Gradle/PostgreSQL)

> **Hinweis:** Dieses Dokument beschreibt den **tatsächlichen Stand** des Projekts im Ordner `lernportal-backend/`. Alle Pfade sind relativ zum Projekt-Root (`/home/casa/AI_Project/JavaAngular/`).

## 1. Spring Initializr – Einstellungen (bereits erledigt)

Das Projekt wurde bereits mit Spring Initializr erstellt. Falls du es neu erstellst, wähle:

- **Project:** Gradle - Kotlin DSL (KTS)
- **Language:** Java
- **Spring Boot:** die aktuelle stabile Version (kein SNAPSHOT)
- **Project Metadata:**
  - Group: z. B. `de.rochdi`
  - Artifact: `lernportal-backend`
  - Name: `lernportal-backend`
  - Description: `Backend für das Programmier-Lernportal`
  - Package name: `de.rochdi.lernportal_backend`
  - Packaging: **Jar**
  - Java: **21**

### Dependencies (unter "Add Dependencies" auswählen)

- **Spring Web** (REST-Controller)
- **Spring Data JPA** (Datenbankzugriff über Repositories)
- **PostgreSQL Driver**
- **Validation** (Bean Validation für DTOs/Entities)
- **Spring Boot DevTools** (Auto-Reload während der Entwicklung)
- **Lombok** (optional, spart Getter/Setter-Boilerplate)

Danach auf **GENERATE** klicken → ein ZIP wird heruntergeladen.

## 2. Projekt entpacken und in VSCode öffnen (bereits vorhanden)

Das Projekt wurde bereits in `lernportal-backend/` entpackt und ist in VSCode importiert.

### Wichtige Ordnerstruktur (tatsächlich vorhanden)

```
lernportal-backend/
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew / gradlew.bat   (Gradle Wrapper - kein lokales Gradle nötig)
└── src/
    ├── main/
    │   ├── java/
    │   │   └── de/rochdi/lernportal_backend/
    │   │       ├── LernportalBackendApplication.java
    │   │       ├── config/
    │   │       │   └── CorsConfig.java
    │   │       ├── controller/
    │   │       │   └── CourseController.java
    │   │       ├── model/
    │   │       │   └── Course.java
    │   │       └── repository/
    │   │           └── CourseRepository.java
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/
            └── de/rochdi/lernportal_backend/
                └── LernportalBackendApplicationTests.java
```

## 3. Datenbankverbindung konfigurieren (bereits in `application.properties`)

Datei: `lernportal-backend/src/main/resources/application.properties`

```properties
spring.application.name=lernportal-backend
spring.datasource.url=jdbc:postgresql://localhost:5432/learning_dashboard
spring.datasource.username=learning_user
spring.datasource.password=learning_pass
spring.datasource.driver-class-name=org.postgresql.Driver

# Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Server
server.port=8080
```

Hinweis zu `ddl-auto`:

- `update` → Hibernate legt/ändert Tabellen automatisch anhand der Entities (gut für die Entwicklungsphase)
- `validate` oder `none` → später für Produktion sinnvoller, wenn du mit Migrationstools wie Flyway arbeitest

Falls deine Datenbank/User im Docker-Container noch nicht existieren, vorher im Container anlegen:

```sql
CREATE DATABASE learning_dashboard;
CREATE USER learning_user WITH ENCRYPTED PASSWORD 'learning_pass';
GRANT ALL PRIVILEGES ON DATABASE learning_dashboard TO learning_user;
```

## 4. CORS für Angular-Frontend erlauben (bereits in `CorsConfig.java`)

Damit später dein Angular-Frontend (Port 4200) die API (Port 8080) ansprechen darf, eine Konfigurationsklasse anlegen:

Datei: `lernportal-backend/src/main/java/de/rochdi/lernportal_backend/config/CorsConfig.java`

```java
package de.rochdi.lernportal_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:4200")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
            }
        };
    }
}
```

## 5. Erste Entity, Repository, Service, Controller (Beispiel: Course)

**Entity** – `lernportal-backend/src/main/java/de/rochdi/lernportal_backend/model/Course.java`

```java
package de.rochdi.lernportal_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    // Getter & Setter (oder Lombok @Getter/@Setter)
}
```

**Repository** – `lernportal-backend/src/main/java/de/rochdi/lernportal_backend/repository/CourseRepository.java`

```java
package de.rochdi.lernportal_backend.repository;

import de.rochdi.lernportal_backend.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
```

**Controller** – `lernportal-backend/src/main/java/de/rochdi/lernportal_backend/controller/CourseController.java`

```java
package de.rochdi.lernportal_backend.controller;

import de.rochdi.lernportal_backend.model.Course;
import de.rochdi.lernportal_backend.repository.CourseRepository;
import org.springframework.web.bind.annotation.*;

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
```

## 6. Starten und testen

1. In VSCode: `LernportalBackendApplication.java` öffnen → "Run" über die integrierte Java-Ausführung, oder im Terminal: `./gradlew bootRun`
2. Prüfen, dass die Konsole "Started LernportalBackendApplication" zeigt und keine DB-Fehler auftauchen
3. Mit einem REST-Client (Postman, Insomnia, oder VSCode-Extension "REST Client") testen:
   - `GET http://localhost:8080/api/courses` → sollte leere Liste `[]` liefern
   - `POST http://localhost:8080/api/courses` mit Body `{"title":"Java Grundlagen","description":"Einstieg in Java"}` → sollte den gespeicherten Kurs mit ID zurückgeben
4. In DBeaver/pgAdmin prüfen, ob die Tabelle `course` automatisch angelegt wurde

## Nächste Schritte (für später)

- Weitere Entities analog anlegen: Module, Section, Quiz, Question, Answer
- DTOs statt Entities direkt in der API zurückgeben (sauberere Trennung)
- Angular-Projekt generieren und an diese API anbinden (siehe `Nächste Schritte – Lernportal.md`)