package com.example.neuroed.model

data class SubjectSyllabusSaveModel(
    val subjectId: String,
    val subjectUnit: Int,
    val subjectChapterName: String,
    val subjectSyllabus: String
)


data class  SubjectSyllabusSaveResponse(
    val message: String
)