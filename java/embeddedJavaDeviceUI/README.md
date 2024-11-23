# EmbeddedJavaDemo menu application

This application is the starting point for building an embedded Java TcMenu application. It is based upon tcMenu's EmbedControl libraries that provide the user interface based on JavaFX. This can run on most Raspberry PI devices and other embedded Linux flavours too. The application as provided has considerable functionality, but you can choose yourself what to keep and what to remove. Embed Control core also includes a really cut down dependency framework that is fully JPMS compliant and very small. Should you wish to, you can use our very light weight database objects that are also used within tcMenu itself. We'll go through each of the components below.

This app framework is somewhat opinionated, but if it doesn't match with what you need, you can always look at the Java API examples.

## How the app is organised.

The application is split up into several files:

### Components required for operation

* `EmbeddedJavaDemoApp` - this is the class that starts up your application and initialises any plugins you selected. This is the starting point and you're free to rearrange as needed. 
* `EmbeddedJavaDemoMenu` - this is the class that holds all the menu definitions and the menu tree. It is available in the spring context, and you can inject it into any of your components that need it. It also has short-cut methods to get every menu item.
* `EmbeddedJavaDemoController` - this is the controller for the menu front-end. Any callbacks that you register in designer will go here, at the moment we support only one controller, in future we may provide support for more than one. Each function callback that you declare in TcMenu Designer will turn into a method in here. This also allows you to listen for any menu item, and for start and stop events. Further, you can change the controller's constructor to include other components if needed. Scroll choice callbacks that request the data for scroll choice items are also added. 
* `MenuConfig` - this is generally used to wire together all of your components. You can create additional components in this class by creating a function annotated with `@TcComponent`, any parameters to the function will be auto-wired using components already available in the context.

### Components that are optional

* `JfxLocalAutoUI` and `LocalTreeComponentManager` provide the local UI, it produces a panel that fills the entire window and represents the menu structure.
* `StatusPanelDrawable` demonstrates how to override drawing with a custom arranged grid for the status submenu. It demonstrates custom drawing too.
* `TcJettyWebServer` and `TcJettyWebSocketEndpoint` both configure and serve up a website that contains EmbedControlJS, which can produce a lightweight remote control application in the browser. 

## Building the app

By default, the app uses maven to build, you'll need a couple of things installed to continue:

* Java - Get the most recent version for your platform, the platform must support JavaFX if you're using the UI components, this is nearly all distributions of Linux I've seen, macOS and Windows.
* A recent maven 3 installation. Maven is a very complete build tool and [you can read more about it here](https://maven.apache.org/guides/getting-started/).
* A Java IDE - we recommend IntelliJ, but have tried the project in Visual-Studio-Code too. Eclipse similarly should work very well with this project.
* To build from the command line ensure you are in the same directory as this README file and type `mvn clean install`, which will build the application and bring down any dependencies.

## Running the application from the CLI 
 
If you use the standard maven setup, after running the above build steps, you should see the following directory has been created: `target/jfx/` containing an `app` directory and a `deps` directory. We recommend running the application from the `target/jfx/app` directory.

If you used a modular build (IE you have a `module-info.java` file in the `src/main/java` directory) then to run the application ensure that the right version of Java using `java -version` is on your path and then the run command should be `java --module-path ../deps "-Dprism.lcdtext=false" --add-modules com.thecoderscorner.menuexample.embeddedjavademo com.thecoderscorner.menu.devicedemo.EmbeddedJavaDemoApp`. Given TcMenu is JPMS compliant, it can be packaged using `jpackage`.

## The simple application context and the MenuConfig class

The menu configuration system within TcMenu allows for very simple arrangements of components. We populate the context with all the components needed to build a UI using JavaFX. There is no absolute requirement that you have to use the UI components, and in fact you could have either no UI, or a different UI technology.

Consider the `MenuConfig` class somewhat like a storage object that can hold instances of objects needed for the running of the application. configuration  

## More details about using the Controller class
