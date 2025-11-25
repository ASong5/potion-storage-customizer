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
        panel = PotionStorageParser.parse(potions, psItems, this);

        pruneMissingPotions();

        boolean layoutChanged = detectLayoutChange(panel);

        if (layoutChanged) {
            recalculateAllPositions(panel);
        } else {
            panel.applyCustomPositions(savedPositions);

            for (PotionSectionWidget section : panel.potionSections) {
                for (PotionWidget potion : section.potions) {
                    if (savedPositions.get(potion.nameLabel.getText()) == null) {
                        savePosition(potion);
                    }
                }
            }
        }

        panel.enableDrag(true);
    }

    private void pruneMissingPotions() {
        Set<String> currentPotions = new HashSet<>();

        for (PotionSectionWidget section : panel.potionSections) {
            for (PotionWidget potion : section.potions) {
                currentPotions.add(potion.getName());
            }
        }

        savedPositions.entrySet().removeIf(entry -> !currentPotions.contains(entry.getKey()));

        savePositionsToConfig();
    }

    private Map<PotionSectionWidget.Category, Integer> lastSectionCounts = new HashMap<>();

    private boolean detectLayoutChange(PotionPanelWidget panel) {
        boolean changed = false;

        for (PotionSectionWidget section : panel.potionSections) {
            Integer lastCount = lastSectionCounts.get(section.category);
            int currentCount = section.potions.size();

            if (lastCount == null || lastCount != currentCount) {
                changed = true;
            }

            lastSectionCounts.put(section.category, currentCount);
        }

        return changed;
    }

    private void recalculateAllPositions(PotionPanelWidget panel) {
        for (PotionSectionWidget section : panel.potionSections) {
            sortPotionsByCustomOrder(section);
            repositionSectionPotions(section);
        }
    }

    private void sortPotionsByCustomOrder(PotionSectionWidget section) {
        section.potions.sort((a, b) -> {
            PotionPositions posA = savedPositions.get(a.nameLabel.getText());
            PotionPositions posB = savedPositions.get(b.nameLabel.getText());

            int indexA = (posA != null && posA.section == section.category)
                    ? posA.index
                    : Integer.MAX_VALUE;
            int indexB = (posB != null && posB.section == section.category)
                    ? posB.index
                    : Integer.MAX_VALUE;

            return Integer.compare(indexA, indexB);
        });
    }

    private void repositionSectionPotions(PotionSectionWidget section) {
        if (section.potions.isEmpty())
            return;

        int baseX = Integer.MAX_VALUE;
        int baseY = Integer.MAX_VALUE;

        for (PotionWidget potion : section.potions) {
            baseX = Math.min(baseX, potion.container.getOriginalX());
            baseY = Math.min(baseY, potion.container.getOriginalY());
        }

        PotionWidget firstPotion = section.potions.get(0);
        int width = firstPotion.container.getOriginalWidth();
        int height = firstPotion.container.getOriginalHeight();

        for (int i = 0; i < section.potions.size(); i++) {
            PotionWidget potion = section.potions.get(i);

            int row = i / 2;
            int col = i % 2;

            int newX = baseX + (col * width);
            int newY = baseY + (row * height);

            int iconOffsetX = potion.icon.getOriginalX() - potion.container.getOriginalX();
            int iconOffsetY = potion.icon.getOriginalY() - potion.container.getOriginalY();
            int nameOffsetX = potion.nameLabel.getOriginalX() - potion.container.getOriginalX();
            int nameOffsetY = potion.nameLabel.getOriginalY() - potion.container.getOriginalY();
            int doseOffsetX = potion.doseLabel.getOriginalX() - potion.container.getOriginalX();
            int doseOffsetY = potion.doseLabel.getOriginalY() - potion.container.getOriginalY();
            int favOffsetX = potion.favButton.getOriginalX() - potion.container.getOriginalX();
            int favOffsetY = potion.favButton.getOriginalY() - potion.container.getOriginalY();

            potion.container.setOriginalX(newX);
            potion.container.setOriginalY(newY);
            potion.icon.setOriginalX(newX + iconOffsetX);
            potion.icon.setOriginalY(newY + iconOffsetY);
            potion.nameLabel.setOriginalX(newX + nameOffsetX);
            potion.nameLabel.setOriginalY(newY + nameOffsetY);
            potion.doseLabel.setOriginalX(newX + doseOffsetX);
            potion.doseLabel.setOriginalY(newY + doseOffsetY);
            potion.favButton.setOriginalX(newX + favOffsetX);
            potion.favButton.setOriginalY(newY + favOffsetY);

            potion.index = i;

            potion.container.revalidate();
            potion.icon.revalidate();
            potion.nameLabel.revalidate();
            potion.doseLabel.revalidate();
            potion.favButton.revalidate();

            savePosition(potion);
        }
    }

    public void savePosition(PotionWidget potion) {
        PotionPositions pos = new PotionPositions();
        pos.container = new Point(potion.container.getOriginalX(), potion.container.getOriginalY());
        pos.icon = new Point(potion.icon.getOriginalX(), potion.icon.getOriginalY());
        pos.nameLabel = new Point(potion.nameLabel.getOriginalX(), potion.nameLabel.getOriginalY());
        pos.doseLabel = new Point(potion.doseLabel.getOriginalX(), potion.doseLabel.getOriginalY());
        pos.favButton = new Point(potion.favButton.getOriginalX(), potion.favButton.getOriginalY());
        pos.index = potion.index;
        pos.section = potion.section.category;

        savedPositions.put(potion.getName(), pos);
        savePositionsToConfig();
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
        savedPositions.putAll(gson.fromJson(json, new TypeToken<Map<String, PotionPositions>>() {
        }.getType()));
        log.info("Loaded {} saved positions", savedPositions.size());
    }

    public Client getClient() {
        return client;
    }

    @Override
    protected void startUp() {
        // configManager.unsetConfiguration("potionstoragecustomizer", "potionPositions");
        loadPositionsFromConfig();
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
