package com.thecoderscorner.menu.devicedemo;

import com.thecoderscorner.embedcontrol.core.util.TcApiDefinitions;
import com.thecoderscorner.menu.mgr.MenuInMenu;
import com.thecoderscorner.menu.mgr.MenuManagerServer;
import com.thecoderscorner.menu.persist.MenuStateSerialiser;
import com.thecoderscorner.menu.remote.ConnectMode;
import com.thecoderscorner.menu.remote.LocalIdentifier;
import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.socket.SocketBasedConnector;
import com.thecoderscorner.menu.devicedemo.optional.JfxLocalAutoUI;
import com.thecoderscorner.menu.devicedemo.optional.TcJettyWebServer;
import javafx.application.Application;

import java.time.Clock;
import java.util.concurrent.ScheduledExecutorService;

/**
 * This class is the application class that is run when the application starts. You can organize the application in
 * here into a series of components. This is the starting point of a Java embedded application, you can rearrange
 * as needed, but to continue round trips with the designer UI ensure the file with menu definitions remains unaltered
 * and that the controller is kept in the same place.
 */
public class EmbeddedJavaDemoApp {
    private final MenuManagerServer manager;
    private final MenuConfig context;
    private final TcJettyWebServer webServer;
    
    public EmbeddedJavaDemoApp() {
        context = new MenuConfig();
        manager = context.getBean(MenuManagerServer.class);
        webServer = context.getBean(TcJettyWebServer.class);
    }

    public void start() {
        // Firstly we get the menu state serializer, this can load and save menu state
        var serializer = context.getBean(MenuStateSerialiser.class);
        serializer.loadMenuStatesAndApply();
        // save states when the app shuts down
        Runtime.getRuntime().addShutdownHook(new Thread(serializer::saveMenuStates));
        // the controller receives updates and things happen on the menu, we register it here.
        manager.addMenuManagerListener(context.getBean(EmbeddedJavaDemoController.class));
        // get the api definitions and start any menu in menu components
        var apiDefinitions = context.getBean(TcApiDefinitions.class);
        apiDefinitions.configureMenuInMenuComponents(context);

        // Give the Local UI access to teh context
        JfxLocalAutoUI.setAppContext(context);

        // here we start a webserver, it is initialised in the config file.
        manager.addConnectionManager(webServer);

        // and finally, start the local JavaFX UI.
        Application.launch(JfxLocalAutoUI.class);
    }

    public static void main(String[] args) {
        new EmbeddedJavaDemoApp().start();
    }
}
