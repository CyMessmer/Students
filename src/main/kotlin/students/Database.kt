package students
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File

class Database(objectMapper: ObjectMapper) {
    // using the term table very loosely
    val studentsTable: Students = objectMapper.readValue(File("build/resources/main/students.json").readText(), Students::class.java)
    val classesTable: Classes = objectMapper.readValue(File("build/resources/main/students.json").readText(), Classes::class.java)
}
