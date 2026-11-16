package dev.bilal.glass;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;

/** Album art, title, artist and a playing indicator, from the media session the listener follows. */
public class NowPlayingCard extends Glass {
    Class<?> self() { return NowPlayingCard.class; }

    PendingIntent tap(Context c) {
        Intent i = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            .setComponent(new ComponentName("com.bw.mediaplayer", "com.bw.mediaplayer.ui.BtActivity"))
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        return PendingIntent.getActivity(c, 1, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    void draw(Context c, Canvas cv, int w, int h, float dp) {
        MediaListener.State s = MediaListener.state;
        float pad = 10 * dp;
        float art = h - 2 * pad;
        float ar = 8 * dp;
        RectF artRect = new RectF(pad, pad, pad + art, pad + art);

        // Art tile: cover image if we have one, else a soft tile with a note glyph.
        Paint tile = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (s.art != null) {
            BitmapShader sh = new BitmapShader(s.art, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            Matrix mx = new Matrix();
            float sc = Math.max(art / s.art.getWidth(), art / s.art.getHeight());
            mx.setScale(sc, sc);
            mx.postTranslate(pad + (art - s.art.getWidth() * sc) / 2f, pad + (art - s.art.getHeight() * sc) / 2f);
            sh.setLocalMatrix(mx);
            tile.setShader(sh);
            cv.drawRoundRect(artRect, ar, ar, tile);
        } else {
            tile.setColor(0x2EFFFFFF);
            cv.drawRoundRect(artRect, ar, ar, tile);
            note(cv, artRect, dp);
        }

        boolean playing = s.playing;
        String title = s.title != null ? s.title : (s.connected ? "Not Playing" : "Not Playing");
        String sub = s.title != null ? (s.artist != null && !s.artist.isEmpty() ? s.artist : "Bluetooth") : "Bluetooth Audio";

        float barsW = 14 * dp;
        float tx = pad + art + 12 * dp;
        float avail = w - tx - pad - (playing ? barsW + 10 * dp : 0);
        Paint tp = text(MED, Math.min(h * 0.24f, 17 * dp), WHITE);
        Paint sp = text(REG, Math.min(h * 0.19f, 14 * dp), DIM);
        float th = -tp.getFontMetrics().ascent + tp.getFontMetrics().descent;
        float shh = -sp.getFontMetrics().ascent + sp.getFontMetrics().descent;
        float gap = 3 * dp;
        float top = (h - (th + gap + shh)) / 2f;
        cv.drawText(fit(tp, title, avail), tx, top - tp.getFontMetrics().ascent, tp);
        cv.drawText(fit(sp, sub, avail), tx, top + th + gap - sp.getFontMetrics().ascent, sp);

        if (playing) bars(cv, w - pad - barsW, h / 2f, barsW, 14 * dp, dp);
    }

    /** Three equalizer bars, the "playing" mark. */
    static void bars(Canvas cv, float x, float cy, float w, float hgt, float dp) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG); p.setColor(WHITE);
        float bw = w / 5f;
        float[] hs = {0.55f, 1.0f, 0.75f};
        for (int i = 0; i < 3; i++) {
            float bh = hgt * hs[i];
            cv.drawRoundRect(new RectF(x + i * 2 * bw, cy + hgt / 2f - bh, x + i * 2 * bw + bw, cy + hgt / 2f), bw / 2f, bw / 2f, p);
        }
    }

    /** A simple music note, drawn so no font or icon resource is needed. */
    static void note(Canvas cv, RectF r, float dp) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG); p.setColor(0xCCFFFFFF);
        float s = r.width();
        float cx = r.centerX(), cy = r.centerY();
        float headR = s * 0.11f;
        float stemH = s * 0.42f;
        float stemW = s * 0.05f;
        float hx = cx - s * 0.08f, hy = cy + s * 0.16f;
        cv.drawOval(new RectF(hx - headR * 1.25f, hy - headR * 0.9f, hx + headR * 1.25f, hy + headR * 0.9f), p);
        cv.drawRoundRect(new RectF(hx + headR * 1.25f - stemW, hy - stemH, hx + headR * 1.25f, hy), stemW / 2f, stemW / 2f, p);
        Path flag = new Path();
        float fx = hx + headR * 1.25f, fy = hy - stemH;
        flag.moveTo(fx - stemW, fy);
        flag.cubicTo(fx + s * 0.16f, fy + s * 0.05f, fx + s * 0.16f, fy + s * 0.22f, fx + s * 0.04f, fy + s * 0.30f);
        flag.cubicTo(fx + s * 0.11f, fy + s * 0.18f, fx + s * 0.08f, fy + s * 0.11f, fx - stemW, fy + s * 0.10f);
        flag.close();
        cv.drawPath(flag, p);
    }
}
