package config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RouteSelector
import io.ktor.server.routing.RouteSelectorEvaluation
import io.ktor.server.routing.RoutingResolveContext
import models.Role
import org.slf4j.LoggerFactory

private val securityLog = LoggerFactory.getLogger("SecurityJWT")
private val rolesLog = LoggerFactory.getLogger("SecurityRoles")

fun Application.configureSecurity() {
    val secret = environment.config.property("jwt.secret").getString()
    val issuer = environment.config.property("jwt.issuer").getString()
    val audience = environment.config.property("jwt.audience").getString()
    val myRealm = environment.config.property("jwt.realm").getString()

    install(Authentication) {
        jwt("auth-jwt") {
            realm = myRealm
            verifier(JWT
                .require(Algorithm.HMAC256(secret))
                .withAudience(audience)
                .withIssuer(issuer)
                .build())

            validate { credential ->
                val username = credential.payload.getClaim("username").asString()
                val role = credential.payload.getClaim("role").asString()
                val id = credential.payload.getClaim("id").asInt()

                if ( id != null && !username.isNullOrBlank() && !role.isNullOrBlank()) {
                    securityLog.debug("SECURITY_JWT validate=ok usernameClaimPresent=true roleClaim='{}'", role)
                    JWTPrincipal(credential.payload)
                } else {
                    securityLog.warn(
                        "SECURITY_JWT validate=failed usernameClaimPresent={} roleClaimPresent={} issuer='{}' audience='{}'",
                        !username.isNullOrBlank(),
                        !role.isNullOrBlank(),
                        credential.payload.issuer,
                        credential.payload.audience.joinToString(",")
                    )
                    null
                }
            }
            challenge { _, _ ->
                securityLog.warn(
                    "SECURITY_JWT challenge=unauthorized method={} path={} origin={} authHeaderPresent={}",
                    call.request.httpMethod.value,
                    call.request.path(),
                    call.request.headers["Origin"] ?: "-",
                    !call.request.headers["Authorization"].isNullOrBlank()
                )
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
}

/**
 * Extension para proteger rutas segun el Rol del usuario.
 */
fun Route.withRoles(vararg roles: Role, build: Route.() -> Unit): Route {
    val route = createChild(object : RouteSelector() {
        override suspend fun evaluate(
            context: RoutingResolveContext,
            segmentIndex: Int
        ): RouteSelectorEvaluation = RouteSelectorEvaluation.Constant
    })

    val rolesGuard = createRouteScopedPlugin("RolesGuard") {
        on(AuthenticationChecked) { call ->
            val principal = call.principal<JWTPrincipal>()
            val userRoleString = principal?.payload?.getClaim("role")?.asString()
            val userRole = userRoleString?.let {
                runCatching { Role.valueOf(it) }.getOrNull()
            }

            if (principal == null) {
                rolesLog.warn(
                    "SECURITY_ROLES principal=null method={} path={} requiredRoles={}",
                    call.request.httpMethod.value,
                    call.request.path(),
                    roles.joinToString(",")
                )
            } else {
                rolesLog.debug(
                    "SECURITY_ROLES principal=ok roleClaim='{}' parsedRole='{}' requiredRoles={}",
                    userRoleString,
                    userRole,
                    roles.joinToString(",")
                )
            }

            if (userRole == null || userRole !in roles) {
                rolesLog.warn(
                    "SECURITY_ROLES access=denied roleClaim='{}' parsedRole='{}' requiredRoles={} method={} path={}",
                    userRoleString,
                    userRole,
                    roles.joinToString(","),
                    call.request.httpMethod.value,
                    call.request.path()
                )
                call.respond(
                    HttpStatusCode.Forbidden,
                    mapOf("error" to "No tienes permisos para realizar esta acción. Requerido: ${roles.joinToString()}")
                )
            }
        }
    }

    route.install(rolesGuard)
    route.build()
    return route
}
