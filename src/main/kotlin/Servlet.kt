import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.underscore.lodash.U
import java.io.IOException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class Servlet : HttpServlet() {
    private val database = Database()

    @Throws(IOException::class)
    override fun doGet(
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        when (request.pathInfo) {
            "/student/gpa" -> {
                val first = request.getParameter("first").orEmpty()
                val last = request.getParameter("last").orEmpty()
                val jsonString = jacksonObjectMapper().writeValueAsString(getGpa(first, last))
                if (request.contentType == "application/xml") {
                    response.contentType = request.contentType
                    val xml = U.jsonToXml(jsonString)
                    response.writer.println(xml)
                } else {
                    response.contentType = "application/json"
                    response.writer.println(jsonString)
                }
            }
            "/student/details" -> {
                val email = request.getParameter("email")
                if (email == null) {
                    response.status = HttpServletResponse.SC_BAD_REQUEST
                } else {
                    val jsonString = jacksonObjectMapper().writeValueAsString(getDetails(email))
                    if (request.contentType == "application/xml") {
                        response.contentType = request.contentType
                        val xml = U.jsonToXml(jsonString)
                        response.writer.println(xml)
                    } else {
                        response.contentType = "application/json"
                        response.writer.println(jsonString)
                    }
                }
            }
            else -> response.status = HttpServletResponse.SC_NOT_FOUND
        }
        request.characterEncoding = "utf-8"
    }

    private fun getGpa(first: String, last: String): StudentsGpaResponse {

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

    private fun getDetails(email: String): StudentDetailsResponse {
        val student = database.studentsTable.students.first { it.email == email }
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
}
