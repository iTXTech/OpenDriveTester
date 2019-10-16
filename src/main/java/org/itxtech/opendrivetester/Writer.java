package org.itxtech.opendrivetester;

import org.itxtech.opendrivetester.daemon.WriteDaemon;
import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;

import java.io.File;

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
public class Writer {
    public static final int RESERVED_SPACE = 1024;

    private SystemInfo info = new SystemInfo();
    private String drive;
    private long totalSpace;

    public Writer(String drive) {
        this(drive, true);
    }

    public Writer(String drive, boolean printInfo) {
        HWDiskStore diskStore = null;
        HWPartition part = null;
        for (var disk : info.getHardware().getDiskStores()) {
            for (var partition : disk.getPartitions()) {
                if (partition.getMountPoint().toLowerCase().startsWith(drive.toLowerCase())) {
                    part = partition;
                    diskStore = disk;
                    this.drive = part.getMountPoint();
                    this.totalSpace = part.getSize();
                }
            }
        }
        if (printInfo) {
            if (part == null) {
                Main.print("Drive not found: " + drive);
            } else {
                Main.print("Drive Name: \t" + diskStore.getModel());
                Main.print("Serial: \t" + diskStore.getSerial());
                Main.print("Total Space: \t" + Main.byteToReadable(Long.valueOf(part.getSize()).doubleValue()));
                for (var store : info.getOperatingSystem().getFileSystem().getFileStores()) {
                    if (store.getMount().toLowerCase().startsWith(drive.toLowerCase())) {
                        Main.print("Free Space: \t" + Main.byteToReadable(Long.valueOf(store.getFreeSpace()).doubleValue()));
                        Main.print("Description: \t" + store.getDescription());
                        break;
                    }
                }
                Main.print("");
            }
        }
    }

    private long getFreeSpace() {
        for (var store : info.getOperatingSystem().getFileSystem().getFileStores()) {
            if (store.getMount().toLowerCase().startsWith(drive.toLowerCase())) {
                return store.getFreeSpace();
            }
        }
        return 0;
    }

    public void check() {
        try {
            var data = new Odtd(new File(drive + File.separator + "1.odtd"));
            data.check();
        } catch (Exception e) {
            Main.print(e.getMessage());
        }
    }

    public void write(WriteDaemon daemon, boolean fixed) {
        var i = 0;
        try {
            long freeSpace = getFreeSpace();
            if (daemon != null) {
                daemon.setTotalSpace(totalSpace);
                daemon.setFreeSpace(freeSpace);
                var thread = new Thread(daemon);
                thread.start();
            }
            while ((freeSpace = getFreeSpace()) > RESERVED_SPACE) {
                Odtd odtd;
                var file = new File(drive + File.separator + (i++) + ".odtd");
                if (freeSpace < Odtd.DEFAULT_SIZE) {
                    odtd = new Odtd(file, freeSpace - RESERVED_SPACE);
                } else {
                    odtd = new Odtd(file);
                }
                if (daemon != null) {
                    daemon.setCurrentOdtd(odtd);
                }
                if (fixed) {
                    odtd.write(true);
                } else {
                    odtd.writeHeader();
                    odtd.write(false);
                    odtd.completeWrite();
                }
            }
            if (daemon != null) {
                daemon.shutdown();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
