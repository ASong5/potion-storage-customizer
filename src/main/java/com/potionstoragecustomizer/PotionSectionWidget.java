package com.potionstoragecustomizer;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;

@Slf4j
class PotionSectionWidget {
    enum Category {
        FAVOURITES,
        POTIONS,
        UNFINISHED_POTIONS
    }

    List<PotionWidget> potions;
    Category category;
    Widget boundary;

    public PotionSectionWidget(Category cat) {
        potions = new ArrayList<>();
        this.category = cat;
    }

    public PotionWidget getPositionByContainer(Widget container) {
        for (PotionWidget potion : potions) {
            if (potion.container == container) {
                return potion;
            }
        }
        return null;
    }

    public PotionWidget getPotionAtPosition(int x, int y) {
        for (PotionWidget potion : potions) {
            Widget container = potion.container;
            if (container.getBounds().contains(x, y)) {
                return potion;
            }
        }
        return null;
    }

    public void increaseBoundaryHeight(int count) {
        this.boundary.setOriginalHeight(
                this.boundary.getOriginalHeight() + potions.get(0).container.getOriginalHeight() * count);
    }

    public void createBoundary(int height, int y, Widget psItems) {
        this.boundary = psItems.createChild(WidgetType.RECTANGLE);
        this.boundary.setOriginalWidth(psItems.getWidth());
        this.boundary.setOriginalHeight(height);
        this.boundary.setOriginalY(y);
        this.boundary.setOriginalX(0);

        this.boundary.revalidate();
    }
}
