package dev.bilal.rspills;

import android.content.Context;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ClockPill extends PillWidget {
    String text(Context c) {
        String f = android.text.format.DateFormat.is24HourFormat(c) ? "HH:mm" : "h:mm a";
        return new SimpleDateFormat(f, Locale.getDefault()).format(new Date());
    }
    int accent() { return 0xFFD7141A; }
    Class<?> self() { return ClockPill.class; }
}
