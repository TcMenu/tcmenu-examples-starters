package com.thecoderscorner.menu.devicedemo.optional;

import com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent;
import com.thecoderscorner.embedcontrol.core.controlmgr.MenuComponentControl;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.core.util.MenuAppVersion;
import com.thecoderscorner.embedcontrol.customization.ColorCustomizable;
import com.thecoderscorner.embedcontrol.customization.GlobalColorCustomizable;
import com.thecoderscorner.embedcontrol.customization.MenuItemStore;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxMenuEditorFactory;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationHeader;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.panels.AuthIoTMonitorPresentable;
import com.thecoderscorner.menu.auth.MenuAuthenticator;
import com.thecoderscorner.menu.auth.PropertiesAuthenticator;
import com.thecoderscorner.menu.devicedemo.EmbeddedJavaDemoMenu;
import com.thecoderscorner.menu.devicedemo.MenuConfig;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.ListResponse;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.mgr.DialogManager;
import com.thecoderscorner.menu.mgr.MenuManagerServer;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.commands.DialogMode;
import com.thecoderscorner.menu.remote.commands.MenuDialogCommand;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This is the local UI plugin, it provides a local UI that will by default render your menu tree onto the display using
 * Java FX. The default UI can be overridden by adding custom panels to the navigationHeader, see below where we've done
 * this for one of the submenu items.
 */
public class JfxLocalAutoUI extends Application {
    private static final AtomicReference<MenuConfig> GLOBAL_CONTEXT = new AtomicReference<>(null);

    private MenuManagerServer mgr;
    private JfxNavigationHeader navigationHeader;
    private LocalDialogManager dlgMgr;
    private MenuAppVersion versionData;
    private LocalTreeComponentManager localTree;
    private EmbeddedJavaDemoMenu menuTree;
    private GlobalSettings globalSettings;

    public static void setAppContext(MenuConfig context) {
        GLOBAL_CONTEXT.set(context);
    }

    @Override
    public void start(Stage stage) {
        var ctx = GLOBAL_CONTEXT.get();
        mgr = ctx.getBean(MenuManagerServer.class);
        menuTree = ctx.getBean(EmbeddedJavaDemoMenu.class);
        var executor = ctx.getBean(ScheduledExecutorService.class);
        versionData = ctx.getBean(MenuAppVersion.class);
        globalSettings = ctx.getBean(GlobalSettings.class);

        dlgMgr = new LocalDialogManager();
        var auth = ctx.getBean(MenuAuthenticator.class);
        if(auth instanceof PropertiesAuthenticator propAuth) propAuth.setDialogManager(dlgMgr);

        stage.setTitle(mgr.getServerName());
        var scroller = new ScrollPane();

        stage.setOnCloseRequest(event -> {
            executor.shutdown();
            Platform.exit();
            System.exit(0);
        });

        var localController = new LocalMenuController();
        navigationHeader = ctx.getBean(JfxNavigationHeader.class);
        var factory = new JfxMenuEditorFactory(localController, Platform::runLater, dlgMgr);
        navigationHeader.addCustomMenuPanel(menuTree.getStatus(), new StatusPanelDrawable(menuTree, executor, factory,
                localController, mgr, new CondColorFromGlobal(globalSettings)));
        navigationHeader.initialiseUI(dlgMgr, localController, scroller);

        localTree = new LocalTreeComponentManager(mgr, navigationHeader, executor);
        mgr.start();
        navigationHeader.pushMenuNavigation(MenuTree.ROOT, ctx.getBean(MenuItemStore.class));

        var dialogComponents = dlgMgr.initialiseControls();
        var border = new BorderPane();
        border.setCenter(scroller);
        VBox vbox = new VBox(dialogComponents, navigationHeader.initialiseControls());
        border.setTop(vbox);
        BorderPane.setMargin(scroller, new Insets(4));

        Scene scene = new Scene(border, 800, 500);
        stage.setScene(scene);
        stage.show();
    }

    class LocalMenuController implements MenuComponentControl {

        @Override
        public CorrelationId editorUpdatedItem(MenuItem menuItem, Object val) {
            if(!(val instanceof ListResponse)) {
                MenuItemHelper.setMenuState(menuItem, val, mgr.getManagedMenu());
            }
            mgr.updateMenuItem(this, menuItem, val);

            return CorrelationId.EMPTY_CORRELATION;
        }

        @Override
        public CorrelationId editorUpdatedItemDelta(MenuItem menuItem, int delta) {
            MenuItemHelper.applyIncrementalValueChange(menuItem, delta, mgr.getManagedMenu());
            mgr.menuItemDidUpdate(this, menuItem);
            return CorrelationId.EMPTY_CORRELATION;
        }

        @Override
        public void connectionStatusChanged(AuthStatus authStatus) {
            // doesn't really apply locally
        }

        @Override
        public MenuTree getMenuTree() {
            return mgr.getManagedMenu();
        }

        @Override
        public String getConnectionName() {
            return mgr.getServerName();
        }

        @Override
        public JfxNavigationManager getNavigationManager() {
            return navigationHeader;
        }

        @Override
        public void presentIoTAuthPanel() {
            navigationHeader.pushNavigation(new AuthIoTMonitorPresentable(mgr));
        }
    }

    private class LocalDialogManager extends DialogManager {
        private boolean sendRemoteAllowed = false; // protected by DialogManager.lock
        private GridPane layoutGrid;
        private Label headerLabel;
        private Label messageLabel;
        private Button dlgButton1;
        private Button dlgButton2;

        public GridPane initialiseControls() {
            layoutGrid = new GridPane();
            layoutGrid.setMaxWidth(9999.99);
            layoutGrid.setPadding(new Insets(4));
            layoutGrid.setHgap(4);
            layoutGrid.setVgap(4);
            layoutGrid.setStyle("-fx-background-color: #39395b;");
            headerLabel = new Label("");
            headerLabel.setStyle("-fx-font-size: 24px; -fx-font-style: bold; -fx-text-fill: #ccc;");
            layoutGrid.add(headerLabel, 0, 0, 2, 1);
            messageLabel = new Label("");
            messageLabel.setStyle("-fx-font-size: 20px; -fx-font-style: bold;-fx-text-fill: #ccc;");
            layoutGrid.add(messageLabel, 0, 1, 2, 1);
            dlgButton1 = new Button("");
            dlgButton2 = new Button("");
            dlgButton1.setStyle("-fx-font-size: 24px; -fx-background-color: #2e2e60; -fx-text-fill: #ccc;");
            dlgButton2.setStyle("-fx-font-size: 24px; -fx-background-color: #2e2e60; -fx-text-fill: #ccc;");
            dlgButton1.setMaxWidth(9999.9);
            dlgButton2.setMaxWidth(9999.9);
            dlgButton1.setOnAction(event -> buttonWasPressed(button1));
            dlgButton2.setOnAction(event -> buttonWasPressed(button2));
            layoutGrid.add(dlgButton1, 0, 2);
            layoutGrid.add(dlgButton2, 1, 2);
            GridPane.setHgrow(dlgButton1, Priority.ALWAYS);
            GridPane.setHgrow(dlgButton2, Priority.ALWAYS);
            dialogDidChange();
            return layoutGrid;
        }

        public LocalDialogManager withRemoteAllowed(boolean remoteAllowed) {
            synchronized (lock) {
                sendRemoteAllowed = remoteAllowed;
            }
            return this;
        }

        @Override
        protected void dialogDidChange() {
            boolean remoteAllowed;
            synchronized (lock) {
                remoteAllowed = sendRemoteAllowed;
            }

            Platform.runLater(() -> {
                synchronized (lock) {
                    layoutGrid.setVisible(mode != DialogMode.HIDE);
                    layoutGrid.setManaged(mode != DialogMode.HIDE);
                    dlgButton1.setText(toPrintableText(button1));
                    dlgButton2.setText(toPrintableText(button2));
                    messageLabel.setText(message);
                    headerLabel.setText(title);
                }
            });

            if(remoteAllowed) {
                mgr.sendCommand(new MenuDialogCommand(mode, title, message, button1, button2, CorrelationId.EMPTY_CORRELATION));
            }
        }
    }

    private class CondColorFromGlobal implements ConditionalColoring {
        private final ColorCustomizable colorCustomizable;
        public CondColorFromGlobal(GlobalSettings globalSettings) {
            colorCustomizable = new GlobalColorCustomizable(globalSettings);
        }

        private ControlColor getControlColor(EditorComponent.RenderingStatus status, ColorComponentType compType) {
            if (status == EditorComponent.RenderingStatus.RECENT_UPDATE) compType = ColorComponentType.CUSTOM;
            else if (status == EditorComponent.RenderingStatus.EDIT_IN_PROGRESS) compType = ColorComponentType.PENDING;
            else if (status == EditorComponent.RenderingStatus.CORRELATION_ERROR) compType = ColorComponentType.ERROR;

            return switch (compType) {
                case TEXT_FIELD -> globalSettings.getTextColor();
                case BUTTON -> globalSettings.getButtonColor();
                case HIGHLIGHT -> globalSettings.getHighlightColor();
                case CUSTOM -> globalSettings.getUpdateColor();
                case DIALOG -> globalSettings.getDialogColor();
                case ERROR -> globalSettings.getErrorColor();
                case PENDING -> globalSettings.getPendingColor();
            };
        }

        @Override
        public PortableColor foregroundFor(EditorComponent.RenderingStatus status, ColorComponentType compType) {
            return getControlColor(status, compType).getFg();
        }

        @Override
        public PortableColor backgroundFor(EditorComponent.RenderingStatus status, ColorComponentType compType) {
            return getControlColor(status, compType).getBg();
        }

        @Override
        public ControlColor colorFor(EditorComponent.RenderingStatus status, ColorComponentType ty) {
            return getControlColor(status, ty);
        }
    }
}
