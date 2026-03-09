package org.example.project.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.contentType
import org.example.project.dtos.CreateUnitDto
import org.example.project.dtos.FilterUnitsDto
import org.example.project.dtos.UnitDto
import org.example.project.dtos.UpdateUnitDto

class UnitRepo(private val httpClient: HttpClient, private val baseUrl: String) {

    suspend fun getAllUnits(): List<UnitDto> =
        httpClient.get("$baseUrl/api/v1/units").body()

    suspend fun searchUnits(filterUnits: FilterUnitsDto): List<UnitDto> {
        return httpClient.get {
            url("$baseUrl/api/v1/units/search")
            filterUnits.name?.let { parameter("name", it) }
            filterUnits.difficulty?.let { parameter("difficulty", it) }
        }.body()
    }


    suspend fun getUnitById(id: Int): UnitDto? =
        httpClient.get("$baseUrl/api/v1/units/$id").body()

    suspend fun createUnit(unit: CreateUnitDto): UnitDto =
        httpClient.post("$baseUrl/api/v1/units") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(unit)
        }.body()

    suspend fun updateUnit(id: Int, unit: UpdateUnitDto): UnitDto =
        httpClient.put("$baseUrl/api/v1/units/$id") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(unit)
        }.body()

    suspend fun reorderUnits(orders: List<Pair<Int, Int>>): Boolean =
        httpClient.put("$baseUrl/api/v1/units/reorder") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(orders)
        }.body()

    suspend fun deleteUnit(id: Int): Boolean =
        httpClient.delete("$baseUrl/api/v1/units/$id").body()
}