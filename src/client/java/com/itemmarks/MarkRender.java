package com.itemmarks;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public final class MarkRender {
    private MarkRender() { }

    public static void draw(GuiGraphics g, ItemStack stack, int x, int y) {
        if (stack.isEmpty()) return;
        MarkStore store = MarkStore.get();
        if (store.isEmpty()) return;
        MarkStore.Mark mark = store.find(MarkIdentity.key(stack));
        if (mark != null) swatch(g, mark.color(), mark.opacity(), x, y);
    }

    public static void swatch(GuiGraphics g, int rgb, int opacity, int x, int y) {
        if (opacity == 0) return;
        int alpha = Math.round(opacity * 2.55f);
        int fill = alpha << 24 | rgb & 0xFFFFFF;
        int edge = Math.min(255, Math.round(alpha * 1.45f)) << 24 | rgb & 0xFFFFFF;
        g.fill(x, y, x + 16, y + 16, fill);
        g.fill(x, y, x + 16, y + 1, edge);
        g.fill(x, y + 15, x + 16, y + 16, edge);
    }
}
