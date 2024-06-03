package com.github.plplmax.planning.divisions

import kotlinx.serialization.Serializable

@Serializable
data class NewDivision(val subgroups: List<Subgroup>)
