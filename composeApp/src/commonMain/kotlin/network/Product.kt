package org.example.project.network


import kotlinx.serialization.Serializable

@Serializable
data class ApiProduct(
    val id: Int,
    val title: String
)