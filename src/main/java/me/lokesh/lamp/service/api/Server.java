package me.lokesh.lamp.service.api;

import com.google.common.eventbus.EventBus;
import me.lokesh.lamp.Shared;
import me.lokesh.lamp.events.AddToPlaylistEvent;
import me.lokesh.lamp.events.StartPlaybackEvent;
import me.lokesh.lamp.service.Config;
import me.lokesh.lamp.service.player.Track;
import me.lokesh.lamp.service.search.AsyncRecursiveDirectoryStream;
import me.lokesh.lamp.service.search.SearchAgent;
import me.lokesh.lamp.service.utils.JsonHandler;
import me.lokesh.lamp.service.utils.SystemProperties;
import org.kevoree.library.nanohttp.NanoHTTPD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by lokesh.
 */
public class Server implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static final int PORT = 15492;

    private static EventBus eventBus = Shared.getEventBus();
    private NanoServer mNanoServer;

    private final Object lock = new Object();

    private final Map<String, CachedResponse> cacheMap;
    private final static long cacheStaleIn = 60 * 1000 * 1000 * 1000L;     //1 min
    private final static int cacheSize = 50;

    public Server() {
        cacheMap = new HashMap<>(cacheSize);
    }

    @Override
    public void run() {

        try {
            mNanoServer = new NanoServer(PORT);
            synchronized (lock) {
                lock.wait();
            }

        } catch (IOException e) {
            logger.error("Error starting server", e);
            mNanoServer.stop();
        } catch (InterruptedException e) {
            logger.info("Api server stopped!");
            mNanoServer.stop();
        }
    }

    private class CachedResponse {
        private String response;
        private long timestamp;

        private CachedResponse(String response, long timestamp) {
            this.response = response;
            this.timestamp = timestamp;
        }

        public String getResponse() {
            return response;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    private CachedResponse checkCache(String key) {
        long curTime = System.nanoTime();
        CachedResponse cachedResponse = cacheMap.get(key);
        if (cachedResponse != null ) {
            if (curTime - cachedResponse.getTimestamp() < cacheStaleIn) {
                logger.info("search: cache hit!");
            } else  {
                logger.info("cache stale");
                cacheMap.remove(key);
            }
        } else {
            logger.info("cache miss");
        }
        return cachedResponse;
    }

    private void addToCache(String key, String response) {
        cacheMap.put(key, new CachedResponse(response, System.nanoTime()));
    }

    private class NanoServer extends NanoHTTPD {

        public NanoServer(int port) throws IOException {
            super(port);
            logger.info("starting nano server @{}", port);
        }

        @Override
        public Response serve(String uri, String method, Properties header,
                              Properties parms, Properties files, InputStream body) {
            switch (uri) {
                case "/file": {
                    return handleFile(parms);
                }
                case "/seed": {
                    return handleSeed(parms);
                }
                case "/search": {
                    return handleSearch(parms);
                }
                case "/addToPlaylist": {
                    return handleAddToPlaylist(parms);
                }
                default: {
                    return new Response(HTTP_OK, MIME_HTML, "LAMP at your service");
                }
            }
        }

        private Response handleFile(Properties parms) {
            //serve the requested file
            String filename = parms.getProperty("f");
            logger.info("request received to play file {}", filename);
            Path fullpath = Config.getMusicFolderPath().resolve(filename);
            logger.info("full path resolved to {}", fullpath.toAbsolutePath().toString());

            if (filename != null) {
                try {
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(
                            new FileInputStream(fullpath.toFile())
                    );

                    return new Response(HTTP_OK, "audio/mpeg", bufferedInputStream);
                } catch (FileNotFoundException e) {
                    return new Response(HTTP_NOTFOUND, MIME_HTML, "Not Found");
                }

            } else  {
                return new Response(HTTP_BADREQUEST, MIME_HTML, "Filename is required parameter");
            }
        }

        private Response handleSeed(Properties parms) {
            //process the command
            //start playing the file from the specified host
            String url = parms.getProperty("url");
            String name = parms.getProperty("name");

            if (url != null && name != null) {
                eventBus.post(new StartPlaybackEvent(new Track(url, name)));
                return new Response(HTTP_OK, MIME_HTML, "got it! playing " + url);

            } else {
                return new Response(HTTP_BADREQUEST, MIME_HTML, "host and file are both required parameters");
            }
        }

        private Response handleSearch(Properties parms) {
            String query = parms.getProperty("q");
            logger.info("got search request. query={}", query);

            if (query != null) {
                CachedResponse cachedResponse = checkCache(query);
                if(cachedResponse != null) {
                    return new NanoHTTPD.Response(HTTP_OK, "application/json", cachedResponse.getResponse());
                }

                List<Track> results = new LinkedList<>();
                try {
                    AsyncRecursiveDirectoryStream searchResult = SearchAgent.local(query);

                    for (Path result : searchResult) {
                        results.add(new Track(
                                "http://" + SystemProperties.getIPAddress() + ":" + PORT + "/file?f=" +
                                        URLEncoder.encode(
                                                Config.getMusicFolderPath().relativize(result).toString(), "UTF-8"
                                        ),
                                result.getFileName().toString()));
                    }

                } catch (IOException e) {
                    logger.error("Error in search ", e);
                }

                logger.info("search finished");
                String response = JsonHandler.stringify(results);
                addToCache(query, response);
                return new Response(HTTP_OK, "application/json", response);

            } else {
                return new Response(HTTP_BADREQUEST, MIME_HTML, "q is a required parameter");
            }
        }

        private Response handleAddToPlaylist(Properties parms) {
            String url = parms.getProperty("url");
            String name = parms.getProperty("name");

            if (url != null && name != null) {
                eventBus.post(new AddToPlaylistEvent(new Track(url, name)));
                return new Response(HTTP_OK, MIME_HTML, "got it! added to playlist " + url);

            } else {
                return new Response(HTTP_BADREQUEST, MIME_HTML, "host and file are both required parameters");
            }
        }
    }
}
