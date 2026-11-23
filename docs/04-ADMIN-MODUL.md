# 04 – Admin-Modul (Phase 4)

## Ziel
Admin-Bereich in Angular: CRUD-Oberfläche zum Pflegen von Kurse, Lektionen, Quiz-Fragen/Antworten.

---

## Voraussetzungen
- Phase 2 erledigt (CourseController, CourseService, DTOs)
- Phase 3 erledigt (Question/Answer Backend + Quiz)

---

## Betroffene Dateien

| Bereich | Datei | Aktion |
|---------|-------|--------|
| **Frontend** | `lernportal-frontend/src/app/features/admin/admin.component.ts` | **neu** |
| **Frontend** | `lernportal-frontend/src/app/features/admin/admin.component.html` | **neu** |
| **Frontend** | `lernportal-frontend/src/app/features/admin/admin.routes.ts` | **neu** (falls eigener Admin-Layout) |
| **Frontend** | `lernportal-frontend/src/app/app.routes.ts` | **ändern** – Route `/admin` ergänzen |
| **Backend** | `lernportal-backend/src/main/java/de/rochdi/lernportal_backend/controller/CourseController.java` | **erweitern** – CRUD via API |
| **Backend** | `lernportal-backend/src/main/java/de/rochdi/lernportal_backend/controller/QuestionController.java` | **erweitern** – CRUD via API |

---

## Admin-UI: Kurse verwalten

- Liste Kurse (`GET /api/courses`)
- Neues Formular / Dialog zum Anlegen (`POST /api/courses`)
- Bearbeiten / Löschen (`PUT /api/courses/{id}`, `DELETE /api/courses/{id}`)

---

## Unit-Tests

| Testdatei | Was wird getestet | Testfälle |
|-----------|-------------------|-----------|
| `admin.component.spec.ts` | Rendering des Admin-Forms | 1. Liste rendert<br>2. Formular-Felder vorhanden<br>3. Save-Action ruft Service auf |
| `CourseControllerTest.java` | Admin-API-Calls | 1. POST → 201<br>2. PUT → 200<br>3. DELETE → 204 |

---

## Definition of Done

- [ ] Admin-Route in `app.routes.ts` (`path: 'admin'`)
- [ ] Admin-Komponente + Template vorhanden
- [ ] API-Endpunkte für CRUD vorhanden
- [ ] Unit-Tests grün
- [ ] Status Task 4.1 in `00-UEBERSICHT.md` auf ✅

---

## Nächster Schritt
→ Phase 5: `05-AUTHENTIFIZIERUNG.md` (Spring Security + JWT)
