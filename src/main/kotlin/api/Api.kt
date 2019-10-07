package api

import Database
import StudentsGpaResponse
import org.http4k.contract.ContractRoute
import org.http4k.contract.contract
import org.http4k.contract.meta
import org.http4k.contract.openapi.ApiInfo
import org.http4k.contract.openapi.v3.OpenApi3
import org.http4k.core.*
import org.http4k.format.Jackson
import org.http4k.format.Jackson.auto
import org.http4k.lens.Query
import org.http4k.lens.string
import org.http4k.routing.bind
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.routes

fun api(): RoutingHttpHandler = "/api" bind routes(
    contract {
        renderer = OpenApi3(ApiInfo("Students Server API", "v1.0", "API to demonstrate http4k"), Jackson)
        descriptionPath = "/api-docs"
        routes += gpaRoute()
    }
)

fun gpaRoute(): ContractRoute {
    val firstName = Query.string().optional("first", "Students first name")
    val lastName = Query.string().optional("last", "Students last name")

    val studentsGpa = Body.auto<StudentsGpaResponse>().toLens()

    val listGPAs: HttpHandler = {
        Response(Status.OK)
    }

    return "/student/gpa" meta {
        summary = "Returns average GPA of students with matching names or all students if both params omitted"
        queries += firstName
        queries += lastName
        returning(Status.OK)
    } bindContract Method.GET to listGPAs
}