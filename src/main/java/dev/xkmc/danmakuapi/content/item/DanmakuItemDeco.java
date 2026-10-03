package dev.xkmc.danmakuapi.content.item;

import dev.xkmc.danmakuapi.api.GrazeHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;

public class DanmakuItemDeco implements IItemDecorator {

	@Override
	public boolean render(GuiGraphicsExtractor g, Font font, ItemStack stack, int x, int y) {
		if (GrazeHelper.globalForbidTime <= 0) return false;
		g.fill(x, y, x + 16, y + 16, 0x7fff0000);
		return true;
	}

}
