# Guía de desarrollo

Cómo levantar el entorno local completo de JADA FIT.

## Requisitos

| Herramienta | Versión |
|-------------|---------|
| Java | 21 |
| Maven | incluido (`mvnw` / `mvnw.cmd`) |
| Docker | para PostgreSQL |
| Flutter | SDK compatible con Dart ^3.11 |
| Git | — |

Opcional para IA local:

- `setup_llama.bat` en `jadafit-api/` (descarga llama.cpp + modelo, puerto 8090)

## 1. Base de datos

```bash
cd jadafit-api
docker compose up -d
```

| Parámetro | Valor por defecto |
|-----------|-------------------|
| Puerto | 5432 |
| Base de datos | postgres |
| Usuario | postgres |
| Contraseña | jadaFit-2025 |

## 2. Backend

### Configuración local

Crear `jadafit-api/src/main/resources/application-local.properties` (gitignored):

```properties
DB_URL=jdbc:postgresql://localhost:5432/postgres
DB_USER=postgres
DB_PASSWORD=jadaFit-2025
SEC_KEY=<cadena-secreta-jwt-64-chars-hex>

# Opcional — reset de contraseña por email
MAIL_HOST=smtp.mailtrap.io
MAIL_PORT=587
MAIL_USERNAME=
MAIL_PASSWORD=
```

`SEC_KEY` debe ser una clave HMAC segura. Sin `application-local.properties` la API no arrancará correctamente.

### Arrancar la API

```bash
cd jadafit-api
.\mvnw.cmd spring-boot:run        # Windows
./mvnw spring-boot:run            # Linux/macOS
```

| Recurso | URL |
|---------|-----|
| API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI | http://localhost:8080/v3/api-docs |

Hibernate crea/actualiza el esquema (`ddl-auto=update`). Al iniciar se ejecutan seeders del catálogo de ejercicios.

### Tests

```bash
.\mvnw.cmd test
```

### IA local (opcional)

```bash
cd jadafit-api
.\setup_llama.bat
```

Servidor en `http://localhost:8090`. Sin esto, `/api/ai/chat` fallará pero el resto de la app funciona.

## 3. Frontend

```bash
cd jadafit-front
flutter pub get
flutter gen-l10n    # regenerar traducciones tras editar .arb
flutter run
```

### URL de la API según plataforma

Definida en `lib/core/config/api_config.dart`:

| Plataforma | Base URL |
|------------|----------|
| Web | `/api` (mismo host que sirve el backend) |
| Android emulador | `http://10.0.2.2:8080/api` |
| iOS simulador / dispositivo físico | **Requiere cambiar la IP** al host de tu máquina |

Para probar en dispositivo físico o iOS, edita `api_config.dart` con la IP local de tu PC (ej. `http://192.168.1.x:8080/api`).

### Web empaquetada con el backend

```bash
cd jadafit-front
flutter build web
```

El backend sirve automáticamente `../jadafit-front/build/web/` en la raíz si existe.

### Análisis y lints

```bash
flutter analyze lib
```

## 4. Flujo de prueba típico

1. `docker compose up -d` en `jadafit-api`
2. Arrancar API con `mvnw spring-boot:run`
3. `flutter run` en emulador Android o `flutter run -d chrome` para web
4. Registrar usuario → completar onboarding → explorar módulos
5. Para social: crear posts desde **Social → Mi Perfil**; retos desde perfil de otro usuario

## 5. Autenticación en Swagger

1. `POST /api/users/register` o `/login` → copiar `token` de la respuesta
2. En Swagger UI → **Authorize** → `Bearer <token>`

## 6. Estructura de branches (referencia)

Según el historial del repositorio API:

- `develop` — integración
- `feature/*` — ramas de desarrollo por persona/feature
- `testing` — pruebas

Confirmar convención con el equipo antes de contribuir.

## Problemas frecuentes

| Problema | Solución |
|----------|----------|
| API no conecta desde emulador | Usar `10.0.2.2`, no `localhost` |
| Error de JWT / 401 | Token expirado; volver a login |
| IA no responde | Comprobar que llama.cpp está en puerto 8090 |
| Tablas vacías de ejercicios | Reiniciar API (DataSeeder corre al boot) |
| `flutter gen-l10n` tras editar ARB | Ejecutar tras cambiar `app_en.arb` / `app_es.arb` |
