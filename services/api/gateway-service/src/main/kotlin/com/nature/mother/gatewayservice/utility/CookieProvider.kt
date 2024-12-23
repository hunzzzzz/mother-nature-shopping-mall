package com.nature.mother.gatewayservice.utility

import com.nature.mother.common.variables.CookieVariables.COOKIE_ENCODER
import com.nature.mother.common.variables.CookieVariables.COOKIE_NAME_RTK
import com.nature.mother.common.variables.CookieVariables.COOKIE_NAME_USER_ID
import com.nature.mother.common.variables.CookieVariables.COOKIE_PATH
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.http.ResponseCookie
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import java.net.URLDecoder
import java.net.URLEncoder

@Component
@RefreshScope
class CookieProvider(
    @Value("\${jwt.expiration-time.rtk}")
    private val expirationTimeOfRtk: Int
) {
    fun getRefreshToken(request: ServerHttpRequest) =
        request.cookies[COOKIE_NAME_RTK]?.first()?.value
            ?.let { token -> URLDecoder.decode(token, COOKIE_ENCODER) }

    fun putUserId(userId: String, response: ServerHttpResponse) =
        response.addCookie(
            ResponseCookie
                .from(COOKIE_NAME_USER_ID, URLEncoder.encode(userId, COOKIE_ENCODER))
                .path(COOKIE_PATH)
                .maxAge(expirationTimeOfRtk.toLong())
                .build()
        )
}