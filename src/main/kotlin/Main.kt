import api.Api
import org.http4k.core.then
import org.http4k.filter.CorsPolicy
import org.http4k.filter.ServerFilters
import org.http4k.routing.RoutingHttpHandler
import org.http4k.server.Netty
import org.http4k.server.asServer
import utils.database

fun main() {
    server(Api().getHandler()).start()
}

fun server(handler: RoutingHttpHandler,port: Int = 8080) =
    ServerFilters.Cors(CorsPolicy.UnsafeGlobalPermissive).then(handler).asServer(Netty(port))

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