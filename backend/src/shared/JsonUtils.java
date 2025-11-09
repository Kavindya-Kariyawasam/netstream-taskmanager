package shared;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.lang.reflect.Type;
import java.util.List;

public class JsonUtils {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Convert object to JSON string
    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    // Convert JSON string to object
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    // Convert JSON string to JsonObject
    public static JsonObject parseJson(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    // Convert JSON string to List
    public static <T> List<T> fromJsonList(String json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }

    // Create success response
    public static String createSuccessResponse(Object data) {
        JsonObject response = new JsonObject();
        response.addProperty("status", "success");
        response.add("data", gson.toJsonTree(data));
        return response.toString();
    }

    // Create success response with message
    public static String createSuccessResponse(String message) {
        JsonObject response = new JsonObject();
        response.addProperty("status", "success");
        response.addProperty("message", message);
        return response.toString();
    }

    // Create error response
    public static String createErrorResponse(String message) {
        JsonObject response = new JsonObject();
        response.addProperty("status", "error");
        response.addProperty("message", message);
        return response.toString();
    }

    // Create error response with exception details
    public static String createErrorResponse(Exception e) {
        JsonObject response = new JsonObject();
        response.addProperty("status", "error");
        response.addProperty("message", e.getMessage());
        response.addProperty("type", e.getClass().getSimpleName());
        return response.toString();
    }
}