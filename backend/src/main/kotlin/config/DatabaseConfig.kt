package config

import com.example.dtos.UnitDto
import com.example.dtos.UserDto
import io.github.cdimascio.dotenv.dotenv
import models.Alternatives
import models.ContentType
import models.ExerciseCompleted
import models.ExerciseContent
import models.Units
import models.UnitExerciseAssignments
import models.Users
import models.Exercises
import models.Notifications
import models.ExerciseWords
import models.Questions
import models.Role
import models.TestCompleted
import models.TestExercises
import models.Tests
import models.UnitsCompleted
import models.UserSessionLogs
import models.WelcomeTests
import models.Words
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.time.LocalDateTime

fun configureDatabase() {

    val dotenv = dotenv()
    val dbUrl = dotenv["DB_URL"]
    val dbUser = dotenv["DB_USER"]
    val dbPassword = dotenv["DB_PASSWORD"]

    Database.connect(
        url = dbUrl, driver = "org.postgresql.Driver", user = dbUser, password = dbPassword
    )

    transaction {

        //Eliminar todas las tablas antes de crearlo
        /*SchemaUtils.drop(
            Users,
            Units,
            Exercises,
            ExerciseContent,
            ExerciseCompleted,
            UnitExerciseAssignments,
            Notifications,
            Words,
            Questions,
            Tests,
            TestExercises,
            UnitsCompleted,
            TestCompleted,
            UserSessionLogs,
            ExerciseWords,
            Alternatives,
            WelcomeTests
        )*/

        SchemaUtils.create(
            Users,
            Units,
            Exercises,
            ExerciseContent,
            ExerciseCompleted,
            UnitExerciseAssignments,
            Notifications,
            Words,
            Questions,
            Tests,
            TestExercises,
            UnitsCompleted,
            TestCompleted,
            UserSessionLogs,
            ExerciseWords,
            Alternatives,
            WelcomeTests
        )
        arrayOf<Table>(UnitExerciseAssignments)
        createAdminUserIfNotExists()
    }
    createExercises()
}



fun createExercises() {
    transaction {
        if (Exercises.selectAll().count() > 0L) {
            return@transaction
        }

        // Limpiar datos previos
        Alternatives.deleteAll()
        Questions.deleteAll()
        ExerciseWords.deleteAll()
        Words.deleteAll()
        ExerciseContent.deleteAll()
        Exercises.deleteAll()
        TestExercises.deleteAll()
        Tests.deleteAll()
        ExerciseCompleted.deleteAll()
        UnitsCompleted.deleteAll()
        TestCompleted.deleteAll()
        UserSessionLogs.deleteAll()

        // Insertar datos de seeds
        insertSeedData()
    }
}