package com.nature.mother.common.variables

object RedisVariables {
    fun getKeyOfRtk(userId: String) = "rtk::$userId"
}