package com.potionstoragecustomizer;

import java.util.Map;

import com.potionstoragecustomizer.PotionStorageCustomizerPlugin.PotionPositions;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarbitID;
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

    Widget potionBar;
    Widget potionSubBar;
    Widget potionBarText;

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

    public void setDraggable(boolean draggable, PotionStorageCustomizerPlugin plugin, PotionPanelWidget panel,
            Map<String, PotionPositions> savedPositions) {
        int mask = container.getClickMask();
        Client client = plugin.getClient();
        if (draggable) {
            container.setDragParent(section.boundary);
            container.setClickMask(mask | WidgetConfig.DRAG | WidgetConfig.DRAG_ON);

            int antiDragDelay = plugin.getAntiDragDelay();
            container.setDragDeadTime(antiDragDelay);

            log.info("Set drag dead time to {} client ticks for {}", antiDragDelay, nameLabel.getText());

            container.setOnDragCompleteListener((JavaScriptCallback) event -> {
                log.info("Drag complete callback triggered");
                log.info("Mouse X: {}, Mouse Y: {}", client.getMouseCanvasPosition().getX(),
                        client.getMouseCanvasPosition().getY());

                PotionWidget target = section.getPotionAtPosition(client.getMouseCanvasPosition().getX(),
                        client.getMouseCanvasPosition().getY());

                if (target != null && target != this) {
                    log.info("Found target: {}", target.nameLabel.getText());
                    boolean isInsertMode = client.getVarbitValue(VarbitID.BANK_INSERTMODE) == 1;
                    log.info("insert mode: {}", isInsertMode);

                    if (isInsertMode) {
                        insert(section.getPositionByContainer(event.getSource()), target, plugin, panel,
                                savedPositions);
                    } else {
                        swap(section.getPositionByContainer(event.getSource()), target, plugin, panel, savedPositions);
                    }
                }
            });
        } else {
            container.setClickMask(mask & ~WidgetConfig.DRAG & ~WidgetConfig.DRAG_ON);
            container.setDragParent(null);
            container.setDragDeadTime(0);
            container.setOnDragCompleteListener((Object[]) null);
        }
    }

    public void changeDragDelay(int newDelay) {
        container.setDragDeadTime(newDelay);
    }

    public String getName() {
        String regex = "\\([0-9]+\\)";
        // trim dose text from name
        String trimmedName = nameLabel.getText().replaceAll(regex, "").trim();

        return trimmedName;
    }

    public void setPotionBar(Widget bar, Widget subBar, Widget barText) {
        potionBar = bar;
        potionSubBar = subBar;
        potionBarText = barText;
    }

    public void insert(PotionWidget source, PotionWidget target, PotionStorageCustomizerPlugin plugin,
            PotionPanelWidget panel, Map<String, PotionPositions> savedPositions) {
        if (source == null || target == null) {
            log.warn("source or target are null, cannot insert");
            return;
        }

        if (source.section != target.section) {
            log.warn("cannot insert potions from different sections");
            return;
        }

        int sourceIndex = source.index;
        int targetIndex = target.index;

        section.potions.remove(sourceIndex);
        section.potions.add(targetIndex, source);

        for (int i = 0; i < section.potions.size(); i++) {
            section.potions.get(i).index = i;
        }

        for (PotionWidget potion : section.potions) {
            PotionPositions pos = savedPositions.get(potion.getName());
            if (pos != null) {
                pos.index = potion.index;
            }
        }

        panel.recalculateAllPositions(savedPositions);
    }

    public void swap(PotionWidget source, PotionWidget target, PotionStorageCustomizerPlugin plugin,
            PotionPanelWidget panel, Map<String, PotionPositions> savedPositions) {
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

        if (source.potionBar != null && source.potionSubBar != null && source.potionBarText != null
                && target.potionBar != null && target.potionSubBar != null && target.potionBarText != null) {
            int sourceBarX = source.potionBar.getOriginalX();
            int sourceBarY = source.potionBar.getOriginalY();
            int sourceSubBarX = source.potionSubBar.getOriginalX();
            int sourceSubBarY = source.potionSubBar.getOriginalY();
            int sourceBarTextX = source.potionBarText.getOriginalX();
            int sourceBarTextY = source.potionBarText.getOriginalY();

            int targetBarX = target.potionBar.getOriginalX();
            int targetBarY = target.potionBar.getOriginalY();
            int targetSubBarX = target.potionSubBar.getOriginalX();
            int targetSubBarY = target.potionSubBar.getOriginalY();
            int targetBarTextX = target.potionBarText.getOriginalX();
            int targetBarTextY = target.potionBarText.getOriginalY();

            source.potionBar.setOriginalX(targetBarX);
            source.potionBar.setOriginalY(targetBarY);
            target.potionBar.setOriginalX(sourceBarX);
            target.potionBar.setOriginalY(sourceBarY);

            source.potionSubBar.setOriginalX(targetSubBarX);
            source.potionSubBar.setOriginalY(targetSubBarY);
            target.potionSubBar.setOriginalX(sourceSubBarX);
            target.potionSubBar.setOriginalY(sourceSubBarY);

            source.potionBarText.setOriginalX(targetBarTextX);
            source.potionBarText.setOriginalY(targetBarTextY);
            target.potionBarText.setOriginalX(sourceBarTextX);
            target.potionBarText.setOriginalY(sourceBarTextY);

            source.potionBar.revalidate();
            source.potionSubBar.revalidate();
            source.potionBarText.revalidate();

            target.potionBar.revalidate();
            target.potionSubBar.revalidate();
            target.potionBarText.revalidate();

            log.info("potion bars swapped!");
        }

        target.favButton.revalidate();

        int tempIndex = source.index;
        source.index = target.index;
        target.index = tempIndex;

        panel.savePosition(source, savedPositions);
        panel.savePosition(target, savedPositions);
    }

    public void hide() {
        container.setHidden(true);
        icon.setHidden(true);
        nameLabel.setHidden(true);
        doseLabel.setHidden(true);
        favButton.setHidden(true);
    }
}
