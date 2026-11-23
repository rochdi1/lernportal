# Spring Boot Setup – Schritt-für-Schritt-Plan

Backend-Setup für das Lernportal-Projekt (Java/Spring Boot/Gradle/PostgreSQL)

## 1. Spring Initializr – Einstellungen

Gehe auf https://start.spring.io und wähle:

- **Project:** Gradle - Groovy (oder Kotlin DSL, falls du das bevorzugst)
- **Language:** Java
- **Spring Boot:** die aktuelle stabile Version (kein SNAPSHOT)
- **Project Metadata:**
  - Group: z. B. `de.rochdi`
  - Artifact: `lernportal-backend`
  - Name: `lernportal-backend`
  - Description: `Backend für das Programmier-Lernportal`
  - Package name: wird automatisch aus Group+Artifact gebildet (z. B. `de.rochdi.lernportal`)
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

## 2. Projekt entpacken und in VSCode öffnen

1. ZIP entpacken in deinen Projektordner, z. B. `~/projekte/lernportal-backend`
2. VSCode öffnen → `File > Open Folder` → den entpackten Ordner wählen
3. VSCode erkennt das Gradle-Projekt automatisch (Extension "Gradle for Java" muss installiert sein) und importiert es
4. Warten, bis unten rechts "Java: Ready" bzw. die Gradle-Synchronisation abgeschlossen ist

### Wichtige Ordnerstruktur, die danach existiert

```
lernportal-backend/
├── build.gradle
├── settings.gradle
├── gradlew / gradlew.bat   (Gradle Wrapper - kein lokales Gradle nötig)
└── src/
    ├── main/
    │   ├── java/de/rochdi/lernportal/
    │   │   └── LernportalBackendApplication.java
    │   └── resources/
    │       └── application.properties
    └── test/
```

## 3. Datenbankverbindung konfigurieren

In `src/main/resources/application.properties` folgendes eintragen (Werte an deinen Docker-PostgreSQL-Container anpassen):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/lernportal
spring.datasource.username=lernportal_user
spring.datasource.password=dein_passwort
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
CREATE DATABASE lernportal;
CREATE USER lernportal_user WITH ENCRYPTED PASSWORD 'dein_passwort';
GRANT ALL PRIVILEGES ON DATABASE lernportal TO lernportal_user;
```

## 4. CORS für Angular-Frontend erlauben

Damit später dein Angular-Frontend (Port 4200) die API (Port 8080) ansprechen darf, eine Konfigurationsklasse anlegen:

`src/main/java/de/rochdi/lernportal/config/CorsConfig.java`

```java
package de.rochdi.lernportal.config;

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

**Entity** – `src/main/java/de/rochdi/lernportal/model/Course.java`

```java
package de.rochdi.lernportal.model;

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

**Repository** – `repository/CourseRepository.java`

```java
package de.rochdi.lernportal.repository;

import de.rochdi.lernportal.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
```

**Controller** – `controller/CourseController.java`

```java
package de.rochdi.lernportal.controller;

import de.rochdi.lernportal.model.Course;
import de.rochdi.lernportal.repository.CourseRepository;
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
- Angular-Projekt generieren und an diese API anbinden
