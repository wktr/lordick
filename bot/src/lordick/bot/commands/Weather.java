package lordick.bot.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lordick.Lordick;
import lordick.bot.CommandListener;
import lordick.bot.InitListener;
import xxx.moparisthebest.irclib.messages.IrcMessage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class Weather implements CommandListener, InitListener {

    private static int TIMEOUT = 10 * 60 * 1000; // length between queries in MS

    @Override
    public boolean init(final Lordick client) {
        // delete old weather data every 20 minutes
        client.getGroup().scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    Statement s = client.getDatabaseConnection().createStatement();
                    s.execute("delete from keyvalues where key like (select 'weather.%.' || substr(key, 19) from keyvalues where key like 'weather.lastquery.%' and value < " + (System.currentTimeMillis() - TIMEOUT) + ")");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, TIMEOUT * 2, TimeUnit.MILLISECONDS);
        return true;
    }

    private static final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather?mode=json&units=metric&q=";

    @Override
    public String getHelp() {
        return "Usage: weather [location]";
    }

    @Override
    public String getCommands() {
        return "weather";
    }

    @Override
    public void handleCommand(Lordick client, String command, IrcMessage message) {
        String location;
        if (!message.hasMessage()) {
            location = client.getKeyValue(message.getServer(), "weather.lastlocation." + message.getHostmask().getNick());
            if (location == null) {
                message.sendChatf("%s: No previous location stored", message.getHostmask().getNick());
                return;
            }
        } else {
            location = message.getMessage();
            client.setKeyValue(message.getServer(), "weather.lastlocation." + message.getHostmask().getNick(), location);
        }
        long lastquery = client.getKeyValueLong(message.getServer(), "weather.lastquery." + location);
        if (lastquery > 0 && System.currentTimeMillis() - lastquery < TIMEOUT) { // if last query was less than 10 minutes ago, send last data
            String lastdata = client.getKeyValue(message.getServer(), "weather.lastdata." + location);
            if (lastdata != null && !lastdata.isEmpty()) {
                message.sendChatf("%s: %s", message.getHostmask().getNick(), lastdata);
            } else {
                message.sendChatf("%s: There was an error last time retrieving the weather for %s, please try again in %d minute(s)", message.getHostmask().getNick(), location,
                        TimeUnit.MILLISECONDS.toMinutes(TIMEOUT - (System.currentTimeMillis() - lastquery)));
            }
        } else {
            String location_encoded;
            try {
                location_encoded = URLEncoder.encode(location, "UTF-8");
            } catch (Exception ex) {
                message.sendChatf("%s: Error parsing location, %s", message.getHostmask().getNick(), ex.getMessage());
                return;
            }
            client.setKeyValue(message.getServer(), "weather.lastquery." + location, System.currentTimeMillis());
            HttpURLConnection conn;
            int code;
            try {
                URL url = new URL(WEATHER_URL + location_encoded);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();
                code = conn.getResponseCode();
            } catch (Exception ex) {
                message.sendChatf("%s: Error connecting to weather service, %s", message.getHostmask().getNick(), ex.getMessage());
                return;
            }
            if (code == 512) {
                message.sendChatf("%s: Invalid location, %s", message.getHostmask().getNick(), location);
                return;
            }
            BufferedReader br;
            StringBuilder sb = new StringBuilder();
            try {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String data;
                while ((data = br.readLine()) != null) {
                    sb.append(data);
                    sb.append(' ');
                }
            } catch (Exception e) {
                message.sendChatf("%s: Error retrieving weather data for, %s", message.getHostmask().getNick(), e.getMessage());
                return;
            }
            try {
                br.close();
            } catch (Exception ignored) {
            }
            System.out.println("resp: " + sb.toString());
            JsonNode json;
            try {
                json = new ObjectMapper().readTree(sb.toString());
            } catch (Exception e) {
                message.sendChatf("%s: Error parsing weather data for, %s", message.getHostmask().getNick(), e.getMessage());
                return;
            }
            client.setKeyValue(message.getServer(), "weather.lastquery." + location, System.currentTimeMillis());
            if (!json.path("message").isMissingNode()) {
                message.sendChatf("%s: Error getting weather data for, %s", message.getHostmask().getNick(), json.get("message").textValue());
            } else {
                StringBuilder weather = new StringBuilder(String.format("Weather for %s %s, ", json.get("name").asText(), json.get("sys").path("country").asText()));
                String description = json.path("weather").path(0).path("description").textValue();
                if (description != null && !description.isEmpty()) {
                    weather.append(description);
                    weather.append(", ");
                }
                weather.append(String.format("Temp %sc (min %sc/max %sc), %s%% Humidity, %s hPa, %s%% Cloudy, Wind Speed %sm/s",
                        json.get("main").get("temp").asText(),
                        json.get("main").get("temp_min").asText(),
                        json.get("main").get("temp_max").asText(),
                        json.get("main").get("humidity").asText(),
                        json.get("main").get("pressure").asText(),
                        json.get("clouds").get("all").asText(),
                        json.get("wind").get("speed").asText()));
                JsonNode windGust = json.path("wind").path("gust");
                if (!windGust.isMissingNode()) {
                    weather.append(String.format(" (gusting %sm/s)", windGust.asText()));
                }

                Iterator<Map.Entry<String, JsonNode>> it = json.path("rain").fields();
                while (it.hasNext()) {
                    Map.Entry<String, JsonNode> item = it.next();
                    weather.append(", Rain ").append(item.getValue().asText()).append("mm/").append(item.getKey());
                    break;
                }
                it = json.path("snow").fields();
                while (it.hasNext()) {
                    Map.Entry<String, JsonNode> item = it.next();
                    weather.append(", Snow ").append(item.getValue().asText()).append("mm/").append(item.getKey());
                    break;
                }
                String formatted = weather.toString();
                client.setKeyValue(message.getServer(), "weather.lastdata." + location, formatted);
                message.sendChatf("%s: %s", message.getHostmask().getNick(), formatted);
            }
        }
    }
}
