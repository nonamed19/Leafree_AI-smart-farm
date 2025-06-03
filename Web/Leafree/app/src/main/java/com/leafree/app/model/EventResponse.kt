package com.leafree.app.model

import com.google.gson.annotations.SerializedName

data class EventResponse(
    @SerializedName("eventType") val eventType: String,
    @SerializedName("code") val eventContentCode: String,
    @SerializedName("occurredAt") val occuredAt: String,
    @SerializedName("eventData") val eventData: Map<String, Any>
)
