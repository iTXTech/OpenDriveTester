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

    public static final int DEFAULT_SIZE = 1024 * 1024 * 1024;//1GB
    public static final String HEADER = "ODTD1.0";
    public static final int SEED_TIMES = 4;

    private File file;
    private int bufferSize = 65536;
    private int creationTime;
    private int completeTime = 0;
    private long seed = 0;
    private Random random;
    private DataOutputStream os;

    public Odtd(File file) throws Exception {
        this.file = file;
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    public boolean check() throws Exception {
        var is = new DataInputStream(new FileInputStream(file));
        var h = new String(is.readNBytes(HEADER.getBytes().length));
        if (!HEADER.equals(h)) {
            throw new IOException("Invalid header " + h + " in " + file.getAbsolutePath());
        }
        creationTime = is.readInt();
        completeTime = is.readInt();
        for (var i = 0; i < SEED_TIMES; i++) {
            if (seed == 0) {
                seed = is.readLong();
            } else if (seed != is.readLong()) {
                throw new IOException("Invalid ODTD seed in " + file.getAbsolutePath());
            }
        }
        return true;
    }

    public void writeHeader() throws Exception {
        creationTime = (int) System.currentTimeMillis() / 1000;
        seed = System.currentTimeMillis();
        os = new DataOutputStream(new FileOutputStream(file));
        os.write(HEADER.getBytes());
        os.writeInt(creationTime);
        os.writeInt(completeTime);
        for (var i = 0; i < SEED_TIMES; i++) {
            os.writeLong(seed);
        }
    }

    public void completeWrite() throws Exception {
        os.flush();
        os.close();
        completeTime = (int) System.currentTimeMillis() / 1000;
        var raf = new RandomAccessFile(file, "rw");
        raf.seek(HEADER.getBytes().length + 4);
        raf.writeInt(completeTime);
        raf.close();
    }

    public void write() throws Exception {
        random = new Random(seed);
        for (var i = 0; i < DEFAULT_SIZE / bufferSize; i++) {
            byte[] buf = new byte[bufferSize];
            random.nextBytes(buf);
            os.write(buf);
        }
    }
}
