package dev.bilal.glass;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RemoteViews;

/** Base for a translucent "glass" card rendered as a bitmap at the size the launcher gives the widget. */
public abstract class Glass extends AppWidgetProvider {
    static final String TICK = "dev.bilal.glass.TICK";
    static final Typeface MED = Typeface.create("sans-serif-medium", Typeface.NORMAL);
    static final Typeface REG = Typeface.create("sans-serif", Typeface.NORMAL);
    static final int WHITE = 0xFFFFFFFF, DIM = 0xB8FFFFFF;

    /** Draw the card contents; the glass plate is already drawn. */
    abstract void draw(Context c, Canvas cv, int w, int h, float dp);
    abstract Class<?> self();
    PendingIntent tap(Context c) { return null; }
    boolean ticks() { return false; }

    @Override public void onReceive(Context c, Intent i) {
        super.onReceive(c, i);
        String a = i.getAction();
        if (TICK.equals(a) || Intent.ACTION_TIME_CHANGED.equals(a) || Intent.ACTION_TIMEZONE_CHANGED.equals(a)) updateAll(c);
    }

    @Override public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        for (int id : ids) push(c, m, id);
        if (ticks()) schedule(c);
    }

    @Override public void onAppWidgetOptionsChanged(Context c, AppWidgetManager m, int id, Bundle o) { push(c, m, id); }

    @Override public void onDisabled(Context c) {
        if (ticks()) ((AlarmManager) c.getSystemService(Context.ALARM_SERVICE)).cancel(pi(c));
    }

    void updateAll(Context c) {
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        int[] ids = m.getAppWidgetIds(new ComponentName(c, self()));
        if (ids.length > 0) onUpdate(c, m, ids);
    }

    void push(Context c, AppWidgetManager m, int id) {
        float dp = c.getResources().getDisplayMetrics().density;
        Bundle o = m.getAppWidgetOptions(id);
        // Landscape-only screen: width comes from MAX_WIDTH, height from MIN_HEIGHT.
        int wdp = o == null ? 0 : o.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
        int hdp = o == null ? 0 : o.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        if (wdp < 60) wdp = 250;
        if (hdp < 40) hdp = 74;
        int w = (int) (wdp * dp), h = (int) (hdp * dp);
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(b);
        plate(cv, w, h, dp);
        draw(c, cv, w, h, dp);
        RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.card);
        rv.setImageViewBitmap(R.id.img, b);
        PendingIntent t = tap(c);
        if (t != null) rv.setOnClickPendingIntent(R.id.img, t);
        m.updateAppWidget(id, rv);
    }

    /** Frosted plate: faint white fill with a top-lit gradient and a hairline rim. */
    static void plate(Canvas cv, int w, int h, float dp) {
        float r = Math.min(20 * dp, h / 2f);
        RectF rect = new RectF(1, 1, w - 1, h - 1);
        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setShader(new LinearGradient(0, 0, 0, h, 0x33FFFFFF, 0x1AFFFFFF, Shader.TileMode.CLAMP));
        cv.drawRoundRect(rect, r, r, fill);
        Paint tint = new Paint(Paint.ANTI_ALIAS_FLAG); tint.setColor(0x26000000);
        cv.drawRoundRect(rect, r, r, tint);
        Paint rim = new Paint(Paint.ANTI_ALIAS_FLAG); rim.setStyle(Paint.Style.STROKE); rim.setStrokeWidth(1.2f * dp / 2f + 0.6f);
        rim.setShader(new LinearGradient(0, 0, 0, h, 0x66FFFFFF, 0x22FFFFFF, Shader.TileMode.CLAMP));
        cv.drawRoundRect(rect, r, r, rim);
    }

    static Paint text(Typeface tf, float size, int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        p.setTypeface(tf); p.setTextSize(size); p.setColor(color);
        return p;
    }

    static String fit(Paint p, String s, float width) {
        return TextUtils.ellipsize(s, new android.text.TextPaint(p), width, TextUtils.TruncateAt.END).toString();
    }

    private PendingIntent pi(Context c) {
        Intent i = new Intent(c, self()).setAction(TICK);
        return PendingIntent.getBroadcast(c, 0, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void schedule(Context c) {
        long next = (System.currentTimeMillis() / 60000 + 1) * 60000 + 300;
        ((AlarmManager) c.getSystemService(Context.ALARM_SERVICE)).setExactAndAllowWhileIdle(AlarmManager.RTC, next, pi(c));
    }
}
