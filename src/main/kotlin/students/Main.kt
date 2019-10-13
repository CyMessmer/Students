package students

import org.http4k.core.then
import org.http4k.filter.CorsPolicy
import org.http4k.filter.ServerFilters
import org.http4k.routing.RoutingHttpHandler
import org.http4k.server.Netty
import org.http4k.server.asServer
import students.api.Api

fun main() {
    server(Api().getHandler()).start()
}

fun server(handler: RoutingHttpHandler, port: Int = 8080) =
    ServerFilters.Cors(CorsPolicy.UnsafeGlobalPermissive).then(handler).asServer(Netty(port))
