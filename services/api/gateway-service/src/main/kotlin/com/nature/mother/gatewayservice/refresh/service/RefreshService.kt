package com.nature.mother.gatewayservice.refresh.service

import com.nature.mother.common.dto.response.TokenResponse
import com.nature.mother.common.exception.ErrorCode.EXPIRED_USER_INFO
import com.nature.mother.common.exception.ErrorCode.INVALID_TOKEN
import com.nature.mother.common.model.SimpleUserInfo
import com.nature.mother.common.utility.JwtProvider
import com.nature.mother.common.utility.RedisCommands
import com.nature.mother.common.variables.JwtVariables.CLAIM_NAME_EMAIL
import com.nature.mother.common.variables.JwtVariables.CLAIM_NAME_ROLE
import com.nature.mother.common.variables.JwtVariables.CLAIM_NAME_TYPE
import com.nature.mother.common.variables.RedisVariables.getKeyOfRtk
import com.nature.mother.gatewayservice.exception.custom.JwtException
import com.nature.mother.gatewayservice.utility.CookieProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RefreshService(
    @Value("\${jwt.expiration-time.rtk}")
    private val expirationTime: Long,
    private val jwtProvider: JwtProvider,
    private val cookieProvider: CookieProvider,
    private val redisCommands: RedisCommands
) {
    fun refresh(serverHttpRequest: ServerHttpRequest): TokenResponse? {
        val refreshTokenValue = cookieProvider.getRefreshToken(request = serverHttpRequest)
        val refreshToken = jwtProvider.substringToken(token = refreshTokenValue)
            ?: throw JwtException(INVALID_TOKEN)

        jwtProvider.validateToken(token = refreshToken)
            .onSuccess {
                val payload = jwtProvider.getUserInfoFromToken(token = refreshToken)
                val refreshTokenInRedis = redisCommands.get(key = getKeyOfRtk(userId = payload.subject))

                // check two rtk (from cookie & from redis)
                if (refreshTokenValue != refreshTokenInRedis) throw JwtException(INVALID_TOKEN)
                else {
                    val tokenUserInfo = SimpleUserInfo(
                        userId = payload.subject,
                        type = payload.get(CLAIM_NAME_TYPE, String::class.java),
                        email = payload.get(CLAIM_NAME_EMAIL, String::class.java),
                        role = payload.get(CLAIM_NAME_ROLE, String::class.java)
                    )
                    // create 'new atk' and 'new rtk' (rtk rotation)
                    val newAccessToken = jwtProvider.createAccessToken(tokenUserInfo = tokenUserInfo)
                    val newRefreshToken = jwtProvider.createRefreshToken(tokenUserInfo = tokenUserInfo)

                    // save 'new rtk' in redis
                    redisCommands.set(
                        key = getKeyOfRtk(userId = payload.subject),
                        value = newRefreshToken,
                        expirationTime = expirationTime,
                        timeUnit = TimeUnit.MILLISECONDS
                    )

                    return TokenResponse(accessToken = newAccessToken, refreshToken = newRefreshToken)
                }
            }
            .onFailure { throw JwtException(EXPIRED_USER_INFO) }

        return null
    }
}