package org.itxtech.opendrivetester.daemon;

import org.itxtech.opendrivetester.Odtd;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
public class WriteDaemon implements Runnable {
    private Odtd currentOdtd;
    private boolean shutdown = false;
    private long startTime = System.currentTimeMillis();
    private long totalSpace;
    private long freeSpace;
    private long lastTime;
    private long lastSize;

    public WriteDaemon() {
    }

    public void setTotalSpace(long totalSpace) {
        this.totalSpace = totalSpace;
    }

    public void setFreeSpace(long freeSpace) {
        this.freeSpace = freeSpace;
    }

    public void shutdown() {
        shutdown = true;
    }

    public void setCurrentOdtd(Odtd currentOdtd) {
        this.currentOdtd = currentOdtd;
        this.lastTime = System.currentTimeMillis();
        this.lastSize = currentOdtd.getCurrentSize();
    }

    @Override
    public void run() {
        while (!shutdown) {
            if (currentOdtd != null) {
                var currentSize = currentOdtd.getCurrentSize();
                var currentTime = System.currentTimeMillis();
                var speed = (currentSize - lastSize) / (currentTime - lastTime);
                System.out.print("\r" + byteToMb(speed));
            }
            try {
                Thread.sleep(200);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String byteToMb(long b) {
        return new BigDecimal((double) b / 1024).setScale(2, RoundingMode.HALF_UP) + "MB";
    }
}
