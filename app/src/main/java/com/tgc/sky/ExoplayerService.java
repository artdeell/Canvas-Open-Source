package com.tgc.sky;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.HardwareBuffer;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.util.Log;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter;

import java.util.Objects;

@SuppressLint("UnsafeOptInUsageError")
class ExoplayerService {
    public String TAG = "Exoplayer";
    Context m_context;
    HlsMediaSource.Factory m_hlsMediaFactory;
    Image m_image0;
    Image m_image1;
    ImageReader m_imageReader;
    ExoPlayer m_player;

    public void Initialize(Context context) {
        if (this.m_player == null) {
            DefaultLoadControl defaultLoadControl = new DefaultLoadControl();
            this.m_context = context;
            DefaultBandwidthMeter build = new DefaultBandwidthMeter.Builder(context).build();
            DefaultTrackSelector defaultTrackSelector = new DefaultTrackSelector(context, new AdaptiveTrackSelection.Factory());
            this.m_hlsMediaFactory = new HlsMediaSource.Factory(new DefaultDataSource.Factory(context));
            ExoPlayer build2 = new ExoPlayer.Builder(context).setBandwidthMeter(build).setTrackSelector(defaultTrackSelector).setLoadControl(defaultLoadControl).build();
            this.m_player = build2;
            build2.addListener(new VideoListener());
        }
    }

    public void Terminate() {
        ExoPlayer exoPlayer = this.m_player;
        if (exoPlayer != null) {
            exoPlayer.release();
            this.m_player = null;
        }
        ImageReader imageReader = this.m_imageReader;
        if (imageReader != null) {
            imageReader.close();
            this.m_imageReader = null;
        }
    }

    public void LoadUrl(String str) {
        EndVideo();
        MediaItem fromUri = MediaItem.fromUri(str);
        if (str.contains(".m3u8")) {
            this.m_player.setMediaSource(this.m_hlsMediaFactory.createMediaSource(fromUri));
        } else if (this.m_player.isCommandAvailable(Player.COMMAND_SET_MEDIA_ITEM)) {
            this.m_player.setMediaItem(fromUri);
        }
        if (this.m_player.isCommandAvailable(Player.COMMAND_PREPARE)) {
            this.m_player.prepare();
        }
    }

    public void EndVideo() {
        ExoPlayer exoPlayer = this.m_player;
        if (exoPlayer != null && exoPlayer.isCommandAvailable(Player.COMMAND_STOP)) {
            this.m_player.stop();
        }
        if (this.m_imageReader != null) {
            if (this.m_image0 != null) {
                if (Build.VERSION.SDK_INT >= 28) {
                    this.m_image0.getHardwareBuffer().close();
                }
                this.m_image0.close();
                this.m_image0 = null;
            }
            if (this.m_image1 != null) {
                if (Build.VERSION.SDK_INT >= 28) {
                    this.m_image1.getHardwareBuffer().close();
                }
                this.m_image1 = null;
            }
            this.m_imageReader.close();
        }
        this.m_imageReader = null;
        if (this.m_player.isCommandAvailable(Player.COMMAND_SET_VIDEO_SURFACE)) {
            this.m_player.clearVideoSurface();
        }
    }

    public void Update() {
        if (this.m_imageReader != null || this.m_player.getVideoFormat() == null) {
            return;
        }
        int i = this.m_player.getVideoFormat().width;
        int i2 = this.m_player.getVideoFormat().height;
        Log.d(this.TAG, "Video Metadata received: " + i + " " + i2);
        if (Build.VERSION.SDK_INT >= 29) {
            this.m_imageReader = ImageReader.newInstance(i, i2, ImageFormat.PRIVATE, 4, HardwareBuffer.USAGE_GPU_SAMPLED_IMAGE);
        } else {
            this.m_imageReader = ImageReader.newInstance(i, i2, ImageFormat.PRIVATE, 4);
        }
        if (this.m_player.isCommandAvailable(Player.COMMAND_SET_VIDEO_SURFACE)) {
            this.m_player.setVideoSurface(this.m_imageReader.getSurface());
        }
    }

    public ExoplayerVideoMetadata GetMetadata() {
        if (this.m_player.getVideoFormat() == null) {
            return null;
        }
        ExoplayerVideoMetadata exoplayerVideoMetadata = new ExoplayerVideoMetadata();
        exoplayerVideoMetadata.width = this.m_player.getVideoFormat().width;
        exoplayerVideoMetadata.height = this.m_player.getVideoFormat().height;
        exoplayerVideoMetadata.framesPerSecond = this.m_player.getVideoFormat().frameRate;
        if (exoplayerVideoMetadata.framesPerSecond <= 30.0) {
            exoplayerVideoMetadata.framesPerSecond = 30.0d;
        }
        return exoplayerVideoMetadata;
    }

    public long GetPlaybackPositionMs() {
        if (this.m_player.isCommandAvailable(Player.COMMAND_GET_CURRENT_MEDIA_ITEM)) {
            return this.m_player.getCurrentPosition();
        }
        return 0L;
    }

    public HardwareBuffer GetNextHardwareBuffer() {
        Image acquireLatestImage;
        HardwareBuffer hardwareBuffer;
        if (this.m_imageReader == null || Build.VERSION.SDK_INT < 28 || (acquireLatestImage = this.m_imageReader.acquireLatestImage()) == null || (hardwareBuffer = acquireLatestImage.getHardwareBuffer()) == null) {
            return null;
        }
        Image image = this.m_image0;
        if (image != null) {
            Objects.requireNonNull(image.getHardwareBuffer()).close();
            this.m_image0.close();
        }
        this.m_image0 = this.m_image1;
        this.m_image1 = acquireLatestImage;
        return hardwareBuffer;
    }

    public void SetVolume(float f) {
        if (this.m_player.isCommandAvailable(Player.COMMAND_SET_VOLUME)) {
            this.m_player.setVolume(f);
        }
    }

    public void Play() {
        if (this.m_player.isCommandAvailable(Player.COMMAND_PLAY_PAUSE)) {
            this.m_player.play();
        }
    }

    public void Pause() {
        if (this.m_player.isCommandAvailable(Player.COMMAND_PLAY_PAUSE)) {
            this.m_player.pause();
        }
    }

    public void Seek(long j) {
        if (this.m_player.isCommandAvailable(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)) {
            this.m_player.seekTo(j);
        }
    }
}