package com.example.neuroed.utils

import com.example.neuroed.AssignmentItem
import com.example.neuroed.AssignmentStatus
import com.example.neuroed.model.Assignmentdata


private fun Assignmentdata.toUi(): AssignmentItem = AssignmentItem(
    id          = (id ?: 0),
    subject     = subject ?: "Unknown",
    title       = topic ?: "Untitled",
    description = description.orEmpty(),
    date        = dueDate.orEmpty(),
    status      = when (status?.lowercase()) {
        "completed", "success", "successful" -> AssignmentStatus.SUCCESSFUL
        "missing",   "missed"               -> AssignmentStatus.MISSING
        else                                  -> AssignmentStatus.PENDING
    },
    fileRequired = true
)
