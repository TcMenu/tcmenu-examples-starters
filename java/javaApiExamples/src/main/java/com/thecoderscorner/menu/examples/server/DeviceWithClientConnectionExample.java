package com.thecoderscorner.menu.examples.server;

import com.thecoderscorner.menu.auth.PreDefinedAuthenticator;
import com.thecoderscorner.menu.domain.util.DomainFixtures;
import com.thecoderscorner.menu.mgr.MenuManagerServer;
import com.thecoderscorner.menu.remote.encryption.AESEncryptionHandlerFactory;
import com.thecoderscorner.menu.remote.mgrclient.ClientBasedConnectionManager;
import com.thecoderscorner.menu.remote.protocol.ConfigurableProtocolConverter;

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
    // where we want to connect to (both host and port)
    public static final String MY_HOST = "localhost";
    public static final int MY_PORT = 3333;
    // the name that we'll provide when we join
    public static final String MY_SERVER_NAME = "ClientDevExample";
    // the UUID of this device
    public static final UUID MY_SERVER_UUID = UUID.fromString("A2BA2013-E17A-4551-8462-A0F6D15968AC");
    // the encryption key the API will use
    public static final String ENCRYPTED_AES_KEY = "A8UvLzdTzUCYeqir6DODRquIbch04kN1EuyocNqoJI4=";

    public static void main(String[] args) {
        // this protocol handler is capable of handling all regular TagVal messages, and you can even add your own extras
        var protocol = new ConfigurableProtocolConverter(true);
        // the clock in system UTC time, you can use any implementation.
        var clock = Clock.systemUTC();
        // the thread pool that we'll use to handle connections.
        var executor = Executors.newScheduledThreadPool(4);
        // an encryption factory, you can use `NoEncryptionFactory` too if not needed.
        var aesEncryptionFactory = new AESEncryptionHandlerFactory(ENCRYPTED_AES_KEY);

        // create the connection logic, in this case we are a device that connects to an "accepting" client.
        var deviceRemote = new ClientBasedConnectionManager(MY_HOST, MY_PORT, protocol, clock, executor, aesEncryptionFactory);

        // create a basic tree that we'll send remotely during bootstrap.
        var tree = DomainFixtures.fullEspAmplifierTestTree();

        // now we create the menu manager object, it is responsible for handling remote connections and bootstrapping
        // clients that connect.
        var menuManager = new MenuManagerServer(executor, tree, MY_SERVER_NAME, MY_SERVER_UUID, new PreDefinedAuthenticator(true), clock);
        menuManager.addConnectionManager(deviceRemote);
        // you can provide an implementation that returns the serial number of the board
        menuManager.setBoardSerialProvider(() -> 1234);

        // this finally spins up the example
        menuManager.start();
    }
}
