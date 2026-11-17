package com.itemmarks;

import com.google.gson.JsonElement;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.TreeMap;

public final class IdentityMath {
    private IdentityMath() { }

    public static String uuid(String input) {
        if (input == null) return null;
        String raw = input.toLowerCase(Locale.ROOT);
        if (raw.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) raw = raw.replace("-", "");
        if (!raw.matches("[0-9a-f]{32}") || raw.equals("00000000000000000000000000000000")) return null;
        return raw;
    }

    public static String unique(String scope, String uuid) {
        return "unique:" + digest(scope + "|" + uuid);
    }

    public static String fingerprint(String item, JsonElement json) {
        StringBuilder canonical = new StringBuilder(item).append('|');
        appendCanonical(canonical, json);
        return "item:" + digest(canonical.toString());
    }

    private static void appendCanonical(StringBuilder out, JsonElement json) {
        if (json.isJsonObject()) {
            out.append('{');
            var sorted = new TreeMap<String, JsonElement>(json.getAsJsonObject().asMap());
            boolean first = true;
            for (var entry : sorted.entrySet()) {
                if (!first) out.append(',');
                first = false;
                out.append(new com.google.gson.JsonPrimitive(entry.getKey())).append(':');
                appendCanonical(out, entry.getValue());
            }
            out.append('}');
        } else if (json.isJsonArray()) {
            out.append('[');
            boolean first = true;
            for (JsonElement entry : json.getAsJsonArray()) {
                if (!first) out.append(',');
                first = false;
                appendCanonical(out, entry);
            }
            out.append(']');
        } else out.append(json);
    }

    static String digest(String input) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new AssertionError(impossible);
        }
    }
}
