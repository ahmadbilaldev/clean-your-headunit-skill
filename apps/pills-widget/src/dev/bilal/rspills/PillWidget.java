package dev.bilal.rspills;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.widget.RemoteViews;

/** A pill drawn as a bitmap, so the font survives the trip into the launcher. Re-rendered every minute. */
public abstract class PillWidget extends AppWidgetProvider {
    static final String TICK = "dev.bilal.rspills.TICK";

    abstract String text(Context c);
    abstract int accent();
    abstract Class<?> self();

    @Override public void onReceive(Context c, Intent i) {
        super.onReceive(c, i);
        String a = i.getAction();
        if (TICK.equals(a) || Intent.ACTION_TIME_CHANGED.equals(a) || Intent.ACTION_TIMEZONE_CHANGED.equals(a)) {
            AppWidgetManager m = AppWidgetManager.getInstance(c);
            int[] ids = m.getAppWidgetIds(new ComponentName(c, self()));
            if (ids.length > 0) onUpdate(c, m, ids);
        }
    }

    @Override public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        Bitmap b = render(c, text(c), accent());
        for (int id : ids) {
            RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.pill);
            rv.setImageViewBitmap(R.id.img, b);
            m.updateAppWidget(id, rv);
        }
        schedule(c);
    }

    @Override public void onDisabled(Context c) {
        ((AlarmManager) c.getSystemService(Context.ALARM_SERVICE)).cancel(pi(c));
    }

    private PendingIntent pi(Context c) {
        Intent i = new Intent(c, self()).setAction(TICK);
        return PendingIntent.getBroadcast(c, 0, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void schedule(Context c) {
        long next = (System.currentTimeMillis() / 60000 + 1) * 60000 + 300;
        ((AlarmManager) c.getSystemService(Context.ALARM_SERVICE)).setExactAndAllowWhileIdle(AlarmManager.RTC, next, pi(c));
    }

    static Bitmap render(Context c, String text, int accent) {
        int h = 120;
        Paint tp = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        tp.setTypeface(c.getResources().getFont(R.font.russo_one));
        tp.setTextSize(h * 0.44f);
        tp.setColor(Color.WHITE);
        tp.setLetterSpacing(0.04f);
        float tw = tp.measureText(text);
        int dot = (int) (h * 0.17f), padX = (int) (h * 0.40f), gap = (int) (h * 0.20f);
        int w = (int) (padX + dot + gap + tw + padX);
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(b);
        RectF r = new RectF(2, 2, w - 2, h - 2);
        Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG); bg.setColor(0xD9161618);
        cv.drawRoundRect(r, h / 2f, h / 2f, bg);
        Paint rim = new Paint(Paint.ANTI_ALIAS_FLAG); rim.setStyle(Paint.Style.STROKE); rim.setStrokeWidth(2.5f); rim.setColor(0x55FFFFFF);
        cv.drawRoundRect(r, h / 2f, h / 2f, rim);
        Paint dp = new Paint(Paint.ANTI_ALIAS_FLAG); dp.setColor(accent);
        dp.setShadowLayer(h * 0.12f, 0, 0, accent);
        cv.drawCircle(padX + dot / 2f, h / 2f, dot / 2f, dp);
        Paint.FontMetrics fm = tp.getFontMetrics();
        cv.drawText(text, padX + dot + gap, h / 2f - (fm.ascent + fm.descent) / 2f, tp);
        return b;
    }
}
