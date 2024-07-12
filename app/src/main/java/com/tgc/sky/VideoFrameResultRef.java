package com.tgc.sky;

import java.nio.ByteBuffer;

public class VideoFrameResultRef {
    public boolean isBufferChange = true;
    public int outBytesPerRow;
    public ByteBuffer returnByteBuffer;
    VideoFrameResultRef() {
    }

}
