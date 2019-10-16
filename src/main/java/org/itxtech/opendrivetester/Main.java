package org.itxtech.opendrivetester;

import org.apache.commons.cli.*;
import org.itxtech.opendrivetester.daemon.WriteDaemon;
import oshi.SystemInfo;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Properties;

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
public class Main {
    public static final String VERSION = "1.0-SNAPSHOT";

    public static void main(String[] args) {
        var properties = new Properties();
        try {
            properties.load(Main.class.getClassLoader().getResourceAsStream("git.properties"));
        } catch (IOException ignored) {
        }
        print("iTXTech OpenDriveTester version " + VERSION);
        print("Revision: " + properties.getProperty("git.commit.id", "Unknown"));
        print("Built on " + properties.getProperty("git.build.time", "Unknown"));
        print("Copyright (C) 2019 iTX Technologies");
        print("https://github.com/iTXTech/OpenDriveTester");
        print("");

        var options = new Options();
        var group = new OptionGroup();
        var write = new Option("w", "write", true, "Write to specified drive");
        write.setArgName("drive");
        group.addOption(write);

        var verify = new Option("v", "verify", true, "Verify the specified drive");
        verify.setArgName("drive");
        group.addOption(verify);

        var writeAndVerify = new Option("a", "write-and-verify", true, "Write and verify the specified drive");
        writeAndVerify.setArgName("drive");
        group.addOption(writeAndVerify);

        var listDrive = new Option("l", "list-drive", false, "List all drives");
        group.addOption(listDrive);

        group.setRequired(true);
        options.addOptionGroup(group);

        try {
            processCmd(new DefaultParser().parse(options, args));
        } catch (ParseException e) {
            print(e.getMessage());
            new HelpFormatter().printHelp("opendrivetester", options);
        }
    }

    private static void processCmd(CommandLine cmd) {
        if (cmd.hasOption("l")) {
            for (var disk : new SystemInfo().getHardware().getDiskStores()) {
                for (var partition : disk.getPartitions()) {
                    if (!partition.getMountPoint().equals("")) {
                        print(partition.getMountPoint() + "\t" + disk.getModel() + "\t" + disk.getSerial() + "\t" +
                                byteToReadable(Long.valueOf(disk.getSize()).doubleValue()));
                    }
                }
            }
        }
        if (cmd.hasOption("w")) {
            writeDrive(cmd.getOptionValue("w"));
        }
    }

    private static void writeDrive(String drive) {
        var writer = new Writer(drive);
        writer.write(new WriteDaemon());
    }

    public static void print(String s) {
        System.out.println(s);
    }

    public static String byteToReadable(Double b) {
        String[] unit = {"B", "KB", "MB", "GB"};
        var i = 0;
        while (b >= 1024 && (unit.length > (i + 1))) {
            b /= 1024;
            i++;
        }
        return new BigDecimal(b).setScale(2, RoundingMode.HALF_UP) + unit[i];
    }
}
