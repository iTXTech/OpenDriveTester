package org.itxtech.opendrivertester;

import org.apache.commons.cli.*;

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
class Main {
    public static void main(String[] args) {
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

        }
    }

    public static void print(String s) {
        System.out.println(s);
    }
}
