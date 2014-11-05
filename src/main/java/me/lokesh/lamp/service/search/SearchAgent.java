package me.lokesh.lamp.service.search;

import me.lokesh.lamp.service.Config;
import me.lokesh.lamp.service.api.Server;
import me.lokesh.lamp.service.player.Track;
import me.lokesh.lamp.service.utils.HttpAgent;
import me.lokesh.lamp.service.utils.JsonHandler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lokesh.
 */
public class SearchAgent {

    public static AsyncRecursiveDirectoryStream local(String query) throws IOException {
        String pattern = "(?i).*";
        if (!query.isEmpty()) {
            //escape regex special characters
            query = query.replaceAll("([\\\\\\.\\[\\{\\(\\*\\+\\?\\^\\$\\|])", "\\\\$1");
            pattern = pattern + query.replaceAll(" ", ".*") + ".*";
        }

        return new AsyncRecursiveDirectoryStream(Config.getMusicFolderPath(), pattern + "\\.mp3");
    }

    public static List<Track> remote(String host, String query) {
        try {
            String url = "http://" + host + ":" + Server.PORT + "/search?q=" + URLEncoder.encode(query, "UTF-8");
            String resultString = HttpAgent.get(url);
            return JsonHandler.parseAsList(resultString, Track.class);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return new ArrayList<>(0);
        }
    }
}
