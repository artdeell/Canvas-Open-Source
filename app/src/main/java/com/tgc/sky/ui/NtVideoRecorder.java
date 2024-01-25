package com.tgc.sky.ui;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.view.Surface;
import androidx.core.view.MotionEventCompat;
import com.tgc.sky.GameActivity;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;


public class NtVideoRecorder {
    private static final int COMPRESS_RATIO = 256;
    private static final int IFRAME_INTERVAL = 1;
    private static String MIME_TYPE = "video/avc";
    private static final int TIMEOUT_USEC = 10000;
    private AudioEncodecThread mAudioEncodecThread;
    private VideoEncodecThread mVideoEncodecThread;
    GameActivity m_activity;
    private MediaCodec.BufferInfo m_audioBufferInfo;
    private ByteBuffer m_audioByteBuffer;
    private MediaCodec m_audioCodec;
    private int m_colorFormat;
    private int m_height;
    private int m_maxHandleNum;
    private MediaMuxer m_mediaMuxer;
    private boolean m_mediaMuxerStarted;
    private MediaCodec.BufferInfo m_videoBufferInfo;
    private ByteBuffer m_videoByteBuffer;
    private MediaCodec m_videoCodec;
    private ConcurrentLinkedQueue<int[]> m_videoFreeArray;
    private ConcurrentLinkedQueue<int[]> m_videoHandleArray;
    private int m_width;
    private long pts;
    private boolean videoExit = true;
    private boolean audioExit = true;
    private boolean m_isRecordAudio = true;
    private boolean m_isRecording = false;

    private static boolean isRecognizedFormat(int i) {
        if (i == 39 || i == 2130706688) {
            return true;
        }
        switch (i) {
            case 19:
            case 20:
            case 21:
                return true;
            default:
                return false;
        }
    }

    public static class VideoEncodecThread extends Thread {
        private WeakReference<NtVideoRecorder> encoderWeakReference;
        private boolean mShouldExit;
        private byte[] m_frameData;
        private int m_height;
        private int m_width;
        private MediaMuxer mediaMuxer;
        private boolean recordError;
        private MediaCodec.BufferInfo videoBufferinfo;
        private MediaCodec videoEncodec;
        private AtomicInteger videoTrackIndex = new AtomicInteger(-1);

        public VideoEncodecThread(WeakReference<NtVideoRecorder> weakReference) {
            this.encoderWeakReference = weakReference;
            this.videoEncodec = weakReference.get().m_videoCodec;
            this.videoBufferinfo = weakReference.get().m_videoBufferInfo;
            this.mediaMuxer = weakReference.get().m_mediaMuxer;
            this.videoTrackIndex.set(-1);
            this.m_width = weakReference.get().m_width;
            this.m_height = weakReference.get().m_height;
            int i = weakReference.get().m_colorFormat;
            if (i == 19 || i == 21) {
                this.m_frameData = new byte[((this.m_width * this.m_height) * 3) / 2];
            } else if (i == 15) {
                this.m_frameData = new byte[this.m_width * this.m_height * 4];
            }
        }

        private void performExit() {
            this.videoEncodec.stop();
            this.videoEncodec.release();
            this.videoEncodec = null;
            this.encoderWeakReference.get().videoExit = true;
            this.m_frameData = null;
            synchronized (this.mediaMuxer) {
                try {
                    this.mediaMuxer.stop();
                    this.mediaMuxer.release();
                } catch (IllegalStateException unused) {
                    this.recordError = true;
                }
            }
        }

        private void encodeFrame(int[] iArr) {
            int i = this.encoderWeakReference.get().m_colorFormat;
            if (i == 19) {
                this.encoderWeakReference.get().encodeYUV420P(iArr, this.m_width, this.m_height, this.m_frameData);
            } else if (i == 21) {
                this.encoderWeakReference.get().encodeYUV420SP(iArr, this.m_width, this.m_height, this.m_frameData);
            } else if (i == 15) {
                ByteBuffer.wrap(this.m_frameData).asIntBuffer().put(iArr);
            }
            this.encoderWeakReference.get().quenueFrameData(this.encoderWeakReference.get().m_videoCodec, this.m_frameData);
            Arrays.fill(iArr, 0);
            this.encoderWeakReference.get().m_videoFreeArray.add(iArr);
            this.encoderWeakReference.get().encodeFrameData(this.videoEncodec, this.videoBufferinfo, this.videoTrackIndex);
        }

        @Override
        public void run() {
            super.run();
            this.mShouldExit = false;
            this.recordError = false;
            while (!this.mShouldExit) {
                int[] iArr = (int[]) this.encoderWeakReference.get().m_videoHandleArray.poll();
                if (iArr != null) {
                    encodeFrame(iArr);
                }
                try {
                    sleep(15L);
                } catch (InterruptedException unused) {
                }
            }
            performExit();
        }

        public void exit() {
            this.mShouldExit = true;
        }

        public boolean recordError() {
            return this.recordError;
        }
    }

    public static class AudioEncodecThread extends Thread {
        private MediaCodec.BufferInfo audioBufferinfo;
        private MediaCodec audioEncodec;
        private AtomicInteger audioTrackIndex = new AtomicInteger(-1);
        private WeakReference<NtVideoRecorder> encoderWeakReference;
        private boolean m_shouldExit;
        private MediaMuxer mediaMuxer;
        private boolean recordError;

        public AudioEncodecThread(WeakReference<NtVideoRecorder> weakReference) {
            this.encoderWeakReference = weakReference;
            this.audioEncodec = weakReference.get().m_audioCodec;
            this.audioBufferinfo = weakReference.get().m_audioBufferInfo;
            this.mediaMuxer = weakReference.get().m_mediaMuxer;
            this.audioTrackIndex.set(-1);
        }

        @Override
        public void run() {
            super.run();
            this.m_shouldExit = false;
            this.recordError = false;
            while (!this.m_shouldExit) {
                this.encoderWeakReference.get().encodeFrameData(this.audioEncodec, this.audioBufferinfo, this.audioTrackIndex);
                try {
                    sleep(15L);
                } catch (InterruptedException unused) {
                }
            }
            performExit();
        }

        public void exit() {
            this.m_shouldExit = true;
        }

        public boolean recordError() {
            return this.recordError;
        }

        private void performExit() {
            this.audioEncodec.stop();
            this.audioEncodec.release();
            this.audioEncodec = null;
            this.encoderWeakReference.get().audioExit = true;
            synchronized (this.mediaMuxer) {
                if (this.encoderWeakReference.get().videoExit && this.mediaMuxer != null) {
                    if (this.encoderWeakReference.get().m_mediaMuxerStarted) {
                        try {
                            this.mediaMuxer.stop();
                            this.mediaMuxer.release();
                        } catch (IllegalStateException unused) {
                            this.recordError = true;
                        }
                    } else {
                        this.recordError = true;
                    }
                    this.mediaMuxer = null;
                }
            }
        }
    }

    public NtVideoRecorder(GameActivity gameActivity) {
        this.m_activity = gameActivity;
    }

    private boolean setRecordingDimensions(int i, int i2) {
        if (this.m_isRecording) {
            return false;
        }
        this.m_width = i;
        this.m_height = i2;
        this.m_maxHandleNum = 209715200 / ((i * i2) * 4);
        this.m_videoByteBuffer = ByteBuffer.allocateDirect(i * i2 * 4);
        this.m_videoHandleArray = new ConcurrentLinkedQueue<>();
        this.m_videoFreeArray = new ConcurrentLinkedQueue<>();
        return true;
    }

    public boolean isRecording() {
        return this.m_isRecording;
    }

    private String findCurrentFileName(String str) {
        try {
            return (this.m_activity.getExternalCacheDir() + "/") + new String(str.getBytes(), "UTF-8");
        } catch (UnsupportedEncodingException unused) {
            return "";
        }
    }

    private boolean createOrRecreateFile(String str) {
        try {
            File file = new File(str);
            if (file.exists()) {
                file.delete();
            }
            file.getParentFile().mkdirs();
            file.createNewFile();
            return true;
        } catch (Exception unused) {
            return false;
        }
    }

    private boolean createVideoCodec(String str, int i, int i2) {
        this.m_videoBufferInfo = new MediaCodec.BufferInfo();
        MediaCodecInfo selectCodec = selectCodec(MIME_TYPE);
        if (selectCodec == null) {
            return false;
        }
        this.m_colorFormat = selectColorFormat(selectCodec, MIME_TYPE);
        MediaFormat createVideoFormat = MediaFormat.createVideoFormat(MIME_TYPE, this.m_width, this.m_height);
        createVideoFormat.setInteger("bitrate", i);
        createVideoFormat.setInteger("frame-rate", i2);
        createVideoFormat.setInteger("color-format", this.m_colorFormat);
        createVideoFormat.setInteger("i-frame-interval", 1);
        try {
            this.m_mediaMuxer = new MediaMuxer(str.toString(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            MediaCodec createByCodecName = MediaCodec.createByCodecName(selectCodec.getName());
            this.m_videoCodec = createByCodecName;
            createByCodecName.configure(createVideoFormat, (Surface) null, (MediaCrypto) null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            return true;
        } catch (IOException unused) {
            return false;
        }
    }

    private boolean createAudioCodec() {
        this.m_audioByteBuffer = ByteBuffer.allocateDirect(2048);
        this.m_audioBufferInfo = new MediaCodec.BufferInfo();
        MediaFormat createAudioFormat = MediaFormat.createAudioFormat("audio/mp4a-latm", 24000, 2);
        createAudioFormat.setInteger("bitrate", 768000);
        createAudioFormat.setInteger("aac-profile", 2);
        createAudioFormat.setInteger("max-input-size", 2048);
        try {
            MediaCodec createEncoderByType = MediaCodec.createEncoderByType("audio/mp4a-latm");
            this.m_audioCodec = createEncoderByType;
            createEncoderByType.configure(createAudioFormat, (Surface) null, (MediaCrypto) null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            return true;
        } catch (IOException unused) {
            return false;
        }
    }

    public boolean startRecordingWithFilename(String str, int i, int i2, String str2, int i3, boolean z, int i4, boolean z2) {
        if (this.m_isRecording || str == null || !str.endsWith(".mp4")) {
            return false;
        }
        setRecordingDimensions(i, i2);
        if (i3 <= 0) {
            i3 = ((((this.m_width * this.m_height) * 4) * 8) * i4) / COMPRESS_RATIO;
        }
        String findCurrentFileName = findCurrentFileName(str);
        if (findCurrentFileName == "" || !createOrRecreateFile(findCurrentFileName) || !createVideoCodec(findCurrentFileName, i3, i4)) {
            return false;
        }
        if (this.m_isRecordAudio && !createAudioCodec()) {
            return false;
        }
        this.m_videoCodec.start();
        VideoEncodecThread videoEncodecThread = new VideoEncodecThread(new WeakReference(this));
        this.mVideoEncodecThread = videoEncodecThread;
        videoEncodecThread.start();
        this.videoExit = false;
        if (this.m_isRecordAudio) {
            this.m_audioCodec.start();
            AudioEncodecThread audioEncodecThread = new AudioEncodecThread(new WeakReference(this));
            this.mAudioEncodecThread = audioEncodecThread;
            audioEncodecThread.start();
            this.audioExit = false;
        }
        this.m_mediaMuxerStarted = false;
        this.m_isRecording = true;
        return true;
    }

    public boolean stopRecording() {
        if (!this.m_isRecording) {
            return false;
        }
        this.m_isRecording = false;
        this.mVideoEncodecThread.exit();
        if (this.m_isRecordAudio) {
            this.mAudioEncodecThread.exit();
        }
        while (true) {
            if (!this.videoExit || !this.audioExit) {
                SystemClock.sleep(100L);
            } else {
                this.mVideoEncodecThread = null;
                this.mAudioEncodecThread = null;
                this.m_videoByteBuffer = null;
                this.m_audioByteBuffer = null;
                this.m_videoHandleArray = null;
                this.m_videoFreeArray = null;
                return true;
            }
        }
    }

    public ByteBuffer beginWriteAudioFrame() {
        ByteBuffer byteBuffer;
        if (this.m_isRecording && this.m_isRecordAudio && (byteBuffer = this.m_audioByteBuffer) != null) {
            return byteBuffer;
        }
        return null;
    }

    public void endWriteAudioFrame() {
        if (this.m_isRecording && this.m_isRecordAudio && this.m_audioCodec != null) {
            quenueAudioFrame();
            this.m_audioByteBuffer.clear();
        }
    }

    public void SetRecordAudio(boolean z) {
        this.m_isRecordAudio = z;
    }

    public ByteBuffer beginWriteVideoFrame() {
        ByteBuffer byteBuffer;
        if (this.m_isRecording && (byteBuffer = this.m_videoByteBuffer) != null) {
            return byteBuffer;
        }
        return null;
    }

    public void endWriteVideoFrame() {
        if (this.m_isRecording && this.m_videoCodec != null) {
            quenueVideoFrame();
            this.m_videoByteBuffer.clear();
        }
    }

    public String videoDir() {
        return Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DCIM + File.separator;
    }

    void quenueFrameData(MediaCodec mediaCodec, byte[] bArr) {
        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
        int dequeueInputBuffer = mediaCodec.dequeueInputBuffer(10000L);
        if (dequeueInputBuffer >= 0) {
            ByteBuffer byteBuffer = inputBuffers[dequeueInputBuffer];
            byteBuffer.clear();
            byteBuffer.put(bArr);
            if (this.pts == 0) {
                this.pts = System.nanoTime() / 1000;
            }
            mediaCodec.queueInputBuffer(dequeueInputBuffer, 0, bArr.length, (System.nanoTime() / 1000) - this.pts, 0);
        }
    }

    public void encodeFrameData(MediaCodec mediaCodec, MediaCodec.BufferInfo bufferInfo, AtomicInteger atomicInteger) {
        ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
        int dequeueOutputBuffer = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000L);
        do {
            if (dequeueOutputBuffer != -1) {
                if (dequeueOutputBuffer == -3) {
                    outputBuffers = mediaCodec.getOutputBuffers();
                } else if (dequeueOutputBuffer == -2) {
                    if (bufferInfo.size != 0) {
                        atomicInteger.set(this.m_mediaMuxer.addTrack(mediaCodec.getOutputFormat()));
                        synchronized (this.m_mediaMuxer) {
                            if (!this.m_mediaMuxerStarted && this.mVideoEncodecThread.videoTrackIndex.get() != -1 && (!this.m_isRecordAudio || this.mAudioEncodecThread.audioTrackIndex.get() != -1)) {
                                this.m_mediaMuxer.start();
                                this.m_mediaMuxerStarted = true;
                            }
                        }
                    }
                } else if (dequeueOutputBuffer >= 0) {
                    ByteBuffer byteBuffer = outputBuffers[dequeueOutputBuffer];
                    if (byteBuffer == null) {
                        throw new RuntimeException("encoderOutputBuffer " + dequeueOutputBuffer + " was null");
                    }
                    if ((bufferInfo.flags & 2) != 0) {
                        bufferInfo.size = 0;
                    }
                    if (bufferInfo.size != 0) {
                        if (atomicInteger.get() < 0) {
                            atomicInteger.set(this.m_mediaMuxer.addTrack(mediaCodec.getOutputFormat()));
                        }
                        synchronized (this.m_mediaMuxer) {
                            if (!this.m_mediaMuxerStarted && this.mVideoEncodecThread.videoTrackIndex.get() != -1 && (!this.m_isRecordAudio || this.mAudioEncodecThread.audioTrackIndex.get() != -1)) {
                                this.m_mediaMuxer.start();
                                this.m_mediaMuxerStarted = true;
                            }
                        }
                        if (this.m_mediaMuxerStarted) {
                            byteBuffer.position(bufferInfo.offset);
                            byteBuffer.limit(bufferInfo.offset + bufferInfo.size);
                            this.m_mediaMuxer.writeSampleData(atomicInteger.get(), byteBuffer, bufferInfo);
                        }
                    }
                    mediaCodec.releaseOutputBuffer(dequeueOutputBuffer, false);
                }
            }
            dequeueOutputBuffer = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000L);
        } while (dequeueOutputBuffer >= 0);
    }

    private void quenueHandleData(IntBuffer intBuffer) {
        int[] poll = this.m_videoFreeArray.poll();
        if (poll == null) {
            if (this.m_videoHandleArray.size() < this.m_maxHandleNum) {
                poll = new int[this.m_width * this.m_height];
            } else {
                while (poll == null) {
                    poll = this.m_videoFreeArray.poll();
                }
            }
        }
        intBuffer.get(poll);
        this.m_videoHandleArray.add(poll);
    }

    private void quenueVideoFrame() {
        quenueHandleData(this.m_videoByteBuffer.asIntBuffer());
    }

    private void quenueAudioFrame() {
        byte[] bArr = new byte[this.m_audioByteBuffer.remaining()];
        Arrays.fill(bArr, (byte) 0);
        this.m_audioByteBuffer.get(bArr);
        quenueFrameData(this.m_audioCodec, bArr);
    }

    private static MediaCodecInfo selectCodec(String str) {
        MediaCodecInfo[] codecInfos;
        try {
            for (MediaCodecInfo mediaCodecInfo : new MediaCodecList(1).getCodecInfos()) {
                if (mediaCodecInfo.isEncoder()) {
                    for (String str2 : mediaCodecInfo.getSupportedTypes()) {
                        if (str2.equalsIgnoreCase(str)) {
                            return mediaCodecInfo;
                        }
                    }
                    continue;
                }
            }
            return null;
        } catch (Exception unused) {
            return null;
        }
    }

    private static int selectColorFormat(MediaCodecInfo mediaCodecInfo, String str) {
        if (Build.MODEL.equalsIgnoreCase("Pixel 4") || Build.MODEL.equalsIgnoreCase("SM-S9010") || Build.MODEL.equalsIgnoreCase("SM-S9080")) {
            return 19;
        }
        if (Build.MODEL.equalsIgnoreCase("SM-A125M")) {
            return 15;
        }
        MediaCodecInfo.CodecCapabilities capabilitiesForType = mediaCodecInfo.getCapabilitiesForType(str);
        for (int i = 0; i < capabilitiesForType.colorFormats.length; i++) {
            int i2 = capabilitiesForType.colorFormats[i];
            if (isRecognizedFormat(i2)) {
                if (i2 == 19 || i2 == 20) {
                    return 21;
                }
                return i2;
            }
        }
        return 0;
    }

    public void encodeYUV420SP(int[] iArr, int i, int i2, byte[] bArr) {
        int i3 = i * i2;
        Arrays.fill(bArr, 0, bArr.length, (byte) 0);
        int i4 = 0;
        int i5 = 0;
        for (int i6 = 0; i6 < i2; i6++) {
            int i7 = 0;
            while (i7 < i) {
                int i8 = iArr[i5];
                int i9 = ((-16777216) & i8) >> 24;
                int i10 = (16711680 & i8) >> 16;
                int i11 = (i8 & MotionEventCompat.ACTION_POINTER_INDEX_MASK) >> 8;
                if (i9 < 0) {
                    i9 += 256;
                }
                if (i10 < 0) {
                    i10 += 256;
                }
                if (i11 < 0) {
                    i11 += 256;
                }
                int i12 = (((((i11 * 66) + (i10 * 129)) + (i9 * 25)) + 128) >> 8) + 16;
                int i13 = (((((i11 * (-38)) - (i10 * 74)) + (i9 * 112)) + 128) >> 8) + 128;
                int i14 = (((((i11 * 112) - (i10 * 94)) - (i9 * 18)) + 128) >> 8) + 128;
                int i15 = i4 + 1;
                if (i12 < 0) {
                    i12 = 0;
                } else if (i12 > 255) {
                    i12 = 255;
                }
                bArr[i4] = (byte) i12;
                if (i6 % 2 == 0 && i5 % 2 == 0) {
                    int i16 = i3 + 1;
                    if (i13 < 0) {
                        i13 = 0;
                    } else if (i13 > 255) {
                        i13 = 255;
                    }
                    bArr[i3] = (byte) i13;
                    i3 = i16 + 1;
                    if (i14 < 0) {
                        i14 = 0;
                    } else if (i14 > 255) {
                        i14 = 255;
                    }
                    bArr[i16] = (byte) i14;
                }
                i5++;
                i7++;
                i4 = i15;
            }
        }
    }

    public void encodeYUV420P(int[] iArr, int i, int i2, byte[] bArr) {
        int i3 = i * i2;
        Arrays.fill(bArr, 0, bArr.length, (byte) 0);
        int i4 = (i3 / 4) + i3;
        int i5 = 0;
        int i6 = 0;
        for (int i7 = 0; i7 < i2; i7++) {
            int i8 = 0;
            while (i8 < i) {
                int i9 = iArr[i5];
                int i10 = ((-16777216) & i9) >> 24;
                int i11 = (16711680 & i9) >> 16;
                int i12 = (i9 & MotionEventCompat.ACTION_POINTER_INDEX_MASK) >> 8;
                if (i10 < 0) {
                    i10 += 256;
                }
                if (i11 < 0) {
                    i11 += 256;
                }
                if (i12 < 0) {
                    i12 += 256;
                }
                int i13 = (((((i12 * 66) + (i11 * 129)) + (i10 * 25)) + 128) >> 8) + 16;
                int i14 = (((((i12 * (-38)) - (i11 * 74)) + (i10 * 112)) + 128) >> 8) + 128;
                int i15 = (((((i12 * 112) - (i11 * 94)) - (i10 * 18)) + 128) >> 8) + 128;
                int i16 = i6 + 1;
                if (i13 < 0) {
                    i13 = 0;
                } else if (i13 > 255) {
                    i13 = 255;
                }
                bArr[i6] = (byte) i13;
                if (i7 % 2 == 0 && i5 % 2 == 0) {
                    int i17 = i3 + 1;
                    if (i14 < 0) {
                        i14 = 0;
                    } else if (i14 > 255) {
                        i14 = 255;
                    }
                    bArr[i3] = (byte) i14;
                    int i18 = i4 + 1;
                    if (i15 < 0) {
                        i15 = 0;
                    } else if (i15 > 255) {
                        i15 = 255;
                    }
                    bArr[i4] = (byte) i15;
                    i4 = i18;
                    i3 = i17;
                }
                i5++;
                i8++;
                i6 = i16;
            }
        }
    }
}