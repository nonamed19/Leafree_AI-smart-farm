package com.leafree.app.repository

import android.content.Context
import com.leafree.app.di.NetworkModule
import com.leafree.app.model.EventResponse

class EventRepository(context: Context) {
    private val api = NetworkModule.provideApi(context)

    suspend fun getEvents(deviceId: Long, date: String): List<EventResponse> {
        return api.getEvents(deviceId, date)
    }
}
