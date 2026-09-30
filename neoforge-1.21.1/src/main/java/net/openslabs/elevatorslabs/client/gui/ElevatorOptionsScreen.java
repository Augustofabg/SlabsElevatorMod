package net.openslabs.elevatorslabs.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.openslabs.elevatorslabs.block.entity.ElevatorSlabBlockEntity;
import net.openslabs.elevatorslabs.menu.ElevatorOptionsMenu;
import net.openslabs.elevatorslabs.network.UpdateSlabOptionsPayload;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom configuration screen for Elevator Slabs.
 * - Title "Elevator Options" rendered in pure white (0xFFFFFF).
 * - Directional starts unchecked (default false).
 * - Cardinal cross and "Hide arrow" are completely hidden when Directional is false.
 * - Checking Directional reveals "Hide arrow" and the cardinal cross.
 * - Dynamic cardinal cross based on player's current horizontal view direction:
 *     Top = front (playerFacing)
 *     Right = clockwise 90 deg (playerFacing.getClockWise())
 *     Bottom = opposite (playerFacing.getOpposite())
 *     Left = counter-clockwise 90 deg (playerFacing.getCounterClockWise())
 * - Active cardinal direction displays in green (#55FF55), unselected in white (#FFFFFF).
 * - "Remove camouflage" button is active only when camouflage is applied.
 */
public class ElevatorOptionsScreen extends AbstractContainerScreen<ElevatorOptionsMenu> {

    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("elevatorid", "textures/gui/elevator_gui.png");

    private Checkbox dirButton;
    private Checkbox hideArrowButton;
    private Button resetCamoButton;
    private final List<FacingButton> facingButtons = new ArrayList<>();

    private Direction selectedFacing = Direction.NORTH;

    public ElevatorOptionsScreen(ElevatorOptionsMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 200;
        this.imageHeight = 100;
    }

    @Override
    protected void init() {
        super.init();

        ElevatorSlabBlockEntity tile = this.menu.getBlockEntity();
        boolean initialDirectional = tile != null && tile.isDirectional();
        boolean initialHideArrow = tile != null && tile.isHideArrow();
        boolean hasCamo = false;
        if (tile != null) {
            net.minecraft.world.phys.HitResult hit = net.minecraft.client.Minecraft.getInstance().hitResult;
            boolean topClicked = false;
            if (hit instanceof net.minecraft.world.phys.BlockHitResult blockHit) {
                topClicked = (blockHit.getLocation().y - blockHit.getBlockPos().getY()) >= 0.5D;
            }
            net.minecraft.world.level.block.state.properties.SlabType type = tile.getBlockState().getValue(net.minecraft.world.level.block.SlabBlock.TYPE);
            if (type == net.minecraft.world.level.block.state.properties.SlabType.BOTTOM) {
                hasCamo = tile.getCamouflagedBottomState() != null;
            } else if (type == net.minecraft.world.level.block.state.properties.SlabType.TOP) {
                hasCamo = tile.getCamouflagedTopState() != null;
            } else {
                if (tile.isAppliedAsFullBlock()) {
                    hasCamo = tile.getCamouflagedBottomState() != null;
                } else {
                    hasCamo = topClicked ? tile.getCamouflagedTopState() != null : tile.getCamouflagedBottomState() != null;
                }
            }
        }
        this.selectedFacing = tile != null ? tile.getFacing() : Direction.NORTH;

        // Checkbox: Directional
        this.dirButton = Checkbox.builder(Component.translatable("screen.elevatorid.elevator.directional"), this.font)
                .pos(this.leftPos + 8, this.topPos + 22)
                .selected(initialDirectional)
                .onValueChange((checkbox, selected) -> {
                    updateVisibility(selected);
                    sendUpdate(false);
                })
                .build();
        addRenderableWidget(this.dirButton);

        // Checkbox: Hide arrow (hidden initially if directional == false)
        this.hideArrowButton = Checkbox.builder(Component.translatable("screen.elevatorid.elevator.hide_arrow"), this.font)
                .pos(this.leftPos + 8, this.topPos + 44)
                .selected(initialHideArrow)
                .onValueChange((checkbox, selected) -> sendUpdate(false))
                .build();
        this.hideArrowButton.visible = initialDirectional;
        this.hideArrowButton.active = initialDirectional;
        addRenderableWidget(this.hideArrowButton);

        // Button: Remove camouflage (visible at bottom left, active only if camo is present)
        this.resetCamoButton = Button.builder(Component.translatable("screen.elevatorid.elevator.reset_camo"), button -> {
            button.active = false;
            sendUpdate(true);
        })
                .pos(this.leftPos + 8, this.topPos + 68)
                .size(110, 20)
                .build();
        this.resetCamoButton.active = hasCamo;
        addRenderableWidget(this.resetCamoButton);

        // Cardinal Direction Selector buttons arranged dynamically relative to current player facing:
        // - Top: current view direction of player (playerFacing)
        // - Right: 90 deg clockwise (playerFacing.getClockWise())
        // - Bottom: opposite of view (playerFacing.getOpposite())
        // - Left: 90 deg counter-clockwise (playerFacing.getCounterClockWise())
        Direction playerFacing = (Minecraft.getInstance().player != null)
                ? Minecraft.getInstance().player.getDirection()
                : this.menu.getPlayerFacing();
        if (playerFacing == null || !playerFacing.getAxis().isHorizontal()) {
            playerFacing = Direction.NORTH;
        }

        this.facingButtons.clear();
        int crossX = this.leftPos + 130;
        int crossY = this.topPos + 20;

        Direction topDir = playerFacing;
        Direction rightDir = playerFacing.getClockWise();
        Direction bottomDir = playerFacing.getOpposite();
        Direction leftDir = playerFacing.getCounterClockWise();

        addFacingButton(crossX + 20, crossY, topDir, getTranslationKey(topDir));          // Top (Cima)
        addFacingButton(crossX + 40, crossY + 20, rightDir, getTranslationKey(rightDir));    // Right (Direita)
        addFacingButton(crossX + 20, crossY + 40, bottomDir, getTranslationKey(bottomDir)); // Bottom (Baixo)
        addFacingButton(crossX, crossY + 20, leftDir, getTranslationKey(leftDir));        // Left (Esquerda)

        updateVisibility(initialDirectional);
    }

    private static String getTranslationKey(Direction dir) {
        return switch (dir) {
            case NORTH -> "screen.elevatorid.elevator.directional_north";
            case SOUTH -> "screen.elevatorid.elevator.directional_south";
            case EAST -> "screen.elevatorid.elevator.directional_east";
            case WEST -> "screen.elevatorid.elevator.directional_west";
            default -> "screen.elevatorid.elevator.directional_north";
        };
    }

    private void addFacingButton(int x, int y, Direction direction, String translationKey) {
        FacingButton button = new FacingButton(x, y, direction, Component.translatable(translationKey));
        this.facingButtons.add(button);
        addRenderableWidget(button);
    }

    private void updateVisibility(boolean directional) {
        this.hideArrowButton.visible = directional;
        this.hideArrowButton.active = directional;
        for (FacingButton button : this.facingButtons) {
            button.visible = directional;
            button.active = directional;
        }
    }

    private void sendUpdate(boolean resetCamo) {
        PacketDistributor.sendToServer(new UpdateSlabOptionsPayload(
                this.menu.getPos(),
                this.dirButton.selected(),
                this.hideArrowButton.selected(),
                this.selectedFacing,
                resetCamo
        ));
    }

    @Override
    public void containerTick() {
        super.containerTick();
        ElevatorSlabBlockEntity tile = this.menu.getBlockEntity();
        if (tile != null) {
            boolean hasCamo = false;
            net.minecraft.world.phys.HitResult hit = net.minecraft.client.Minecraft.getInstance().hitResult;
            boolean topClicked = false;
            if (hit instanceof net.minecraft.world.phys.BlockHitResult blockHit) {
                topClicked = (blockHit.getLocation().y - blockHit.getBlockPos().getY()) >= 0.5D;
            }
            net.minecraft.world.level.block.state.properties.SlabType type = tile.getBlockState().getValue(net.minecraft.world.level.block.SlabBlock.TYPE);
            if (type == net.minecraft.world.level.block.state.properties.SlabType.BOTTOM) {
                hasCamo = tile.getCamouflagedBottomState() != null;
            } else if (type == net.minecraft.world.level.block.state.properties.SlabType.TOP) {
                hasCamo = tile.getCamouflagedTopState() != null;
            } else {
                if (tile.isAppliedAsFullBlock()) {
                    hasCamo = tile.getCamouflagedBottomState() != null;
                } else {
                    hasCamo = topClicked ? tile.getCamouflagedTopState() != null : tile.getCamouflagedBottomState() != null;
                }
            }
            this.resetCamoButton.active = hasCamo;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(GUI_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Render title in pure white (#FFFFFF)
        guiGraphics.drawString(this.font, this.title, 8, 8, 0xFFFFFF, false);
    }

    /**
     * Custom button widget for cardinal directions (E, N, S, W).
     * Renders active direction in bright green (#55FF55) and unselected directions in white (#FFFFFF).
     * Completely hidden when directional mode is inactive.
     */
    private class FacingButton extends Button {
        private final Direction direction;

        FacingButton(int x, int y, Direction direction, Component message) {
            super(x, y, 20, 20, message, b -> {
                ElevatorOptionsScreen.this.selectedFacing = direction;
                ElevatorOptionsScreen.this.sendUpdate(false);
            }, DEFAULT_NARRATION);
            this.direction = direction;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            if (!this.visible) {
                return;
            }

            // Hover highlight
            if (isHoveredOrFocused() && this.active) {
                guiGraphics.fill(getX(), getY(), getX() + this.width, getY() + this.height, 0x80FFFFFF);
            }

            Font font = ElevatorOptionsScreen.this.font;
            boolean isSelected = (ElevatorOptionsScreen.this.selectedFacing == this.direction);

            // Active/selected: #55FF55 (Green)
            // Active/unselected: #FFFFFF (White)
            int textColor = isSelected ? 0x55FF55 : 0xFFFFFF;

            int textX = getX() + (this.width / 2);
            int textY = getY() + ((this.height - 8) / 2);
            guiGraphics.drawCenteredString(font, getMessage(), textX, textY, textColor);
        }
    }
}
