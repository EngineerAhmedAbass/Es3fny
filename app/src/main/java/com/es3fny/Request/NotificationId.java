package com.es3fny.Request;

import android.support.annotation.NonNull;

/**
 * Created by ahmed on 27-Mar-18.
 */

public class NotificationId {
    public String notificationId ;

    public <T extends NotificationId > T withId(@NonNull final String id){
        this.notificationId = id;
        return (T) this;
    }
}
