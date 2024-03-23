# file-server-ui

A client frontend for [ploiu/file_server](https://github.com/ploiu/file_server), written in java

## Building

1. make sure you have the server certificate in the same directory as this README, and make sure it's
   named `file_server_cert.pem`
2. run `./gradlew jpackage`

### For Linux

Linux requires extra tools to be installed:

- `binutils`
- `fakeroot`

both can be installed with apt

## Installing

the built installer is located in `./build/file-server-installer`. For linux, you will need to write your own .desktop
file until I can figure out how to get the jlink plugin to work with automatic start menu installation. For windows, you
will need to navigate to `C:\Program Files\ploiu-file-server` and manually add `ploiu-file-server.exe` to the start menu
for the same reason.

Linux desktop file:

```ini
[Desktop Entry]
Type=Application
Encoding=UTF-8
Name=Ploiu File Server
Exec=/opt/ploiu-file-server/bin/ploiu-file-server
Terminal=false
Icon=<project install location>/src/main/resources/assets/img/icon.png
```

## Other

parts of the application icon were created with images on the public domain. I found
it [here](https://www.svgrepo.com/svg/153295/server)
