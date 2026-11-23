# 99 – Test-Strategie (Cross-Cutting)

## Überblick

Alle Features werden getestet mit **zwei parallelen Frameworks**:

| Frontend | Vitest (`ng test`) |
|----------|---------------------|
| Backend | JUnit 5 + Mockito (`./gradlew test`) |

### Regeln

1. **Jedes Feature-MD listet seine Tests explizit** (siehe `03-QUIZ-MODUL.md`, `04-ADMIN-MODUL.md` etc.).
2. **Frontend-Tests** starten mit `cd lernportal-frontend && ng test`.
3. **Backend-Tests** starten mit `cd lernportal-backend && ./gradlew test`.
4. Ein Test muss in **beiden** Umgebungen grün sein, bevor der Task ✅ gesetzt wird.

---

## Test-Frameworks im Detail

### Frontend (Vitest)
- Installiert: `@angular/cli`, `@angular/compiler`, `vitest`
- Befehl: `ng test`
- Typische Struktur:
  ```bash
  cd lernportal-frontend
  ng test
  ```
- Nutzt `TestBed.configureTestingModule({ imports: [...] })` wie im `course-list.spec.ts`.

### Backend (JUnit 5 + Mockito)
- Gradle: `./gradlew test`
- Mockito für Controller/Service.
- `@WebMvcTest(CourseController.class)`, `@DataJpaTest`, `@SpringBootTest(webEnvironment = SpringBootTestEnvironment.RANDOM_PORT)`

---

## Cross-Cutting Concerns

| Area | Testfokus |
|------|-----------|
| **HttpClient** | `CourseService.getAll()` → GET `/api/courses` |
| **CORS** | `CorsConfig.java` erlaubt `/api/**` für `http://localhost:4200` |
| **Global ExceptionHandler** | HTTP 404 / 400 zurückgeben bei CourseNotFoundException |
| **Auth** | Login/Register Flow, Token Validierung |
| **Database** | `CourseRepository.findAll()` → Tabelle `course` automatisch angelegt |

---

## Definition of Done (Tests)

- [ ] Frontend-Tests grün: `ng test`
- [ ] Backend-Tests grün: `./gradlew test`
- [ ] Kein Test fehlgeschlagen
- [ ] In jedem MD die Testsliste vorhanden
- [ ] Status Tasks auf ✅ gesetzt

---

## Nächster Schritt
→ Phase 6: Deployment & CI/CD (GitHub Actions, Dockerfiles)
