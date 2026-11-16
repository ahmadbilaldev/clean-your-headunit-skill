package dev.bilal.rspills;

import android.content.Context;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

public class RamPill extends PillWidget {
    String text(Context c) {
        long total = 0, avail = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("/proc/meminfo"))) {
            String l;
            while ((l = br.readLine()) != null) {
                if (l.startsWith("MemTotal:")) total = kb(l);
                else if (l.startsWith("MemAvailable:")) avail = kb(l);
            }
        } catch (IOException e) { return "RAM ?"; }
        if (total == 0) return "RAM ?";
        return String.format(Locale.US, "RAM %.1f / %.1f GB", (total - avail) / 1048576.0, total / 1048576.0);
    }
    private static long kb(String l) { return Long.parseLong(l.replaceAll("[^0-9]", "")); }
    int accent() { return 0xFF30D158; }
    Class<?> self() { return RamPill.class; }
}
