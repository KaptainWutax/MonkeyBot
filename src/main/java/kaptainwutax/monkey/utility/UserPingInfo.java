package kaptainwutax.monkey.utility;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class UserPingInfo {

    private static final long SPAM_TIMEFRAME = 60L * 1000L * 1000L * 1000L; // one minute

    private final LinkedHashMap<String, Long> pingTimeStamps = new LinkedHashMap<>(1);

    /**
     * Removes ping entries which are more than a minute old
     */
    public void purge() {
        long now = System.nanoTime();
        Iterator<Map.Entry<String, Long>> itr = pingTimeStamps.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, Long> entry = itr.next();
            if (now - entry.getValue() >= SPAM_TIMEFRAME)
                itr.remove();
            else
                break;
        }
    }

    /**
     * This input string may be message IDs in the case of @everyone or @here, or role or user IDs in the case of
     * role or user mentions.
     */
    public void addPing(String ping) {
        long now = System.nanoTime();
        // removing then re-adding puts it at the end of the LinkedHashMap
        pingTimeStamps.remove(ping);
        pingTimeStamps.put(ping, now);
    }

    /**
     * WARNING: may return a higher value until calling {@link #purge()}
     */
    public int getPingCount() {
        return pingTimeStamps.size();
    }

}
