package com.shadowings.devfestmilano26.core.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Today

/** [date] null means "show today's entry" -- the Log/Detail tab's default when nothing specific was selected. */
@Serializable
data class Detail(val date: String? = null)

@Serializable
data object Favorites

@Serializable
data object Login
