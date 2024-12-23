package com.nature.mother.gatewayservice.auth

import com.nature.mother.common.exception.ErrorCode.EXPIRED_ACCESS_TOKEN
import com.nature.mother.common.exception.ErrorCode.INVALID_TOKEN
import com.nature.mother.common.utility.JwtProvider
import com.nature.mother.gatewayservice.exception.custom.JwtException
import com.nature.mother.gatewayservice.utility.CookieProvider
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Component

@Component
class JwtAuthorizationFilter(
    private val jwtProvider: JwtProvider,
    private val cookieProvider: CookieProvider
) : AbstractGatewayFilterFactory<JwtAuthorizationFilter.Config>(Config::class.java) {
    class Config

    override fun apply(config: Config?): GatewayFilter =
        GatewayFilter { exchange, chain ->
            val request = exchange.request
            val response = exchange.response

            val authorizationHeader = request.headers.getFirst(AUTHORIZATION)
            val accessToken = jwtProvider.substringToken(token = authorizationHeader)
                ?: throw JwtException(INVALID_TOKEN)

            // validate atk
            jwtProvider.validateToken(token = accessToken)
                // if atk valid -> put 'userId' in cookie
                .onSuccess {
                    val payload = jwtProvider.getUserInfoFromToken(token = accessToken!!)

                    cookieProvider.putUserId(userId = payload.subject, response = response)
                }
                // if atk invalid -> exception
                .onFailure { throw JwtException(EXPIRED_ACCESS_TOKEN) }

            chain.filter(exchange)
        }
}