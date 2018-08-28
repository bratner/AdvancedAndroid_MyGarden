package com.example.android.mygarden;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViewsService;

/**
 * Created by bratner on 27/08/18.
 */

public class WidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.d("WidgetRemoteViewsSer", "onGetViewFactory()");
        return new WidgetRemoteViewsFactory(this.getApplicationContext());
    }
}
