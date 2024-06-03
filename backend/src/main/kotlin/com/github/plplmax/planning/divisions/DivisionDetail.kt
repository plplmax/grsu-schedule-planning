package com.github.plplmax.planning.divisions

import kotlinx.serialization.Serializable

@Serializable
data class DivisionDetail(val id: Int, val subgroups: List<Subgroup>)
