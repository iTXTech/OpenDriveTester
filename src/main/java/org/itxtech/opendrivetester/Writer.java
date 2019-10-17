package org.itxtech.opendrivetester;

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
public class Writer extends DriveAccessor {
    public static final int RESERVED_SPACE = 512 * 1024;//512KB

    public Writer(String drive) throws IOException {
        super(drive);
    }

    public Writer(String drive, boolean printInfo) throws IOException {
        super(drive, printInfo);
    }

    public void write(Daemon daemon, boolean fixed, boolean overwrite) {
        var i = 0;
        long freeSpace = getFreeSpace();
        if (daemon != null) {
            daemon.setTotalSpace(freeSpace);
            var thread = new Thread(daemon);
            thread.start();
        }
        var end = false;
        try {
            while ((freeSpace = getFreeSpace()) > RESERVED_SPACE && !end && (freeSpace - RESERVED_SPACE) > RESERVED_SPACE) {
                Odtd odtd;
                var file = new File(drive + File.separator + (i++) + ".odtd");
                while (!overwrite && file.exists()) {
                    file = new File(drive + File.separator + (i++) + ".odtd");
                }
                if (freeSpace < Odtd.DEFAULT_SIZE) {
                    end = true;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (daemon != null) {
            daemon.shutdown();
        }
    }
}
