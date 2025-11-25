package com.potionstoragecustomizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.potionstoragecustomizer.PotionStorageCustomizerPlugin.PotionPositions;

import lombok.extern.slf4j.Slf4j;
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

    public void enableDrag(boolean dragEnabled) {
        for (PotionSectionWidget section : potionSections) {
            for (PotionWidget potion : section.potions) {
                potion.setDraggable(dragEnabled, plugin);
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

    // public void movePotionToBottomOfSection(PotionWidget potion) {
    // PotionSectionWidget targetSection = null;
    //
    // for (PotionSectionWidget section : potionSections) {
    // if (section.category == potion.section.category) {
    // targetSection = section;
    // break;
    // }
    // }
    //
    // if (targetSection == null || targetSection.potions.isEmpty()) {
    // return;
    // }
    //
    // PotionWidget lastPotion =
    // targetSection.potions.get(targetSection.potions.size() - 1);
    // int lastIndex = lastPotion.index;
    //
    // int lastY = lastPotion.container.getOriginalY();
    // int lastX = lastPotion.container.getOriginalX();
    // int height = lastPotion.container.getOriginalHeight();
    // int width = lastPotion.container.getOriginalWidth();
    //
    // int newX, newY;
    //
    // if (lastIndex % 2 == 0) {
    // newX = lastX + width;
    // newY = lastY;
    // } else {
    // newX = 0;
    // newY = lastY + height;
    // targetSection.increaseBoundaryHeight(1);
    // }
    //
    // int iconOffsetX = potion.icon.getOriginalX() -
    // potion.container.getOriginalX();
    // int iconOffsetY = potion.icon.getOriginalY() -
    // potion.container.getOriginalY();
    // int nameOffsetX = potion.nameLabel.getOriginalX() -
    // potion.container.getOriginalX();
    // int nameOffsetY = potion.nameLabel.getOriginalY() -
    // potion.container.getOriginalY();
    // int doseOffsetX = potion.doseLabel.getOriginalX() -
    // potion.container.getOriginalX();
    // int doseOffsetY = potion.doseLabel.getOriginalY() -
    // potion.container.getOriginalY();
    // int favOffsetX = potion.favButton.getOriginalX() -
    // potion.container.getOriginalX();
    // int favOffsetY = potion.favButton.getOriginalY() -
    // potion.container.getOriginalY();
    //
    // potion.container.setOriginalX(newX);
    // potion.container.setOriginalY(newY);
    // potion.icon.setOriginalX(newX + iconOffsetX);
    // potion.icon.setOriginalY(newY + iconOffsetY);
    // potion.nameLabel.setOriginalX(newX + nameOffsetX);
    // potion.nameLabel.setOriginalY(newY + nameOffsetY);
    // potion.doseLabel.setOriginalX(newX + doseOffsetX);
    // potion.doseLabel.setOriginalY(newY + doseOffsetY);
    // potion.favButton.setOriginalX(newX + favOffsetX);
    // potion.favButton.setOriginalY(newY + favOffsetY);
    //
    // potion.container.revalidate();
    // potion.icon.revalidate();
    // potion.nameLabel.revalidate();
    // potion.doseLabel.revalidate();
    // potion.favButton.revalidate();
    // }

    public void applyCustomPositions(Map<String, PotionPositions> savedPositions) {
        for (PotionSectionWidget section : potionSections) {
            for (PotionWidget potion : section.potions) {
                String potionName = potion.getName();
                PotionPositions pos = savedPositions.get(potionName);

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

                potion.container.revalidate();
                potion.icon.revalidate();
                potion.nameLabel.revalidate();
                potion.doseLabel.revalidate();
                potion.favButton.revalidate();
            }
        }
    }
}
