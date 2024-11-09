package com.thecoderscorner.menu.examples.server;

import com.thecoderscorner.menu.auth.PreDefinedAuthenticator;
import com.thecoderscorner.menu.domain.util.DomainFixtures;
import com.thecoderscorner.menu.mgr.MenuManagerServer;
import com.thecoderscorner.menu.remote.mgrclient.ClientBasedConnectionManager;
import com.thecoderscorner.menu.remote.mgrclient.SocketServerConnectionManager;
import com.thecoderscorner.menu.remote.protocol.ConfigurableProtocolConverter;

import java.time.Clock;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * This example shows how to create a Java device server, IE where this code would reside as an application on an
 * embedded device such as a Raspberry PI. This example will accept connections in TagVal format from API clients.
 * It takes a port to accept on, and will hold connections open until connection failure.
 */
public class DeviceSocketServerAcceptingConnections {
    // the port on which we'll listen for connections.
    public final static int MY_PORT = 3333;
    // the name of this device, sent during join
    public static final String MY_SERVER_NAME = "ClientDevExample";
    // the UUID of this device, sent during join
    public static final UUID MY_SERVER_UUID = UUID.fromString("A2BA2013-E17A-4551-8462-A0F6D15968AC");

    public static void main(String[] args) {
        // set up logging
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.FINEST);
        Logger.getLogger("").addHandler(handler);
        Logger.getLogger("").setLevel(Level.FINEST);

        // this protocol handler is capable of handling all regular TagVal messages, and you can even add your own extras
        var protocol = new ConfigurableProtocolConverter(true);
        // the clock in system UTC time, you can use any implementation.
        var clock = Clock.systemUTC();
        // the thread pool that we'll use to handle connections.
        var executor = Executors.newScheduledThreadPool(4);

        // create the connection logic, in this case we are a device that connects to an "accepting" client.
        var deviceRemote = new SocketServerConnectionManager(protocol, executor, MY_PORT, clock, 15000);

        // create a basic tree that we'll send remotely during bootstrap.
        var tree = DomainFixtures.fullEspAmplifierTestTree();

        // now we create the menu manager object, it is responsible for handling remote connections and bootstrapping
        // clients that connect.
        var menuManager = new MenuManagerServer(executor, tree, MY_SERVER_NAME, MY_SERVER_UUID, new PreDefinedAuthenticator(true), clock);
        menuManager.addConnectionManager(deviceRemote);
        // you can provide an implementation that returns the serial number of the board
        menuManager.setBoardSerialProvider(() -> 1234);

        menuManager.start();
    }
}
