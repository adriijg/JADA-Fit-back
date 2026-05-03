# JADA FIT - Resumen funcional de la aplicación.

## 1. Enfoque general

JADA FIT será una aplicación fitness modular, organizada por secciones claras y separando responsabilidades.

La idea principal es evitar que una pantalla acumule demasiada información. Cada sección debe cumplir una función concreta:

- Home: resumen general del estado del usuario.
- Nutrition: alimentación, comidas, macros e historial nutricional.
- Workout: rutinas, ejercicios y entrenamiento.
- AI: entrenador inteligente conectado a los datos de la app.
- Social: amigos, retos, feed y comparativas.
- Profile: cuenta, configuración e integraciones.

---

## 2. Navegación principal

Navegación principal

La barra de navegación principal tendrá estas secciones:

- Home
- Nutrition
- AI
- Workout
- Social

El perfil no formará parte de la navbar. Se accederá desde el icono de usuario en el header.

---

## 3. HomeScreen

### Objetivo

La pantalla Home funcionará como un dashboard general. No será una pantalla para gestionar detalles profundos, sino para mostrar un resumen rápido del estado actual del usuario.

### Funcionalidades
- Saludo de la IA.
- Resumen de entrenamiento.
- Resumen nutricional.
- Mini progreso físico.
- Actividad social.
- Accesos rápidos. 

### Ejemplo de contenido

Hola, Alejandro 👋

Tu IA recomienda subir proteína hoy.

Entrenamiento: Push Day · 5 ejercicios pendientes.

Nutrición: 1450 / 2300 kcal · 95 / 160 g proteína.

Progreso: 68 kg · +8 kg desde el inicio.

Social: 2 retos activos · 1 solicitud de amistad.

### Rol de la pantalla

Home debe responder a estas preguntas:

- ¿Cómo voy hoy?
- Qué tengo pendiente?
- ¿Qué me recomienda la app?
- ¿Qué accesos rápidos necesito?

## 4. ProfileScreen

### Decisión de diseño

El perfil quedará centrado en la cuenta del usuario, no en sus datos físicos ni en su progreso deportivo.

Esto evita que la pantalla de perfil se sobrecargue y permite separar mejor la información.

### Funcionalidades
- Username.
- Email.
- Configuración de cuenta.
- Integraciones futuras.
- Cerrar sesión.

### Futuras integraciones

En el futuro, el perfil podrá incluir accesos o conexiones con servicios externos:

- Apple Health.
- Google Fit.
- Wearables.
- Preferencias de privacidad.
- Configuración de notificaciones.
- Gestión de cuenta.
- Cambio de contraseña.

### Lo que no irá en Profile

Los siguientes datos no deberían vivir directamente en Profile:

- Peso.
- Altura.
- Edad.
- Objetivo físico.
- Gráficas de progreso.
- Historial corporal.
- Progreso muscular.

Estos datos irán en una sección separada de perfil físico o progreso.

---

## 5. Physical Profile

### Objetivo

El perfil físico será una sección separada del perfil de cuenta.

Aquí se gestionarán los datos físicos actuales del usuario.

### Datos físicos actuales

- Peso actual.
- Altura.
- Edad.
- Género.
- Objetivo.
- Grasa corporal.
- Masa muscular.

### Uso dentro de la app

Estos datos podrán ser utilizados por:

- HomeScreen.
- Nutrition.
- Workout.
- AI.
- ProgressScreen.

---

## 6. Progress

### Objetivo

La sección de progreso servirá para analizar la evolución física del usuario a lo largo del tiempo.

### Datos de progreso

- Evolución de peso.
- Evolución de grasa corporal.
- Evolución de masa muscular.
- Cambios desde el inicio.
- Tendencias semanales.
- Tendencias mensuales.

### Uso futuro

Estos datos se podrán mostrar en:

- HomeScreen como resumen pequeño.
- ProgressScreen como vista detallada.
- AI para recomendaciones personalizadas.
- Gráficas futuras.

### Decisión importante

El progreso físico no debe mostrarse de forma completa dentro de Profile.

Profile será para cuenta. Progress será para evolución física.

---

## 7. Nutrition

### Objetivo

Nutrition será el centro de control alimenticio del usuario.

### Funcionalidades
- Escanear alimento.
- Buscar alimento.
- Registrar comida.
- Comidas del día.
- Historial nutricional.
- Calendario de cumplimiento.
- Macros del día.

### Escaneo y búsqueda de alimentos

El usuario podrá buscar alimentos mediante:

- Código de barras.
- Escaneo con cámara.
- Búsqueda manual.

La app utilizará una API externa, como Open Food Facts, y guardará alimentos consultados en la base de datos como caché.

### Registro de comidas

El usuario podrá registrar alimentos en diferentes comidas:

- Desayuno.
- Comida.
- Cena.
- Snacks.

Cada registro tendrá en cuenta:

- Alimento.
- Cantidad en gramos.
- Calorías.
- Proteínas.
- Carbohidratos.
- Grasas.

### Macros del día

La pantalla mostrará el progreso diario respecto a los objetivos nutricionales:

- Calorías consumidas / objetivo.
- Proteínas consumidas / objetivo.
- Carbohidratos consumidos / objetivo.
- Grasas consumidas / objetivo.

### Calendario nutricional

Se plantea añadir un calendario para analizar el cumplimiento diario.

Ejemplo de estados:

- Verde: objetivos cumplidos.
- Amarillo: cerca del objetivo.
- Rojo: lejos del objetivo.
- Gris: sin registros.

Este calendario permitirá revisar el historial y detectar patrones de alimentación.

---

## 8. Workout

### Objetivo

Workout gestionará rutinas, ejercicios y registro de entrenamientos.

### Funcionalidades

- Rutina actual.
- Ejercicios.
- Sugerencias de ejercicios.
- Registro de entrenamientos.
- Progreso por músculo.
- Planeador de rutinas.

### Rutinas

La app permitirá gestionar rutinas planificadas:

- Ver rutina actual.
- Crear rutina.
- Editar rutina.
- Añadir ejercicios.
- Ordenar ejercicios.
- Generar rutina con IA.

### Registro de entrenamientos

Se diferenciará entre la rutina planificada y lo que el usuario realmente hizo.

El registro podrá incluir:

- Ejercicio realizado.
- Series.
- Repeticiones.
- Peso utilizado.
- Fecha del entrenamiento.
- Notas.

### Progreso muscular

La app podrá analizar volumen y frecuencia de entrenamiento por grupo muscular:

- Pecho.
- Espalda.
- Piernas.
- Hombros.
- Brazos.
- Core.

---

## 9. Social
   
### Objetivo

Social permitirá conectar con otros usuarios y aumentar la motivación.

### Funcionalidades

- Amigos.
- Feed.
- Retos.
- Comparativas.

### Amigos

El usuario podrá añadir y gestionar amigos dentro de la app.

### Feed

El feed podrá mostrar actividad compartida:

- Entrenamientos completados.
- Retos conseguidos.
- Progreso compartido.
- Publicaciones fitness.

### Retos

Los usuarios podrán participar en retos como:

- Entrenar X días seguidos.
- Completar X entrenamientos.
- Alcanzar cierto volumen de entrenamiento.
- Cumplir macros durante X días.

### Comparativas

Se podrán comparar métricas entre usuarios, respetando siempre la privacidad.

### Privacidad

Será importante permitir al usuario decidir qué datos comparte:

- Peso: privado.
- Progreso: privado o amigos.
- Entrenamientos: privado o amigos.
- Retos: amigos o público.

--- 

## 10. AI

### Objetivo

La IA será un entrenador fitness personalizado, conectado a los datos reales de la app.

No será solo un chat genérico, sino una capa inteligente sobre las distintas funcionalidades.

### Funcionalidades

- Chat de IA.
- Sugerencias de alimentos.
- Sugerencias de ejercicios.
- Recomendaciones de entrenamiento.
- Recomendaciones nutricionales.
- Análisis del progreso.
- Alertas inteligentes.

### Datos que podrá usar la IA

- Perfil físico.
- Objetivo del usuario.
- Nutrición del día.
- Historial nutricional.
- Rutinas.
- Entrenamientos registrados.
- Progreso físico.
- Actividad social.

### Ejemplos de recomendaciones

- Hoy vas bajo de proteína. Te sugiero una cena rica en proteína.
- Llevas varios días sin entrenar pierna. ¿Quieres una rutina corta?
- Has subido peso y bajado grasa corporal. Vas en buena dirección para ganar masa muscular.
- Tu consumo de calorías ha sido irregular esta semana. Podemos ajustar el plan.

---

## 11. Pantallas fuera de la navbar

Además de las secciones principales, existirán pantallas secundarias.

Estas pantallas se abrirán desde Home, Profile, Nutrition, Workout, Social o AI, pero no formarán parte de la navegación principal.

### Pantallas secundarias previstas

- ProfileScreen.
- SettingsScreen.
- EditAccountScreen.
- PhysicalProfileScreen.
- ProgressScreen.
- FoodScannerScreen.
- FoodSearchScreen.
- FoodDetailScreen.
- RoutineDetailScreen.
- WorkoutLogScreen.
- RoutinePlannerScreen.
- FriendProfileScreen.
- ChallengeDetailScreen.

---

## 12. Prioridad de desarrollo

### Fase 1: Autenticación y cuenta

- Login.
- Registro.
- JWT.
- Persistencia de sesión.
- Perfil de cuenta básico.

### Fase 2: Perfil físico y progreso
- Perfil físico separado.
- Edición de datos físicos.
- Histórico de progreso físico.

### Fase 3: Nutrición básica

- Escaneo de alimentos.
- Búsqueda de alimentos.
- Registro de comidas.
- Macros diarios.

### Fase 4: Home como dashboard real

- Resumen nutricional.
- Resumen de entrenamiento.
- Mini progreso físico.
- Actividad social básica.
- Accesos rápidos.

### Fase 5: Entrenamiento

- Rutinas.
- Ejercicios.
- Registro de entrenamientos.
- Progreso muscular.

### Fase 6: Historial y análisis

- Calendario nutricional.
- Gráficas de progreso.
- Análisis semanal.
- Análisis mensual.

### Fase 7: Social

- Amigos.
- Feed.
- Retos.
- Comparativas.

### Fase 8: IA conectada a datos reales

- Recomendaciones personalizadas.
- Entrenador fitness inteligente.
- Sugerencias de nutrición.
- Sugerencias de entrenamiento.
- Análisis de progreso.

---

## 13. Decisiones clave

### Separación de responsabilidades

- Profile: cuenta.
- Physical Profile: datos físicos actuales.
- Progress: evolución e histórico.
- Home: resumen general.
- Nutrition: alimentación.
- Workout: entrenamiento.
- AI: entrenador inteligente.
- Social: comunidad.

### Decisión sobre Profile

Profile quedará como una pantalla limpia de cuenta y configuración.

Los datos físicos y el progreso se moverán a secciones independientes para mantener la app limpia, escalable y fácil de usar.

### Decisión sobre Home

Home será el centro de resumen de la app.

Mostrará información de todas las secciones, pero no reemplazará las pantallas detalladas.

### Decisión sobre AI

La IA será una capa transversal.

Podrá leer información de Nutrition, Workout, Progress y Profile para dar recomendaciones personalizadas.