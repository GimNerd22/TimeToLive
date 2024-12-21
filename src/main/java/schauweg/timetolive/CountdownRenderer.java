package schauweg.timetolive;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import schauweg.timetolive.config.TTLConfigManger;
import schauweg.timetolive.mixin.CreeperEntityMixin;

public class CountdownRenderer {
    public static <E extends Entity> void renderCountdown(E entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider immediate, EntityRenderer<? super E, ?> renderer) {
        if (entity instanceof TntEntity || entity instanceof CreeperEntity) {
            double x = MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX());
            double y = MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY());
            double z = MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ());

            Vec3d pos = new Vec3d(x, y, z);

            MinecraftClient client = MinecraftClient.getInstance();
            Camera camera = client.gameRenderer.getCamera();

            Vec3d vec3d = pos.subtract(camera.getPos());

            matrices.push();

            int fuse = 0;

            if (entity instanceof TntEntity) {
                fuse = ((TntEntity) entity).getFuse();
                matrices.translate(vec3d.x, vec3d.y + entity.getHeight() + 0.5, vec3d.z);
            } else if (entity instanceof CreeperEntity) {
                if (((CreeperEntity) entity).isIgnited()) {
                    fuse = ((CreeperEntityMixin)entity).getFuseTime() - ((CreeperEntityMixin)entity).getCurrentFuseTime();
                    matrices.translate(vec3d.x, vec3d.y + entity.getHeight() + 0.5, vec3d.z);
                }
            }

            matrices.multiplyPositionMatrix(new Matrix4f().rotation(camera.getRotation()));
            matrices.scale(0.025F, -0.025F, 0.025F);
            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            TextRenderer textRenderer = renderer.getTextRenderer();


            String time = TTLConfigManger.getConfig().isDisplayInTicks() ? fuse + " t" : ticksToTime(fuse);

            float f = (float)(-textRenderer.getWidth(time)) / 2.0F;

            textRenderer.draw(time, f, 0, 553648127, false, matrix4f, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 1056964608, 15728640);
            textRenderer.draw(time, f, 0, -1, false, matrix4f, immediate, TextRenderer.TextLayerType.NORMAL, 0, 15728640);

            matrices.pop();
        }
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
