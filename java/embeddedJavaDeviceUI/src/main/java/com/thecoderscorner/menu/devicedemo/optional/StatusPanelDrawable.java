package com.thecoderscorner.menu.devicedemo.optional;

import com.thecoderscorner.embedcontrol.core.controlmgr.*;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.customization.FontInformation;
import com.thecoderscorner.embedcontrol.customization.FontInformation.SizeMeasurement;
import com.thecoderscorner.embedcontrol.customization.customdraw.NumberCustomDrawingConfiguration;
import com.thecoderscorner.menu.devicedemo.EmbeddedJavaDemoMenu;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.mgr.MenuManagerServer;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.PortableAlignment;
import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.RenderingStatus.NORMAL;
import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring.ColorComponentType.TEXT_FIELD;
import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.asFxColor;
import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.fromFxColor;
import static com.thecoderscorner.embedcontrol.customization.customdraw.CustomDrawingConfiguration.NO_CUSTOM_DRAWING;
import static com.thecoderscorner.embedcontrol.customization.customdraw.CustomDrawingConfiguration.NumericColorRange;

/// Demonstrates how to create your own panel to be presented instead of an automatic menu panel. Simply
/// register this panel with the navigation manager class and it will be presented instead of the standard
/// panel. See `JfxLocalAutoUI` where this panel is added. Any panel that implements `UpdatablePanel` will
/// automatically be told when menu items have updated, and be provided with a tick function for animations.
public class StatusPanelDrawable implements PanelPresentable<Node>, UpdatablePanel {
    private final EmbeddedJavaDemoMenu menuDef;
    private final MenuEditorFactory<Node> editorFactory;
    private final MenuComponentControl componentControl;
    private final ConditionalColoring globalColors;
    private final ScheduledExecutorService executor;
    private final MenuManagerServer manager;
    private final HashMap<Integer, EditorComponent<Node>> controlsBeingManaged = new HashMap<>();
    private double presentableWidth = 999;
    private GridPane gridPane;
    private double lastWidth = 999;

    public StatusPanelDrawable(EmbeddedJavaDemoMenu menuDef, ScheduledExecutorService executor,
                               MenuEditorFactory<Node> factory, MenuComponentControl control,
                               MenuManagerServer manager, ConditionalColoring globalColors) {
        this.menuDef = menuDef;
        this.executor = executor;
        this.editorFactory = factory;
        this.componentControl = control;
        this.manager = manager;
        this.globalColors = globalColors;
    }

    @Override
    public Node getPanelToPresent(double width) throws Exception {
        lastWidth = width;
        if (gridPane != null) {
            // empty it if it already exists to make GC easier
            gridPane.getChildren().clear();
            gridPane.getColumnConstraints().clear();
            gridPane.getRowConstraints().clear();
        }
        gridPane = makeGridPanel();
        presentableWidth = width;
        return gridPane;
    }

    private GridPane makeGridPanel() {
        // here we create a JavaFX grid using the regular way of doing so. You can consult the JavaFx documentation
        // for more on how to create grids.
        gridPane = new GridPane();
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        gridPane.setMaxWidth(9999);
        gridPane.setPrefWidth(presentableWidth);
        gridPane.getChildren().clear();

        // the grid will be 4 across by three down.
        gridPane.getColumnConstraints().clear();
        gridPane.getRowConstraints().clear();
        gridPane.setBackground(new Background(new BackgroundFill(
                asFxColor(globalColors.colorFor(NORMAL, TEXT_FIELD).getBg()), null, null
        )));
        gridPane.getRowConstraints().add(new RowConstraints(80));
        gridPane.getRowConstraints().add(new RowConstraints(20));
        gridPane.getRowConstraints().add(new RowConstraints(20));
        for (int i = 0; i < 4; i++) {
            var cc = new ColumnConstraints(10, presentableWidth / 4, 9999, Priority.SOMETIMES, HPos.CENTER, true);
            gridPane.getColumnConstraints().add(cc);
        }

        // now we add some labels into the grid on the left.
        gridPane.add(new Label("Case Temperature"), 0, 0);
        gridPane.add(new Label("Light Color"), 0, 1);
        gridPane.add(new Label("Authenticator"), 0, 2);

        gridPane.add(new Label("Start Simulating"), 2, 0);
        var runSimButton = new Button("Run Sim");
        runSimButton.setOnAction(event -> executor.scheduleAtFixedRate(this::updateTemp, 200L, 200L, TimeUnit.MILLISECONDS));
        gridPane.add(runSimButton, 2, 1);

        // and now we add in a component that will render using the VU meter style. It is a float item, and we provide
        // custom drawing configuration for it, so it has three ranges, green, orange, red.
        FontInformation font100Pc = new FontInformation(100, SizeMeasurement.PERCENT);
        var greenOrangeRed = new NumberCustomDrawingConfiguration(List.of(
                new NumericColorRange(0.0, 70.0, fromFxColor(Color.GREEN), fromFxColor(Color.WHITE)),
                new NumericColorRange(70.0, 90.0, fromFxColor(Color.ORANGE), fromFxColor(Color.WHITE)),
                new NumericColorRange(90.0, 100.0, fromFxColor(Color.RED), fromFxColor(Color.LIGHTGRAY))
        ), "vuColors");
        putIntoGrid(menuDef.getStatusCaseTempOC(), new ComponentSettings(
                globalColors, font100Pc, PortableAlignment.CENTER,
                new ComponentPositioning(0, 1), RedrawingMode.SHOW_VALUE, ControlType.VU_METER,
                greenOrangeRed, true)
        );

        putIntoGrid(menuDef.getStatusIoTMonitor(), new ComponentSettings(
                globalColors, font100Pc, PortableAlignment.RIGHT,
                new ComponentPositioning(2, 1), RedrawingMode.SHOW_VALUE, ControlType.AUTH_IOT_CONTROL,
                NO_CUSTOM_DRAWING, true));

        putIntoGrid(menuDef.getStatusLightColor(), new ComponentSettings(
                globalColors, font100Pc, PortableAlignment.RIGHT,
                new ComponentPositioning(1, 1), RedrawingMode.SHOW_VALUE, ControlType.RGB_CONTROL,
                NO_CUSTOM_DRAWING, true));

        return gridPane;
    }

    private void updateTemp() {
        manager.updateMenuItem(this, menuDef.getStatusCaseTempOC(), Math.random() * 100);
        manager.updateMenuItem(this, menuDef.getLED1Brightness(), Math.random() * 100 );
    }

    private void putIntoGrid(MenuItem item, ComponentSettings componentSettings) {
        ComponentPositioning pos = componentSettings.getPosition();
        var component = editorFactory.getComponentEditorItem(item, componentSettings, this::noAction);
        component.ifPresent(comp -> {
            controlsBeingManaged.put(item.getId(), comp);
            gridPane.add(comp.createComponent(), pos.getCol(), pos.getRow(), pos.getColSpan(), pos.getRowSpan());
        });
    }

    private void noAction(MenuItem menuItem) {
    }

    @Override
    public String getPanelName() {
        if (componentControl == null) return "empty";
        return "Status Panel";
    }

    @Override
    public boolean canBeRemoved() {
        return true;
    }

    @Override
    public boolean canClose() {
        return true;
    }

    @Override
    public void closePanel() {
        controlsBeingManaged.clear();
    }

    @Override
    public void itemHasUpdated(MenuItem item) {
        if(controlsBeingManaged.containsKey(item.getId())) {
            controlsBeingManaged.get(item.getId()).onItemUpdated(item, manager.getManagedMenu().getMenuState(item));
        }
    }

    @Override
    public void tickAll() {
        for(var component : controlsBeingManaged.values()) {
            component.tick();
        }
    }

}
