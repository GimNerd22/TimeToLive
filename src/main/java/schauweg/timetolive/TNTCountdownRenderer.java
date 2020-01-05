package schauweg.timetolive;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.lwjgl.opengl.GL11;

public class TNTCountdownRenderer {

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {

        Minecraft mc = Minecraft.getInstance();

        Entity cameraEntity = mc.getRenderViewEntity();
        BlockPos renderingVector = cameraEntity.getPosition();
        Frustum frustum = new Frustum();

        float partialTicks = event.getPartialTicks();
        double viewX = cameraEntity.lastTickPosX + (cameraEntity.posX - cameraEntity.lastTickPosX) * partialTicks;
        double viewY = cameraEntity.lastTickPosY + (cameraEntity.posY - cameraEntity.lastTickPosY) * partialTicks;
        double viewZ = cameraEntity.lastTickPosZ + (cameraEntity.posZ - cameraEntity.lastTickPosZ) * partialTicks;
        frustum.setPosition(viewX, viewY, viewZ);

        ClientWorld client = mc.world;
        Iterable<Entity> entitiesById = client.getAllEntities();

        for(Entity entity : entitiesById) {
            if (entity != null && entity instanceof TNTEntity && entity.isInRangeToRender3d(renderingVector.getX(), renderingVector.getY(), renderingVector.getZ()) && (entity.ignoreFrustumCheck || frustum.isBoundingBoxInFrustum(entity.getBoundingBox()))){
                renderFuseCountdown((TNTEntity) entity, partialTicks, 0.5F);
            }
        }

    }

    private void renderFuseCountdown(TNTEntity passedEntity, float partialTicks, float nameOffset) {
        Minecraft mc = Minecraft.getInstance();
        float pastTranslate = 0F;

        int fuse = passedEntity.getFuse();

        String fuseText = ticksToTime(fuse);

        double x = passedEntity.lastTickPosX + (passedEntity.posX - passedEntity.lastTickPosX) * partialTicks;
        double y = passedEntity.lastTickPosY + (passedEntity.posY - passedEntity.lastTickPosY) * partialTicks;
        double z = passedEntity.lastTickPosZ + (passedEntity.posZ - passedEntity.lastTickPosZ) * partialTicks;

        EntityRendererManager renderManager = Minecraft.getInstance().getRenderManager();
        double renderPosX = ObfuscationReflectionHelper.getPrivateValue(EntityRendererManager.class, renderManager, "field_78725_b"); //renderPosX
        double renderPosY = ObfuscationReflectionHelper.getPrivateValue(EntityRendererManager.class, renderManager, "field_78726_c"); //renderPosY
        double renderPosZ = ObfuscationReflectionHelper.getPrivateValue(EntityRendererManager.class, renderManager, "field_78723_d"); //renderPosZ

        GlStateManager.translatef(0F, pastTranslate, 0F);

        float scale = 0.026666672F;

        GlStateManager.pushMatrix();
        GlStateManager.translatef((float) (x - renderPosX), (float) (y - renderPosY + passedEntity.getHeight()) + nameOffset, (float) (z - renderPosZ));
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scalef(-scale, -scale, scale);
        boolean lighting = GL11.glGetBoolean(GL11.GL_LIGHTING);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepthTest();
        GlStateManager.disableTexture();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        int j = mc.fontRenderer.getStringWidth(fuseText) / 2;

        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos((double)(-j - 1), (double)(-1 ), 0.0D).color(0, 0, 0, 64).endVertex();
        buffer.pos((double)(-j - 1), (double)(8), 0.0D).color(0, 0, 0, 64).endVertex();
        buffer.pos((double)(j + 1), (double)(8), 0.0D).color(0, 0, 0, 64).endVertex();
        buffer.pos((double)(j + 1), (double)(-1), 0.0D).color(0, 0, 0, 64).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture();
        mc.fontRenderer.drawString(fuseText, -j,0, 553648127);
        GlStateManager.enableDepthTest();
        GlStateManager.depthMask(true);
        if(lighting)
            GlStateManager.enableLighting();
        mc.fontRenderer.drawString(fuseText, -j,0, -1);
        GlStateManager.disableBlend();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }


    private static String ticksToTime(int ticks){
        if(ticks > 20*3600){
            int h = ticks/20/3600;
            return h+" h";
        } else if(ticks > 20*60){
            int m = ticks/20/60;
            return m+" m";
        } else {
            int s = ticks / 20;
            int ms = (ticks % 20) / 2;
            return s+"."+ms+" s";
        }
    }
}
