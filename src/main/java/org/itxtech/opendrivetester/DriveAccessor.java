package org.itxtech.opendrivetester;

import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;

import java.io.File;
import java.io.IOException;

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
public abstract class DriveAccessor {
    protected SystemInfo info = new SystemInfo();
    protected String drive;
    protected File file;

    public DriveAccessor(String drive) throws IOException {
        this(drive, true);
    }

    public DriveAccessor(String drive, boolean printInfo) throws IOException {
        HWDiskStore diskStore = null;
        HWPartition part = null;
        for (var disk : info.getHardware().getDiskStores()) {
            for (var partition : disk.getPartitions()) {
                if (partition.getMountPoint().toLowerCase().startsWith(drive.toLowerCase())) {
                    part = partition;
                    diskStore = disk;
                    this.drive = part.getMountPoint();
                    file = new File(this.drive + File.separator);
                }
            }
        }
        if (printInfo) {
            if (part == null) {
                throw new IOException("Drive not found: " + drive);
            } else {
                Main.print("Drive Name: \t" + diskStore.getModel());
                Main.print("Serial: \t" + diskStore.getSerial());
                Main.print("Total Space: \t" + Main.byteToReadable(Long.valueOf(part.getSize()).doubleValue()));
                Main.print("Free Space: \t" + Main.byteToReadable(Long.valueOf(getFreeSpace()).doubleValue()));
                for (var store : info.getOperatingSystem().getFileSystem().getFileStores()) {
                    if (store.getMount().toLowerCase().startsWith(drive.toLowerCase())) {
                        Main.print("Description: \t" + store.getDescription());
                        break;
                    }
                }
                Main.print("");
            }
        }
    }

    protected long getFreeSpace() {
        return file.getFreeSpace();
    }
}
