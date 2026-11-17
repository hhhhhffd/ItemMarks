package com.itemmarks.mixin;

import com.itemmarks.ItemPicker;
import com.itemmarks.MarkIdentity;
import com.itemmarks.MarkRender;
import com.itemmarks.PickerAccess;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
abstract class ContainerScreenMixin extends Screen implements PickerAccess {
    @Shadow @Final protected AbstractContainerMenu menu;
    @Shadow private Slot getHoveredSlot(double x, double y) { throw new AssertionError(); }
    @Unique private ItemPicker itemmarks$picker;
    @Unique private int itemmarks$swallowedButtons;

    protected ContainerScreenMixin(Component title) { super(title); }

    @Unique private boolean itemmarks$owns(Slot slot) {
        return minecraft != null && minecraft.player != null && slot != null && slot.isActive()
                && !slot.isFake() && slot.container == minecraft.player.getInventory();
    }

    @Inject(method = "renderSlot", at = @At("HEAD"))
    private void itemmarks$markCell(GuiGraphics graphics, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        if (itemmarks$owns(slot)) MarkRender.draw(graphics, slot.getItem(), slot.x, slot.y);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void itemmarks$click(MouseButtonEvent e, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (itemmarks$picker != null) {
            itemmarks$capture(e.button());
            itemmarks$picker.click(e, doubleClick);
            itemmarks$updatePicker();
            cir.setReturnValue(true);
            return;
        }
        if (e.button() != GLFW.GLFW_MOUSE_BUTTON_MIDDLE || (e.modifiers() & GLFW.GLFW_MOD_ALT) == 0) return;
        Slot slot = getHoveredSlot(e.x(), e.y());
        if (!itemmarks$owns(slot) || !slot.hasItem()) return;
        // Consume this dedicated gesture before vanilla turns a middle click into a clone.
        cir.setReturnValue(true);
        itemmarks$capture(e.button());
        if (!menu.getCarried().isEmpty()) return;
        String key = MarkIdentity.key(slot.getItem());
        if (key == null) return;
        clearFocus();
        itemmarks$picker = new ItemPicker(font, slot.getItem(), key, width, height);
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void itemmarks$drag(MouseButtonEvent e, double dx, double dy, CallbackInfoReturnable<Boolean> cir) {
        if (itemmarks$picker != null) {
            itemmarks$picker.drag(e, dx, dy);
            cir.setReturnValue(true);
        } else if (itemmarks$captured(e.button())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void itemmarks$release(MouseButtonEvent e, CallbackInfoReturnable<Boolean> cir) {
        boolean captured = itemmarks$captured(e.button());
        if (captured) itemmarks$swallowedButtons &= ~(1 << e.button());
        if (itemmarks$picker != null) itemmarks$picker.release(e);
        if (captured || itemmarks$picker != null) cir.setReturnValue(true);
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void itemmarks$scroll(double mx, double my, double sx, double sy, CallbackInfoReturnable<Boolean> cir) {
        if (itemmarks$picker != null) cir.setReturnValue(true);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void itemmarks$key(KeyEvent e, CallbackInfoReturnable<Boolean> cir) {
        if (itemmarks$picker == null) return;
        itemmarks$picker.key(e);
        itemmarks$updatePicker();
        cir.setReturnValue(true);
    }

    @Override public boolean charTyped(CharacterEvent e) {
        if (itemmarks$picker == null) return super.charTyped(e);
        itemmarks$picker.character(e);
        return true;
    }

    @Override public void itemmarks$renderPicker(GuiGraphics g, int mx, int my, float delta) {
        if (itemmarks$picker != null) itemmarks$picker.render(g, mx, my, delta);
    }

    @Inject(method = "renderTooltip", at = @At("HEAD"), cancellable = true)
    private void itemmarks$hideTooltip(GuiGraphics g, int mx, int my, CallbackInfo ci) {
        if (itemmarks$picker != null) ci.cancel();
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void itemmarks$close(CallbackInfo ci) {
        itemmarks$picker = null;
        itemmarks$swallowedButtons = 0;
    }

    @Unique private void itemmarks$capture(int button) {
        if (button >= 0 && button < 8) itemmarks$swallowedButtons |= 1 << button;
    }

    @Unique private boolean itemmarks$captured(int button) {
        return button >= 0 && button < 8 && (itemmarks$swallowedButtons & (1 << button)) != 0;
    }

    @Unique private void itemmarks$updatePicker() {
        if (itemmarks$picker != null && itemmarks$picker.isClosed()) itemmarks$picker = null;
    }
}
