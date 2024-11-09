package com.thecoderscorner.menu.example.websocket;

import com.thecoderscorner.menu.auth.PropertiesAuthenticator;
import com.thecoderscorner.menu.domain.AnalogMenuItem;
import com.thecoderscorner.menu.domain.util.DomainFixtures;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.mgr.MenuManagerServer;
import com.thecoderscorner.menu.mgr.NoDialogFacilities;
import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.mgrclient.SocketServerConnectionManager;
import com.thecoderscorner.menu.remote.protocol.ConfigurableProtocolConverter;

import java.time.Clock;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class SimpleWebSocketExample {

    public static void main(String[] args) {
        // enable logging onto the console.
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.FINEST);
        Logger.getLogger("").addHandler(handler);
        Logger.getLogger("").setLevel(Level.FINEST);

        // now build a tree that the menu manager will manage.
        var tree = DomainFixtures.fullEspAmplifierTestTree();
        // and an executor, simple single thread one is enough for this.
        var executor = Executors.newSingleThreadScheduledExecutor();
        // and a clock, we choose default timezone.
        var clock = Clock.systemDefaultZone();
        // this protocol handler can deal with all TagVal messages, you can even add your own custom ones too.
        MenuCommandProtocol tagValProtocol = new ConfigurableProtocolConverter(true);
        // now create the menu manager, it manages all remotes, bootstrapping API connections etc.
        var menuManager = new MenuManagerServer(executor, tree,
                "WS Test", UUID.randomUUID(),
                new PropertiesAuthenticator("./auth.properties", new NoDialogFacilities()),
                clock);
        // then we add the websocket and regular endpoints to it, it can handle both regular socket connections and
        // also websocket connections.
        menuManager.addConnectionManager(new WebSocketServerConnectionManager(tagValProtocol, 3333, clock));
        menuManager.addConnectionManager(new SocketServerConnectionManager(tagValProtocol, executor, 3334, clock, 15000));
        // fire it up so we can connect
        menuManager.start();

        // an example of updating a menu item, in this case a list.
        var menuList = menuManager.getManagedMenu().getMenuById(21).orElseThrow();
        menuManager.updateMenuItem(menuManager, menuList, List.of("salad", "pasta", "pizza"));

        // if you want to send simulated updates when running set VM option -DsendSimulatedUpdates=true
        if(Boolean.getBoolean("sendSimulatedUpdates")) {

            // update the list item randomly every 5 seconds.
            executor.scheduleAtFixedRate(() -> menuManager.updateMenuItem(menuManager, menuList, randomListData()), 5000, 5000, TimeUnit.MILLISECONDS);

            // now update a few analog items randomly.
            executor.scheduleAtFixedRate(() -> {
                if (menuManager.isAnyRemoteConnection()) return;
                var menuVolume = (AnalogMenuItem) tree.getMenuById(1).orElseThrow();
                var menuLeftVU = (AnalogMenuItem) tree.getMenuById(15).orElseThrow();
                var menuRightVU = (AnalogMenuItem) tree.getMenuById(16).orElseThrow();

                int amt = (int) (Math.random() * 2000);
                if (Math.random() > 0.5) {
                    menuManager.updateMenuItem(menuManager, menuLeftVU, MenuItemHelper.getValueFor(menuLeftVU, tree, 0) + amt);
                    menuManager.updateMenuItem(menuManager, menuRightVU, MenuItemHelper.getValueFor(menuRightVU, tree, 0) - amt);
                } else {
                    menuManager.updateMenuItem(menuManager, menuLeftVU, MenuItemHelper.getValueFor(menuLeftVU, tree, 0) - amt);
                    menuManager.updateMenuItem(menuManager, menuRightVU, MenuItemHelper.getValueFor(menuRightVU, tree, 0) + amt);

                }

                menuManager.updateMenuItem(menuManager, menuVolume, Math.random() * menuVolume.getMaxValue());
            }, 150, 150, TimeUnit.MILLISECONDS);
        }
    }

    private static List<String> randomListData() {
        int random = (int) (Math.random() * 7.0);
        return switch (random) {
            case 0 -> List.of("aubergine", "courgette", "tomatoes");
            case 1 -> List.of("oregano", "basil", "thyme");
            case 2 -> List.of("pizza", "mozzarella", "pepperoni");
            case 3 -> List.of("pasta", "tomatoes", "olive oil");
            case 4 -> List.of("carrot", "sweet potato", "chickpeas");
            case 5 -> List.of("turnip", "carrot", "potato");
            default -> List.of("cumin", "coriander", "turmeric");
        };
    }
}