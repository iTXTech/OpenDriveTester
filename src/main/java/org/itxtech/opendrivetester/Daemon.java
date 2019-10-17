package org.itxtech.opendrivetester;

import java.io.File;
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
public class Daemon implements Runnable {
    public static final int TYPE_WRITE = 0;
    public static final int TYPE_VERIFY = 1;

    private static class Handler extends Odtd.VerificationHandler {
        long errorBytes = 0;

        @Override
        public void error(long pos, byte correctValue, byte invalidValue, File file) {
            Main.print("");
            Main.print("Wrong value at " + pos + ", got " + correctValue + " should be " + correctValue +
                    " in " + file.getAbsolutePath());
            errorBytes++;
        }
    }

    private Odtd currentOdtd;
    private boolean shutdown = false;
    private long startTime = System.currentTimeMillis();
    private long totalSpace;
    private long passed = 0;
    private long lastTime;
    private long lastSize;
    private Handler handler = new Handler();

    private int type;

    public Daemon(int type) {
        this.type = type;
    }

    public void setTotalSpace(long totalSpace) {
        this.totalSpace = totalSpace;
    }

    public void shutdown() {
        shutdown = true;
    }

    public void setCurrentOdtd(Odtd currentOdtd) {
        if (this.currentOdtd != null) {
            this.passed += this.currentOdtd.getCurrentSize();
        }
        this.currentOdtd = currentOdtd;
        this.currentOdtd.setVerificationHandler(handler);
        this.lastTime = System.currentTimeMillis();
        this.lastSize = currentOdtd.getCurrentSize();
    }

    @Override
    public void run() {
        switch (type) {
            case TYPE_WRITE:
                Main.print("Current \tAverage \tWritten \tRemaining \tETA");
                break;
            case TYPE_VERIFY:
                Main.print("Current \tAverage \tPassed  \tRemaining \tETA");
                break;
        }
        while (!shutdown) {
            if (currentOdtd != null) {
                var currentSize = currentOdtd.getCurrentSize();
                var currentTime = System.currentTimeMillis();
                var total = passed + currentSize;
                var remaining = (totalSpace - total) / 1024;
                var average = total / Math.max(1, currentTime - startTime);
                average = (average > 0) ? average : 1;
                var timeDiff = currentTime - lastTime;
                if (timeDiff == 0) {
                    timeDiff = Integer.MAX_VALUE;
                }
                System.out.print("\r" + byteToMb((currentSize - lastSize) / timeDiff) + "/s\t" +
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
        switch (type) {
            case TYPE_WRITE:
                Main.print("Write completed.");
                break;
            case TYPE_VERIFY:
                Main.print("Verification completed.");
                Main.print("Error Bytes: " + handler.errorBytes + "/" + totalSpace);
                Main.print("Yield: " + new BigDecimal(Long.valueOf(totalSpace - handler.errorBytes).doubleValue() /
                        Long.valueOf(totalSpace).doubleValue() * 100).setScale(2, RoundingMode.DOWN) + "%");
                break;
        }
    }

    private static String byteToMb(long b) {
        return new BigDecimal((double) b / 1024).setScale(2, RoundingMode.HALF_UP) + "MB";
    }
}
