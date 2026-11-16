package dev.bilal.glass;

import android.content.ComponentName;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.Looper;
import android.service.notification.NotificationListenerService;
import java.util.List;

/**
 * Follows the active media session (the Bluetooth AVRCP one, when music streams from the phone)
 * and re-renders the Now Playing card on every change. Notification-listener access is what
 * Android requires to read sessions; the service never touches notifications.
 */
public class MediaListener extends NotificationListenerService {
    static class State {
        volatile String title, artist, pkg; volatile Bitmap art; volatile boolean playing, connected;
    }
    static final State state = new State();

    private MediaSessionManager msm;
    private MediaController current;
    private final Handler ui = new Handler(Looper.getMainLooper());

    private final Sessions sessions = new Sessions(this);
    private final Cb cb = new Cb(this);

    // Static nested classes on purpose: JDK 21's javac emits a nameless "mandated" constructor
    // parameter for inner classes, which the build-tools 34 d8 cannot read.
    static class Sessions implements MediaSessionManager.OnActiveSessionsChangedListener {
        final MediaListener o; Sessions(MediaListener o) { this.o = o; }
        @Override public void onActiveSessionsChanged(List<MediaController> l) { o.pick(l); }
    }
    static class Cb extends MediaController.Callback {
        final MediaListener o; Cb(MediaListener o) { this.o = o; }
        @Override public void onMetadataChanged(MediaMetadata m) { o.read(); }
        @Override public void onPlaybackStateChanged(PlaybackState s) { o.read(); }
        @Override public void onSessionDestroyed() { o.pick(null); }
    }

    @Override public void onListenerConnected() {
        msm = (MediaSessionManager) getSystemService(MEDIA_SESSION_SERVICE);
        ComponentName me = new ComponentName(this, MediaListener.class);
        try {
            msm.addOnActiveSessionsChangedListener(sessions, me, ui);
            pick(msm.getActiveSessions(me));
        } catch (SecurityException e) { state.connected = false; refresh(); }
    }

    @Override public void onListenerDisconnected() {
        if (msm != null) msm.removeOnActiveSessionsChangedListener(sessions);
        follow(null);
        state.connected = false; refresh();
    }

    /** Prefer a playing session with a title; else any session with a title; else the first. */
    void pick(List<MediaController> list) {
        if (list == null) { try { list = msm.getActiveSessions(new ComponentName(this, MediaListener.class)); } catch (SecurityException e) { list = null; } }
        MediaController best = null; int bestScore = -1;
        if (list != null) for (MediaController mc : list) {
            MediaMetadata md = mc.getMetadata(); PlaybackState ps = mc.getPlaybackState();
            boolean hasTitle = md != null && md.getString(MediaMetadata.METADATA_KEY_TITLE) != null;
            boolean playing = ps != null && ps.getState() == PlaybackState.STATE_PLAYING;
            int score = (playing ? 2 : 0) + (hasTitle ? 1 : 0);
            if ("com.android.server.telecom".equals(mc.getPackageName())) score = -1;
            if (score > bestScore) { best = mc; bestScore = score; }
        }
        follow(best);
        read();
    }

    private void follow(MediaController mc) {
        if (current != null) current.unregisterCallback(cb);
        current = mc;
        if (current != null) current.registerCallback(cb, ui);
    }

    void read() {
        state.connected = true;
        if (current == null) { state.title = null; state.artist = null; state.art = null; state.playing = false; state.pkg = null; refresh(); return; }
        MediaMetadata md = current.getMetadata(); PlaybackState ps = current.getPlaybackState();
        state.pkg = current.getPackageName();
        state.playing = ps != null && ps.getState() == PlaybackState.STATE_PLAYING;
        if (md != null) {
            state.title = md.getString(MediaMetadata.METADATA_KEY_TITLE);
            String a = md.getString(MediaMetadata.METADATA_KEY_ARTIST);
            if (a == null || a.isEmpty()) a = md.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST);
            state.artist = a;
            Bitmap b = md.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
            if (b == null) b = md.getBitmap(MediaMetadata.METADATA_KEY_ART);
            if (b == null) b = md.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON);
            if (b != null && b.getWidth() > 256) b = Bitmap.createScaledBitmap(b, 256, 256 * b.getHeight() / b.getWidth(), true);
            state.art = b;
        } else { state.title = null; state.artist = null; state.art = null; }
        refresh();
    }

    private void refresh() { new NowPlayingCard().updateAll(this); }
}
