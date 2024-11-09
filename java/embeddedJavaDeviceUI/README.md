# EmbeddedJavaDemo menu application

This application is an example of how to use tcMenu's EmbedControl to produce a basic user interface that would run on an embedded device such as a raspberry PI. The application is composed of objects that are wired together in the main class. We also include a really cut down dependency framework that is fully JPMS compliant and very small. Should you wish to, you can use our very light weight database objects that are also used within tcMenu itself.

## How the app is organised.

The application is split up into several files:

* `EmbeddedJavaDemoApp` - this is the class that starts up your application and initialises any plugins you selected. You should not touch this class as it is overwritten every time around.
* `EmbeddedJavaDemoMenu` - this is the class that holds all the menu definitions and the menu tree. It is available in the spring context, and you can inject it into any of your components that need it. It also has short-cut methods to get every menu item.
* `EmbeddedJavaDemoController` - this is where any callbacks that you register go, at the moment we support only one controller, in future we may provide support for more than one. Each function callback that you declare in TcMenu Designer will turn into a method in here. This also allows you to listen for any menu item, and for start and stop events. Further, you can change the controller's constructor to include other components if needed.

## Building the app

By default, the app uses maven to build, you'll need a couple of things installed to continue:

* Java - Get the most recent version for your platform, the platform must support JavaFX if you're using the UI components, this is nearly all distributions of Linux I've seen, macOS and Windows.
* A recent maven 3 installation. Maven is a very complete build tool and [you can read more about it here](https://maven.apache.org/guides/getting-started/).
* A Java IDE - we recommend IntelliJ, but have tried the project in Visual-Studio-Code too. Eclipse similarly should work very well with this project.
* To build from the command line ensure you are in the same directory as this README file and type `mvn clean install`, which will build the application and bring down any dependencies.

## Running the application from the CLI 
 
If you use the standard maven setup, after running the above build steps, you should see the following directory has been created: `target/jfx/` containing an `app` directory and a `deps` directory. We recommend running the application from the `target/jfx/app` directory.

If you used a modular build (IE you have a `module-info.java` file in the `src/main/java` directory) then to run the application ensure that the right version of Java using `java -version` is on your path and then the run command should be `java --module-path ../deps "-Dprism.lcdtext=false" --add-modules com.thecoderscorner.menuexample.embeddedjavademo com.thecoderscorner.menu.devicedemo.EmbeddedJavaDemoApp`

## JavaFx local UI

In the optional folder, there's a couple of classes that will generate a JavaFX UI automatically for your UI. These are documented within the folder.

## Web server and TagVal server

There's both a webserver that serves up [EmbedControlJS](https://github.com/TcMenu/embedcontrolJS) to a browser, and also TagVal support too, so you connect to it from the API.
