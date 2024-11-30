package com.thecoderscorner.menu.devicedemo.optional;

import com.thecoderscorner.menu.mgr.NewServerConnectionListener;
import com.thecoderscorner.menu.mgr.ServerConnection;
import com.thecoderscorner.menu.mgr.ServerConnectionManager;
import com.thecoderscorner.menu.mgr.ServerConnectionMode;
import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.commands.MenuCommand;
import com.thecoderscorner.menu.remote.protocol.CommandProtocol;
import com.thecoderscorner.menu.remote.protocol.ProtocolHelper;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketServletFactory;
import org.eclipse.jetty.ee10.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

public class TcJettyWebServer implements ServerConnectionManager {
    private static TcJettyWebServer INSTANCE = null;
    public final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final boolean listDirectories;
    private final String resourceDirectory;
    private final MenuCommandProtocol protocol;
    private final Clock clock;
    private Server server;
    private final int portNumber;
    private NewServerConnectionListener listener;
    private TcJettyWebSocketEndpoint webSockEndpoint;

    public TcJettyWebServer(MenuCommandProtocol protocol, Clock clock, String resourceDirectory, int portNumber, boolean listDirectories) {
        this.portNumber = portNumber;
        this.listDirectories = listDirectories;
        this.resourceDirectory = resourceDirectory;
        this.protocol = protocol;
        this.clock = clock;
        INSTANCE = this;
    }

    public static TcJettyWebServer getInstance() {
        return INSTANCE;
    }

    public void start(NewServerConnectionListener listener) {
        try {
            this.listener = listener;
            server = new Server(portNumber);

            var staticHandler = new ResourceHandler();
            staticHandler.setDirAllowed(listDirectories);
            staticHandler.setWelcomeFiles("index.html");
            Path webRootPath = Paths.get(resourceDirectory).toAbsolutePath().normalize();
            staticHandler.setBaseResource(ResourceFactory.of(server).newResource(webRootPath));
            var contextHandler = new ContextHandler();
            contextHandler.setHandler(staticHandler);

            ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
            servletContext.setContextPath("/");

            JettyWebSocketServletContainerInitializer.configure(servletContext, null);
            var wsHolder = new ServletHolder("tcmenu", new TcMenuWebSocket());
            servletContext.addServlet(wsHolder, "/ws/*");

            var handlers = new ContextHandlerCollection();
            handlers.addHandler(contextHandler);
            handlers.addHandler(servletContext);
            server.setHandler(handlers);

            server.start();
        } catch (Exception e) {
            logger.log(System.Logger.Level.ERROR, "Exception while starting web server", e);
        }
    }

    @Override
    public List<ServerConnection> getServerConnections() {
        return webSockEndpoint.getAllConnections();
    }

    @Override
    public void stop() throws Exception {
        server.stop();
    }

    public NewServerConnectionListener getListener() {
        return listener;
    }

    public MenuCommandProtocol getProtocol() {
        return protocol;
    }

    public Clock getClock() {
        return clock;
    }

    public static class TcJettyWebSocketConnection implements ServerConnection {
        public final System.Logger logger = System.getLogger(getClass().getSimpleName());
        private final Session session;
        private final Clock clock;
        private final AtomicInteger hbFrequency = new AtomicInteger(1500);
        private final AtomicLong lastMsgIn = new AtomicLong();
        private final AtomicLong lastMsgOut = new AtomicLong(0);
        private final AtomicReference<ServerConnectionMode> connectionMode = new AtomicReference<>(ServerConnectionMode.DISCONNECTED);
        private final AtomicReference<String> userName = new AtomicReference<>("Not set");
        private final AtomicReference<BiConsumer<ServerConnection, Boolean>> connectionListener = new AtomicReference<>();
        private final Object socketLock = new Object();
        ByteBuffer outputBuffer = ByteBuffer.allocate(8192);
        private final MenuCommandProtocol protocol;
        private final ProtocolHelper protocolHelper;

        public TcJettyWebSocketConnection(Session session, Clock clock, MenuCommandProtocol protocol) {
            this.session = session;
            this.clock = clock;
            this.protocol = protocol;
            protocolHelper = new ProtocolHelper(protocol);
            lastMsgIn.set(clock.millis());
            lastMsgOut.set(clock.millis());
        }

        @Override
        public int getHeartbeatFrequency() {
            return hbFrequency.get();
        }

        @Override
        public void closeConnection() {
            try {
                session.close();
                if (connectionListener.get() != null) connectionListener.get().accept(this, false);
            } catch (Exception e) {
                logger.log(System.Logger.Level.ERROR, "Close on session failed ", session);
            }
        }

        @Override
        public long lastReceivedHeartbeat() {
            return lastMsgIn.get();
        }

        @Override
        public long lastTransmittedHeartbeat() {
            return lastMsgOut.get();
        }

        @Override
        public void sendCommand(MenuCommand command) {
            lastMsgOut.set(clock.millis());
            logger.log(System.Logger.Level.DEBUG, session + " - " + command);
            try {
                synchronized (socketLock) {
                    if(protocol.getProtocolForCmd(command) == CommandProtocol.TAG_VAL_PROTOCOL) {
                        String text = protocolHelper.protoBufferToText(command);
                        session.sendText(text, Callback.NOOP);
                    } else {
                        protocol.toChannel(outputBuffer, command);
                        outputBuffer.flip();
                        session.sendBinary(outputBuffer, Callback.NOOP);
                    }
                }
            } catch (Exception e) {
                logger.log(System.Logger.Level.ERROR, "Socket failed to write - " + session, e);
                closeConnection();
            }
        }

        @Override
        public void registerConnectionListener(BiConsumer<ServerConnection, Boolean> connectionListener) {
            this.connectionListener.set(connectionListener);
        }

        @Override
        public void registerMessageHandler(BiConsumer<ServerConnection, MenuCommand> messageHandler) {
            this.protocolHelper.setMessageHandler(messageHandler);
        }

        @Override
        public void setConnectionMode(ServerConnectionMode mode) {
            connectionMode.set(mode);
        }

        @Override
        public ServerConnectionMode getConnectionMode() {
            return connectionMode.get();
        }

        @Override
        public String getUserName() {
            return userName.get();
        }

        @Override
        public String getConnectionName() {
            return String.format("JettyWS %s as %s", session, getUserName());
        }

        public void stringDataRx(String data) {
            try {
                lastMsgIn.set(clock.millis());
                protocolHelper.dataReceived(this, data);
            } catch (Exception e) {
                closeConnection();
                logger.log(System.Logger.Level.ERROR, "Problem while reading data from " + session, e);
            }
        }

        public void socketDidClose() {
            if (connectionListener.get() != null) connectionListener.get().accept(this, false);
        }
    }

    public class TcMenuWebSocket extends JettyWebSocketServlet
    {
        @Override
        public void configure(JettyWebSocketServletFactory factory)
        {
            factory.register(TcJettyWebSocketEndpoint.class);
        }
    }
}
