package com.example.neuroed.model

import com.google.gson.annotations.SerializedName


/**
 * Assignment data model for handling assignment data from the Django backend
 */
data class Assignmentdata(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("user")
    val userId: Int? = null,

    @SerializedName("subject")
    val subject: String? = null,

    @SerializedName("topic")
    val topic: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("due_date")
    val dueDate: String? = null,

    @SerializedName("status")
    val status: String? = "pending",

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
) {
    /**
     * Returns a formatted string representation of the assignment
     */
    override fun toString(): String {
        return "$subject - ${topic ?: "No Topic"}"
    }

    /**
     * Helper function to get status display text with proper capitalization
     */
    fun getStatusDisplay(): String {
        return status?.replaceFirstChar { it.uppercase() } ?: "Pending"
    }

    /**
     * Helper function to check if the assignment is overdue
     */
    fun isOverdue(): Boolean {
        // Implementation would require parsing the dueDate string into a Date object
        // and comparing with current date
        return false // Placeholder implementation
    }
}

