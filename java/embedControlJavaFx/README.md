## embedCONTROL UI JavaFX version

Contained in this package is the source code for the complete user interface that connects to a remote device running tcMenu, as long as the board is using one of the supported Serial or Network based plugins.

Before releasing a package based on this we recommend that you change the icons and naming, especially if releasing publicly.

## Building a packaged version

The below will produce you a package that can be used on most platforms supported by Java. It will use the [Java SDK jpackage tool](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jpackage.html) to build a native image that can be installed onto a user system. 

### Building the embedCONTROLFx desktop UI for Windows

Ensure you are in the embedCONTROLFx/target directory. This will produce an executable that can be run directly on Windows, depending on the options you chose, it will either generate an MSI installer, self installing EXE, or just an executable.  

    cp classes/fximg/embedCONTROL.ico .

    jpackage --type app-image -n embedCONTROL -p jfx/deps --input jfx/app --resource-dir .\classes\fximg\ --icon embedCONTROL.ico --app-version 4.4.0 --verbose --java-options "-Dprism.lcdtext=false" --add-modules "jdk.crypto.cryptoki" -m com.thecoderscorner.tcmenu.embedcontrolfx/com.thecoderscorner.embedcontrol.jfxapp.EmbedControlApp

### Building the embedCONTROLFx desktop for macOS

Ensure you are in the embedCONTROLFx/target directory. This will produce a disk image in the regular macOS install format. You can also choose to create an install package or just an executable. 

    jpackage -n embedCONTROL -p jfx/deps --input jfx/app --icon ./classes/fximg/MyIcon.icns --vendor TheCodersCorner --app-version 4.4.0 --verbose --license-file ../../LICENSE --java-options "-Dprism.lcdtext=false" --add-modules "jdk.crypto.cryptoki" -m com.thecoderscorner.tcmenu.embedcontrolfx/com.thecoderscorner.embedcontrol.jfxapp.EmbedControlApp

### Building the embedCONTROLFx desktop for Linux

Ensure you are in the embedCONTROLFx/target directory. This will create a native package depending on which Linux you are using. It has been tested on Ubuntu and also ArmArch64 NOMone.

    jpackage -n embedCONTROL -p jfx/deps --input jfx/app --icon ./classes/fximg/large_icon.png --verbose --license-file ../../LICENSE --linux-app-category Utility --linux-menu-group "Utility;" --java-options "-Dprism.lcdtext=false" --app-version 4.4.0  --add-modules "jdk.crypto.cryptoki" -m com.thecoderscorner.tcmenu.embedcontrolfx/com.thecoderscorner.embedcontrol.jfxapp.EmbedControlApp
