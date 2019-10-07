package external

import ClassGradeResponse
import StudentDetailsResponse
import StudentGpaResponse
import Students
import StudentsGpaResponse
import org.http4k.core.*
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.filter.ClientFilters
import org.http4k.filter.HandleUpstreamRequestFailed
import org.http4k.format.Jackson.auto

class StudentsDirectory(httpHandler: HttpHandler) {
    private val http = ClientFilters.HandleUpstreamRequestFailed({
        status.successful || status == Status.NOT_FOUND
    }).then(httpHandler)

    val students = Body.auto<Students>().toLens()

    fun list(): Students = students(http(Request(Method.GET, "/students")))
}