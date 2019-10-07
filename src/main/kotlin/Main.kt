import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.http4k.contract.contract
import org.http4k.contract.meta
import org.http4k.contract.openapi.ApiInfo
import org.http4k.contract.openapi.v3.OpenApi3
import org.http4k.contract.security.NoSecurity.filter
import org.http4k.core.*
import org.http4k.filter.CorsPolicy
import org.http4k.filter.ServerFilters
import org.http4k.format.Jackson
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Netty
import org.http4k.server.asServer

val jacksonXmlObjectMapper = XmlMapper().registerKotlinModule()

fun main() {
    val contract = contract {
        renderer = OpenApi3(ApiInfo("Students API", "0.0.1", "Api to get information about students"), Jackson)
        descriptionPath = "/docs/swagger.json"
        routes += "/healthcheck" meta {
            summary = "healthcheck"
            description = "Ensure API is alive"
            returning(Status.OK to "result")
        } bindContract Method.GET to { Response(Status.OK).body("It's alive") }
        routes += "/student/gpa" meta {
            summary = "Find students average GPA"
            description =
                "Returns all students who name matches the first and last param. All students returned if params omitted"
            returning(Status.OK to "result")
        } bindContract Method.GET to { request ->
            val gpa = getGpa(request.query("first").orEmpty(), request.query("last").orEmpty())
            val response = toXmlOrJson(request.header("Accept").orEmpty(), gpa)
            Response(Status.OK).body(response)
        }
        routes += "/student/details" meta {
            summary = "Get student details"
            description = "Get all details of a student"
            returning(Status.OK to "result")
        } bindContract Method.POST to { request ->
            val email =
                fromXmlOrJson<StudentEmail>(request.header("Content-Type").orEmpty(), request.bodyString()).email
            val details = getDetails(email.orEmpty())
            val response = toXmlOrJson(request.header("Accept").orEmpty(), details)
            Response(Status.OK).body(response)
        }
    }

    val handler = routes("/api" bind filter.then(contract))

    ServerFilters.Cors(CorsPolicy.UnsafeGlobalPermissive).then(handler).asServer(Netty(8080)).start()
}

inline fun <reified T> fromXmlOrJson(acceptHeader: String, bodyString: String): T {
    return if (acceptHeader == ContentType.APPLICATION_XML.value) {
        jacksonXmlObjectMapper.readValue(bodyString)
    } else {
        jacksonObjectMapper().readValue(bodyString)
    }
}

fun toXmlOrJson(acceptHeader: String, data: Any?): String {
    return if (acceptHeader == ContentType.APPLICATION_XML.value) {
        jacksonXmlObjectMapper.writeValueAsString(data)
    } else {
        jacksonObjectMapper().writeValueAsString(data)
    }
}

val database = Database(jacksonObjectMapper())

fun getGpa(first: String, last: String): StudentsGpaResponse {
    val students = database.studentsTable.students.filter {
        it.first.contains(first) && it.last.contains(last)
    }
    val studentsGPA = mutableListOf<StudentGpaResponse>()
    students.forEach { student ->
        val grades = student.studentClasses.map { it.grade }
        studentsGPA.add(StudentGpaResponse(student.first, student.last, grades.average()))
    }
    return StudentsGpaResponse(studentsGPA)
}

fun getDetails(email: String): StudentDetailsResponse? {
    val student = database.studentsTable.students.firstOrNull { it.email == email } ?: return null
    val classes = database.classesTable.classes

    val classGradeList = mutableListOf<ClassGradeResponse>()
    student.studentClasses.map { classGradeList.add(ClassGradeResponse(classes[it.id].toString(), it.grade)) }
    return StudentDetailsResponse(
        student.first,
        student.last,
        student.email,
        student.studentClasses.map { it.grade }.average(),
        classGradeList
    )
}