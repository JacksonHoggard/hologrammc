package me.jacksonhoggard.holoframes.screen;

import me.jacksonhoggard.holoframes.network.packet.HoloFrameScreenCloseRequestPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class HologramScreen extends HandledScreen<HologramScreenHandler> {

    private final List<ButtonWidget> HOLOGRAM_SELECT_WIDGETS = new ArrayList<>();

    private final String[] hologramFiles;

    public HologramScreen(HologramScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.hologramFiles = handler.getHologramFiles();
    }

    private static class HologramScreenTitle extends TextWidget {
        public HologramScreenTitle(Text message, TextRenderer textRenderer) {
            super(message, textRenderer);
        }

        @Override
        public int getWidth() {
            return 200;
        }

        @Override
        public int getHeight() {
            return 20;
        }
    }

    @Override
    protected void init() {
        super.init();
        int titlePos = 20;
        int buttonGroupPos = 40;
        this.HOLOGRAM_SELECT_WIDGETS.clear();
        for(String hologramFile : hologramFiles) {
            ButtonWidget buttonWidget = ButtonWidget.builder(
                    Text.literal(hologramFile),
                            HologramScreen::onButtonClick)
                    .dimensions(
                            (this.width / 2) - 100,
                            buttonGroupPos + this.HOLOGRAM_SELECT_WIDGETS.size() * 20,
                            200,
                            20
                    )
                    .build();
            this.HOLOGRAM_SELECT_WIDGETS.add(buttonWidget);
            this.addDrawableChild(buttonWidget);
        }
        HologramScreenTitle titleWidget = new HologramScreenTitle(this.title, this.textRenderer);
        titleWidget.setPosition(width / 2 - titleWidget.getWidth() / 2, titlePos);
        this.addDrawableChild(titleWidget);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        renderBackground(context, mouseX, mouseY, deltaTicks);
        super.render(context, mouseX, mouseY, deltaTicks);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
        return;
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        return;
    }

    private static void onButtonClick(ButtonWidget button) {
        ClientPlayNetworking.send(new HoloFrameScreenCloseRequestPacket(button.getMessage().getString()).toPayload());
    }

    @Override
    public void close() {
        this.HOLOGRAM_SELECT_WIDGETS.clear();
        super.close();
    }
}
