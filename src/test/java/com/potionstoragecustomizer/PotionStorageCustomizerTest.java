package com.potionstoragecustomizer;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PotionStorageCustomizerTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(PotionStorageCustomizerPlugin.class);
		RuneLite.main(args);
	}
}
