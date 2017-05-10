package com.elytradev.teckle.client.gui;

import com.elytradev.teckle.common.container.ContainerFabricator;
import com.elytradev.teckle.common.network.FabricatorTemplateMessage;
import com.elytradev.teckle.common.tile.TileFabricator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * Created by darkevilmac on 4/12/2017.
 */
public class GuiFabricator extends GuiContainer {

    public static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("teckle", "textures/gui/fabricator.png");
    public final TileFabricator fabricator;
    public final EntityPlayer player;

    private Template[] templates = new Template[9];


    public GuiFabricator(TileFabricator tile, EntityPlayer player) {
        super(new ContainerFabricator(tile, player));
        this.fabricator = tile;
        this.player = player;
    }

    @Override
    public void initGui() {
        super.initGui();

        buttonList.clear();
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                buttonList.add(new Template(j + i * 3, 62 + j * 18, 17 + i * 18));
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        for (int i = 0; i < templates.length; i++) {
            if (button == templates[i]) {
                new FabricatorTemplateMessage(fabricator.getPos(), getMouseItem(), i).sendToServer();

                fabricator.templates.set(i, getMouseItem());
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        fontRenderer.drawString(I18n.format("gui.inventory"), 8, 73, 0x404040);
    }

    public ItemStack getMouseItem() {
        return player.inventory.getItemStack();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
        GlStateManager.color(1, 1, 1);
        Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        drawTexturedModalRect((width - xSize) / 2, (height - ySize) / 2, 0, 0, xSize, ySize);
        GlStateManager.enableLighting();
    }

    public class Template extends GuiButton {
        public final int templateIndex;

        public Template(int buttonId, int x, int y) {
            super(buttonId, x, y, "");
            this.templateIndex = buttonId;
        }

        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            if (this.visible && !getTemplateStack().isEmpty()) {
                GuiFabricator.this.itemRender.renderItemIntoGUI(getTemplateStack(), this.xPosition, this.yPosition);
            }
        }

        private ItemStack getTemplateStack() {
            return fabricator.templates.get(templateIndex);
        }
    }

}