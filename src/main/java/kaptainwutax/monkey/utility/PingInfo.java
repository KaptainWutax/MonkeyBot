package kaptainwutax.monkey.utility;

import java.util.HashMap;
import java.util.Map;

public class PingInfo {

    private final Map<SpamInfoKey, UserPingInfo> userSpamInfos = new HashMap<>();

    public UserPingInfo get(String guildId, String userId) {
        return userSpamInfos.computeIfAbsent(new SpamInfoKey(guildId, userId), k -> new UserPingInfo());
    }

    private static final class SpamInfoKey {
        private final String guildId;
        private final String userId;

        public SpamInfoKey(String guildId, String userId) {
            this.guildId = guildId;
            this.userId = userId;
        }

        @Override
        public int hashCode() {
            return guildId.hashCode() + 31 * userId.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) return true;
            if (other == null) return false;
            if (other.getClass() != SpamInfoKey.class) return false;
            SpamInfoKey that = (SpamInfoKey) other;
            return guildId.equals(that.guildId) && userId.equals(that.userId);
        }
    }

}
