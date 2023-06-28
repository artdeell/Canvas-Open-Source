package org.fmod;

import android.media.MediaCrypto;
import android.media.MediaDataSource;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;

public class MediaCodec {
    private int mChannelCount = 0;
    /* access modifiers changed from: private */
    public long mCodecPtr = 0;
    private int mCurrentOutputBufferIndex = -1;
    private Object mDataSourceProxy = null;
    private android.media.MediaCodec mDecoder = null;
    private MediaExtractor mExtractor = null;
    private ByteBuffer[] mInputBuffers = null;
    private boolean mInputFinished = false;
    private long mLength = 0;
    private ByteBuffer[] mOutputBuffers = null;
    private boolean mOutputFinished = false;
    private int mSampleRate = 0;

    /* access modifiers changed from: private */
    public static native long fmodGetSize(long j);

    /* access modifiers changed from: private */
    public static native int fmodReadAt(long j, long j2, byte[] bArr, int i, int i2);

    public long getLength() {
        return this.mLength;
    }

    public int getSampleRate() {
        return this.mSampleRate;
    }

    public int getChannelCount() {
        return this.mChannelCount;
    }

    public boolean init(long j) {
        this.mCodecPtr = j;
        int i = 0;
        if (Build.VERSION.SDK_INT < 17) {
            Log.w("fmod", "MediaCodec::init : MediaCodec unavailable, ensure device is running at least 4.2 (JellyBean).\n");
            return false;
        }
        if (Build.VERSION.SDK_INT < 23) {
            try {
                Class<?> cls = Class.forName("android.media.DataSource");
                Method method = Class.forName("android.media.MediaExtractor").getMethod("setDataSource", new Class[]{cls});
                this.mExtractor = new MediaExtractor();
                Object newProxyInstance = Proxy.newProxyInstance(cls.getClassLoader(), new Class[]{cls}, new InvocationHandler() {
                    public Object invoke(Object obj, Method method, Object[] objArr) {
                        if (method.getName().equals("readAt")) {
                            return Integer.valueOf(MediaCodec.fmodReadAt(MediaCodec.this.mCodecPtr, (long)objArr[0], (byte[])objArr[1], 0, (int)objArr[2]));
                        }
                        if (method.getName().equals("getSize")) {
                            return Long.valueOf(MediaCodec.fmodGetSize(MediaCodec.this.mCodecPtr));
                        }
                        if (method.getName().equals("close")) {
                            return null;
                        }
                        Log.w("fmod", "MediaCodec::DataSource::invoke : Unrecognised method found: " + method.getName());
                        return null;
                    }
                });
                this.mDataSourceProxy = newProxyInstance;
                method.invoke(this.mExtractor, new Object[]{newProxyInstance});
            } catch (ClassNotFoundException e) {
                Log.w("fmod", "MediaCodec::init : " + e.toString());
                return false;
            } catch (NoSuchMethodException e2) {
                Log.w("fmod", "MediaCodec::init : " + e2.toString());
                return false;
            } catch (IllegalAccessException e3) {
                Log.e("fmod", "MediaCodec::init : " + e3.toString());
                return false;
            } catch (InvocationTargetException e4) {
                Log.e("fmod", "MediaCodec::init : " + e4.toString());
                return false;
            }
        } else {
            try {
                MediaExtractor mediaExtractor = new MediaExtractor();
                this.mExtractor = mediaExtractor;
                mediaExtractor.setDataSource(new MediaDataSource() {
                    public void close() {
                    }

                    public int readAt(long j, byte[] bArr, int i, int i2) {
                        return MediaCodec.fmodReadAt(MediaCodec.this.mCodecPtr, j, bArr, i, i2);
                    }

                    public long getSize() {
                        return MediaCodec.fmodGetSize(MediaCodec.this.mCodecPtr);
                    }
                });
            } catch (IOException e5) {
                Log.w("fmod", "MediaCodec::init : " + e5.toString());
                return false;
            }
        }
        int trackCount = this.mExtractor.getTrackCount();
        int i2 = 0;
        while (i2 < trackCount) {
            MediaFormat trackFormat = this.mExtractor.getTrackFormat(i2);
            String string = trackFormat.getString("mime");
            Log.d("fmod", "MediaCodec::init : Format " + i2 + " / " + trackCount + " -- " + trackFormat);
            if (string.equals("audio/mp4a-latm")) {
                try {
                    this.mDecoder = android.media.MediaCodec.createDecoderByType(string);
                    this.mExtractor.selectTrack(i2);
                    this.mDecoder.configure(trackFormat, (Surface) null, (MediaCrypto) null, 0);
                    this.mDecoder.start();
                    this.mInputBuffers = this.mDecoder.getInputBuffers();
                    this.mOutputBuffers = this.mDecoder.getOutputBuffers();
                    int integer = trackFormat.containsKey("encoder-delay") ? trackFormat.getInteger("encoder-delay") : 0;
                    if (trackFormat.containsKey("encoder-padding")) {
                        i = trackFormat.getInteger("encoder-padding");
                    }
                    long j2 = trackFormat.getLong("durationUs");
                    this.mChannelCount = trackFormat.getInteger("channel-count");
                    int integer2 = trackFormat.getInteger("sample-rate");
                    this.mSampleRate = integer2;
                    this.mLength = (long) ((((int) (((j2 * ((long) integer2)) + 999999) / 1000000)) - integer) - i);
                    return true;
                } catch (IOException e6) {
                    Log.e("fmod", "MediaCodec::init : " + e6.toString());
                    return false;
                }
            } else {
                i2++;
            }
        }
        return false;
    }

    public void release() {
        android.media.MediaCodec mediaCodec = this.mDecoder;
        if (mediaCodec != null) {
            mediaCodec.stop();
            this.mDecoder.release();
            this.mDecoder = null;
        }
        MediaExtractor mediaExtractor = this.mExtractor;
        if (mediaExtractor != null) {
            mediaExtractor.release();
            this.mExtractor = null;
        }
    }

    public int read(byte[] bArr, int i) {
        int dequeueInputBuffer;
        int i2 = (!this.mInputFinished || !this.mOutputFinished || this.mCurrentOutputBufferIndex != -1) ? 0 : -1;
        while (!this.mInputFinished && (dequeueInputBuffer = this.mDecoder.dequeueInputBuffer(0)) >= 0) {
            int readSampleData = this.mExtractor.readSampleData(this.mInputBuffers[dequeueInputBuffer], 0);
            if (readSampleData >= 0) {
                this.mDecoder.queueInputBuffer(dequeueInputBuffer, 0, readSampleData, this.mExtractor.getSampleTime(), 0);
                this.mExtractor.advance();
            } else {
                this.mDecoder.queueInputBuffer(dequeueInputBuffer, 0, 0, 0, 4);
                this.mInputFinished = true;
            }
        }
        if (!this.mOutputFinished && this.mCurrentOutputBufferIndex == -1) {
            android.media.MediaCodec.BufferInfo bufferInfo = new android.media.MediaCodec.BufferInfo();
            int dequeueOutputBuffer = this.mDecoder.dequeueOutputBuffer(bufferInfo, 10000);
            if (dequeueOutputBuffer >= 0) {
                this.mCurrentOutputBufferIndex = dequeueOutputBuffer;
                this.mOutputBuffers[dequeueOutputBuffer].limit(bufferInfo.size);
                this.mOutputBuffers[dequeueOutputBuffer].position(bufferInfo.offset);
            } else if (dequeueOutputBuffer == -3) {
                this.mOutputBuffers = this.mDecoder.getOutputBuffers();
            } else if (dequeueOutputBuffer == -2) {
                Log.d("fmod", "MediaCodec::read : MediaCodec::dequeueOutputBuffer returned MediaCodec.INFO_OUTPUT_FORMAT_CHANGED " + this.mDecoder.getOutputFormat());
            } else if (dequeueOutputBuffer == -1) {
                Log.d("fmod", "MediaCodec::read : MediaCodec::dequeueOutputBuffer returned MediaCodec.INFO_TRY_AGAIN_LATER.");
            } else {
                Log.w("fmod", "MediaCodec::read : MediaCodec::dequeueOutputBuffer returned " + dequeueOutputBuffer);
            }
            if ((bufferInfo.flags & 4) != 0) {
                this.mOutputFinished = true;
            }
        }
        int i3 = this.mCurrentOutputBufferIndex;
        if (i3 == -1) {
            return i2;
        }
        ByteBuffer byteBuffer = this.mOutputBuffers[i3];
        int min = Math.min(byteBuffer.remaining(), i);
        byteBuffer.get(bArr, 0, min);
        if (!byteBuffer.hasRemaining()) {
            byteBuffer.clear();
            this.mDecoder.releaseOutputBuffer(this.mCurrentOutputBufferIndex, false);
            this.mCurrentOutputBufferIndex = -1;
        }
        return min;
    }

    public void seek(int i) {
        int i2 = this.mCurrentOutputBufferIndex;
        if (i2 != -1) {
            this.mOutputBuffers[i2].clear();
            this.mCurrentOutputBufferIndex = -1;
        }
        this.mInputFinished = false;
        this.mOutputFinished = false;
        this.mDecoder.flush();
        long j = (long) i;
        this.mExtractor.seekTo((j * 1000000) / ((long) this.mSampleRate), MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
        long sampleTime = ((this.mExtractor.getSampleTime() * ((long) this.mSampleRate)) + 999999) / 1000000;
        int i3 = (int) ((j - sampleTime) * ((long) this.mChannelCount) * 2);
        if (i3 < 0) {
            Log.w("fmod", "MediaCodec::seek : Seek to " + i + " resulted in position " + sampleTime);
            return;
        }
        byte[] bArr = new byte[1024];
        while (i3 > 0) {
            i3 -= read(bArr, Math.min(1024, i3));
        }
    }
}
