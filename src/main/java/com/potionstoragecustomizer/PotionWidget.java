package com.potionstoragecustomizer;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ScriptEvent;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetConfig;

@Slf4j
class PotionWidget {
    Widget container;
    Widget icon;
    Widget nameLabel;
    Widget doseLabel;
    Widget favButton;
    int index;
    PotionSectionWidget section;

    public PotionWidget(Widget container, Widget nameLabel, Widget icon, Widget doseLabel, Widget favButton,
            PotionSectionWidget favouritesSection, int index) {
        this.container = container;
        this.icon = icon;
        this.nameLabel = nameLabel;
        this.doseLabel = doseLabel;
        this.favButton = favButton;
        this.section = favouritesSection;
        this.index = index;
    }

    public void setDraggable(boolean draggable, PotionStorageCustomizerPlugin plugin) {
        int mask = container.getClickMask();
        Client client = plugin.getClient();
        if (draggable) {
            container.setDragParent(section.boundary);
            container.setClickMask(mask | WidgetConfig.DRAG | WidgetConfig.DRAG_ON);

            container.setOnDragCompleteListener((JavaScriptCallback) event -> {
                log.info("Drag complete callback triggered");
                log.info("Mouse X: {}, Mouse Y: {}", client.getMouseCanvasPosition().getX(),
                        client.getMouseCanvasPosition().getY());

                PotionWidget target = section.getPotionAtPosition(client.getMouseCanvasPosition().getX(),
                        client.getMouseCanvasPosition().getY());

                if (target != null && target != this) {
                    log.info("Found target: {}", target.nameLabel.getText());
                    swap(section.getPositionByContainer(event.getSource()), target, plugin);
                }
            });
        } else {
            container.setClickMask(mask & ~WidgetConfig.DRAG & ~WidgetConfig.DRAG_ON);
        }
    }

    public String getName() {
        String regex = "\\([0-9]\\)";
        // trim dose text from name
        String trimmedName = nameLabel.getText().replaceAll(regex, "");

        return trimmedName;
    }

    public void swap(PotionWidget source, PotionWidget target, PotionStorageCustomizerPlugin plugin) {
        if (source == null || target == null) {
            log.warn("source or target are null, cannot swap");
            return;
        }

        if (source.section != target.section) {
            log.warn("cannot swap potions from different sections");
            return;
        }

        log.info("source {} | target {}", source.nameLabel.getText(), target.nameLabel.getText());

        int sourceContainerX = source.container.getOriginalX();
        int sourceContainerY = source.container.getOriginalY();
        int sourceIconX = source.icon.getOriginalX();
        int sourceIconY = source.icon.getOriginalY();
        int sourceNameX = source.nameLabel.getOriginalX();
        int sourceNameY = source.nameLabel.getOriginalY();
        int sourceDoseX = source.doseLabel.getOriginalX();
        int sourceDoseY = source.doseLabel.getOriginalY();
        int sourceFavX = source.favButton.getOriginalX();
        int sourceFavY = source.favButton.getOriginalY();

        int targetContainerX = target.container.getOriginalX();
        int targetContainerY = target.container.getOriginalY();
        int targetIconX = target.icon.getOriginalX();
        int targetIconY = target.icon.getOriginalY();
        int targetNameX = target.nameLabel.getOriginalX();
        int targetNameY = target.nameLabel.getOriginalY();
        int targetDoseX = target.doseLabel.getOriginalX();
        int targetDoseY = target.doseLabel.getOriginalY();
        int targetFavX = target.favButton.getOriginalX();
        int targetFavY = target.favButton.getOriginalY();

        source.container.setOriginalX(targetContainerX);
        source.container.setOriginalY(targetContainerY);
        source.icon.setOriginalX(targetIconX);
        source.icon.setOriginalY(targetIconY);
        source.nameLabel.setOriginalX(targetNameX);
        source.nameLabel.setOriginalY(targetNameY);
        source.doseLabel.setOriginalX(targetDoseX);
        source.doseLabel.setOriginalY(targetDoseY);
        source.favButton.setOriginalX(targetFavX);
        source.favButton.setOriginalY(targetFavY);

        target.container.setOriginalX(sourceContainerX);
        target.container.setOriginalY(sourceContainerY);
        target.icon.setOriginalX(sourceIconX);
        target.icon.setOriginalY(sourceIconY);
        target.nameLabel.setOriginalX(sourceNameX);
        target.nameLabel.setOriginalY(sourceNameY);
        target.doseLabel.setOriginalX(sourceDoseX);
        target.doseLabel.setOriginalY(sourceDoseY);
        target.favButton.setOriginalX(sourceFavX);
        target.favButton.setOriginalY(sourceFavY);

        source.container.revalidate();
        source.icon.revalidate();
        source.nameLabel.revalidate();
        source.doseLabel.revalidate();
        source.favButton.revalidate();

        target.container.revalidate();
        target.icon.revalidate();
        target.nameLabel.revalidate();
        target.doseLabel.revalidate();
        target.favButton.revalidate();

        plugin.savePosition(source);
        plugin.savePosition(target);
    }

    public void hide() {
        container.setHidden(true);
        icon.setHidden(true);
        nameLabel.setHidden(true);
        doseLabel.setHidden(true);
        favButton.setHidden(true);
    }
}
