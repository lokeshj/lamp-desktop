package me.lokesh.lamp.service.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sujeet
 * Date: 11/29/13
 * Time: 1:10 AM
 * To change this template use File | Settings | File Templates.
 */
public final class JsonHandler {
    private static final Logger logger = LoggerFactory
            .getLogger(JsonHandler.class);

    private static ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    private JsonHandler() {
    }

    /**
     * * parses String in json format and maps it to
     * an object of given class
     *
     * @param json     String
     * @param classOfT class to map the json to
     * @return T
     */
    public static <T> T parse(String json, Class<T> classOfT) {
        try {
            if (json == null) {
                return null;
            }
            return mapper.readValue(json, classOfT);

        } catch (JsonParseException e) {
            logger.error("JsonParseException Failed to parse json correctly. " + e);
        } catch (JsonMappingException e) {
            logger.error("JsonMappingException Failed to parse json correctly. " + e);
        } catch (IOException e) {
            logger.error("IOException Failed to parse json correctly. " + e);
        }
        return null;
    }

    public static <T> List<T> parseAsList(String json, Class<T> classOfT) {
        try {
            if (json == null) {
                return null;
            }
            return mapper.readValue(json,
                    mapper.getTypeFactory().constructCollectionType(List.class, classOfT));

        } catch (JsonParseException e) {
            logger.error("JsonParseException Failed to parse json correctly. " + e);
        } catch (JsonMappingException e) {
            logger.error("JsonMappingException Failed to parse json correctly. " + e);
        } catch (IOException e) {
            logger.error("IOException Failed to parse json correctly. " + e);
        }
        return null;
    }

    public static String stringify(Object object) {
        try {
            return mapper.writeValueAsString(object);

        } catch (JsonProcessingException e) {
            logger.error("JsonProcessingException Failed to stringify json correctly. " + e);
            return null;
        }
    }
}
