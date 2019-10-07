package utils

import Database
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.http4k.core.ContentType

// quick access for parsing xml
val jacksonXmlObjectMapper = XmlMapper().registerKotlinModule()


inline fun <reified T> fromXmlOrJson(acceptHeader: ContentType, bodyString: String): T? {
    return try {
        return if (acceptHeader == ContentType.APPLICATION_XML) {
            jacksonXmlObjectMapper.readValue(bodyString)
        } else {
            jacksonObjectMapper().readValue(bodyString)
        }
    } catch (e: Exception){
        // XML will throw an error on empty or invalid XML. Ignore and throw BAD_REQUEST
        null
    }
}

fun toXmlOrJson(acceptHeader: ContentType, data: Any?): String {
    return if (acceptHeader == ContentType.APPLICATION_XML) {
        jacksonXmlObjectMapper.writeValueAsString(data)
    } else {
        jacksonObjectMapper().writeValueAsString(data)
    }
}

// still unsure where you will end up living
val database = Database(jacksonObjectMapper())

// constants
const val API_ROOT = "/api"
const val SWAGGER_ENDPOINT = "/docs/swagger.json"
const val HEALTH_CHECK = "/healthcheck"
const val STUDENT_GPA = "/student/gpa"
const val STUDENT_DETAILS = "/student/details"