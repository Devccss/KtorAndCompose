package config
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
    val url = System.getenv("DB_URL")
    val user = System.getenv("DB_USER")
    val password = System.getenv("DB_PASSWORD")

    Database.connect(
        url = url,
        driver = "org.postgresql.Driver",
        user = user,
        password = password
    )

    transaction {
        SchemaUtils.create(
            Users, Levels, Dialogs, Phrase, PhraseOrder, Word,
            PhraseWords, UserPhraseStandby, Tests,
            TestQuestions, UserProgress
        )
    }
}
