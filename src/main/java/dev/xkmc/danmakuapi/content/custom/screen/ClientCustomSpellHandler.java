package dev.xkmc.danmakuapi.content.custom.screen;

import dev.xkmc.danmakuapi.content.custom.data.ISpellFormData;
import dev.xkmc.danmakuapi.init.DanmakuAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ClientCustomSpellHandler {

	public static void open(Component title, ISpellFormData<?> data) {
		Minecraft.getInstance().setScreenAndShow(new EditorScreen(title, data));
	}

	public static void sendToPlayer(ISpellFormData<?> val) {
		DanmakuAPI.HANDLER.toServer(new SpellSetToServer(val.cast()));

	}

}
