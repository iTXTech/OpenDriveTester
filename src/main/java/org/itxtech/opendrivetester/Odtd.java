package org.itxtech.opendrivetester;

import java.io.File;
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
     * 8 Bytes - Random Seed for 10 TIMES
     * 8 Bytes - Data
     */

    public static final int DEFAULT_SIZE = 1024 * 1024 * 1024;//1GB

    public Odtd(File file) {
        if (file.exists()) {

        }
    }
}