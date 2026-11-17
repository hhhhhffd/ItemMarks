import com.google.gson.JsonParser;
import com.itemmarks.IdentityMath;

public class IdentityMathTest {
    private static int checks;
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
        ++checks;
    }

    public static void main(String[] args) {
        final String canonical = "550e8400e29b41d4a716446655440000";
        final String second = "550e8400e29b41d4a716446655440001";
        check(IdentityMath.uuid("550E8400-E29B-41D4-A716-446655440000").equals(canonical), "case and dash stable");
        check(IdentityMath.uuid(canonical).equals(canonical), "compact id recognized");
        check(IdentityMath.uuid("0-0-0-0-0") == null, "not a valid raw uuid spelling");
        check(IdentityMath.uuid("00000000-0000-0000-0000-000000000000") == null, "nil ID ignored");
        check(IdentityMath.uuid("item-5566") == null && IdentityMath.uuid("1234") == null, "not an arbitrary number");
        check(IdentityMath.unique("server:a", canonical).equals(IdentityMath.unique("server:a", canonical)), "same instance key");
        check(!IdentityMath.unique("server:a", canonical).equals(IdentityMath.unique("server:b", canonical)), "world scopes differ");
        check(!IdentityMath.unique("server:a", canonical).equals(IdentityMath.unique("server:a", second)), "true UUIDs distinguish instances");

        var serializedA = JsonParser.parseString("{\"id\":\"diamond_sword\",\"components\":{\"name\":\"Alpha\",\"enchant\":{\"sharpness\":5,\"unbreaking\":3}}}");
        var serializedB = JsonParser.parseString("{\"components\":{\"enchant\":{\"unbreaking\":3,\"sharpness\":5},\"name\":\"Alpha\"},\"id\":\"diamond_sword\"}");
        var serializedOther = JsonParser.parseString("{\"components\":{\"enchant\":{\"unbreaking\":3,\"sharpness\":5},\"name\":\"Bravo\"},\"id\":\"diamond_sword\"}");
        check(IdentityMath.fingerprint("minecraft:diamond_sword", serializedA).equals(
                IdentityMath.fingerprint("minecraft:diamond_sword", serializedB)), "object key order ignored recursively");
        check(!IdentityMath.fingerprint("minecraft:diamond_sword", serializedA).equals(
                IdentityMath.fingerprint("minecraft:diamond_sword", serializedOther)), "custom name affects pattern");
        check(!IdentityMath.fingerprint("minecraft:diamond_sword", serializedA).equals(
                IdentityMath.fingerprint("minecraft:iron_sword", serializedA)), "registry identifier affects pattern");
        check(!IdentityMath.fingerprint("minecraft:tipped_arrow", JsonParser.parseString("[\"venom\",\"ice\"]")).equals(
                IdentityMath.fingerprint("minecraft:tipped_arrow", JsonParser.parseString("[\"ice\",\"venom\"]"))), "ordered component lists stay ordered");
        System.out.println("Passed " + checks + " canonical identity checks.");
    }
}
