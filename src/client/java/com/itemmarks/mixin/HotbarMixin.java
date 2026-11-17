package com.itemmarks.mixin;

import com.itemmarks.MarkRender;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
abstract class HotbarMixin {
    @Inject(method = "renderSlot", at = @At("HEAD"))
    private void itemmarks$hotbar(GuiGraphics graphics, int x, int y, DeltaTracker delta, Player player,
                                 ItemStack stack, int seed, CallbackInfo ci) {
        MarkRender.draw(graphics, stack, x, y);
    }
}
