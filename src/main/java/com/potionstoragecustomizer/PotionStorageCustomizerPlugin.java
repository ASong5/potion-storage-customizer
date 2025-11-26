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
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(name = "Potion Storage Customizer")
public class PotionStorageCustomizerPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private PotionStorageCustomizerConfig config;

    @Inject
    ConfigManager configManager;

    @Inject
    private Gson gson;

    @Data
    static class PotionPositions {
        Point container;
        Point icon;
        Point nameLabel;
        Point doseLabel;
        Point favButton;
        int index;
        PotionSectionWidget.Category section;
    }

    private Map<PotionSectionWidget.Category, Integer> lastSectionCounts = new HashMap<>();

    private final Map<String, PotionPositions> savedPositions = new HashMap<>();
    private PotionPanelWidget panel;

    @Subscribe
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
            log.info("detectLayoutChanged()");
        } else {
            panel.applyCustomPositions(savedPositions);
        }
        panel.enableDrag(true, savedPositions);

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

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
        }
    }

    @Provides
    PotionStorageCustomizerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(PotionStorageCustomizerConfig.class);
    }
}
