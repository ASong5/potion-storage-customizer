package com.potionstoragecustomizer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(PotionStorageCustomizerPlugin.CONFIG_GROUP)
public interface PotionStorageCustomizerConfig extends Config {
    @ConfigItem(
        keyName = "disableDrag",
        name = "Disable drag",
        description = "Prevents potions from being dragged. Enable this to lock your configured potion positions and avoid accidental re-positioning",
        position = 1
    )
    default boolean disableDrag() {
        return false;
    }
}
