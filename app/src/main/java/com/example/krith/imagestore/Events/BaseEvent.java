package com.example.krith.imagestore.Events;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by krith on 28/09/16.
 */

public class BaseEvent {

    public void post() {
        EventBus.getDefault().post(this);
    }
}
