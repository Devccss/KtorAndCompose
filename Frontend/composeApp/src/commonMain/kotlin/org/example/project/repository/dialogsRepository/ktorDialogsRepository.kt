package org.example.project.repository.dialogsRepository

import io.ktor.client.HttpClient

class ktorDialogsRepository(private val httpClient: HttpClient, private val baseUrl: String):
    DialogsRepository {


}