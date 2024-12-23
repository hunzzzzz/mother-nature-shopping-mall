package com.nature.mother.common.utility

import com.nature.mother.common.model.SimpleUserInfo
import com.nature.mother.common.variables.JwtVariables.BEARER_PREFIX
import com.nature.mother.common.variables.JwtVariables.CLAIM_NAME_EMAIL
import com.nature.mother.common.variables.JwtVariables.CLAIM_NAME_ROLE
import com.nature.mother.common.variables.JwtVariables.CLAIM_NAME_TYPE
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
@RefreshScope
class JwtProvider(
    @Value("\${jwt.secret.key}")
    private val secretKey: String,

    @Value("\${jwt.issuer}")
    private val issuer: String,

    @Value("\${jwt.expiration-time.atk}")
    private val expirationTimeOfAtk: Int,

    @Value("\${jwt.expiration-time.rtk}")
    private val expirationTimeOfRtk: Int
) {
    private val key: SecretKey =
        Base64.getDecoder().decode(secretKey).let { Keys.hmacShaKeyFor(it) }

    private fun createToken(tokenUserInfo: SimpleUserInfo, expirationTime: Int) =
        Jwts.builder().let {
            it.subject(tokenUserInfo.userId)
            it.claims(
                Jwts.claims().add(
                    mapOf(
                        CLAIM_NAME_TYPE to tokenUserInfo.type,
                        CLAIM_NAME_ROLE to tokenUserInfo.role,
                        CLAIM_NAME_EMAIL to tokenUserInfo.email,
                    )
                ).build()
            )
            it.expiration(Date(Date().time + expirationTime))
            it.issuedAt(Date())
            it.issuer(issuer)
            it.signWith(key)
            it.compact()
        }.let { jwt -> "$BEARER_PREFIX$jwt" }

    fun createAccessToken(tokenUserInfo: SimpleUserInfo) =
        createToken(tokenUserInfo = tokenUserInfo, expirationTime = expirationTimeOfAtk)

    fun createRefreshToken(tokenUserInfo: SimpleUserInfo) =
        createToken(tokenUserInfo = tokenUserInfo, expirationTimeOfRtk)

    fun substringToken(token: String?) =
        if (!token.isNullOrBlank() && token.startsWith(BEARER_PREFIX))
            token.substring(BEARER_PREFIX.length)
        else null

    fun validateToken(token: String) =
        kotlin.runCatching { Jwts.parser().verifyWith(key).build().parseSignedClaims(token) }

    fun getUserInfoFromToken(token: String): Claims =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
}