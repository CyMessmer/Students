import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

// classes mapped to "database" json store
@JsonIgnoreProperties(ignoreUnknown = true)
data class Students(val students: List<Student>)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Student(val first: String, val last: String, val email: String, val studentClasses: List<StudentClass>)
@JsonIgnoreProperties(ignoreUnknown = true)
data class StudentClass(val id: Int, val grade: Double)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Classes(val classes: Map<Int, String>)

// requests data classes
data class StudentEmail(val email: String?)

// responses data classes
data class StudentsGpaResponse(val students: List<StudentGpaResponse>)
data class StudentGpaResponse(val first: String, val last: String, val gpa: Double)
data class StudentDetailsResponse(
    val first: String,
    val last: String,
    val email: String,
    val gpaAverage: Double,
    val classes: List<ClassGradeResponse>
)
data class ClassGradeResponse(@JsonProperty("name") val className: String, val gpa: Double)
