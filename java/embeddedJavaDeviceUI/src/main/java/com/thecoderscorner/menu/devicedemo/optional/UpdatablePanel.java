package com.thecoderscorner.menu.devicedemo.optional;

import com.thecoderscorner.menu.domain.MenuItem;

/// Represents a `PanelPresentable` that can be updated when menu items change, it is also provided with a
/// tick function so that the implementor can tick down updates that occur. When an item updates the update
/// will be sent through the `itemHasUpdated` method, and you will be on the JavaFx thread when it occurs.
public interface UpdatablePanel {
    /// called every 100 millis by the framework so that you can tick any animations that are in progress.
    void tickAll();

    /// called whenever there is a menu item update so that the display can be updated.
    /// @param item the item that has updated
    void itemHasUpdated(MenuItem item);
}
