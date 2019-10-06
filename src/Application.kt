package co.cy.students

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import org.slf4j.event.Level

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(ContentNegotiation) {
        gson {}
    }

    val database = Database()

    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        get("/student/gpa") {
            val first = call.request.queryParameters["first"].orEmpty()
            val last = call.request.queryParameters["last"].orEmpty()

            val students = database.studentsTable.students.filter {
                it.first.contains(first) && it.last.contains(last)
            }
            val studentsGPA = mutableListOf<Triple<String, String, Double>>()
            students.forEach {student ->
                val grades = student.studentClasses.map { it.grade }
                studentsGPA.add(Triple(student.first, student.last, grades.average()))
            }

            //todo graphql to get average and show data
            call.respond(studentsGPA)
        }

        get("/student/detail/{email}") {
            val email = call.request.queryParameters["email"]

            val student = database.studentsTable.students.first { it.email == email }
            val classes = database.classesTable.classes
            // todo graph ql to get this data
            student.first
            student.last
            student.email
            student.studentClasses.map { it.grade }.average()
            val studentClasses = student.studentClasses.map { Pair(classes[it.id], it.grade) }

        }
    }
}

