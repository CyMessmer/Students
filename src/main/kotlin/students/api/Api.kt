package students.api

import org.http4k.contract.ContractRoute
import org.http4k.contract.contract
import org.http4k.contract.meta
import org.http4k.contract.openapi.ApiInfo
import org.http4k.contract.openapi.v3.OpenApi3
import org.http4k.core.*
import org.http4k.format.Jackson
import org.http4k.format.Jackson.auto
import org.http4k.lens.Lens
import org.http4k.lens.Query
import org.http4k.routing.bind
import org.http4k.routing.routes
import students.*
import students.utils.*
import org.http4k.format.JacksonXml.auto as autoXml

class Api {
    private fun apiContract() = contract {
        // set up Swagger endpoint for generation
        renderer = OpenApi3(ApiInfo("Students API", "0.0.1", "Api to get information about students"), Jackson)
        descriptionPath = SWAGGER_ENDPOINT
        routes += HEALTH_CHECK meta {
            summary = "Health check"
            description = "Ensures API is alive"
            returning(Status.OK to "Result")
        } bindContract Method.GET to { Response(Status.OK).body("It's alive") }
        routes += generateStudentsGpaContract()
        routes += generateStudentDetailsContract()
    }

    /**
     * Generate the ContractRoute and create the swagger OpenApi3 documentation
     * use lens to generate query type and schemas for open api
     */
    private fun generateStudentsGpaContract(): ContractRoute {
        // use lens to generate query type and schemas for open api
        val first = Query.map(::FirstName).optional("first", "Students first name")
        val last = Query.map(::LastName).optional("last", "Students last name")
        val bodyJson = Body.auto<StudentsGpaResponse>().toLens()
        val bodyXml = Body.autoXml<StudentsGpaResponse>().toLens()

        // Create the meta data used to create OpenAPI Spec
        return STUDENT_GPA meta {
            summary = "Find students average GPA"
            description =
                "Returns all students who name matches the first and last param. All students returned if params omitted"
            consumes.plusAssign(listOf(ContentType.APPLICATION_JSON, ContentType.APPLICATION_XML))
            produces.plusAssign(listOf(ContentType.APPLICATION_JSON, ContentType.APPLICATION_XML))
            queries.plusAssign(listOf(first, last))
            returning(Status.OK, bodyJson to StudentsGpaResponse(listOf(StudentGpaResponse("Cy", "Messmer", 3.6))))
            returning(Status.OK, bodyXml to StudentsGpaResponse(listOf(StudentGpaResponse("Cy", "Messmer", 3.6))))
        } bindContract Method.GET to createStudentsGpaHandler(first, last)
    }


    /**
     * Creates the HttpHandler for GET /api/students/gpa endpoint
     */
    private fun createStudentsGpaHandler(
        first: Lens<Request, FirstName?>,
        last: Lens<Request, LastName?>
    ): HttpHandler = { request ->
        val requestAcceptType =
            if (request.header("Accept") == ContentType.APPLICATION_XML.value) ContentType.APPLICATION_XML
            else ContentType.APPLICATION_JSON

        val gpa = getGpa(first(request), last(request))
        val response = toXmlOrJson(requestAcceptType, gpa)

        if (gpa.students.isNullOrEmpty()) {
            Response(Status.NOT_FOUND)
        } else {
            Response(Status.OK)
                .header("Content-Type", requestAcceptType.value)
                .body(response)
        }
    }

    /**
     * Handle business  of dealing with "database" to get data requested
     */
    private fun getGpa(first: FirstName?, last: LastName?): StudentsGpaResponse {
        val students = database.studentsTable.students.filter {
            it.first.contains(first?.first.orEmpty()) && it.last.contains(last?.last.orEmpty())
        }
        val studentsGPA = mutableListOf<StudentGpaResponse>()
        students.forEach { student ->
            val grades = student.studentClasses.map { it.grade }
            studentsGPA.add(StudentGpaResponse(student.first, student.last, grades.average()))
        }
        return StudentsGpaResponse(studentsGPA)
    }

    /**
     * Generate the ContractRoute and create the swagger OpenApi3 documentation
     * use lens to generate query type and schemas for open api
     */
    private fun generateStudentDetailsContract(): ContractRoute {
        val responseBodyJson = Body.auto<StudentDetailsResponse>().toLens()
        val responseBodyXml = Body.autoXml<StudentDetailsResponse>().toLens()

        return STUDENT_DETAILS meta {
            summary = "Get student details"
            description = "Get students class list and class GPA as well average GPA and contact info"
            consumes.plusAssign(listOf(ContentType.APPLICATION_JSON, ContentType.APPLICATION_XML))
            produces.plusAssign(listOf(ContentType.APPLICATION_JSON, ContentType.APPLICATION_XML))
            returning(
                Status.OK, responseBodyJson to StudentDetailsResponse(
                    "Cy",
                    "Messmer",
                    "cy.messmer@gmail.com",
                    3.6,
                    listOf(ClassGradeResponse("Comp Sci 101", 3.8))
                )
            )
            returning(
                Status.OK, responseBodyXml to StudentDetailsResponse(
                    "Cy",
                    "Messmer",
                    "cy.messmer@gmail.com",
                    3.6,
                    listOf(ClassGradeResponse("Comp Sci 101", 3.8))
                )
            )
        } bindContract Method.POST to createStudentDetailsHandler()
    }

    /**
     * Creates the HttpHandler for GET /api/students/details endpoint
     */
    private fun createStudentDetailsHandler(): HttpHandler = { request ->
        val requestContentType =
            if (request.header("Content-Type") == ContentType.APPLICATION_XML.value) ContentType.APPLICATION_XML
            else ContentType.APPLICATION_JSON
        val requestAcceptType =
            if (request.header("Accept") == ContentType.APPLICATION_XML.value) ContentType.APPLICATION_XML
            else ContentType.APPLICATION_JSON

        val email =
            fromXmlOrJson<StudentEmail>(requestContentType, request.bodyString())?.email
        val details = getDetails(email.orEmpty())
        val response = toXmlOrJson(requestAcceptType, details)
        if (details == null) {
            Response(Status.BAD_REQUEST)
        } else {
            Response(Status.OK)
                .header("Content-Type", requestAcceptType.value)
                .body(response)
        }
    }

    /**
     * Handle business  of dealing with "database" to get data requested
     */
    private fun getDetails(email: String): StudentDetailsResponse? {
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

    fun getHandler() = routes(API_ROOT bind apiContract())
}