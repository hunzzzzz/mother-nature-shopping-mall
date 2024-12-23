package com.nature.mother.gatewayservice.exception.custom

import com.nature.mother.common.exception.ErrorCode

class JwtException(errorCode: ErrorCode) : RuntimeException("[${errorCode}] ${errorCode.message}")