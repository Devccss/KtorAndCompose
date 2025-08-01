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
    Database.connect(
        "jdbc:postgresql://pgsqltrans.face.ubiobio.cl:5432/dsandoval_bd",
        driver = "org.postgresql.Driver",
        user = "dsandoval",
        password = "deivid2025"
    )
    transaction {
        SchemaUtils.create(
            Users, Levels, Dialogs, Phrase,PhraseOrder, Word,
            PhraseWords, UserPhraseStandby, Tests,
            TestQuestions, UserProgress
        )
    }
}