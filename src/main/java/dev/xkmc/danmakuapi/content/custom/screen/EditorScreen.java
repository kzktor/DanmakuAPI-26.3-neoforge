package dev.xkmc.danmakuapi.content.custom.screen;

import dev.xkmc.danmakuapi.content.custom.data.ISpellFormData;
import dev.xkmc.danmakuapi.content.custom.editor.SpellOptionInstances;
import dev.xkmc.danmakuapi.init.data.DanmakuConfig;
import dev.xkmc.danmakuapi.init.data.DanmakuLang;
import dev.xkmc.l2serial.util.Wrappers;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

public class EditorScreen extends OptionsSubScreen {

	private final SpellOptionInstances<?> ins;
	private Button cancel, reset, save;
	private OptionsList list;


	public EditorScreen(Component title, ISpellFormData<?> data) {
		super(null, null, title);
		ins = SpellOptionInstances.create(Wrappers.cast(data));
	}


	@Override
	protected void init() {
		this.addTitle();
		this.addFooter();
		this.addContents();
		this.layout.visitWidgets(this::addRenderableWidget);
		this.repositionElements();
	}

	protected void addOptions() {
		ins.add(list, this::setChanged);
		reset.active = false;
	}

	protected void addContents() {
		this.list = this.layout.addToContents(new OptionsList(this.minecraft, this.width, this));
		this.addOptions();
	}

	protected void addFooter() {
		int w = width / 4;
		layout.addToFooter(cancel = new Button.Builder(
				Component.translatable("gui.cancel"),
				e -> onClose()
		).size(w, 20).build(), e -> e.align(0.1F, 0.5F));
		layout.addToFooter(reset = new Button.Builder(
				DanmakuLang.EDITOR_RESET.get(),
				e -> reset()
		).size(w, 20).build(), e -> e.align(0.5F, 0.5F));
		layout.addToFooter(save = new Button.Builder(
				Component.translatable("gui.done"),
				e -> save()
		).size(w, 20).build(), e -> e.align(0.9F, 0.5F));
	}

	private void setChanged() {
		reset.active = true;
		updateSave();
	}

	private void reset() {
		ins.reset();
		removeWidget(list);
		addOptions();
		updateSave();
	}

	private void updateSave() {
		var e = ins.build();
		save.setTooltip(null);
		save.active = false;
		if (e != null) {
			int max = DanmakuConfig.SERVER.customSpellMaxDuration.get();
			int dur = e.getDuration();
			if (dur > max) {
				save.setTooltip(Tooltip.create(DanmakuLang.INVALID_TIME.get(max, dur)));
			} else {
				save.active = true;
			}
		}
	}

	private void save() {
		ins.save();
		onClose();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float pTick) {
		extractBackground(g, mx, my, pTick);
		super.extractRenderState(g, mx, my, pTick);
	}

}
