package com.blackmessage.Fragments;

import com.blackmessage.Notifications.MyResponse;
import com.blackmessage.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=	AAAABW4aMx4:APA91bEUSn-yHoYu0Ei5X-Gl-QB7KiRPB6-Sux70YIz3YY-ABH0v4Td7a9i3VdlYjHZCcK3flW0qopAIFyuFesxBuADvHTYewWfP8nQ1_D5ZEktc1oUKDkREvn0L8ftLGuVhXfxLAgAA"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
