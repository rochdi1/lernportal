# Nächste Schritte – Lernportal

Angular-Frontend (Standalone, Angular 22) aufsetzen und an das Spring-Boot-Backend anbinden

## 1. Angular-Projekt generieren

Im Terminal (außerhalb des Backend-Ordners):

```bash
ng new lernportal-frontend --routing --style=scss
cd lernportal-frontend
ng serve
```

- `--routing` → legt `src/app/app.routes.ts` an (Routing-Konfiguration für Standalone-App)
- `--style=scss` → SCSS statt reinem CSS
- Standardmäßig läuft die App danach auf `http://localhost:4200`
- Angular 22 erzeugt standardmäßig **Standalone-Components** (kein `AppModule` / `app.module.ts` mehr)

## 2. Projektstruktur anlegen

In Angular 22 werden keine `NgModule`s mehr erzeugt. Stattdessen legt man einfach Ordner an und verwendet Standalone-Komponenten / -Services:

```bash
mkdir -p src/app/core/services
mkdir -p src/app/shared/models
mkdir -p src/app/features/courses
mkdir -p src/app/features/quiz
mkdir -p src/app/features/admin
```

Empfohlene Ordnerstruktur:

```
src/app/
├── app.config.ts         (ApplicationConfig mit Providern, ersetzt AppModule)
├── app.routes.ts         (Routing-Konfiguration, ersetzt app-routing.module.ts)
├── app.ts                (Root Standalone-Component)
├── core/
│   └── services/         (Services, Guards, Interceptors - einmalig geladen)
├── shared/
│   └── models/           (TypeScript-Interfaces)
├── features/
│   ├── courses/          (Kursliste, Lektionsansicht)
│   ├── quiz/             (Quiz-Komponente)
│   └── admin/            (CRUD-Verwaltung der Inhalte)
└── main.ts               (Bootstrap der Standalone-App)
```

## 3. HttpClient einbinden

In `src/app/app.config.ts` (statt `app.module.ts`):

```typescript
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { provideClientHydration } from '@angular/platform-browser';
import { provideHttpClient } from '@angular/common/http';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideClientHydration(),
    provideHttpClient()   // <-- HttpClient für Backend-Aufrufe
  ]
};
```

## 4. Models (TypeScript-Interfaces) anlegen

`src/app/shared/models/course.model.ts`

```typescript
export interface Course {
  id?: number;
  title: string;
  description: string;
}
```

## 5. Service zur Anbindung ans Backend

`src/app/core/services/course.service.ts`

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Course } from '../../shared/models/course.model';

@Injectable({ providedIn: 'root' })
export class CourseService {
  private apiUrl = 'http://localhost:8080/api/courses';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Course[]> {
    return this.http.get<Course[]>(this.apiUrl);
  }

  create(course: Course): Observable<Course> {
    return this.http.post<Course>(this.apiUrl, course);
  }

  update(id: number, course: Course): Observable<Course> {
    return this.http.put<Course>(`${this.apiUrl}/${id}`, course);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
```

## 6. Erste Komponente: Kursliste

```bash
ng generate component features/courses/course-list --standalone
```

`course-list.component.ts`

```typescript
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CourseService } from '../../../core/services/course.service';
import { Course } from '../../../shared/models/course.model';

@Component({
  selector: 'app-course-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './course-list.component.html'
})
export class CourseListComponent implements OnInit {
  courses: Course[] = [];

  constructor(private courseService: CourseService) {}

  ngOnInit(): void {
    this.courseService.getAll().subscribe(data => this.courses = data);
  }
}
```

`course-list.component.html`

```html
<ul>
  <li *ngFor="let course of courses">
    {{ course.title }} – {{ course.description }}
  </li>
</ul>
```

## 7. Routing einrichten

In `src/app/app.routes.ts`:

```typescript
import { Routes } from '@angular/router';
import { CourseListComponent } from './features/courses/course-list.component';

export const routes: Routes = [
  { path: 'courses', component: CourseListComponent },
  { path: '', redirectTo: '/courses', pathMatch: 'full' }
];
```

## 8. Testen

1. Backend starten (`./gradlew bootRun` im Backend-Ordner)
2. Frontend starten (`ng serve` im Frontend-Ordner)
3. Browser: `http://localhost:4200/courses`
4. Prüfen, ob die Kursliste vom Backend geladen wird (Browser-Konsole/Network-Tab bei Fehlern checken, meist CORS)

## 9. Danach: Quiz-Komponente

- Entities `Question`/`Answer` im Backend analog zu `Course` anlegen (Entity, Repository, Controller)
- Im Frontend: `QuizService` + `QuizComponent`, das Fragen anzeigt, Auswahl speichert und am Ende auswertet
- Fortschritt/Ergebnis optional an `/api/quiz-results` senden und in der DB speichern

## 10. Offene Themen für später

- Authentifizierung (Spring Security + JWT), damit Fortschritt pro Nutzer gespeichert werden kann
- Admin-Bereich (CRUD-Oberfläche in Angular) zum Pflegen von Kursen/Lektionen/Quizfragen
- Einbindung der Erklärvideos/Visualisierungen (Video-Hosting oder eigene SVG/Canvas-Animationen)
- Deployment-Strategie (Docker-Compose für Backend + Frontend + PostgreSQL zusammen)
