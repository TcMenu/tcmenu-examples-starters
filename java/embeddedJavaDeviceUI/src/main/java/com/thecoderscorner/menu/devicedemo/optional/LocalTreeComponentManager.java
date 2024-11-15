package com.thecoderscorner.menu.devicedemo.optional;

import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxMenuPresentable;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.mgr.MenuManagerListener;
import com.thecoderscorner.menu.mgr.MenuManagerServer;
import javafx.application.Platform;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Goes with the local UI, this handles changes on behalf of the local UI in both directions. You can add additional
 * functionality as needed here.
 */
public class LocalTreeComponentManager implements MenuManagerListener {
    private final MenuManagerServer menuMgr;
    private final JfxNavigationManager navigationManager;
    private final ScheduledExecutorService executor;

    public LocalTreeComponentManager(MenuManagerServer menuMgr, JfxNavigationManager navigationManager, ScheduledExecutorService executor) {
        this.menuMgr = menuMgr;
        this.navigationManager = navigationManager;
        this.executor = executor;

        menuMgr.addMenuManagerListener(this);

        menuMgr.addTreeStructureChangeListener(hint -> {
            // update all non sub menu items as the tree has structurally changed
            menuItemHasChanged(null, null);
        });

        executor.scheduleAtFixedRate(this::tickProc, 100L, 100L, TimeUnit.MILLISECONDS);
    }

    private void tickProc() {
        Platform.runLater(() -> {
            if(navigationManager.currentNavigationPanel() instanceof UpdatablePanel panel) {
                panel.tickAll();
            }
        });
    }

    @Override
    public void menuItemHasChanged(Object sender, MenuItem item) {
        Platform.runLater(() -> {
            if (navigationManager.currentNavigationPanel() instanceof JfxMenuPresentable menuPanel) {
                if (item == null) {
                    menuPanel.entirelyRebuildGrid();
                } else {
                    menuPanel.getGridComponent().itemHasUpdated(item);
                }
            } else if(navigationManager.currentNavigationPanel() instanceof UpdatablePanel panel) {
                if(item != null) {
                    panel.itemHasUpdated(item);
                }
            }
        });
    }

    @Override
    public void managerWillStart() {
    }

    @Override
    public void managerWillStop() {

    }
}