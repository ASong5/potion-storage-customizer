package com.potionstoragecustomizer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("potionstoragecustomizer")
public interface PotionStorageCustomizerConfig extends Config
{
	@ConfigItem(
		keyName = "greeting",
		name = "Better Potion Storage",
		description = "Enhances the potion storage by allowing you to organize the locations of potions and enabling search functionality"
	)
	default String greeting()
	{
		return "Hello";
	}
}
