package com.potionstoragecustomizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.potionstoragecustomizer.PotionStorageCustomizerPlugin.PotionPositions;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Point;
import net.runelite.api.widgets.Widget;

@Slf4j
class PotionPanelWidget {
    List<PotionSectionWidget> potionSections;
    Widget panel;
    Widget vialCount;

    private final PotionStorageCustomizerPlugin plugin;

    public PotionPanelWidget(PotionStorageCustomizerPlugin plugin) {
        potionSections = new ArrayList<>();
        this.plugin = plugin;
    }

    public void enableDrag(boolean dragEnabled, Map<String, PotionPositions> savedPositions) {
        for (PotionSectionWidget section : potionSections) {
            for (PotionWidget potion : section.potions) {
                potion.setDraggable(dragEnabled, plugin, this, savedPositions);
            }
        }
    }

    public void hidePanel() {
        for (PotionSectionWidget section : potionSections) {
            for (PotionWidget potion : section.potions) {
                potion.hide();
            }
        }
    }

    public PotionWidget getPotionByName(String name) {
        for (PotionSectionWidget section : potionSections) {
            for (PotionWidget potion : section.potions) {
                if (potion.nameLabel.getText().equals(name)) {
                    return potion;
                }
            }
        }
        return null;
    }

    public void savePosition(PotionWidget potion, Map<String, PotionPositions> savedPositions) {
        PotionPositions pos = new PotionPositions();
        pos.container = new Point(potion.container.getOriginalX(), potion.container.getOriginalY());
        pos.icon = new Point(potion.icon.getOriginalX(), potion.icon.getOriginalY());
        pos.nameLabel = new Point(potion.nameLabel.getOriginalX(), potion.nameLabel.getOriginalY());
        pos.doseLabel = new Point(potion.doseLabel.getOriginalX(), potion.doseLabel.getOriginalY());
        pos.favButton = new Point(potion.favButton.getOriginalX(), potion.favButton.getOriginalY());
        pos.index = potion.index;
        pos.section = potion.section.category;

        savedPositions.put(potion.getName(), pos);
        log.info("{} - index: {}, section: {}", potion.getName(), pos.index, pos.section);
    }

    public void recalculateAllPositions(Map<String, PotionPositions> savedPositions) {
        for (PotionSectionWidget section : potionSections) {
            sortPotionsByCustomOrder(section, savedPositions);
            repositionSectionPotions(section, savedPositions);
        }
    }

    private void sortPotionsByCustomOrder(PotionSectionWidget section, Map<String, PotionPositions> savedPositions) {
        section.potions.sort((a, b) -> {
            PotionPositions posA = savedPositions.get(a.getName());
            PotionPositions posB = savedPositions.get(b.getName());

            int indexA = (posA != null && posA.section == section.category)
                    ? posA.index
                    : Integer.MAX_VALUE;
            int indexB = (posB != null && posB.section == section.category)
                    ? posB.index
                    : Integer.MAX_VALUE;
            return Integer.compare(indexA, indexB);
        });
    }

    private void repositionSectionPotions(PotionSectionWidget section, Map<String, PotionPositions> savedPositions) {
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

            savePosition(potion, savedPositions);
        }
    }

    public void pruneMissingPotions(Map<String, PotionPositions> savedPositions) {
        Set<String> currentPotions = new HashSet<>();
        for (PotionSectionWidget section : potionSections) {
            for (PotionWidget potion : section.potions) {
                currentPotions.add(potion.getName());
            }
        }
        savedPositions.entrySet().removeIf(entry -> !currentPotions.contains(entry.getKey()));
    }

    public boolean detectLayoutChange(Map<PotionSectionWidget.Category, Integer> lastSectionCounts) {
        boolean changed = false;
        for (PotionSectionWidget section : potionSections) {
            Integer lastCount = lastSectionCounts.get(section.category);
            int currentCount = section.potions.size();
            if (lastCount == null || lastCount != currentCount) {
                changed = true;
            }
            lastSectionCounts.put(section.category, currentCount);
        }
        return changed;
    }

    public void applyCustomPositions(Map<String, PotionPositions> savedPositions) {
        for (PotionSectionWidget section : potionSections) {
            for (PotionWidget potion : section.potions) {
                String potionName = potion.getName();
                PotionPositions pos = savedPositions.get(potionName);

                if (pos == null)
                    continue;

                potion.container.setOriginalX(pos.container.getX());
                potion.container.setOriginalY(pos.container.getY());
                potion.icon.setOriginalX(pos.icon.getX());
                potion.icon.setOriginalY(pos.icon.getY());
                potion.nameLabel.setOriginalX(pos.nameLabel.getX());
                potion.nameLabel.setOriginalY(pos.nameLabel.getY());
                potion.doseLabel.setOriginalX(pos.doseLabel.getX());
                potion.doseLabel.setOriginalY(pos.doseLabel.getY());
                potion.favButton.setOriginalX(pos.favButton.getX());
                potion.favButton.setOriginalY(pos.favButton.getY());

                potion.index = pos.index;

                potion.container.revalidate();
                potion.icon.revalidate();
                potion.nameLabel.revalidate();
                potion.doseLabel.revalidate();
                potion.favButton.revalidate();
            }
        }
    }
}
