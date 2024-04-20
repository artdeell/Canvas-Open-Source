package com.tgc.sky;

import android.util.Log;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.datasource.HttpDataSource;

class VideoListener implements Player.Listener {
    public String TAG = "VideoListener";

    VideoListener() {
    }

    @Override
    public void onPlayerError(PlaybackException playbackException) {
        Log.d(this.TAG, "Video Error: " + playbackException.getMessage());
        Throwable cause = playbackException.getCause();
        if (cause instanceof HttpDataSource.HttpDataSourceException) {
            HttpDataSource.HttpDataSourceException httpDataSourceException = (HttpDataSource.HttpDataSourceException) cause;
            Throwable cause2 = httpDataSourceException.getCause();
            boolean z = httpDataSourceException instanceof HttpDataSource.InvalidResponseCodeException;
        }
    }
}