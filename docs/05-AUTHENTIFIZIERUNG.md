# 05 – Authentifizierung (Phase 5)

## Ziel
Spring Security + JWT: Login/Register, geschützte Endpunkte, Frontend-Auth-Service + Guards.

---

## Voraussetzungen
- Phase 1–4 erledigt
- Backend mit User-Entity (neu) + Role

---

## Betroffene Dateien

| Bereich | Datei | Aktion |
|---------|-------|--------|
| **Backend** | `UserEntity.java` | **neu** |
| **Backend** | `UserRepository.java` | **neu** |
| **Backend** | `JwtService.java` | **neu** |
| **Backend** | `AuthController.java` | **neu** (Login/Register) |
| **Backend** | `SecurityConfig.java` | **neu** (Spring Security + JWT Filter) |
| **Backend** | `AuthEntryPointJwt.java` / `AuthTokenFilter.java` | **neu** |
| **Frontend** | `auth.service.ts` | **neu** |
| **Frontend** | `auth.guard.ts` | **neu** (CanActivate) |
| **Frontend** | `login.component.ts/.html` | **neu** |
| **Frontend** | `register.component.ts/.html` | **neu** |

---

## Backend: Security-Konfiguration

- JWT Token Generation/Validierung
- `/api/auth/**` öffentlich, alle anderen `/api/**` geschützt
- BCrypt Password Encoding

---

## Unit-Tests

| Testdatei | Was wird getestet | Testfälle |
|-----------|-------------------|-----------|
| `AuthControllerTest.java` | Login/Register | 1. Register → 201 + User<br>2. Login → 200 + JWT<br>3. Invalid Credentials → 401 |
| `JwtServiceTest.java` | Token-Generierung/Validierung | 1. Token generiert<br>2. Gültiger Token → true<br>3. Abgelaufener Token → false |
| `SecurityConfigTest.java` | Filter-Kette | 1. Öffentliche Endpunkte ohne Token<br>2. Geschützte Endpunkte mit Token |

**Frontend:**
| Testdatei | Was wird getestet |
|-----------|-------------------|
| `auth.service.spec.ts` | Login speichert Token, Logout löscht |
| `auth.guard.spec.ts` | Guard leitet zu Login um, wenn kein Token |

---

## Definition of Done

- [ ] User-Entity + Repository
- [ ] JWT-Service (Issue, Validate, Refresh)
- [ ] AuthController (Register/Login/Logout)
- [ ] SecurityConfig (JWT Filter, Permit/Deny)
- [ ] Frontend AuthService + Guard
- [ ] Login/Register Komponenten
- [ ] Unit-Tests grün (`./gradlew test` + `ng test`)
- [ ] Status Task 5.1 in `00-UEBERSICHT.md` auf ✅