package org.itxtech.opendrivetester;

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
public class Verifier extends DriveAccessor {
    public Verifier(String drive) {
        super(drive);
    }

    public Verifier(String drive, boolean printInfo) {
        super(drive, printInfo);
    }

    public void verify(Daemon daemon, boolean fixed) {
        var dir = new File(drive + File.separator);
        var list = dir.listFiles((d, name) -> name.endsWith(".odtd"));
        if (list != null) {
            if (daemon != null) {
                long total = 0;
                for (var file : list) {
                    total += file.length();
                }
                daemon.setFreeSpace(total);
                var thread = new Thread(daemon);
                thread.start();
            }
            try {
                for (var file : list) {
                    var odtd = new Odtd(file);
                    if (daemon != null) {
                        daemon.setCurrentOdtd(odtd);
                    }
                    if (!fixed) {
                        odtd.check();
                    }
                    odtd.verify(fixed);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (daemon != null) {
                daemon.shutdown();
            }
        }
    }
}
