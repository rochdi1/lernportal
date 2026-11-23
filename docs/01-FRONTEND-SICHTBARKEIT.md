# 01 – Frontend Sichtbarkeit (Phase 1)

## Ziel
Die Kursliste unter `http://localhost:4200/courses` sofort sichtbar machen, ohne dass der User scrollen muss. Die aktuelle Angular-Platzhalter-Seite in `app.html` verdeckt den `<router-outlet />`.

---

## Voraussetzungen
- Angular-Frontend läuft (`ng serve` auf Port 4200)
- Backend liefert Daten unter `GET http://localhost:8080/api/courses` (Postman bestätigt)

---

## Betroffene Dateien

| Datei | Aktion |
|-------|--------|
| `lernportal-frontend/src/app/app.html` | **ändern** – Platzhalter entfernen, nur `<router-outlet />` |
| `lernportal-frontend/src/app/features/courses/course-list/course-list.html` | **ändern** – Platzhalter-Text `<p>course-list works!</p>` entfernen |
| `lernportal-frontend/src/app/features/courses/course-list/course-list.ts` | **erweitern** – Loading-State + Error-Handling |

---

## 1.1 `app.html` bereinigen

**Aktueller Zustand:** Zeilen 1–343 enthalten CSS + vollständige Angular-Willkommens-Seite. `<router-outlet />` steht ganz unten (Zeile 344).

**Ziel:** Nur `<router-outlet />` (optional mit minimalem Wrapper).

**Neuer Inhalt:**

```html
<!-- lernportal-frontend/src/app/app.html -->
<router-outlet />
```

> 💡 Tipp: Wenn du später ein Layout (Header/Footer) willst, füge hier ein `<header>`/`<footer>` hinzu, aber halte `<router-outlet />` als einziges Kind.

---

## 1.2 `course-list.html` bereinigen

**Aktueller Zustand:** Enthält `<p>course-list works!</p>` vor der Liste.

**Ziel:** Nur die `<ul>`-Liste.

**Neuer Inhalt:**

```html
<!-- lernportal-frontend/src/app/features/courses/course-list/course-list.html -->
<ul>
  <li *ngFor="let course of courses">
    {{ course.title }} – {{ course.description }}
  </li>
</ul>
```

---

## 1.3 Loading-State + Error-Handling in `course-list.ts`

**Aktueller Zustand:** Einfacher `subscribe()` ohne Feedback.

**Erweiterung:**

```typescript
// lernportal-frontend/src/app/features/courses/course-list/course-list.ts
import { Component, OnInit, signal } from '@angular/core';
import { Course } from '../../../shared/models/course.model';
import { CourseService } from '../../../core/services/course.service';
import { CommonModule } from '@angular/common';

@Component({
  imports: [CommonModule],
  selector: 'app-course-list',
  standalone: true,
  styleUrl: './course-list.scss',
  templateUrl: './course-list.html',
})
export class CourseList implements OnInit {
  courses = signal<Course[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

  constructor(private courseService: CourseService) {}

  ngOnInit(): void {
    this.loadCourses();
  }

  loadCourses(): void {
    this.loading.set(true);
    this.error.set(null);

    this.courseService.getAll().subscribe({
      next: (data) => {
        this.courses.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Fehler beim Laden der Kurse:', err);
        this.error.set('Kurse konnten nicht geladen werden. Bitte Backend prüfen.');
        this.loading.set(false);
      }
    });
  }
}
```

**Template anpassen (`course-list.html`):**

```html
<div *ngIf="loading(); else content">Lade Kurse…</div>

<ng-template #content>
  <div *ngIf="error()" class="error">{{ error() }}</div>

  <ul *ngIf="courses().length > 0">
    <li *ngFor="let course of courses()">
      {{ course.title }} – {{ course.description }}
    </li>
  </ul>

  <p *ngIf="!loading() && !error() && courses().length === 0">
    Keine Kurse vorhanden.
  </p>
</ng-template>
```

---

## Unit-Tests

| Testdatei | Was wird getestet | Testfälle |
|-----------|-------------------|-----------|
| `course-list.spec.ts` | Component rendert Loading, Error, Liste | 1. Loading-Text sichtbar<br>2. Error-Meldung bei HTTP-Fehler<br>3. Liste rendert bei Erfolg |
| `course.service.spec.ts` | Service ruft korrekt API auf | 1. `getAll()` macht GET auf `/api/courses`<br>2. `create()` macht POST<br>3. Fehler-Handling in Service |

**Frontend-Test-Befehl:**
```bash
cd lernportal-frontend
ng test
```

---

## Definition of Done

- [ ] `app.html` enthält nur `<router-outlet />`
- [ ] `course-list.html` zeigt nur Liste (kein Platzhalter-Text)
- [ ] `course-list.ts` hat `loading`, `error`, `loadCourses()`
- [ ] Browser: `http://localhost:4200/courses` zeigt sofort Liste
- [ ] Unit-Tests grün: `ng test` (Vitest)
- [ ] In `00-UEBERSICHT.md` Status Task 1.1–1.3 auf ✅ gesetzt

---

## Nächster Schritt
→ Phase 2: `02-BACKEND-QUALITAET.md` (Service-Layer, DTOs, Controller-Tests)