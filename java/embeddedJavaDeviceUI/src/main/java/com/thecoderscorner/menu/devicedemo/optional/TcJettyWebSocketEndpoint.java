package com.thecoderscorner.menu.devicedemo.optional;

import com.thecoderscorner.menu.mgr.MenuManagerServer;
import com.thecoderscorner.menu.mgr.ServerConnection;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.System.Logger.Level.*;

@WebSocket
public class TcJettyWebSocketEndpoint {
    private static final Map<String, TcJettyWebServer.TcJettyWebSocketConnection> connectionsBySession = new ConcurrentHashMap<>();
    private static final System.Logger logger = System.getLogger(MenuManagerServer.class.getSimpleName());

    @OnWebSocketOpen
    public void onOpen(Session session) {
        TcJettyWebServer server = TcJettyWebServer.getInstance();
        var newSession = new TcJettyWebServer.TcJettyWebSocketConnection(session, server.getClock(), server.getProtocol());
        connectionsBySession.put(sessionId(session), newSession);
        logger.log(INFO, "Creating new session for ID " + session);
        server.getListener().connectionCreated(newSession);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        var connection = connectionsBySession.get(sessionId(session));
        logger.log(DEBUG, "Message Received on " + session + " message = " + message);
        if (connection != null) {
            connection.stringDataRx(message);
        }
    }

    @OnWebSocketClose
    public void onClose(Session session) {
        var con = connectionsBySession.get(sessionId(session));
        if (con != null) {
            logger.log(INFO, "Close of session " + sessionId(session));
            con.socketDidClose();
            connectionsBySession.remove(sessionId(session));
        } else {
            logger.log(INFO, "Close of session with no reference " + sessionId(session));
        }
    }

    @OnWebSocketError
    public void onError(Session session, Throwable throwable) {
        logger.log(ERROR, "Error on session " + sessionId(session), throwable);
        var con = connectionsBySession.get(sessionId(session));
        if (con != null) {
            con.closeConnection();
            connectionsBySession.remove(sessionId(session));
        }
    }

    String sessionId(Session session) {
        return session.getRemoteSocketAddress().toString();
    }

    public List<ServerConnection> getAllConnections() {
        return List.copyOf(connectionsBySession.values());
    }
}