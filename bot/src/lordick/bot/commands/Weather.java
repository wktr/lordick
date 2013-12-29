package lordick.bot.commands;

import lordick.Lordick;
import lordick.bot.CommandListener;
import lordick.util.Json;
import xxx.moparisthebest.irclib.messages.IrcMessage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@SuppressWarnings("unused")
public class Weather implements CommandListener {

    private static final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather?mode=json&units=metric&q=";

    @Override
    public String getHelp() {
        return "Usage: weather [location]";
    }

    @Override
    public String getCommands() {
        return "weather";
    }

    private long getLong(String s) {
        try {
            return Long.valueOf(s);
        } catch (Exception ignored) {
            return -1;
        }
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
        long lastquery = getLong(client.getKeyValue(message.getServer(), "weather.lastquery." + location));
        if (lastquery > 0 && System.currentTimeMillis() - lastquery < 10 * 60 * 1000) { // if last query was less than 10 minutes ago, send last data
            String lastdata = client.getKeyValue(message.getServer(), "weather.lastdata." + location);
            if (lastdata != null && !lastdata.isEmpty()) {
                message.sendChatf("%s: %s", message.getHostmask().getNick(), lastdata);
            } else {
                long timeout = 10 * 60 - (System.currentTimeMillis() - lastquery) / 1000;
                message.sendChatf("%s: There was an error last time retrieving the weather for %s, please try again in %d minute(s)", message.getHostmask().getNick(), location, timeout / 60 + 1);
            }
        } else {
            String location_encoded;
            try {
                location_encoded = URLEncoder.encode(location, StandardCharsets.UTF_8.name());
            } catch (Exception ex) {
                message.sendChatf("%s: Error parsing location, %s", message.getHostmask().getNick(), ex.getMessage());
                return;
            }
            HttpURLConnection conn;
            int code;
            try {
                URL url = new URL(WEATHER_URL + location_encoded);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();
                code = conn.getResponseCode();
                System.out.println("CODE: " + code);
            } catch (Exception ex) {
                message.sendChatf("%s: Error connecting to weather service, %s", message.getHostmask().getNick(), ex.getMessage());
                return;
            }
            client.setKeyValue(message.getServer(), "weather.lastquery." + location, System.currentTimeMillis());
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
            Map<String, String> json = Json.parse(sb.toString());
            client.setKeyValue(message.getServer(), "weather.lastquery." + location, System.currentTimeMillis());
            if (json.containsKey("message")) {
                message.sendChatf("%s: Error getting weather data for, %s", message.getHostmask().getNick(), json.get("message"));
            } else {
                StringBuilder weather = new StringBuilder(String.format("Weather for %s %s, ", json.get("name"), json.get("sys.country")));
                if (json.containsKey("weather.0.description")) {
                    weather.append(json.get("weather.0.description"));
                    weather.append(", ");
                }
                weather.append(String.format(", %s, Temp %sc (min %sc/max %sc), %s%% Humidity, %s hPa, %s%% Cloudy, Wind Speed %sm/s",
                        json.get("weather.0.description"),
                        json.get("main.temp"),
                        json.get("main.temp_min"),
                        json.get("main.temp_max"),
                        json.get("main.humidity"),
                        json.get("main.pressure"),
                        json.get("clouds.all"),
                        json.get("wind.speed")));
                if (json.containsKey("wind.gust")) {
                    weather.append(String.format(" (gusting %sm/s)", json.get("wind.gust")));
                }
                if (json.containsKey("rain.3h")) {
                    weather.append(String.format(", Rain %smm/3h", json.get("rain.3h")));
                }
                if (json.containsKey("snow.3h")) {
                    weather.append(String.format(", Snow %smm/3h", json.get("snow.3h")));
                }
                String formatted = weather.toString();
                client.setKeyValue(message.getServer(), "weather.lastdata." + location, formatted);
                message.sendChatf("%s: %s", message.getHostmask().getNick(), formatted);
            }
        }
    }
}
