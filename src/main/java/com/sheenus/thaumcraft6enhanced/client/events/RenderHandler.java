package com.sheenus.thaumcraft6enhanced.client.events;

import org.lwjgl.opengl.GL11;

import com.sheenus.thaumcraft6enhanced.events.PlayerRunicEvents;
import com.sheenus.thaumcraft6enhanced.util.Reference;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.lib.UtilsFX;

@EventBusSubscriber(modid = Reference.MOD_ID)
public class RenderHandler {

	@SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void renderRunicOnTick(final TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            UtilsFX.sysPartialTicks = event.renderTickTime;
        }
        else {
            final Minecraft mc = FMLClientHandler.instance().getClient();
            if (Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityPlayer) {
                final EntityPlayer player = (EntityPlayer)Minecraft.getMinecraft().getRenderViewEntity();
                final long time = System.currentTimeMillis();
                if (player != null && mc.inGameHasFocus && !player.isCreative() && Minecraft.isGuiEnabled() && PlayerRunicEvents.hasRunicHP(player) && PlayerRunicEvents.getRunicHP(player) > 0) {
                    renderRunicArmorBar(mc, event.renderTickTime, player, time);
                }
            }
        }
	}
	
	@SideOnly(value=Side.CLIENT)
	static void renderRunicArmorBar(Minecraft mc, float partialTicks, EntityPlayer player, long time) {
        float total = (float)PlayerRunicEvents.getMaxRunic(player);	// needs to be changed to reference player value for total runic armor value
        float current = PlayerRunicEvents.getRunicHP(player);	// needs to be changed to reference player value for current runic armor value      
        GL11.glPushMatrix();
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        GL11.glClear(256);
        GL11.glMatrixMode(5889);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0, sr.getScaledWidth_double(), sr.getScaledHeight_double(), 0.0, 1000.0, 3000.0);
        GL11.glMatrixMode(5888);
        GL11.glLoadIdentity();
        GL11.glTranslatef(0.0f, 0.0f, -2000.0f);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        // GL11.glDisable(3008);					// commented out due to changes with rendering in 1.12 (but might be useful later)
        int k = sr.getScaledWidth();
        int l = sr.getScaledHeight();
        GL11.glTranslatef((float)(k / 2 - 91), (float)(l - 39), 0.0f);
        mc.renderEngine.bindTexture(ParticleEngine.particleTexture);
        GL11.glScaled((double)4, (double)4, (double)4);
        float fill = current / total;
        int a = 0;
        while ((float)a < fill * 10.0f) {
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            UtilsFX.drawTexturedQuad(a * 8/4, 0, 160/4, 16/4, 9/4, 9/4, -90.0);
            GL11.glPushMatrix();
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glColor4f(1.0f, 0.75f, 0.24f, (MathHelper.sin(((float)player.ticksExisted / 4.0f + (float)a)) * 0.4f + 0.6f));
            UtilsFX.drawTexturedQuad(a * 16/4, 0, a * 16/4, 96/4, 16/4, 16/4, -90.0);
            GL11.glPopMatrix();
            ++a;
        }
        GL11.glDepthMask(true);
        GL11.glEnable(2929);
        GL11.glDisable(3042);
        // GL11.glEnable(3008);						// commented out due to changes with rendering in 1.12 (but might be useful later)
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glPopMatrix();
    }
}
	