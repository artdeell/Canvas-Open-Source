package com.tgc.sky;

import android.util.Log;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.datasource.HttpDataSource;

/* compiled from: SystemIO_android.java */
/* loaded from: classes2.dex */
public class VideoListener implements Player.Listener {
    public String TAG = "VideoListener";

    @Override // androidx.media3.common.Player.Listener
    public void onPlayerError(PlaybackException playbackException) {
        Log.d(this.TAG, "Video Error: " + playbackException.getMessage());
        Throwable cause = playbackException.getCause();
        if (cause instanceof HttpDataSource.HttpDataSourceException) {
            HttpDataSource.HttpDataSourceException httpDataSourceException = (HttpDataSource.HttpDataSourceException) cause;
            Throwable cause2 = httpDataSourceException.getCause();
            if (cause2 != null) {
                //Log.d(this.TAG, "Video HTTP Error: " + cause2.toString() + CertificateUtil.DELIMITER + cause2.getMessage());
            }
            boolean z = httpDataSourceException instanceof HttpDataSource.InvalidResponseCodeException;
        }
    }
}
