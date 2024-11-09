package com.thecoderscorner.menu.devicedemo;

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
 * here into a series of components. This is and example of how to arrange such an application.
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
        // See the method for more information.
        buildMenuInMenuComponents();

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

    ///
    /// here we demonstrate how to include menu in menu components. You can use tcMenu Designer to generate the menu in
    /// menu components and add them here, it can build the entire method for you from `Code -> Menu In Menu`
    ///
    public void buildMenuInMenuComponents() {
        MenuManagerServer menuManager = context.getBean(MenuManagerServer.class);
        MenuCommandProtocol protocol = context.getBean(MenuCommandProtocol.class);
        ScheduledExecutorService executor = context.getBean(ScheduledExecutorService.class);
        LocalIdentifier localId = new LocalIdentifier(menuManager.getServerUuid(), menuManager.getServerName());
        var remMenuAvrBoardConnector = new SocketBasedConnector(localId, executor, Clock.systemUTC(), protocol, "192.168.0.96", 3333, ConnectMode.FULLY_AUTHENTICATED, null);
        var remMenuAvrBoard = new MenuInMenu(remMenuAvrBoardConnector, menuManager, menuManager.getManagedMenu().getMenuById(16).orElseThrow(), MenuInMenu.ReplicationMode.REPLICATE_ADD_STATUS_ITEM, 100000, 65000);
        remMenuAvrBoard.start();
    }

}
