package co.cy.students

import com.google.gson.GsonBuilder
import com.google.gson.LongSerializationPolicy
import java.io.File

class Database {
    private val gson = GsonBuilder()
        .setLongSerializationPolicy(LongSerializationPolicy.STRING)
        .create()

    // using the term table very loosely given that I'm just parsing json
    val studentsTable = gson.fromJson(File("./resources/", "students.json").readText(), Students::class.java)
    val classesTable = gson.fromJson(File("./resources/", "students.json").readText(), Classes::class.java)
}