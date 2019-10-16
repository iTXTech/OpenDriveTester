package org.itxtech.opendrivetester;

import java.io.*;
import java.util.Random;

/*
 * iTXTech OpenDriveTester
 *
 * Copyright (C) 2019 iTX Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class Odtd {
    /*
     * ODTD - OpenDriveTesterData
     *
     * 8 Bytes - ODTD Header
     * 4 Bytes - Creation time
     * 4 Bytes - Complete time
     * 8 Bytes - Random Seed for 4 times
     * 8 Bytes - Data
     */

    public static final long DEFAULT_SIZE = (long) 2 * 1024 * 1024 * 1024;//2GB
    public static final int DEFAULT_BUFFER_SIZE = 128 * 1024;//128KB
    public static final String HEADER = "ODTD1.0";
    public static final int SEED_TIMES = 4;

    private File file;
    private int bufferSize;
    private long size;
    private long currentSize = 0;
    private int creationTime;
    private int completionTime = 0;
    private long seed = 0;
    private FileOutputStream os;

    public int getCreationTime() {
        return creationTime;
    }

    public int getCompletionTime() {
        return completionTime;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public File getFile() {
        return file;
    }

    public Odtd(File file) throws Exception {
        this(file, DEFAULT_SIZE, DEFAULT_BUFFER_SIZE);
    }

    public Odtd(File file, long size) throws Exception {
        this(file, size, DEFAULT_BUFFER_SIZE);
    }

    public Odtd(File file, long size, int bufferSize) throws Exception {
        this.file = file;
        this.size = size;
        this.bufferSize = bufferSize;
        if (size < 0) {
            throw new Exception("Invalid size");
        }
        if (bufferSize < 0) {
            throw new Exception("Invalid buffer size");
        }
        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("Cannot create file " + file.getAbsolutePath());
        }
    }

    public boolean check() throws Exception {
        var is = new DataInputStream(new FileInputStream(file));
        var h = new String(is.readNBytes(HEADER.getBytes().length));
        if (!HEADER.equals(h)) {
            throw new IOException("Invalid header " + h + " in " + file.getAbsolutePath());
        }
        creationTime = is.readInt();
        completionTime = is.readInt();
        for (var i = 0; i < SEED_TIMES; i++) {
            if (seed == 0) {
                seed = is.readLong();
            } else if (seed != is.readLong()) {
                throw new IOException("Invalid ODTD seed in " + file.getAbsolutePath());
            }
        }
        return true;
    }

    public boolean verify() {
        return true;
    }

    public void writeHeader() throws Exception {
        creationTime = (int) System.currentTimeMillis() / 1000;
        seed = System.currentTimeMillis();
        os = new FileOutputStream(file);
        os.write(HEADER.getBytes());
        os.write(writeInt(creationTime));
        os.write(writeInt(completionTime));
        for (var i = 0; i < SEED_TIMES; i++) {
            os.write(writeLong(seed));
        }
    }

    public void write() throws Exception {
        var random = new Random(seed);
        for (var i = 0; i < size / bufferSize; i++) {
            byte[] buf = new byte[bufferSize];
            random.nextBytes(buf);
            os.write(buf);
            currentSize += bufferSize;
        }
    }

    public void completeWrite() throws Exception {
        os.flush();
        os.close();
        completionTime = (int) System.currentTimeMillis() / 1000;
        var raf = new RandomAccessFile(file, "rw");
        raf.seek(HEADER.getBytes().length + 4);
        raf.writeInt(completionTime);
        raf.close();
    }

    private static byte[] writeInt(int v) {
        byte[] buf = new byte[4];
        buf[0] = (byte) ((v >>> 24) & 0xFF);
        buf[1] = (byte) ((v >>> 16) & 0xFF);
        buf[2] = (byte) ((v >>> 8) & 0xFF);
        buf[3] = (byte) ((v >>> 0) & 0xFF);
        return buf;
    }

    private static byte[] writeLong(long v) {
        byte[] buf = new byte[8];
        buf[0] = (byte) (v >>> 56);
        buf[1] = (byte) (v >>> 48);
        buf[2] = (byte) (v >>> 40);
        buf[3] = (byte) (v >>> 32);
        buf[4] = (byte) (v >>> 24);
        buf[5] = (byte) (v >>> 16);
        buf[6] = (byte) (v >>> 8);
        buf[7] = (byte) (v >>> 0);
        return buf;
    }
}
