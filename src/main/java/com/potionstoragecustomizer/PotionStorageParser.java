package com.potionstoragecustomizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.potionstoragecustomizer.PotionSectionWidget.Category;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Point;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;

@Slf4j
class PotionStorageParser {
    static final int ICON_OFFSET = 1;
    static final int NAME_OFFSET = 2;
    static final int DOSE_OFFSET = 3;
    static final int FAV_OFFSET = 4;
    static final int NEXT_POTION_OFFSET = 5;
    static final int REMOVE_FAVOURITE_SPRITE_ID = 1340;
    static final int FAVOURITE_SPRITE_ID = 1341;

    static final int POTION_STORAGE_PLUGIN_BAR_OFFSET = 3;

    public static PotionPanelWidget parse(Widget[] flatArray, Widget psItems, PotionStorageCustomizerPlugin plugin) {
        PotionPanelWidget panel = new PotionPanelWidget(plugin);
        PotionSectionWidget favouritesSection = new PotionSectionWidget(Category.FAVOURITES);
        PotionSectionWidget potionsSection = new PotionSectionWidget(Category.POTIONS);
        PotionSectionWidget unfinishedSection = new PotionSectionWidget(Category.UNFINISHED_POTIONS);

        for (int i = 0; i < flatArray.length;) {
            if (flatArray[i].getText().contains("Vials")) {
                i++;
                continue;
            }

            if (flatArray[i].getType() == WidgetType.RECTANGLE && !flatArray[i].isHidden()
                    && flatArray[i].getHeight() < potionsSection.potions.get(0).container.getOriginalHeight()
                    && flatArray[i].getWidth() < psItems.getWidth() / 2) {
                List<PotionSectionWidget> sections = new ArrayList<PotionSectionWidget>();
                sections.add(favouritesSection);
                sections.add(potionsSection);
                sections.add(unfinishedSection);
                parsePotionStorageBars(i, flatArray, sections);
                i += POTION_STORAGE_PLUGIN_BAR_OFFSET;
                continue;
            }

            if (flatArray[i].getText().contains("Favourites")) {
                favouritesSection.createBoundary(flatArray[i + 1].getOriginalHeight(), flatArray[i + 1].getOriginalY(),
                        psItems);
                i += 2;
                continue;
            } else if (flatArray[i].getText().equals("Potions")) {
                potionsSection.createBoundary(flatArray[i + 1].getOriginalHeight(), flatArray[i + 1].getOriginalY(),
                        psItems);
                i += 2;
                continue;
            } else if (flatArray[i].getText().equals("Unfinished Potions")) {
                unfinishedSection.createBoundary(flatArray[i + 1].getOriginalHeight(), flatArray[i + 1].getOriginalY(),
                        psItems);
                i += 2;
                continue;
            } else if ((flatArray[i].isSelfHidden() || flatArray[i].isHidden()) && !Arrays
                    .asList(FAVOURITE_SPRITE_ID, REMOVE_FAVOURITE_SPRITE_ID)
                    .contains(flatArray[i].getSpriteId())) {
                i++;
            }

            if (i + FAV_OFFSET < flatArray.length) {
                Widget container = flatArray[i];
                Widget name = flatArray[i + NAME_OFFSET];
                Widget icon = flatArray[i + ICON_OFFSET];
                Widget dose = flatArray[i + DOSE_OFFSET];
                Widget favourite = flatArray[i + FAV_OFFSET];
                int favouriteSpriteId = flatArray[i + FAV_OFFSET].getSpriteId();

                if (favouriteSpriteId == REMOVE_FAVOURITE_SPRITE_ID) {
                    PotionWidget potion = new PotionWidget(container, name, icon, dose, favourite, favouritesSection,
                            favouritesSection.potions.size());
                    favouritesSection.potions.add(potion);
                    i += NEXT_POTION_OFFSET;
                    continue;
                } else if (favouriteSpriteId == FAVOURITE_SPRITE_ID) {
                    if (name.getText().contains("(unf)")) {
                        PotionWidget potion = new PotionWidget(container, name, icon, dose, favourite,
                                unfinishedSection, unfinishedSection.potions.size());
                        unfinishedSection.potions.add(potion);
                        i += NEXT_POTION_OFFSET;
                        continue;
                    } else {
                        PotionWidget potion = new PotionWidget(container, name, icon, dose, favourite,
                                potionsSection, potionsSection.potions.size());
                        potionsSection.potions.add(potion);
                        i += NEXT_POTION_OFFSET;
                        continue;
                    }
                }
            }
            i++;
        }

        panel.potionSections.add(favouritesSection);
        panel.potionSections.add(potionsSection);
        panel.potionSections.add(unfinishedSection);

        return panel;
    }

    public static void parsePotionStorageBars(int start, Widget[] flatArray, List<PotionSectionWidget> sections) {
        Widget bar = flatArray[start];
        Widget subBar = flatArray[start + 1];
        Widget barText = flatArray[start + 2];

        int barX = bar.getOriginalX();
        int barY = bar.getOriginalY();

        for (PotionSectionWidget section : sections) {
            for (PotionWidget potion : section.potions) {
                int doseX = potion.doseLabel.getOriginalX();
                int doseY = potion.doseLabel.getOriginalY();

                if (barX == doseX && barY == doseY) {
                    potion.setPotionBar(bar, subBar, barText);
                    return;
                }
            }
        }
    }
}
