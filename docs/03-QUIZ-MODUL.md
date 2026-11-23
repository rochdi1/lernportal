# 03 – Quiz-Modul (Phase 3)

## Ziel
Erweiterung um Fragen und Antworten: Backend-Entitäten `Question`/`Answer`, Controller, Service, Repository; Frontend: Quiz-Komponente mit Auswahl und Auswertung.

---

## Voraussetzungen
- Phase 1 (Frontend Sichtbarkeit) erledigt
- Phase 2 (Backend Qualität) erledigt – `CourseService`, `CourseDto`, `GlobalExceptionHandler` vorhanden
- Entity `CourseEntity` und DTO `CourseDto` existieren

---

## Betroffene Dateien

| Bereich | Datei | Aktion |
|---------|-------|--------|
| **Backend Entity** | `CourseEntity.java` | **ändern** – Relation `questions: One-to-Many` hinzufügen |
| **Backend Entity** | `Question.java` | **neu** |
| **Backend Entity** | `Answer.java` | **neu** |
| **Backend Repository** | `QuestionRepository.java` | **neu** |
| **Backend Service** | `QuestionService.java` | **neu** |
| **Backend Controller** | `QuestionController.java` | **neu** |
| **Frontend Service** | `quiz.service.ts` | **neu** |
| **Frontend Komponente** | `quiz.component.ts` + `quiz.component.html` | **neu** |

---

### 3.1 Backend Entity: `Question`

Erweiterung von `CourseEntity.java`:

```java
@OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Question> questions;
```

**Neue Datei:** `lernportal-backend/src/main/java/de/rochdi/lernportal_backend/entity/Question.java`

```java
package de.rochdi.lernportal_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private CourseEntity course;

    private String hint; // optional
}
```

---

### 3.2 Backend Entity: `Answer`

**Neue Datei:** `lernportal-backend/src/main/java/de/rochdi/lernportal_backend/entity/Answer.java`

```java
package de.rochdi.lernportal_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    private boolean correct; // richtige Antwort

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;
}
```

---

### 3.3 Backend Repository

**Neue Datei:** `lernportal-backend/src/main/java/de/rochdi/lernportal_backend/repository/QuestionRepository.java`

```java
package de.rochdi.lernportal_backend.repository;

import de.rochdi.lernportal_backend.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM Question q WHERE q.course.id = :courseId")
    List<Question> findByCourseId(@Param("courseId") Long courseId);
}
```

---

### 3.4 Backend Service

**Neue Datei:** `lernportal-backend/src/main/java/de/rochdi/lernportal_backend/service/QuestionService.java`

```java
package de.rochdi.lernportal_backend.service;

import de.rochdi.lernportal_backend.entity.Question;
import de.rochdi.lernportal_backend.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class QuestionService {

    private final QuestionRepository repository;

    public QuestionService(QuestionRepository repository) {
        this.repository = repository;
    }

    public List<Question> findByCourse(Long courseId) {
        return repository.findByCourseId(courseId);
    }

    public Question save(Question question) {
        return repository.save(question);
    }
}
```

---

### 3.5 Backend Controller

**Neue Datei:** `lernportal-backend/src/main/java/de/rochdi/lernportal_backend/controller/QuestionController.java`

```java
package de.rochdi.lernportal_backend.controller;

import de.rochdi.lernportal_backend.dto.QuestionDto;
import de.rochdi.lernportal_backend.entity.Question;
import de.rochdi.lernportal_backend.exception.CourseNotFoundException;
import de.rochdi.lernportal_backend.service.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService service;

    public QuestionController(QuestionService service) {
        this.service = service;
    }

    @GetMapping
    public List<QuestionDto> getAll() {
        return service.findAllMapped(); // später implementiert
    }

    @GetMapping("/course/{courseId}")
    public List<QuestionDto> getByCourse(@PathVariable Long courseId) {
        return service.findByCourse(courseId);
    }

    @PostMapping
    public ResponseEntity<QuestionDto> create(@RequestBody QuestionDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }
}
```

> **Hinweis:** Für den MVP kann der MVP erst einmal `getByCourse` und `create` implementieren. Eine vollständige Frage/Auswertung folgt in Phase 4/5.

---

### 3.5 Frontend Service

**Neue Datei:** `lernportal-frontend/src/app/features/quiz/quiz.service.ts`

```typescript
import { Injectable } from '@angular/core';
import { CourseService, Course } from '../core/services/course.service';
import { HttpClient } from '@angular/common/http';
import { signal, Signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class QuizService {
  private apiUrl = 'http://localhost:8080/api/questions';

  constructor(private http: HttpClient) {}

  getByCourse(courseId: number): Signal<any> {
    // Returns signal for reactive template
    return this.http.get<any[]>(`${this.apiUrl}/course/${courseId}`) as any;
  }
}
```

> **Vereinfachung:** In Phase 3 wird ein einfacher Service gereicht; später (Phase 5) wird `HttpClientTestingModule` für Tests genutzt.

---

### 3.6 Frontend Komponente

**Neue Datei:** `lernportal-frontend/src/app/features/quiz/quiz.component.ts`

```typescript
import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CourseService, Course } from '../core/services/course.service';
import { QuizService } from '../quiz.service';
import { Question } from '../../shared/models/question.model';

@Component({
  selector: 'app-quiz',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './quiz.component.html',
})
export class QuizComponent implements OnInit {
  courseId = 1; // Default oder via Route-Param
  questions = signal<any[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);
  currentIndex = signal(0);
  score = signal(0);
  total = signal(0);

  constructor(private quizService: QuizService, private courseService: CourseService) {}

  ngOnInit(): void {
    this.loadQuestions();
  }

  loadQuestions(): void {
    this.loading.set(true);
    this.error.set(null);
    this.questions.set([]);
    this.score.set(0);
    this.total.set(0);

    this.quizService.getByCourse(this.courseId).subscribe({
      next: (data) => {
        this.questions.set(data);
        this.total.set(data.length);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Fehler beim Laden der Fragen:', err);
        this.error.set('Fragen konnten nicht geladen werden.');
        this.loading.set(false);
      }
    });
  }

  answerQuestion(index: number): void {
    const q = this.questions().at(index);
    // Einfache Logik: erste korrekte Antwort als Lösung markieren
    if (q && q.correct) {
      this.score.update(s => s + 1);
    }
    this.currentIndex.update(i => i + 1);
  }
}
```

**Template:** `lernportal-frontend/src/app/features/quiz/quiz.component.html`

```html
<div *ngIf="loading(); else content">Lade Fragen…</div>

<ng-template #content>
  <div *ngIf="error()" class="error">{{ error() }}</div>

  <div *ngIf="!loading() && questions().length > 0">
    <p>Frage {{ currentIndex() + 1 }} von {{ total() }}</p>

    <ul>
      <li *ngFor="let q of questions(); let i = index">
        <strong>{{ q.text }}</strong>
        <ul>
          <li [class.correct]="q.correct" (click)="answerQuestion(i)">
            {{ q.text }} {{ q.correct ? '(RICHTIG)' : '' }}
          </li>
        </ul>
      </li>
    </ul>

    <p *ngIf="currentIndex() >= total()">
      Fertig! {{ score() }} von {{ total() }} richtig.
    </p>
  </div>
</ng-template>
```

---

### 3.7 Frontend Model: `question.model.ts`

> Falls noch nicht vorhanden: `lernportal-frontend/src/app/shared/models/question.model.ts`

```typescript
export interface Question {
  id?: number;
  text: string;
  hint?: string;
  courseId?: number;
}

export interface Answer {
  id?: number;
  text: string;
  correct: boolean;
  questionId?: number;
}
```

---

## Unit-Tests

| Testdatei | Was wird getestet | Testfälle |
|-----------|-------------------|-----------|
| `QuestionServiceTest.java` | Service-Methoden (mit Mockito) | 1. `findByCourse` gibt Fragen zurück<br>2. `save` persistiert Frage<br>3. Exception bei null |
| `QuestionControllerTest.java` | Controller-Endpunkte | 1. GET `/api/questions/course/{id}` → 200 + Liste<br>2. POST `/api/questions` → 201<br>3. Error-Handling |
| `quiz.component.spec.ts` | Komponenten-Rendering | 1. Loading-Text sichtbar<br>2. Fragen gerendert<br>3. Antwort-Click erhöht Score |

**Frontend-Test-Befehl:**
```bash
cd lernportal-frontend
ng test
```

**Backend-Test-Befehl:**
```bash
cd lernportal-backend
./gradlew test
```

---

## Definition of Done

- [ ] `Question` Entity mit `@OneToMany` zu `CourseEntity`
- [ ] `Answer` Entity mit `@ManyToOne` zu `Question`
- [ ] `QuestionRepository` (Custom Query `findByCourseId`)
- [ ] `QuestionService` (CRUD)
- [ ] `QuestionController` (`/api/questions`, `/api/questions/course/{courseId}`)
- [ ] `QuizService` (Frontend) + `QuizComponent` (Template + Daten)
- [ ] `question.model.ts` (Frontend Types)
- [ ] `QuestionServiceTest` grün
- [ ] `QuestionControllerTest` grün (`@WebMvcTest`)
- [ ] `quiz.component.spec.ts` grün (Vitest)
- [ ] `./gradlew test` + `ng test` erfolgreich
- [ ] In `00-UEBERSICHT.md` Status Task 3.1–3.2 auf ✅ gesetzt

---

## Nächster Schritt
→ Phase 4: `04-ADMIN-MODUL.md` (CRUD-Oberfläche für Kurse/Lektionen/Quizfragen)