import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File

class Database {
    private val objectMapper = ObjectMapper().registerModule(KotlinModule())

    // using the term table very loosely
    val studentsTable: Students = objectMapper.readValue(File("src/main/resources/students.json").readText(), Students::class.java)
    val classesTable: Classes = objectMapper.readValue(File("src/main/resources/students.json").readText(), Classes::class.java)
}