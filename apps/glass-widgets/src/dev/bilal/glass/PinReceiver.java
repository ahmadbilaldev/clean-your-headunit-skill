package dev.bilal.glass;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/** `am broadcast -n dev.bilal.glass/.PinReceiver --es which clock|now` asks the launcher to place a widget. */
public class PinReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent i) {
        Class<?> p = "now".equals(i.getStringExtra("which")) ? NowPlayingCard.class : ClockCard.class;
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        if (m.isRequestPinAppWidgetSupported()) m.requestPinAppWidget(new ComponentName(c, p), null, null);
    }
}
