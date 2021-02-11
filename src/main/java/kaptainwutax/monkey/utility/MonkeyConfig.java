package kaptainwutax.monkey.utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import kaptainwutax.monkey.holder.UserInfo;
import kaptainwutax.monkey.init.Guilds;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MonkeyConfig {

    @Expose public String token;
    @Expose public List<String> botAdmins = new ArrayList<>();
    @Expose public Guilds guilds = new Guilds();
    /**
     * A list of users SORTED BY ID
     */
    @Expose private final List<UserInfo> users = new ArrayList<>();

    // Debugging features
    @Expose public String[] commandPrefix = {"monkey", "\uD83D\uDC12"};
    @Expose public boolean simulateBans = false;

    public static MonkeyConfig generateConfig(String location) throws FileNotFoundException {
        return new Gson().fromJson(new FileReader(location), MonkeyConfig.class);
    }

    public static void saveConfig(MonkeyConfig config, String location) throws IOException {
        FileWriter writer = new FileWriter(location);
        new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create().toJson(config, writer);
        writer.flush();
        writer.close();
    }

    public UserInfo getOrCreateUser(long id) {
        int index = Collections.binarySearch(users, new UserInfo(id), Comparator.comparingLong(u -> u.id));
        if (index >= 0)
            return users.get(index);
        UserInfo user = new UserInfo(id);
        users.add(-index - 1, user);
        return user;
    }

}
