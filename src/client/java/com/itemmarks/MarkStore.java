package com.itemmarks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.LoggerFactory;

public final class MarkStore {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("itemmarks.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int LIMIT = 5000;
    private static MarkStore current;
    private LinkedHashMap<String, Mark> entries = new LinkedHashMap<>();

    public record Mark(int color, int opacity) {
        public Mark {
            color &= 0xFFFFFF;
            opacity = Math.clamp(opacity, 0, 100);
        }
    }

    public static MarkStore get() {
        if (current == null) current = load();
        return current;
    }

    private static MarkStore load() {
        if (Files.isRegularFile(FILE)) {
            try (var reader = Files.newBufferedReader(FILE)) {
                MarkStore saved = GSON.fromJson(reader, MarkStore.class);
                if (saved != null && saved.entries != null) return saved;
            } catch (IOException | RuntimeException e) {
                LoggerFactory.getLogger("Item Marks").warn("Could not load item marks", e);
            }
        }
        return new MarkStore();
    }

    public boolean isEmpty() { return entries.isEmpty(); }
    public Mark find(String key) { return key == null ? null : entries.get(key); }

    public boolean set(String key, Mark mark) {
        if (key == null || (!entries.containsKey(key) && mark != null && entries.size() >= LIMIT)) return false;
        Mark previous = mark == null ? entries.remove(key) : entries.put(key, mark);
        if (java.util.Objects.equals(previous, mark)) return true;
        if (save()) return true;
        if (previous == null) entries.remove(key);
        else entries.put(key, previous);
        return false;
    }

    private boolean save() {
        Path temp = null;
        try {
            Files.createDirectories(FILE.getParent());
            temp = Files.createTempFile(FILE.getParent(), "itemmarks-", ".tmp");
            try (var writer = Files.newBufferedWriter(temp)) { GSON.toJson(this, writer); }
            try {
                Files.move(temp, FILE, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, FILE, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException | RuntimeException e) {
            LoggerFactory.getLogger("Item Marks").warn("Could not save item marks", e);
            return false;
        } finally {
            if (temp != null) {
                try { Files.deleteIfExists(temp); } catch (IOException ignored) { }
            }
        }
    }
}
