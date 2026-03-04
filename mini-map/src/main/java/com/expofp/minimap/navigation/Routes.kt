package com.expofp.minimap.navigation

import kotlinx.serialization.Serializable

@Serializable
object ExhibitorList

@Serializable
data class ExhibitorDetail(val exhibitorName: String)
