package es.jadafit.jadafit_api.config;

import es.jadafit.jadafit_api.model.CatalogExercise;
import es.jadafit.jadafit_api.repository.CatalogExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CatalogExerciseRepository catalogExerciseRepository;

    @Override
    public void run(String... args) throws Exception {
        // Limpiamos los datos anteriores para asegurar que se apliquen los nuevos enlaces de video reales
        catalogExerciseRepository.deleteAll();

        System.out.println("Sembrando datos iniciales de CatalogExercise...");
        
        catalogExerciseRepository.saveAll(List.of(
            CatalogExercise.builder()
                .name("Sentadillas (Squats)")
                .description("Ejercicio fundamental para desarrollar la fuerza y el tamaño de los músculos de las piernas y los glúteos. Consiste en flexionar las rodillas y bajar la cadera como si fueras a sentarte en una silla imaginaria.")
                .benefits("Fortalece cuádriceps, isquiotibiales, glúteos, gemelos y el core. Mejora la movilidad y el equilibrio.")
                .videoUrl("https://raw.githubusercontent.com/MichistaLin/mediapipe-Fitness-counter/master/code/video-sample/squat-sample.mp4")
                .build(),
            CatalogExercise.builder()
                .name("Flexiones (Push-ups)")
                .description("Ejercicio de peso corporal clásico que se enfoca en el pecho, los hombros y los tríceps. Se realiza acostado boca abajo y levantando el cuerpo empujando con las manos contra el suelo.")
                .benefits("Desarrolla fuerza en el tren superior y estabilidad en el core. Se puede realizar en cualquier lugar sin equipo.")
                .videoUrl("https://raw.githubusercontent.com/airscholar/AI-Workout-Manager/main/assets/pushup.mp4")
                .build(),
            CatalogExercise.builder()
                .name("Press de Banca")
                .description("Ejercicio de levantamiento de pesas en banco plano, enfocado principalmente en el desarrollo del músculo pectoral mayor.")
                .benefits("Es uno de los mejores ejercicios para ganar fuerza y masa muscular en el pecho, hombros frontales y tríceps.")
                .videoUrl("https://raw.githubusercontent.com/jeonggyuhyung/YOLOv5-Pose_Estimation/master/YOLO+pose_estimation/sample_files/bench_press.mp4")
                .build(),
            CatalogExercise.builder()
                .name("Dominadas (Pull-ups)")
                .description("Ejercicio de tracción en el que te cuelgas de una barra fija y levantas tu cuerpo hasta que la barbilla pase por encima de la barra.")
                .benefits("Excelente para desarrollar la espalda (dorsales) y bíceps. Mejora la fuerza de agarre.")
                .videoUrl("https://raw.githubusercontent.com/PanduDcau/AI-Workout-Manager/main/assets/pullup.mp4")
                .build(),
            CatalogExercise.builder()
                .name("Peso Muerto (Deadlift)")
                .description("Consiste en levantar una barra cargada desde el suelo hasta el nivel de las caderas, para luego volver a bajarla al suelo.")
                .benefits("Trabaja casi todos los músculos del cuerpo, especialmente la cadena posterior (espalda baja, glúteos, isquiotibiales).")
                .videoUrl("https://raw.githubusercontent.com/PanduDcau/AI-Workout-Manager/main/assets/deadlift.mp4")
                .build()
        ));
        
        System.out.println("Datos iniciales de ejercicios sembrados exitosamente.");
    }
}
