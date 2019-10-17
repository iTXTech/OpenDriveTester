# iTXTech OpenDriveTester

Open Source Drive Tester for UFDs and SSDs.

## Introduction

* Use Java Random to generate data
* Unique `ODTD` file format which supports both fixed seed and random seed
* Fixed seed is always `2019070920000531L`
* Disable system write and read cache when testing

## Usage

```
X:\> java -jar .\OpenDriveTester-1.0-SNAPSHOT.jar
usage: opendrivetester
 -a,--write-and-verify <drive>   Write and verify the specified drive
 -f,--fixed-seed                 Use fixed seed
 -l,--list-drive                 List all drives
 -o,--overwrite                  Overwrite existing ODTD files
 -r,--loop <loops>               Write and verify automatically for
                                 specified loops
 -v,--verify <drive>             Verify the specified drive
 -w,--write <drive>              Write to the specified drive

X:\> java -jar .\OpenDriveTester-1.0-SNAPSHOT.jar -a d
```

## License

    Copyright (C) 2019 iTX Technologies

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
