# Nächste Schritte – Lernportal

Angular-Frontend (Standalone, Angular 22) aufsetzen und an das Spring-Boot-Backend anbinden

> **Hinweis:** Dieses Dokument beschreibt den **tatsächlichen Stand** des Projekts im Ordner `lernportal-frontend/`. Alle Pfade sind relativ zum Projekt-Root (`/home/casa/AI_Project/JavaAngular/`).

## 1. Angular-Projekt generieren (bereits erledigt)

Das Projekt wurde bereits mit Angular CLI 22.3.0 erstellt:

```bash
ng new lernportal-frontend --routing --style=scss
cd lernportal-frontend
ng serve
```

- `--routing` → legt `lernportal-frontend/src/app/app.routes.ts` an (Routing-Konfiguration für Standalone-App)
- `--style=scss` → SCSS statt reinem CSS
- Standardmäßig läuft die App auf `http://localhost:4200`
- Angular 22 erzeugt standardmäßig **Standalone-Components** (kein `AppModule` / `app.module.ts`)

## 2. Projektstruktur (bereits angelegt)

Die folgende Struktur existiert bereits in `lernportal-frontend/src/app/`:

```
lernportal-frontend/src/app/
├── app.config.ts              (ApplicationConfig mit Providern, ersetzt AppModule)
├── app.config.server.ts       (Server-seitige Config für SSR)
├── app.routes.ts              (Routing-Konfiguration, ersetzt app-routing.module.ts)
├── app.routes.server.ts       (Server-seitige Routen)
├── app.ts                     (Root Standalone-Component: class App)
├── app.html                   (Template der Root-Component – enthält <router-outlet>)
├── app.scss                   (Styles der Root-Component)
├── app.spec.ts                (Unit-Test für App)
├── main.ts                    (Bootstrap der Standalone-App: bootstrapApplication)
├── main.server.ts             (Server-Entry für SSR)
├── core/
│   └── services/
│       └── course.service.ts  (CourseService mit providedIn: 'root')
├── shared/
│   └── models/
│       └── course.model.ts    (TypeScript-Interface Course)
├── features/
│   ├── courses/
│   │   └── course-list/
│   │       ├── course-list.ts         (Standalone-Component: class CourseList)
│   │       ├── course-list.html       (Template mit *ngFor)
│   │       ├── course-list.scss       (Styles)
│   │       └── course-list.spec.ts    (Unit-Test)
│   ├── quiz/                  (leer – für spätere Quiz-Komponente)
│   └── admin/                 (leer – für spätere Admin-Komponente)
└── server.ts                  (Express-Server für SSR)
```

## 3. HttpClient einbinden (bereits in `app.config.ts`)

Datei: `lernportal-frontend/src/app/app.config.ts`

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
    provideHttpClient()   // HttpClient für Backend-Aufrufe
  ]
};
```

## 4. Models (TypeScript-Interfaces)

Datei: `lernportal-frontend/src/app/shared/models/course.model.ts`

```typescript
export interface Course {
  id?: number;
  title: string;
  description: string;
}
```

## 5. Service zur Anbindung ans Backend

Datei: `lernportal-frontend/src/app/core/services/course.service.ts`

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

## 6. Erste Komponente: Kursliste (bereits vorhanden)

Datei: `lernportal-frontend/src/app/features/courses/course-list/course-list.ts`

```typescript
import { Component, OnInit } from '@angular/core';
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
  courses: Course[] = [];

  constructor(private courseService: CourseService) {}

  ngOnInit(): void {
    this.courseService.getAll().subscribe(data => this.courses = data);
  }
}
```

Template: `lernportal-frontend/src/app/features/courses/course-list/course-list.html`

```html
<p>course-list works!</p>
<ul>
  <li *ngFor="let course of courses">
    {{ course.title }} – {{ course.description }}
  </li>
</ul>
```

**Wichtige Unterschiede zur Angular-CLI-Standardbenennung:**
- Dateiname: `course-list.ts` (nicht `course-list.component.ts`)
- Klassenname: `CourseList` (nicht `CourseListComponent`)
- Template: `course-list.html` (nicht `course-list.component.html`)

## 7. Routing einrichten (bereits in `app.routes.ts`)

Datei: `lernportal-frontend/src/app/app.routes.ts`

```typescript
import { Routes } from '@angular/router';
import { CourseList } from './features/courses/course-list/course-list';

export const routes: Routes = [
  { path: 'courses', component: CourseList },
  { path: '', redirectTo: '/courses', pathMatch: 'full' }
];
```

Root-Template `app.html` enthält bereits `<router-outlet />` am Ende der Datei.

## 8. Testen

1. Backend starten (`./gradlew bootRun` im `lernportal-backend/`-Ordner)
2. Frontend starten (`ng serve` im `lernportal-frontend/`-Ordner)
3. Browser: `http://localhost:4200/courses`
4. Prüfen, ob die Kursliste vom Backend geladen wird (Browser-Konsole/Network-Tab bei Fehlern checken, meist CORS)

> **Wichtig:** Die Datei `app.html` enthält noch die komplette Angular-Platzhalter-Seite (Zeilen 1–343). Die Kursliste wird **unterhalb** dieser Seite gerendert. Um die Liste sofort sichtbar zu machen, in `app.html` alles bis auf `<router-outlet />` löschen.

## 9. Danach: Quiz-Komponente

- Entities `Question`/`Answer` im Backend analog zu `Course` anlegen (Entity, Repository, Controller)
- Im Frontend: `QuizService` + `QuizComponent`, das Fragen anzeigt, Auswahl speichert und am Ende auswertet
- Fortschritt/Ergebnis optional an `/api/quiz-results` senden und in der DB speichern

## 10. Offene Themen für später

- Authentifizierung (Spring Security + JWT), damit Fortschritt pro Nutzer gespeichert werden kann
- Admin-Bereich (CRUD-Oberfläche in Angular) zum Pflegen von Kursen/Lektionen/Quizfragen
- Einbindung der Erklärvideos/Visualisierungen (Video-Hosting oder eigene SVG/Canvas-Animationen)
- Deployment-Strategie (Docker-Compose für Backend + Frontend + PostgreSQL zusammen)