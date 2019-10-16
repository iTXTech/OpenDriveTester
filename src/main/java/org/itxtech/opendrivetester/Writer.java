package org.itxtech.opendrivetester;

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

    public void write() {
        var i = 0;
        try {
            while (getFreeSpace() > RESERVED_SPACE) {
                var odtd = new Odtd(new File(drive + File.separator + (i++) + ".odtd"));
                odtd.writeHeader();
                odtd.write();
                odtd.completeWrite();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
