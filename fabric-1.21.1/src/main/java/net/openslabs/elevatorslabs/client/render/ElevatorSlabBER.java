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
 * BlockEntityRenderer for Elevator Slabs on Fabric 1.21.1.
 * Renders the directional black arrow matching the classic OpenBlocks Elevator.
 * Identical logic to NeoForge version.
 */
public class ElevatorSlabBER implements BlockEntityRenderer<ElevatorSlabBlockEntity> {

    private static final ResourceLocation ARROW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ElevatorSlabsMod.MOD_ID, "textures/block/arrow.png");

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
        // 1. Translate to the center of the slab surface
        poseStack.translate(0.5D, yOffset, 0.5D);

        // 2. Rotate around Y according to cardinal facing + 180 deg to point arrow apex outward
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot() + 180.0F));

        // 3. Forward offset towards the target border (5/16 = 0.3125f)
        poseStack.translate(0.0D, 0.0D, -0.3125D);

        // 4. Quad geometry: 6x6 pixel proportion (half-size s = 3/16 = 0.1875f, total size 0.375f)
        float s = 0.1875f;
        PoseStack.Pose last = poseStack.last();
        Matrix4f pose = last.pose();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutout(ARROW_TEXTURE));

        consumer.addVertex(pose, -s, 0.0f, -s).setColor(255, 255, 255, 255).setUv(0.0f, 0.0f).setOverlay(packedOverlay).setLight(packedLight).setNormal(last, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(pose, -s, 0.0f,  s).setColor(255, 255, 255, 255).setUv(1.0f, 0.0f).setOverlay(packedOverlay).setLight(packedLight).setNormal(last, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(pose,  s, 0.0f,  s).setColor(255, 255, 255, 255).setUv(1.0f, 1.0f).setOverlay(packedOverlay).setLight(packedLight).setNormal(last, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(pose,  s, 0.0f, -s).setColor(255, 255, 255, 255).setUv(0.0f, 1.0f).setOverlay(packedOverlay).setLight(packedLight).setNormal(last, 0.0f, 1.0f, 0.0f);

        poseStack.popPose();
    }
}
