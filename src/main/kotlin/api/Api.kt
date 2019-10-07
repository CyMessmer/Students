package api

import StudentEmail
import getDetails
import getGpa
import org.http4k.contract.contract
import org.http4k.contract.meta
import org.http4k.contract.openapi.ApiInfo
import org.http4k.contract.openapi.v3.OpenApi3
import org.http4k.contract.security.NoSecurity
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.format.Jackson
import org.http4k.routing.bind
import org.http4k.routing.routes
import utils.*

class Api {
    private fun apiContract() = contract {
        renderer = OpenApi3(ApiInfo("Students API", "0.0.1", "Api to get information about students"), Jackson)
        descriptionPath = SWAGGER_ENDPOINT
        routes += HEALTH_CHECK meta {
            summary = "Health check"
            description = "Ensures API is alive"
            returning(Status.OK to "result")
        } bindContract Method.GET to { Response(Status.OK).body("It's alive") }

        routes += STUDENT_GPA meta {
            summary = "Find students average GPA"
            description =
                "Returns all students who name matches the first and last param. All students returned if params omitted"
            returning(Status.OK to "result")
        } bindContract Method.GET to { request ->
            val gpa = getGpa(request.query("first").orEmpty(), request.query("last").orEmpty())
            val response = toXmlOrJson(request.header("Accept").orEmpty(), gpa)
            if (gpa.students.isNullOrEmpty()){
                Response(Status.NOT_FOUND)
            } else {
                Response(Status.OK).body(response)
            }
        }

        routes += STUDENT_DETAILS meta {
            summary = "Get student details"
            description = "Get all details of a student"
            returning(Status.OK to "result")
        } bindContract Method.POST to { request ->
            val email =
                fromXmlOrJson<StudentEmail>(request.header("Content-Type").orEmpty(), request.bodyString())?.email
            val details = getDetails(email.orEmpty())
            val response = toXmlOrJson(request.header("Accept").orEmpty(), details)
            if (details == null) {
                Response(Status.BAD_REQUEST)
            } else {
                Response(Status.OK).body(response)
            }
        }
    }

    fun getHandler() = routes(API_ROOT bind NoSecurity.filter.then(apiContract()))
}