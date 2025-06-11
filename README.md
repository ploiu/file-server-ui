# file-server-ui

A client frontend for [ploiu/file_server](https://github.com/ploiu/file_server), written in java

>[!NOTE]
>This project is archived, a rewrite is in the works and will be published as it becomes more concrete.
>I heavily dislike the javafx framework. It feels like a hack and does a terrible job half-adapting web standards like CSS, along with finicky and hard to read documentation.
>I'd rather use something that doesn't even try to use web standards than something that (frankly) half-asses it

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
