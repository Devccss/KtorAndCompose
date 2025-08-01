package config
import io.github.cdimascio.dotenv.dotenv
import models.TestQuestions
import models.Levels
import models.UserPhraseStandby
import models.UserProgress
import models.Users
import models.Word
import models.Tests
import models.Dialogs
import models.PhraseWords
import models.PhraseOrder
import models.Phrase
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

fun configureDatabases() {

    val dotenv = dotenv()
    val dbUrl = dotenv["DB_URL"]
    val dbUser = dotenv["DB_USER"]
    val dbPassword = dotenv["DB_PASSWORD"]

    Database.connect(
        url = dbUrl,
        driver = "org.postgresql.Driver",
        user = dbUser,
        password = dbPassword
    )

    transaction {
        SchemaUtils.create(
            Users, Levels, Dialogs, Phrase, PhraseOrder, Word,
            PhraseWords, UserPhraseStandby, Tests,
            TestQuestions, UserProgress
        )
    }
}
