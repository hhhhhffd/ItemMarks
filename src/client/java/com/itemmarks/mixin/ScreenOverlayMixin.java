package com.itemmarks.mixin;

import com.itemmarks.PickerAccess;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
abstract class ScreenOverlayMixin {
    @Inject(method = "renderWithTooltipAndSubtitles", at = @At("TAIL"))
    private void itemmarks$drawModal(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if ((Object)this instanceof PickerAccess screen) screen.itemmarks$renderPicker(graphics, mouseX, mouseY, delta);
    }
}
