package com.itemmarks;

import java.awt.Color;
import java.util.Locale;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public final class ItemPicker {
    private static final int W = 242;
    private static final int H = 163;
    private static final int WHITE = 0xFFF3F8FA;
    private static final int TEXT2 = 0xFF9EB7C0;
    private static final int BORDER = 0xFF49636C;
    private static final int SAT_W = 145;
    private static final int SAT_H = 64;
    private static final int ALPHA_W = 132;

    private final Font font;
    private final ItemStack item;
    private final String itemName;
    private final String key;
    private final float[] hsv = new float[3];
    private int rgb;
    private int opacity;
    private int drag;
    private int x;
    private int y;
    private boolean updatingHex;
    private boolean error;
    private boolean closed;
    private boolean hasMark;
    private final EditBox hex;

    public ItemPicker(Font font, ItemStack stack, String key, int width, int height) {
        this.item = stack.copyWithCount(1);
        this.itemName = font.plainSubstrByWidth(stack.getHoverName().getString(), W - 37);
        this.key = key;
        this.font = font;
        MarkStore.Mark stored = MarkStore.get().find(key);
        hasMark = stored != null;
        rgb = hasMark ? stored.color() : 0x5BC5EE;
        opacity = hasMark ? stored.opacity() : 42;
        readHsv();
        hex = new EditBox(font, 0, 0, 71, 18, null, Component.literal("HEX mark color"));
        hex.setMaxLength(7);
        hex.setValue(format());
        hex.setResponder(text -> {
            if (updatingHex) return;
            String digits = digits(text);
            boolean valid = digits.matches("[0-9A-Fa-f]{6}");
            hex.setTextColor(valid ? WHITE : 0xFFFF8290);
            if (valid) {
                rgb = Integer.parseInt(digits, 16);
                readHsv();
                error = false;
            }
        });
        position(width, height);
    }

    private static String digits(String s) { return s.startsWith("#") ? s.substring(1) : s; }
    private boolean valid() { return digits(hex.getValue()).matches("[0-9a-fA-F]{6}"); }
    private String format() { return String.format(Locale.ROOT, "#%06X", rgb); }

    private void readHsv() { Color.RGBtoHSB(rgb >> 16 & 255, rgb >> 8 & 255, rgb & 255, hsv); }

    private void position(int width, int height) {
        x = (width - W) / 2;
        y = (height - H) / 2;
        hex.setPosition(x + 32, y + 111);
    }

    public boolean isClosed() { return closed; }

    public void render(GuiGraphics g, int mx, int my, float delta) {
        position(g.guiWidth(), g.guiHeight());
        int sx = x + 9;
        int sy = y + 39;
        int hx = sx + SAT_W + 8;

        g.nextStratum();
        g.fill(0, 0, g.guiWidth(), g.guiHeight(), 0x980A1620);
        g.fill(x, y, x + W, y + H, 0xFF1D2B32);
        g.renderOutline(x, y, W, H, BORDER);
        g.fill(x + 1, y + 1, x + W - 1, y + 3, 0xFF76D8F6);
        g.drawString(font, "Mark item", sx, y + 10, WHITE);
        g.drawString(font, itemName, sx, y + 24, TEXT2);

        for (int col = 0; col < SAT_W; col++) {
            int top = Color.HSBtoRGB(hsv[0], col / (float)(SAT_W - 1), 1f);
            g.fillGradient(sx + col, sy, sx + col + 1, sy + SAT_H, top, 0xFF000000);
        }
        for (int row = 0; row < SAT_H; row++) {
            int hue = Color.HSBtoRGB(row / (float)(SAT_H - 1), 1f, 1f);
            g.fill(hx, sy + row, hx + 13, sy + row + 1, hue);
        }
        g.renderOutline(sx - 1, sy - 1, SAT_W + 2, SAT_H + 2, BORDER);
        g.renderOutline(hx - 1, sy - 1, 15, SAT_H + 2, BORDER);
        int px = sx + Math.round(hsv[1] * (SAT_W - 1)), py = sy + Math.round((1f - hsv[2]) * (SAT_H - 1));
        g.renderOutline(px - 4, py - 4, 9, 9, 0xFF000000);
        g.renderOutline(px - 3, py - 3, 7, 7, WHITE);
        int hy = sy + Math.round(hsv[0] * (SAT_H - 1));
        g.renderOutline(hx - 2, hy - 2, 17, 5, 0xFF000000);
        g.renderOutline(hx - 1, hy - 1, 15, 3, WHITE);

        int ix = x + 198, iy = y + 53;
        g.fill(ix - 4, iy - 4, ix + 20, iy + 20, 0xFF3D4A50);
        g.renderOutline(ix - 4, iy - 4, 24, 24, BORDER);
        g.fill(ix, iy, ix + 16, iy + 16, 0xFF8E989D);
        MarkRender.swatch(g, rgb, opacity, ix, iy);
        g.renderFakeItem(item, ix, iy);
        g.drawCenteredString(font, "Preview", ix + 8, iy + 23, TEXT2);

        g.drawString(font, "HEX", sx, y + 115, TEXT2);
        hex.render(g, mx, my, delta);

        int ax = x + 107, ay = y + 119;
        for (int i = 0; i < ALPHA_W; i++) {
            g.fill(ax + i, ay, ax + i + 1, ay + 8, ((i / 5) & 1) == 0 ? 0xFF40535C : 0xFF68808C);
            g.fill(ax + i, ay, ax + i + 1, ay + 8, (Math.round(255f * i / (ALPHA_W - 1)) << 24) | rgb);
        }
        int alphaCursor = ax + Math.round((ALPHA_W - 1) * opacity / 100f);
        g.renderOutline(alphaCursor - 2, ay - 1, 5, 10, 0xFF111A20);
        g.renderOutline(alphaCursor - 1, ay - 1, 3, 10, WHITE);
        g.drawString(font, "Alpha " + opacity + "%", ax + 1, ay - 10, TEXT2);

        button(g, "Clear", x + 9, mx, my, hasMark, 0xFF48424A);
        button(g, "Done", x + 84, mx, my, valid(), 0xFF245567);
        button(g, "Cancel", x + 159, mx, my, true, 0xFF34454E);
        if (error) {
            g.fill(x + 10, y + 125, x + W - 10, y + 138, 0xFF514348);
            g.drawCenteredString(font, "Could not save item mark", x + W / 2, y + 127, 0xFFFFA3A9);
        }
    }

    private void button(GuiGraphics g, String label, int px, int mx, int my, boolean enabled, int bg) {
        int py = y + 137;
        int bw = 72;
        boolean hover = hit(mx, my, px, py, bw, 18);
        g.fill(px, py, px + bw, py + 18, enabled ? bg : 0xFF28343A);
        g.renderOutline(px, py, bw, 18, hover && enabled ? WHITE : BORDER);
        g.drawCenteredString(font, label, px + bw / 2, py + 5, enabled ? WHITE : 0xFF73848C);
    }

    public void click(MouseButtonEvent e, boolean doubleClick) {
        if (e.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;
        double mx = e.x(), my = e.y();
        boolean inHex = hit(mx, my, x + 32, y + 111, 71, 18);
        hex.setFocused(inHex);
        if (inHex) {
            hex.mouseClicked(e, doubleClick);
            return;
        }
        if (hit(mx, my, x + 9, y + 39, SAT_W, SAT_H)) drag = 1;
        else if (hit(mx, my, x + 9 + SAT_W + 8, y + 39, 13, SAT_H)) drag = 2;
        else if (hit(mx, my, x + 107, y + 109, ALPHA_W, 22)) drag = 3;
        if (drag != 0) {
            pick(mx, my);
            return;
        }

        if (hit(mx, my, x + 9, y + 137, 72, 18) && hasMark) apply(null);
        else if (hit(mx, my, x + 84, y + 137, 72, 18) && valid()) apply(new MarkStore.Mark(rgb, opacity));
        else if (hit(mx, my, x + 159, y + 137, 72, 18)) closed = true;
    }

    public void drag(MouseButtonEvent e, double deltaX, double deltaY) {
        if (e.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;
        if (drag != 0) pick(e.x(),e.y());
        else if (hex.isFocused()) hex.mouseDragged(e,deltaX,deltaY);
    }

    public void release(MouseButtonEvent e) {
        if (e.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) drag = 0;
    }

    public void key(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) closed = true;
        else if ((event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER) && valid())
            apply(new MarkStore.Mark(rgb, opacity));
        else if (hex.isFocused()) hex.keyPressed(event);
    }

    public void character(CharacterEvent event) {
        if (hex.isFocused()) hex.charTyped(event);
    }

    private void pick(double mx, double my) {
        if (drag == 3) {
            opacity = Math.clamp((int)Math.round(100 * (mx - (x + 107)) / (ALPHA_W - 1)), 0, 100);
        } else {
            float ratio = Math.clamp((float)(my - (y + 39)) / (SAT_H - 1), 0f, 1f);
            if (drag == 1) {
                hsv[1] = Math.clamp((float)(mx - (x + 9)) / (SAT_W - 1), 0f, 1f);
                hsv[2] = 1f - ratio;
            } else hsv[0] = ratio;
            rgb = Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]) & 0xFFFFFF;
            updatingHex = true;
            try {
                hex.setValue(format());
                hex.setTextColor(WHITE);
            } finally {
                updatingHex = false;
            }
        }
        error = false;
    }

    private static boolean hit(double x, double y, int left, int top, int width, int height) {
        return x >= left && y >= top && x < left + width && y < top + height;
    }

    private void apply(MarkStore.Mark mark) {
        if (MarkStore.get().set(key, mark)) closed = true;
        else error = true;
    }
}
