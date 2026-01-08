package config

import io.github.cdimascio.dotenv.dotenv
import models.ContentExercises
import models.ContentWords
import models.ExerciseCompleted
import models.Units
import models.Users
import models.Exercises
import models.ExercisesOnHold
import models.Notifications
import models.QuestionCompleted
import models.Questions
import models.TestCompleted
import models.TestExercises
import models.Tests
import models.UnitsCompleted
import models.Words

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

fun configureDatabases() {

    val dotenv = dotenv()
    val dbUrl = dotenv["DB_URL"]
    val dbUser = dotenv["DB_USER"]
    val dbPassword = dotenv["DB_PASSWORD"]

    Database.connect(
        url = dbUrl, driver = "org.postgresql.Driver", user = dbUser, password = dbPassword
    )

    transaction {
        SchemaUtils.create(
            Users, Units, Exercises, Notifications,
            ContentExercises, ContentWords, Words, Questions,
            Tests, TestExercises, ExercisesOnHold, UnitsCompleted,
            ExerciseCompleted, TestCompleted, QuestionCompleted
        )
    }
}
