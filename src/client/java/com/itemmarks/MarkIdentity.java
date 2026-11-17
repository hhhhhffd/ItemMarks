package com.itemmarks;

import com.mojang.serialization.JsonOps;
import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;

public final class MarkIdentity {
    private static final WeakHashMap<ItemStack, Cached> CACHE = new WeakHashMap<>();
    private static WeakReference<ClientLevel> previousLevel = new WeakReference<>(null);
    private static String scope;

    private MarkIdentity() { }

    public static String key(ItemStack stack) {
        if (stack.isEmpty()) return null;
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return null;
        if (client.level != previousLevel.get()) {
            previousLevel = new WeakReference<>(client.level);
            scope = worldScope(client);
            CACHE.clear();
        }
        DataComponentPatch patch = stack.getComponentsPatch();
        Cached cached = CACHE.get(stack);
        if (cached != null && cached.components.equals(patch)) return cached.identity;

        String identity = null;
        var customData = stack.get(DataComponents.CUSTOM_DATA);
        if (scope != null && customData != null) {
            CompoundTag tag = customData.copyTag();
            CompoundTag skyblock = tag.getCompound("ExtraAttributes").orElse(tag);
            String uuid = IdentityMath.uuid(skyblock.getStringOr("uuid", ""));
            if (uuid == null && skyblock != tag) uuid = IdentityMath.uuid(tag.getStringOr("uuid", ""));
            if (uuid != null) identity = IdentityMath.unique(scope, uuid);
        }
        if (identity == null) {
            ItemStack normalized = stack.copyWithCount(1);
            normalized.remove(DataComponents.DAMAGE);
            normalized.remove(DataComponents.REPAIR_COST);
            try {
                var ops = RegistryOps.create(JsonOps.INSTANCE, client.level.registryAccess());
                var data = ItemStack.CODEC.encodeStart(ops, normalized).result();
                if (data.isPresent()) identity = IdentityMath.fingerprint(
                        BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), data.get());
            } catch (RuntimeException ignored) {
                // Never fall back to a broad type match when a nonempty item's identity is unknown.
            }
        }
        CACHE.put(stack, new Cached(patch, identity));
        return identity;
    }

    private static String worldScope(Minecraft client) {
        var remote = client.getCurrentServer();
        if (remote != null && remote.ip != null && !remote.ip.isBlank())
            return "server:" + remote.ip.strip().toLowerCase(Locale.ROOT);
        var local = client.getSingleplayerServer();
        if (local != null) return "local:" + local.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
        return null;
    }

    private record Cached(DataComponentPatch components, String identity) { }
}
