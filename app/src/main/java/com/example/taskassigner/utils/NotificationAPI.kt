package com.example.taskassigner.utils

import com.example.taskassigner.models.PushNotification
import com.example.taskassigner.utils.Constants.CONTENT_TYPE
import com.example.taskassigner.utils.Constants.FCM_SERVER_KEY
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


interface NotificationAPI {
    @Headers("Authorization: key=$FCM_SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification: PushNotification,
    ): Response<ResponseBody>
}
