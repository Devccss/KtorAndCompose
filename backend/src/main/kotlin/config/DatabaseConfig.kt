package config

import Dialogs
import Levels
import PhraseWords
import Phrases
import TestQuestions
import Tests
import UserPhraseStandby
import UserProgress
import Users
import Words
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
            Users, Levels, Dialogs, Phrases, Words,
            PhraseWords, UserPhraseStandby, Tests,
            TestQuestions, UserProgress
        )
    }
}