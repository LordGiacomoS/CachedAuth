package com.lordgiacomos.cachedauth.config;

import com.google.gson.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class CachedAuthConfigManager {
    //public static final Logger LOGGER = LoggerFactory.getLogger(CachedAuthClientInit.class);
    private static File file;
    private static void prepareConfigFile() {
        if (file != null) {
            return;
        }
        //file = new File(FabricLoader.getInstance().getConfigDir().toFile(), CachedAuthClientInit.MOD_ID.toLowerCase(Locale.ROOT) + ".json");
        file = new File(new File(System.getProperty("CWD")), "config.json");
    }

    public static void load() {
        prepareConfigFile();
        try {
            if (!file.exists()) {
                save();
            }

            BufferedReader br = new BufferedReader(new FileReader(file));
            JsonObject json = JsonParser.parseReader(br).getAsJsonObject();

            CachedAuthConfig.setModEnabled(json.get("modEnabled").getAsBoolean());
            CachedAuthConfig.setSavedAccounts(
                new ArrayList<>() {{
                    for (JsonElement accountElement : json.getAsJsonArray("savedAccounts")) {
                        add(new SavedAccount((JsonObject) accountElement));
                    }
                }}
            );


        } catch (IOException e) {
            System.out.println("unable to load config");
        }

    }

    public static void save() {
        prepareConfigFile();

        JsonObject config = new JsonObject();
        config.addProperty("modEnabled", CachedAuthConfig.getModEnabled());
        JsonArray savedAccounts = new JsonArray();
        for (SavedAccount savedAccount : CachedAuthConfig.getSavedAccounts()) {
            savedAccounts.add(savedAccount.getJsonOutput());
        } // this loop is nice & laconic compared to my last config file, I love it
        config.add("savedAccounts", savedAccounts);

        //String jsonString = CachedAuthClientInit.GSON.toJson(config);
        String jsonString = config.getAsString();
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(jsonString);
        } catch (IOException e) {
            //LOGGER.error("unable to save config", e);
            System.out.println("unable to save config");
        }
    }

}
