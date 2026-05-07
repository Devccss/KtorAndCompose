package org.example.project.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.example.project.dtos.CreateUnitCompletedDto
import org.example.project.dtos.CreateUnitDto
import org.example.project.dtos.FilterUnitsDto
import org.example.project.dtos.UnitDto
import org.example.project.dtos.UpdateUnitDto

class UnitRepo(private val httpClient: HttpClient, private val baseUrl: String) {

    suspend fun getAllUnits(): List<UnitDto> =
        httpClient.get("$baseUrl/api/v1/units") {
            addAuthHeader()
        }.parseOrThrow()

    suspend fun searchUnits(filterUnits: FilterUnitsDto): List<UnitDto> {
        return httpClient.get {
            url("$baseUrl/api/v1/units/search")
            filterUnits.name?.let { parameter("name", it) }
            filterUnits.difficulty?.let { parameter("difficulty", it) }
            filterUnits.isActive?.let { parameter("isActive", it) }
            addAuthHeader()
        }.parseOrThrow()
    }

    suspend fun getUnitById(id: Int): UnitDto? =
        httpClient.get("$baseUrl/api/v1/units/$id") {
            addAuthHeader()
        }.let { response ->
            if (response.status == HttpStatusCode.NotFound) null else response.parseOrThrow()
        }

    suspend fun getUnitByTestId(testId: Int): UnitDto? =
        httpClient.get("$baseUrl/api/v1/units/byTest/$testId") {
            addAuthHeader()
        }.let { response ->
            if (response.status == HttpStatusCode.NotFound) null else response.parseOrThrow()
        }

    suspend fun createUnit(unit: CreateUnitDto): UnitDto =
        httpClient.post("$baseUrl/api/v1/units") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(unit)
            addAuthHeader()
        }.parseOrThrow()

    suspend fun updateUnit(id: Int, unit: UpdateUnitDto): UnitDto =
        httpClient.put("$baseUrl/api/v1/units/$id") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(unit)
            addAuthHeader()
        }.parseOrThrow()

    suspend fun reorderUnits(orders: List<Pair<Int, Int>>): Boolean =
        httpClient.put("$baseUrl/api/v1/units/reorder") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(orders)
            addAuthHeader()
        }.ensureSuccessOrThrow()

    suspend fun deleteUnit(id: Int): Boolean =
        httpClient.delete("$baseUrl/api/v1/units/$id") {
            addAuthHeader()
        }.ensureSuccessOrThrow()

    //Complete unit

    suspend fun getAllCompletedUnitsByUserId(userId: Int): List<UnitDto> =
        httpClient.get("$baseUrl/api/v1/unitsCompleted/user/$userId") {
            addAuthHeader()
        }.parseOrThrow()

    suspend fun getCompletedUnitById(id: Int): UnitDto? =
        httpClient.get("$baseUrl/api/v1/unitsCompleted/$id") {
            addAuthHeader()
        }.let { response ->
            if (response.status == HttpStatusCode.NotFound) null else response.parseOrThrow()
        }

    suspend fun createUnitCompleted(dto: CreateUnitCompletedDto): UnitDto =
        httpClient.post("$baseUrl/api/v1/unitsCompleted") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(dto)
            addAuthHeader()
        }.parseOrThrow()

    suspend fun deleteUnitsCompleted(completedId: Int): Boolean =
        httpClient.delete("$baseUrl/api/v1/unitsCompleted/$completedId") {
            addAuthHeader()
        }.ensureSuccessOrThrow()






}