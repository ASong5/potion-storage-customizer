package com.potionstoragecustomizer;

import java.util.Arrays;

import com.potionstoragecustomizer.PotionSectionWidget.Category;

import net.runelite.api.widgets.Widget;

class PotionStorageParser {
    static final int ICON_OFFSET = 1;
    static final int NAME_OFFSET = 2;
    static final int DOSE_OFFSET = 3;
    static final int FAV_OFFSET = 4;
    static final int NEXT_POTION_OFFSET = 5;

    static final int REMOVE_FAVOURITE_SPRITE_ID = 1340;
    static final int FAVOURITE_SPRITE_ID = 1341;

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
}
