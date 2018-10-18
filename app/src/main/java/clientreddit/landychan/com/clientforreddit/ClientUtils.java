package clientreddit.landychan.com.clientforreddit;

import com.google.gson.Gson;

public class ClientUtils {

    private static Gson gson = new Gson();

    public static Gson getGson() {
        return gson;
    }
}
