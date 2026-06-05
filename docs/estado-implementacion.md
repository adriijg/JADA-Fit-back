# Estado de implementación

Última revisión: junio 2026. Refleja el estado real del código en `jadafit-front` y `jadafit-api`.

## Resumen por capa

| Área | Backend | Frontend | Notas |
|------|---------|----------|-------|
| Auth (email/password) | ✅ | ✅ | JWT + sesiones en BD |
| OAuth (Google/Apple/FB) | ❌ | 🔶 UI stub | Botones muestran "próximamente" |
| Onboarding | ✅ | ✅ | Perfil físico inicial |
| Home / dashboard | — | ✅ | Resumen nutrición, rutina, progreso, IA |
| Nutrición | ✅ | ✅ | Comidas, agua, recetas, barcode, custom foods |
| Rutinas | ✅ | ✅ | CRUD + catálogo; completado **solo local** |
| Perfil físico | ✅ | ✅ | Logs, gráficos, calendario |
| Coach IA | ✅ | ✅ | Chat + historial; requiere LLM local |
| Configuración | — | ✅ | Tema, idioma ES/EN, unidades |
| Social — core | ✅ | 🔶 Parcial | Ver detalle abajo |
| Integraciones wearables | ❌ | 🔶 Stub | Settings → "Integraciones próximamente" |
| i18n | — | 🔶 ~95% | Mayoría migrada a l10n; restos menores |

Leyenda: ✅ completo · 🔶 parcial · ❌ no implementado

---

## Módulo social (detalle)

El backend expone la capa social completa. El frontend tiene servicios conectados pero la **experiencia de usuario aún no está cerrada**.

### Implementado (front + back)

| Funcionalidad | API | UI |
|---------------|-----|-----|
| Feed de posts | `GET /social/posts/feed` | Tab Inicio en SocialScreen |
| Stories (24h) | `GET/POST /social/stories/*` | Carrusel en feed + StoryViewerScreen |
| Explorar posts | `GET /social/posts/explore` | ExploreScreen |
| Buscar usuarios | `GET /social/users/search` | Barra de búsqueda en Explore |
| Perfil de otro usuario | `GET /social/profile/{id}` | UserProfileScreen |
| Seguir / dejar de seguir | `POST/DELETE /social/follow/*` | Botón en UserProfileScreen |
| Crear post (foto) | `POST /social/posts` + upload | MySocialProfileScreen |
| Crear historia | `POST /social/stories` | MySocialProfileScreen |
| Editar bio / avatar | `PUT /users/me/profile` | MySocialProfileScreen |
| Retos (piques) | `POST/GET /challenges/*` | Tab Piques: crear, aceptar, rechazar |
| Actualizar marcas personales | `POST /challenges/records` | Diálogo en tab Piques |
| Privacidad `shareProgress` | `PUT /users/me/privacy` | ProfileScreen |

### Pendiente en frontend (API ya existe)

| Funcionalidad | Servicio front | Qué falta |
|---------------|----------------|-----------|
| **Likes en posts** | `PostService.likePost/unlikePost` | Iconos del feed sin `onTap` |
| **Comentarios** | `getComments/addComment` | Sin hoja de comentarios ni contador |
| **Lista de seguidores** | `getFollowers/getFollowing` | Contadores visibles, sin pantalla al pulsar |
| **Historial de marcas** | `getMyRecords` | Solo diálogo de actualización, sin listado |
| **Detalle de post** | — | No hay pantalla de post individual |
| **Compartir / guardar** | — | Iconos decorativos en `_PostCard` |
| **Métricas sociales en Home** | — | `SocialSummaryCard` sin datos reales de retos |
| **i18n social** | — | Alguna etiqueta aún hardcodeada |

### No planificado en código actual

- Mensajería directa
- Retos en vivo / streaming
- Gamificación por niveles (solo en roadmap del README)

---

## Otros pendientes transversales

| Item | Descripción |
|------|-------------|
| **Sincronización de rutinas completadas** | Front guarda en `CompletedStorageService`; backend tiene endpoints `/complete` no usados por la UI |
| **Cliente HTTP centralizado** | Token y errores duplicados en cada `*_service.dart` |
| **Dispositivo físico / iOS** | `api_config.dart` solo contempla web y emulador Android |
| **`app_strings.dart`** | Constantes legacy en español; candidato a eliminar tras cerrar l10n |
| **Catálogo de ejercicios** | Descripciones en español hardcodeadas en `exercise_catalog.dart` (datos, no UI) |

---

## Endpoints API por módulo (referencia rápida)

Documentación completa en Swagger. Resumen:

| Módulo | Endpoints |
|--------|-----------|
| Users | 8 |
| Onboarding | 1 |
| Fitness | 4 |
| Nutrition | 11 |
| Foods | 6 |
| Recipes | 5 |
| Routines | 7 |
| Catalog exercises | 2 |
| AI | 1 |
| Social | 14 |
| Challenges | 6 |
| Upload | 1 |

**Total: ~60 endpoints** bajo `/api`.

---

## Roadmap sugerido (prioridad)

1. **Cerrar social UI** — likes, comentarios, listas de seguidores
2. **Sincronizar completado de rutinas** con backend
3. **Terminar l10n** — formularios de perfil, strings restantes
4. **Cliente HTTP único** — interceptor de token y errores
5. **OAuth** — cuando el backend lo soporte
6. **Despliegue** — documentar entorno de producción

Ver visión a largo plazo en `jadafit-front/Plan de Empresa JADA-FIT.md`.
