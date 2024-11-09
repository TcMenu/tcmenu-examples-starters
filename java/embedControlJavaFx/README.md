## embedCONTROL UI JavaFX version

Contained in this package is the source code for the complete user interface that connects to a remote device running tcMenu, as long as the board is using one of the supported Serial or Network based plugins.

## Customizing the application

It is likely before any public release that you'll want to customize the application. This can easily be acheived by editing the images and forms stored in the `src/main/resources` folder. A few of the most common ones are provided here:

* `version.properties` this contains the name of the application, and the build version and timestamp (the later are set during a maven build).
* `aboutPage.fxml` is the JavaFX form definition for the about window.
* `generalSettings.fxml` is the JavaFX settings form definition, only change this if you also modify the controller accordingly.
* `mainWindow.fxml` is the main window when the app starts
* `fximg/large_icon.png` is the icon drawn by the application forms.
* `fximg/embedCONTROL.ico` is the start up application icon, only used by the packager on Windows
* `fximg/MyIcons.icns` is the start up application icon, only used by the packager on macOS

You can use the EmbedControl icon or logo to show compatibility with TagVal protocol. 

You can also provide your own custom pages, each page must extend from `PanelPresentable` and can be added to the navigation manager, in this case the `JfxNavigationHeader` in `RemoteConnectionPanel` using `navigationManager.addCustomMenuPanel(subMenuCustom, myPanelPresentable)`.

## Developing the App Locally

To develop the app locally we strongly recommend [IntelliJ either community or ultimate](https://www.jetbrains.com/idea/) but any other Java IDE should work as well. Ensure you also install the most recently available Java JDK and if you want to build from the command line, then apache maven too.

From IntelliJ you can just create an empty project in the project root directory, and then from project structure import the maven POM file for this project.

Once the project has fully loaded, you can run the application by opening `EmbedControlApp` and clicking on the run icon.

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
