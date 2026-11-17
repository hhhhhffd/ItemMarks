package com.itemmarks.compat.inventoryhud.mixin;

import com.itemmarks.MarkRender;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draw once per actual populated Inventory HUD+ cell, in the mod's local transformed matrix. */
@Pseudo
@Mixin(targets = "dlovin.inventoryhud.gui.InventoryHUDGui", remap = false)
abstract class InventoryHudGuiMixin {
    @Inject(method = "renderInventory", remap = false, require = 0,
            at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", remap = false))
    private void itemmarks$separateInventoryBorder(GuiGraphics graphics, DeltaTracker delta, CallbackInfo ci) {
        // Inventory HUD+ queued its textured backdrop before dispatching the slot renderers.
        // Force a layer boundary: GUI pipeline sorting may otherwise paint it over our fill.
        graphics.nextStratum();
    }

    @Inject(method = "lambda$renderInventory$0", at = @At("HEAD"), remap = false, require = 0)
    private void itemmarks$behindInventoryHudItem(
            ItemStack stack, DeltaTracker delta, GuiGraphics graphics,
            int column, int row, int seed, CallbackInfo ci) {
        MarkRender.draw(graphics, stack, column * 18, row * 18);
    }
}
