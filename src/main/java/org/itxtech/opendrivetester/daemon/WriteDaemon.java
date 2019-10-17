package org.itxtech.opendrivetester.daemon;

import org.itxtech.opendrivetester.Main;
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
    private long freeSpace;
    private long written = 0;
    private long lastTime;
    private long lastSize;

    public void setFreeSpace(long freeSpace) {
        this.freeSpace = freeSpace;
    }

    public void shutdown() {
        shutdown = true;
    }

    public void setCurrentOdtd(Odtd currentOdtd) {
        if (this.currentOdtd != null) {
            this.written += this.currentOdtd.getCurrentSize();
        }
        this.currentOdtd = currentOdtd;
        this.lastTime = System.currentTimeMillis();
        this.lastSize = currentOdtd.getCurrentSize();
    }

    @Override
    public void run() {
        Main.print("Current \tAverage \tWritten \tRemaining \tETA");
        while (!shutdown) {
            if (currentOdtd != null) {
                var currentSize = currentOdtd.getCurrentSize();
                var currentTime = System.currentTimeMillis();
                var total = written + currentSize;
                var remaining = (freeSpace - total) / 1024;
                var average = total / (currentTime - startTime);
                System.out.print("\r" + byteToMb((currentSize - lastSize) / (currentTime - lastTime)) + "/s\t" +
                        byteToMb(average) + "/s\t" +
                        byteToMb(total / 1024) + "\t" + byteToMb(remaining) + "\t" +
                        Main.secToTime(remaining / average));
            }
            try {
                Thread.sleep(200);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Main.print("");
        Main.print("Write completed.");
    }

    private static String byteToMb(long b) {
        return new BigDecimal((double) b / 1024).setScale(2, RoundingMode.HALF_UP) + "MB";
    }
}
