package net.openslabs.elevatorslabs.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.openslabs.elevatorslabs.ElevatorSlabsMod;
import net.openslabs.elevatorslabs.block.entity.ElevatorSlabBlockEntity;
import org.joml.Matrix4f;

/**
 * BlockEntityRenderer for Elevator Slabs for Forge 1.20.1.
 */
public class ElevatorSlabBER implements BlockEntityRenderer<ElevatorSlabBlockEntity> {

    private static final ResourceLocation ARROW_TEXTURE =
            new ResourceLocation(ElevatorSlabsMod.MOD_ID, "textures/block/arrow.png");

    public ElevatorSlabBER(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ElevatorSlabBlockEntity blockEntity, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {
        if (!blockEntity.isDirectional() || blockEntity.isHideArrow()) {
            return;
        }

        BlockState state = blockEntity.getBlockState();
        float yOffset = 0.501f;
        if (state.hasProperty(SlabBlock.TYPE)) {
            SlabType type = state.getValue(SlabBlock.TYPE);
            if (type == SlabType.TOP || type == SlabType.DOUBLE) {
                yOffset = 1.001f;
            }
        }

        Direction facing = blockEntity.getFacing();
        if (facing == null || !facing.getAxis().isHorizontal()) {
            facing = Direction.NORTH;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, yOffset, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot() + 180.0F));
        poseStack.translate(0.0D, 0.0D, -0.3125D);

        float s = 0.1875f;
        PoseStack.Pose last = poseStack.last();
        Matrix4f pose = last.pose();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutout(ARROW_TEXTURE));

        consumer.vertex(pose, -s, 0.0f, -s).color(255, 255, 255, 255).uv(0.0f, 0.0f).overlayCoords(packedOverlay).uv2(packedLight).normal(last.normal(), 0.0f, 1.0f, 0.0f).endVertex();
        consumer.vertex(pose, -s, 0.0f,  s).color(255, 255, 255, 255).uv(1.0f, 0.0f).overlayCoords(packedOverlay).uv2(packedLight).normal(last.normal(), 0.0f, 1.0f, 0.0f).endVertex();
        consumer.vertex(pose,  s, 0.0f,  s).color(255, 255, 255, 255).uv(1.0f, 1.0f).overlayCoords(packedOverlay).uv2(packedLight).normal(last.normal(), 0.0f, 1.0f, 0.0f).endVertex();
        consumer.vertex(pose,  s, 0.0f, -s).color(255, 255, 255, 255).uv(0.0f, 1.0f).overlayCoords(packedOverlay).uv2(packedLight).normal(last.normal(), 0.0f, 1.0f, 0.0f).endVertex();

        poseStack.popPose();
    }
}
