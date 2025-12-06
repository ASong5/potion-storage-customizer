package com.potionstoragecustomizer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Point;
import net.runelite.api.ScriptID;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.PluginChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.antidrag.AntiDragConfig;
import net.runelite.client.plugins.antidrag.AntiDragPlugin;
import net.runelite.client.plugins.PluginManager;

@Slf4j
@PluginDescriptor(name = "Potion Storage Customizer")
@PluginDependency(AntiDragPlugin.class)
public class PotionStorageCustomizerPlugin extends Plugin {
    static final String CONFIG_GROUP = "potionsstoragecustomizer";

    @Inject
    private Client client;

    @Inject
    ConfigManager configManager;

    @Inject
    private PotionStorageCustomizerConfig config;

    @Inject
    private PluginManager pluginManager;

    @Inject
    private AntiDragPlugin antiDragPlugin;

    @Inject
    private Gson gson;

    @Data
    static class PotionPositions {
        Point container;
        Point icon;
        Point nameLabel;
        Point doseLabel;
        Point favButton;
        Point bar;
        Point subBar;
        Point barText;
        int index;
        PotionSectionWidget.Category section;
    }

    private Map<PotionSectionWidget.Category, Integer> lastSectionCounts = new HashMap<>();

    private final Map<String, PotionPositions> savedPositions = new HashMap<>();
    private PotionPanelWidget panel;

    // ensure this runs after potion storage bars plugin
    @Subscribe(priority = -1)
    protected void onScriptPostFired(ScriptPostFired event) {
        if (event.getScriptId() == ScriptID.POTIONSTORE_BUILD) {
            rebuildPanel();
        }
    }

    private void rebuildPanel() {
        Widget psItems = client.getWidget(InterfaceID.Bankmain.POTIONSTORE_ITEMS);
        if (psItems == null)
            return;

        Widget[] potions = psItems.getDynamicChildren();
        if (potions.length == 0)
            return;

        panel = PotionStorageParser.parse(potions, psItems, this);

        panel.pruneMissingPotions(savedPositions);

        boolean layoutChanged = panel.detectLayoutChange(lastSectionCounts);
        if (layoutChanged) {
            panel.recalculateAllPositions(savedPositions);
        } else {
            panel.applyCustomPositions(savedPositions);
        }
        panel.enableDrag(!config.disableDrag(), savedPositions);

        savePositionsToConfig();
        saveSectionCountsToConfig();
    }

    private void savePositionsToConfig() {
        String json = gson.toJson(savedPositions);
        configManager.setConfiguration("potionstoragecustomizer", "potionPositions", json);
    }

    private void loadPositionsFromConfig() {
        String json = configManager.getConfiguration("potionstoragecustomizer", "potionPositions");
        if (json == null || json.isEmpty()) {
            return;
        }
        savedPositions
                .putAll(gson.fromJson(json, new TypeToken<Map<String, PotionPositions>>() {
                }.getType()));
        log.info("Loaded {} saved positions", savedPositions.size());
    }

    private void saveSectionCountsToConfig() {
        String json = gson.toJson(lastSectionCounts);
        configManager.setConfiguration("potionstoragecustomizer", "sectionCounts", json);
    }

    private void loadSectionCountsFromConfig() {
        String json = configManager.getConfiguration("potionstoragecustomizer", "sectionCounts");
        if (json == null || json.isEmpty()) {
            return;
        }
        lastSectionCounts.putAll(gson.fromJson(json,
                new TypeToken<Map<PotionSectionWidget.Category, Integer>>() {
                }.getType()));
        log.info("Loaded section counts: {}", lastSectionCounts);
    }

    private boolean isAntiDragActive() {
        return antiDragPlugin != null && pluginManager.isPluginEnabled(antiDragPlugin);
    }

    public int getAntiDragDelay() {
        if (!isAntiDragActive()) {
            return 0;
        }
        return configManager.getConfig(AntiDragConfig.class).dragDelay();
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (panel == null)
            return;

        if ("antiDrag".equals(event.getGroup())) {
            int delay = getAntiDragDelay();
            panel.updateDragDeadTime(delay);
        }

        if (CONFIG_GROUP.equals(event.getGroup())) {
            panel.enableDrag(!config.disableDrag(), savedPositions);
        }
    }

    @Subscribe
    public void onPluginChanged(PluginChanged event) {
        if (event.getPlugin() != null &&
                event.getPlugin().getClass().equals(AntiDragPlugin.class) &&
                panel != null) {
            int delay = getAntiDragDelay();
            panel.updateDragDeadTime(delay);
        }
    }

    @Provides
    PotionStorageCustomizerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(PotionStorageCustomizerConfig.class);
    }

    public Client getClient() {
        return client;
    }

    @Override
    protected void startUp() {
        // configManager.unsetConfiguration("potionstoragecustomizer",
        // "potionPositions");
        loadPositionsFromConfig();
        loadSectionCountsFromConfig();
    }

    @Override
    protected void shutDown() throws Exception {
        log.debug("Potion Storage Customizer stopped!");
    }
}
