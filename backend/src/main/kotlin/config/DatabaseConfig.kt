package com.example.config

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
import io.ktor.server.application.Application
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

fun Application.configureDatabases() {
    Database.connect(
        "jdbc:postgresql://localhost:5432/ktorDatabase",
        user = "Deivid_user",
        password = "-Ducli123"
    )
    transaction {
        SchemaUtils.create(
            Users, Levels, Dialogs, Phrases, Words,
            PhraseWords, UserPhraseStandby, Tests,
            TestQuestions, UserProgress
        )
    }
}