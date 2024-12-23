package com.nature.mother.common.exception

enum class ErrorCode(val message: String) {
    // jwt
    INVALID_TOKEN("유효하지 않은 JWT 토큰입니다."),
    EXPIRED_ACCESS_TOKEN("Access Token이 만료되었습니다."),
    EXPIRED_USER_INFO("사용자 정보가 만료되었습니다. 다시 로그인해주세요"),
}