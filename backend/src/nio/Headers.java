package nio;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class Headers {
    private final Map<String, String> map = new HashMap<>();

    public static Headers parse(BufferedInputStream bis) throws IOException {
        Headers headers = new Headers();
        String line;

        while(true){
            line = readLine(bis);
            if(line == null || line.isEmpty()) break;
            int index = line.indexOf(':');
            if(index > 0) {
                String name = line.substring(0, index).trim().toLowerCase(Locale.ROOT);
                String value = line.substring(0, index);
                headers.map.put(name, value);
            }
        }
        return headers;
    }
    public Optional<String> get(String name) {
        return Optional.ofNullable(map.get(name.toLowerCase(Locale.ROOT)));
    }

    private static String readLine(BufferedInputStream bis) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        int value;
        boolean seenCR = false;
        while((value = bis.read()) != -1) {
            if(value == '\r') {
                seenCR = true;
                continue;
            }
            if(value == '\n') break;
            if(seenCR) {
                stringBuilder.append('\r');
                seenCR = false;
            }
            stringBuilder.append((char)value);
        }
        if(stringBuilder.length() == 0 && value == -1) return null;
        return stringBuilder.toString();
    }
}
