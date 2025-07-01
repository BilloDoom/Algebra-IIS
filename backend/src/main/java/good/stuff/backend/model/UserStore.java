package good.stuff.backend.model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class UserStore {
    public static final Map<String, String> users = new ConcurrentHashMap<>();

    static {
        users.put("user", "password");
    }
}
