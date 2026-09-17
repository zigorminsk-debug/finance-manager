package com.byfinancemanager.data.remote

import com.google.gson.annotations.SerializedName

data class NbrbRate(
    @SerializedName("Cur_ID")
    val curId: Int,
    @SerializedName("Date")
    val date: String,
    @SerializedName("Cur_Abbreviation")
    val abbreviation: String,
    @SerializedName("Cur_Scale")
    val scale: Int,
    @SerializedName("Cur_Name")
    val name: String,
    @SerializedName("Cur_OfficialRate")
    val officialRate: Double?
)

data class NbrbRateShort(
    val rate: Double,
    val date: String,
    val scale: Int = 1
)
