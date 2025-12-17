package com.AduProjects.FileMover.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.LongConsumer;

public class ProgressTrackingInputStream extends InputStream {

    private final InputStream wrapped;
    private final LongConsumer progressCallback;
    private long totalRead = 0;

    public ProgressTrackingInputStream(InputStream wrapped, LongConsumer progressCallback) {
        this.wrapped = wrapped;
        this.progressCallback = progressCallback;
    }

    @Override
    public int read() throws IOException {
        int data = wrapped.read();
        if (data != -1) {
            totalRead++;
            progressCallback.accept(totalRead);
        }
        return data;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int bytesRead = wrapped.read(b, off, len);
        if (bytesRead > 0) {
            totalRead += bytesRead;
            progressCallback.accept(totalRead);
        }
        return bytesRead;
    }

    @Override
    public void close() throws IOException {
        wrapped.close();
        super.close();
    }
}
