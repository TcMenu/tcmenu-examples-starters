module com.thecoderscorner.menuexample.embeddedjavademo {
    requires java.prefs;
    requires com.google.gson;
    requires com.fazecast.jSerialComm;
    requires com.thecoderscorner.tcmenu.javaapi;
    requires com.thecoderscorner.embedcontrol.core;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    exports com.thecoderscorner.menu.devicedemo.optional;
    opens com.thecoderscorner.menu.devicedemo;

    requires org.eclipse.jetty.ee10.websocket.jetty.server;
}
