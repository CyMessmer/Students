import api.Api
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isWithin
import org.http4k.client.OkHttp
import org.http4k.core.ContentType
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.hamkrest.hasStatus
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import utils.database
import utils.fromXmlOrJson

/**
 * If this app did more than just read a json file, I would add more clean up, and test by creating new data;
 * given that it does not I'm making do with more static tests against known data
 */
class EndToEndTest {
    private val client = OkHttp()
    private val server = server(Api().getHandler(), 8000)

    @BeforeEach
    fun setup() {
        server.start()
    }

    @AfterEach
    fun teardown() {
        server.stop()
    }

    // Are endpoints kicking?
    @Test
    fun `respond OK to healthcheck`() {
        assertThat(client(Request(Method.GET, "http://localhost:8000/api/healthcheck")), hasStatus(Status.OK))
    }

    @Test
    fun `respond OK to GET student gpa`() {
        assertThat(client(Request(Method.GET, "http://localhost:8000/api/student/gpa")), hasStatus(Status.OK))
    }

    @Test
    // no body data will result in error and is expected
    fun `respond BAD_REQUEST to POST student details`() {
        assertThat(
            client(Request(Method.POST, "http://localhost:8000/api/student/details")),
            hasStatus(Status.BAD_REQUEST)
        )
    }

    // /api/student/gpa
    @Test
    fun `respond OK with all student GPA's`() {
        val response = client(
            Request(Method.GET, "http://localhost:8000/api/student/gpa")
                .header("Content-Type", ContentType.APPLICATION_XML.value)
        )
        val studentsGPA = fromXmlOrJson<StudentsGpaResponse>("", response.bodyString())
        assertThat(response, hasStatus(Status.OK))
        assertThat(database.studentsTable.students.size, equalTo(studentsGPA?.students?.size))
    }

    @Test
    fun `respond OK with Jane Smith's GPA's`() {
        val response = client(Request(Method.GET, "http://localhost:8000/api/student/gpa?first=Jane&?last=Smith"))
        val studentsGPA = fromXmlOrJson<StudentsGpaResponse>("", response.bodyString())
        assertThat(response, hasStatus(Status.OK))
        assertThat(studentsGPA!!.students.size, equalTo(1))
        assertThat(studentsGPA.students.first().first, equalTo("Jane"))
        assertThat(studentsGPA.students.last().last, equalTo("Smith"))
        assertThat(studentsGPA.students.first().gpa, isWithin(0.0..4.0))
    }

    @Test
    fun `respond NOT_FOUND Expialidocious Supercalifragilistic's GPA's`() {
        val response = client(
            Request(
                Method.GET,
                "http://localhost:8000/api/student/gpa?first=Expialidocious&?last=Supercalifragilistic"
            )
        )
        val studentsGPA = fromXmlOrJson<StudentsGpaResponse>("", response.bodyString())
        Assertions.assertTrue(studentsGPA?.students.isNullOrEmpty())
        assertThat(response, hasStatus(Status.NOT_FOUND))
    }


    // /api/student/details tests
    @Test
    fun `respond OK sware@mailinator's details`() {
        val request = Request(Method.POST, "http://localhost:8000/api/student/details")
            .header("Content-Type", ContentType.APPLICATION_JSON.value)
            .body("""{"email": "sware@mailinator.com"}""")

        val response = client(request)
        val studentsGPA = fromXmlOrJson<StudentDetailsResponse>("", response.bodyString())
        assertThat(response, hasStatus(Status.OK))
        Assertions.assertTrue(studentsGPA!!.classes.isNotEmpty())
        assertThat(studentsGPA.email, equalTo("sware@mailinator.com"))
        assertThat(studentsGPA.first, equalTo("Samantha"))
        assertThat(studentsGPA.last, equalTo("Ware"))
    }

    @Test
    fun `respond OK when xml email give for details`() {
        val request = Request(Method.POST, "http://localhost:8000/api/student/details")
            .header("Content-Type", ContentType.APPLICATION_XML.value)
            .body("<StudentEmail><email>sware@mailinator.com</email></StudentEmail>")
        val response = client(request)
        val studentsGPA = fromXmlOrJson<StudentDetailsResponse>("", response.bodyString())
        assertThat(response, hasStatus(Status.OK))
        Assertions.assertTrue(studentsGPA!!.classes.isNotEmpty())
        assertThat(studentsGPA.email, equalTo("sware@mailinator.com"))
        assertThat(studentsGPA.first, equalTo("Samantha"))
        assertThat(studentsGPA.last, equalTo("Ware"))
    }

    @Test
    fun `respond BAD_REQUEST when no email give for details`() {
        val response = client(Request(Method.POST, "http://localhost:8000/api/student/details"))
        val studentsGPA = fromXmlOrJson<StudentDetailsResponse>("", response.bodyString())
        assertThat(response, hasStatus(Status.BAD_REQUEST))
        Assertions.assertTrue(studentsGPA == null)
    }

    @Test
    fun `respond BAD_REQUEST when email invalid for details`() {
        val request = Request(Method.POST, "http://localhost:8000/api/student/details")
            .header("Content-Type", ContentType.APPLICATION_XML.value)
            .body("<StudentEmail><email>swaremailinator.x</email></StudentEmail>")
        val response = client(Request(Method.POST, "http://localhost:8000/api/student/details"))
        val studentsGPA = fromXmlOrJson<StudentDetailsResponse>("", response.bodyString())
        assertThat(response, hasStatus(Status.BAD_REQUEST))
        Assertions.assertTrue(studentsGPA == null)
    }
}