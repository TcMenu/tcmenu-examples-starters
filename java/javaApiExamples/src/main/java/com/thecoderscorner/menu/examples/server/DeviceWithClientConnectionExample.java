package com.thecoderscorner.menu.examples.server;

import com.thecoderscorner.menu.auth.PreDefinedAuthenticator;
import com.thecoderscorner.menu.domain.util.DomainFixtures;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.mgr.MenuInMenu;
import com.thecoderscorner.menu.mgr.MenuManagerServer;
import com.thecoderscorner.menu.remote.ConnectMode;
import com.thecoderscorner.menu.remote.LocalIdentifier;
import com.thecoderscorner.menu.remote.RemoteConnector;
import com.thecoderscorner.menu.remote.encryption.AESEncryptionHandlerFactory;
import com.thecoderscorner.menu.remote.mgrclient.ClientBasedConnectionManager;
import com.thecoderscorner.menu.remote.protocol.ConfigurableProtocolConverter;
import com.thecoderscorner.menu.remote.socket.SocketBasedConnector;

import java.time.Clock;
import java.util.UUID;
import java.util.concurrent.Executors;

/**
 * This example shows how to create a Java device connection, IE where this code would reside as an application on an
 * embedded device such as a Raspberry PI. This example will repeatedly try to open a socket to a server or other
 * component that was accepting connections. It takes a host and port to connect to, and will try and reconnect in the
 * event of connection failure.
 *
 * See ClientThatAcceptsForRemoteExample for the other side of this example.
 */
public class DeviceWithClientConnectionExample {
    // where we want to connect to both host and port
    public static final String MY_HOST = "localhost";
    public static final int MY_PORT = 3333;
    // the name that we'll provide when we join
    public static final String MY_SERVER_NAME = "ClientDevExample";
    // the UUID of this device
    public static final UUID MY_SERVER_UUID = UUID.fromString("A2BA2013-E17A-4551-8462-A0F6D15968AC");
    public static final String ENCRYPTED_AES_KEY = "A8UvLzdTzUCYeqir6DODRquIbch04kN1EuyocNqoJI4=";
    private static final String MENU_IN_MENU_REMOTE = "192.168.0.222";
    private static final int MENU_IN_MENU_PORT = 3333;

    public static void main(String[] args) {
        // First we create a few objects that are needed by both the remote connection logic and the menu manager.
        var protocol = new ConfigurableProtocolConverter(true);
        var clock = Clock.systemUTC();
        var executor = Executors.newScheduledThreadPool(4);
        var aesEncryptionFactory = new AESEncryptionHandlerFactory(ENCRYPTED_AES_KEY);

        // create the connection logic, in this case we are a device that connects to an "accepting" client.
        var deviceRemote = new ClientBasedConnectionManager(MY_HOST, MY_PORT, protocol, clock, executor, aesEncryptionFactory);

        // create a basic menu manager, in any menu app we'd normally have one of these.
        var tree = DomainFixtures.fullEspAmplifierTestTree();

        // now we create the menu manager object, it is responsible for handling remote connections and bootstrapping
        // clients that connect.
        var menuManager = new MenuManagerServer(executor, tree, MY_SERVER_NAME, MY_SERVER_UUID, new PreDefinedAuthenticator(true), clock);
        menuManager.addConnectionManager(deviceRemote);
        menuManager.setBoardSerialProvider(() -> 1234);

        // start optional menu in menu set up.

        // optionally we can now add one or more menu-in-menu structures. See here for more information:
        // https://tcmenu.github.io/documentation/arduino-libraries/tc-menu/tcmenu-iot/java-menu-in-menu/

        // first we create a remote connector that will read the source menu and receive updates as it changes.
        RemoteConnector connector = new SocketBasedConnector(
                new LocalIdentifier(MY_SERVER_UUID, MY_SERVER_NAME),
                executor, clock, protocol, MENU_IN_MENU_REMOTE, MENU_IN_MENU_PORT,
                ConnectMode.FULLY_AUTHENTICATED, null
        );

        // now we create a menu in menu item that puts all the items in the "source" menu into our menu at an offset.
        var menuInMenu = new MenuInMenu(connector, menuManager, MenuTree.ROOT,
                MenuInMenu.ReplicationMode.REPLICATE_SILENTLY,
                50100, 55000);

        // now we start the menu in menu configuration, it puts the "source" menu structure into our tree and keeps it
        // in sync in both directions.
        menuInMenu.start();

        // end optional menu in menu setup.

        // this finally spins up the example
        menuManager.start();
    }
}
