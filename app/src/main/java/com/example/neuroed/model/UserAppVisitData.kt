package com.example.neuroed.model

data class UserAppVisitData(
    val userid: Int,
    val day: String,
    val visitCount: Int,
    val visitTime:Int,
    val date:String,
)

data class Uservisitdatarespponse(
    val message: String,
)