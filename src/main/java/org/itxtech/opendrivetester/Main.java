package org.itxtech.opendrivetester;

import org.apache.commons.cli.*;
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
        var write = new Option("w", "write", true, "Write to the specified drive");
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

        var useFixedSeed = new Option("f", "fixed-seed", false, "Use fixed seed");
        options.addOption(useFixedSeed);

        var overwrite = new Option("o", "overwrite", false, "Overwrite existing ODTD files");
        options.addOption(overwrite);

        var loop = new Option("r", "loop", true, "Write and verify automatically for specified loops");
        loop.setArgName("loops");
        options.addOption(loop);

        group.setRequired(true);
        options.addOptionGroup(group);

        try {
            processCmd(new DefaultParser().parse(options, args));
        } catch (ParseException e) {
            print(e.getMessage());
            new HelpFormatter().printHelp("opendrivetester", options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processCmd(CommandLine cmd) throws Exception {
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
            writeDrive(cmd.getOptionValue("w"), cmd.hasOption("f"), cmd.hasOption("o"));
        }
        if (cmd.hasOption("v")) {
            verifyDrive(cmd.getOptionValue("v"), cmd.hasOption("f"));
        }
        if (cmd.hasOption("a")) {
            writeAndVerify(cmd.getOptionValue("a"), cmd.hasOption("f"), Integer.parseInt(cmd.getOptionValue("r", "1")));
        }
    }

    private static void sleep(int t) {
        try {
            Thread.sleep(t);
        } catch (Exception ignored) {
        }
    }

    private static void writeAndVerify(String drive, boolean fixed, int loops) throws Exception {
        loops = Math.max(loops, 1);
        var writer = new Writer(drive);
        var verifier = new Verifier(drive, false);
        if (verifier.check()) {
            print("Found existing data, start verifying.");
            verifier.verify(new Daemon(Daemon.TYPE_VERIFY), fixed);
            if (loops > 1) {
                verifier.remove();
            }
            loops--;
            sleep(500);
        }
        for (var i = 0; i < loops; i++) {
            print("Loop " + (i + 1));
            writer.write(new Daemon(Daemon.TYPE_WRITE), fixed, false);
            sleep(500);
            verifier.verify(new Daemon(Daemon.TYPE_VERIFY), fixed);
            if (i < (loops - 1)) {
                verifier.remove();
            }
            sleep(500);
        }
    }

    private static void writeDrive(String drive, boolean fixed, boolean overwrite) throws Exception {
        var writer = new Writer(drive);
        writer.write(new Daemon(Daemon.TYPE_WRITE), fixed, overwrite);
    }

    private static void verifyDrive(String drive, boolean fixed) throws Exception {
        var verifier = new Verifier(drive);
        verifier.verify(new Daemon(Daemon.TYPE_VERIFY), fixed);
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

    public static String secToTime(long time) {
        String timeStr;
        if (time <= 0)
            return "00:00";
        else {
            var minute = time / 60;
            if (minute < 60) {
                var second = time % 60;
                timeStr = unitFormat(minute) + ":" + unitFormat(second);
            } else {
                var hour = minute / 60;
                if (hour > 99) {
                    return "99:59:59";
                }
                minute = minute % 60;
                var second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    private static String unitFormat(long i) {
        return (i >= 0 && i < 10) ? ("0" + i) : ("" + i);
    }
}
