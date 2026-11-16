package dev.bilal.glass;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.format.DateFormat;
import java.util.Date;

/** Time and date, CarPlay style: big time, small meridiem, date underneath. */
public class ClockCard extends Glass {
    Class<?> self() { return ClockCard.class; }
    boolean ticks() { return true; }

    void draw(Context c, Canvas cv, int w, int h, float dp) {
        Date now = new Date();
        boolean h24 = DateFormat.is24HourFormat(c);
        String time = DateFormat.format(h24 ? "HH:mm" : "h:mm", now).toString();
        String ampm = h24 ? "" : DateFormat.format("a", now).toString().toUpperCase();
        String date = DateFormat.format("EEEE, d MMMM", now).toString();

        float pad = 14 * dp;
        float big = Math.min(h * 0.50f, w * 0.22f);
        Paint tp = text(MED, big, WHITE); tp.setLetterSpacing(-0.02f);
        Paint ap = text(MED, big * 0.38f, DIM);
        Paint dpnt = text(REG, Math.min(h * 0.19f, 15 * dp), DIM);

        Paint.FontMetrics fm = tp.getFontMetrics();
        float timeH = -fm.ascent + fm.descent;
        float dateH = -dpnt.getFontMetrics().ascent + dpnt.getFontMetrics().descent;
        float gap = 2 * dp;
        float top = (h - (timeH + gap + dateH)) / 2f;
        float base = top - fm.ascent;
        cv.drawText(time, pad, base, tp);
        if (!ampm.isEmpty()) cv.drawText(ampm, pad + tp.measureText(time) + 5 * dp, base, ap);
        cv.drawText(fit(dpnt, date, w - 2 * pad), pad + 1 * dp, base + fm.descent + gap - dpnt.getFontMetrics().ascent, dpnt);
    }
}
