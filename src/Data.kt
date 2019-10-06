package co.cy.students

data class Students(val students: List<Student>)

data class Student(val first: String, val last: String, val email: String, val studentClasses: List<StudentClass>)

data class StudentClass(val id: Int, val grade: Float)

data class Classes(val classes: Map<Int, String>)