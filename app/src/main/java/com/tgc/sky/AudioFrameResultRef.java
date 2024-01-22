package com.tgc.sky;

import java.nio.ByteBuffer;

class AudioFrameResultRef {
    public boolean isBufferChange = true;
    public int outBytesPerRow;
    public ByteBuffer returnByteBuffer;
}