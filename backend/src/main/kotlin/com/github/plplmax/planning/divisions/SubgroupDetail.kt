package com.github.plplmax.planning.divisions

import kotlinx.serialization.Serializable

@Serializable
data class SubgroupDetail(val id: Int, val name: String, val division: Division)
