package com.nature.mother.gatewayservice.refresh.controller

import com.nature.mother.gatewayservice.refresh.service.RefreshService
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class RefreshController(
    private val refreshService: RefreshService
) {
    @GetMapping("/refresh")
    fun refresh(serverHttpRequest: ServerHttpRequest) =
        refreshService.refresh(serverHttpRequest = serverHttpRequest)
}