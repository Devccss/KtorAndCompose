# Seeds de Datos - Guía de Modificación

Este documento describe cómo usar y personalizar los datos de prueba (seeds) del backend.

## Estructura

### Archivo: `SeedData.kt`

Contiene toda la lógica para generar datos de prueba. Está organizado en:

1. **Data Classes**
   - `QuestionSeed`: Define preguntas con alternativas
   - `VocabularySeed`: Palabras con traducción y fonética
   - `ExerciseSeed`: Ejercicios completos (contenido, preguntas, vocabulario)
   - `TestSeed`: Tests que agrupan ejercicios
   - `CompletedExerciseSeed`, `CompletedUnitSeed`, etc.: Registros de completados
   - `SessionLogSeed`: Sesiones de usuario

2. **SeedDataProvider**
   - Objeto singleton con métodos que retornan listas de seeds
   - `getExerciseSeeds()`: Retorna ~10 ejercicios por unidad
   - `getTestSeeds()`: Define tests
   - `getCompletedExerciseSeeds()`: Datos de ejercicios completados
   - `getSessionLogSeeds()`: Simulaciones de sesiones de usuario

3. **Funciones de inserción**
   - `insertSeedData()`: Inserta todos los datos en la BD

## Cómo agregar más ejercicios

### Opción 1: Agregar a la lista existente

En `SeedDataProvider.getExerciseSeeds()`, agrega un nuevo `ExerciseSeed`:

```kotlin
ExerciseSeed(
    id = exerciseCounter++,
    name = "Nombre del Ejercicio",
    unitId = 1, // ID de la unidad
    description = "Descripción breve",
    content = "Contenido/contexto de la lección",
    grammar = "Explicación gramatical detallada",
    questions = listOf(
        QuestionSeed(
            "Pregunta 1?",
            listOf("Opción A", "Opción B", "Respuesta Correcta", "Opción D"),
            "Respuesta Correcta"
        ),
        QuestionSeed(
            "Pregunta 2?",
            listOf("Opción 1", "Opción 2", "Opción 3", "Opción 4"),
            "Opción 2"
        )
    ),
    vocabulary = listOf(
        VocabularySeed("english_word", "palabra_en_español", "/fonética/"),
        VocabularySeed("another_word", "otra_palabra", "/fónica/")
    )
)
```

### Opción 2: Crear una nueva unidad

1. En `DatabaseConfig.kt`, agrega en `createExercises()`:
```kotlin
val unitXId = ensureUnit(
    orderUnit = X,
    difficulty = models.DifficultyLevel.A2, // o B1, B2, etc.
    name = "Unidad X: Tema",
    description = "Descripción de la unidad"
)
```

2. Luego, en `SeedData.kt`, crea ejercicios con `unitId = X`

## Cómo agregar tests

En `SeedDataProvider.getTestSeeds()`:

```kotlin
TestSeed(
    name = "Test Unidad X",
    unitId = X,
    description = "Descripción del test",
    exerciseIds = listOf(1, 2, 3, 4, 5) // IDs de ejercicios incluidos
)
```

## Cómo simular datos completados

Edita los métodos en `SeedDataProvider`:

- `getCompletedExerciseSeeds()`: Qué ejercicios completó qué usuario
- `getCompletedUnitSeeds()`: Qué unidades completó qué usuario
- `getTestCompletedSeeds()`: Tests completados y puntuación
- `getSessionLogSeeds()`: Historico de sesiones (logins/logouts)

Ejemplo:
```kotlin
fun getCompletedExerciseSeeds(): List<CompletedExerciseSeed> {
    return listOf(
        CompletedExerciseSeed(userId = 2, exerciseId = 1),
        CompletedExerciseSeed(userId = 2, exerciseId = 2),
        CompletedExerciseSeed(userId = 3, exerciseId = 1)
    )
}
```

## Cómo agregar vocabulario a ejercicios

Cada `ExerciseSeed` tiene un listado `vocabulary`:

```kotlin
vocabulary = listOf(
    VocabularySeed("word", "palabra", "/wɜːrd/"),
    VocabularySeed("teaching", "enseñanza", "/ˈtiːtʃɪŋ/")
)
```

- **english**: palabra en inglés
- **spanish**: traducción al español
- **phonetic**: pronunciación (usa IPA si es posible)

## Estructura actual de datos

- **Unidades**: 1 (Unidad 1: To Be, A1)
- **Ejercicios**: 10 por unidad
- **Preguntas**: 2 por ejercicio (mínimo)
- **Vocabulario**: 4-6 palabras por ejercicio
- **Tests**: 1 test por unidad (agrupa todos los ejercicios)
- **Usuario completado**: Usuario 2 (student@example.com) ha completado:
  - 3 ejercicios
  - 1 unidad
  - 1 test (puntuación: 85)
  - 5 sesiones simuladas

## Cómo resetear la BD y cargar nuevos seeds

Si modificas los seeds y quieres que se carguen:

1. Descomenta el bloque `SchemaUtils.drop(...)` en `DatabaseConfig.kt`
2. Ejecuta la aplicación
3. Vuelve a comentar el bloque

Esto eliminará todas las tablas y las recreará con los nuevos datos.

## Notas importantes

- El `exerciseCounter` se incrementa automáticamente
- Los `unitId` deben existir en la tabla `Units` antes de usarse
- Las preguntas **siempre** deben tener la respuesta correcta en la lista de alternativas
- Los `userId` deben ser válidos (por defecto existe el usuario 2 = student)
- Las fechas de sesión se calculan automáticamente relativas a `LocalDateTime.now()`

## Ejemplo de flujo completo

Para agregar una nueva unidad completa:

```kotlin
// 1. En DatabaseConfig.kt, dentro de createExercises():
val unit4Id = ensureUnit(
    orderUnit = 4,
    difficulty = models.DifficultyLevel.A2,
    name = "Unidad 4: Present Continuous",
    description = "Aprender el present continuous"
)

// 2. En SeedData.kt, agregar 10 ejercicios con unitId = 4

// 3. En SeedDataProvider.getTestSeeds(), agregar:
TestSeed(
    name = "Test Unidad 4",
    unitId = 4,
    description = "Evaluación de Present Continuous",
    exerciseIds = (11..20).toList() // IDs de los nuevos ejercicios
)

// 4. Descomentar SchemaUtils.drop y ejecutar
```

¡Listo! Tendrás una nueva unidad con 10 ejercicios, preguntas, vocabulario y un test.

