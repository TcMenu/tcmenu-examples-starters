package com.thecoderscorner.menu.devicedemo;

import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.core.util.BaseMenuConfig;
import com.thecoderscorner.embedcontrol.core.util.MenuAppVersion;
import com.thecoderscorner.embedcontrol.core.util.TcComponent;
import com.thecoderscorner.embedcontrol.customization.ApplicationThemeManager;
import com.thecoderscorner.embedcontrol.customization.MenuItemStore;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationHeader;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import com.thecoderscorner.menu.auth.MenuAuthenticator;
import com.thecoderscorner.menu.auth.PropertiesAuthenticator;
import com.thecoderscorner.menu.mgr.MenuManagerServer;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;
import com.thecoderscorner.menu.persist.PropertiesMenuStateSerialiser;
import com.thecoderscorner.menu.persist.VersionInfo;
import com.thecoderscorner.menu.remote.protocol.ConfigurableProtocolConverter;
import com.thecoderscorner.menu.devicedemo.optional.TcJettyWebServer;

import java.nio.file.Path;
import java.time.Clock;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/// This class creates an application context out of all these components, and you can request any components that are
/// put into the context using getBean(ClassName.class). See the base class `BaseMenuConfig` for more details.
/// In summary, every function annotated with `@TcComponent` will be added to the "context" and you can then request
/// it later using `getBean`. Consider it a lightweight spring that is JPMS module system compliant. Anything that is
/// marked as a `TcComponent` will be available automatically to other methods marked `TcComponent`.
public class MenuConfig extends BaseMenuConfig {
    @TcComponent
    public JfxNavigationHeader navMgr(ScheduledExecutorService executorService, GlobalSettings settings) {
        return new JfxNavigationHeader(executorService, settings);
    }

    @TcComponent
    public ConfigurableProtocolConverter protocol() {
        return new ConfigurableProtocolConverter(true);
    }

    @TcComponent
    public MenuManagerServer menuManagerServer(Clock clock, EmbeddedJavaDemoMenu menuDef, ScheduledExecutorService executorService, MenuAuthenticator authenticator) {
        return new MenuManagerServer(executorService, menuDef.getMenuTree(), mandatoryStringProp("server.name"), UUID.fromString(mandatoryStringProp("server.uuid")), authenticator, clock);
    }

    @TcComponent
    public MenuItemStore itemStore(GlobalSettings settings, EmbeddedJavaDemoMenu menuDef) {
        return new MenuItemStore(settings, menuDef.getMenuTree(), "", 7, 2, settings.isDefaultRecursiveRendering());
    }

    @TcComponent
    public GlobalSettings globalSettings() {
        var settings = new GlobalSettings(new ApplicationThemeManager());
        // load or adjust the settings as needed here. You could use the JDBC components to load and store
        // these values just like embed control does. See TcPreferencesPersistence and TccDatabaseUtilities.
        settings.setDefaultFontSize(16);
        settings.setDefaultRecursiveRendering(false);
        return settings;
    }

    @TcComponent
    public MenuAppVersion versionInfo() {
        var version = mandatoryStringProp("build.version");
        var timestamp = mandatoryStringProp("build.timestamp");
        var groupId = mandatoryStringProp("build.groupId");
        var artifact = mandatoryStringProp("build.artifactId");
        return new MenuAppVersion(new VersionInfo(version), timestamp, groupId, artifact);
    }

    @TcComponent
    public LocaleMappingHandler localeHandler() {
        return LocaleMappingHandler.NOOP_IMPLEMENTATION;
    }

    @TcComponent
    public MenuAuthenticator menuAuthenticator() {
        return new PropertiesAuthenticator(mandatoryStringProp("file.auth.storage"));
    }

    @TcComponent
    public ConfigurableProtocolConverter tagVal() {
        return new ConfigurableProtocolConverter(true);
    }

    @TcComponent
    public TcJettyWebServer webServer(ConfigurableProtocolConverter protocol, Clock clock) {
        return new TcJettyWebServer(protocol, clock, "./data/www", 8080, false);
    }

    @TcComponent
    public EmbeddedJavaDemoController controller(EmbeddedJavaDemoMenu menuDef, JfxNavigationManager navMgr,
                                                 ScheduledExecutorService executor, GlobalSettings settings) {
        return new EmbeddedJavaDemoController(menuDef, navMgr, executor, settings);
    }

    public MenuConfig() {
        super(null, null);
        // put a clock and thread pool into the "context"
        asBean(Clock.systemUTC());
        asBean(Executors.newScheduledThreadPool(propAsIntWithDefault("threading.pool.size", 4)));
        // add the menu tree into the context
        var menuDef = asBean(new EmbeddedJavaDemoMenu());
        asBean(new PropertiesMenuStateSerialiser(menuDef.getMenuTree(), Path.of(resolvedProperties.getProperty("file.menu.storage")).resolve("menuStorage.properties")));
        scanForComponents();
    }

    // Auto generated menu callbacks end here. Please do not remove this line or change code after it.
}
