package com.fpvdrone.client.osd;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;

public final class OsdLayoutIO {
    private static final String FOLDER = "cubeflight/osd_layouts";
    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private OsdLayoutIO() {
    }

    public static Path getLayoutDir() {
        Path dir = Minecraft.getInstance().gameDirectory.toPath().resolve("cubeflight/osd_layouts");
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException var2) {
            }
        }

        return dir;
    }

    public static Path exportLayout(OsdLayout layout, String craftName) throws IOException {
        Path dir = getLayoutDir();
        String safeName = sanitizeFilename(craftName);
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FMT);
        String filename = safeName + "_" + timestamp + ".json";
        Path file = dir.resolve(filename);
        JsonObject root = toJson(layout);
        Files.writeString(file, GSON.toJson(root));
        return file;
    }

    public static OsdLayout importLayout(Path file) throws IOException {
        String json = Files.readString(file);
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        return fromJson(root);
    }

    public static List<Path> listLayouts() {
        Path dir = getLayoutDir();
        if (!Files.isDirectory(dir)) {
            return Collections.emptyList();
        } else {
            try {
                List var2;
                try (Stream<Path> stream = Files.list(dir)) {
                    var2 = stream.filter(p -> p.toString().endsWith(".json")).sorted().collect(Collectors.toList());
                }

                return var2;
            } catch (IOException var6) {
                return Collections.emptyList();
            }
        }
    }

    static JsonObject toJson(OsdLayout layout) {
        JsonObject root = new JsonObject();
        root.addProperty("gridCols", 30);
        root.addProperty("gridRows", 16);
        JsonArray elements = new JsonArray();

        for (OsdElement elem : layout.getAllElements()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", elem.getName());
            obj.addProperty("x", elem.getX());
            obj.addProperty("y", elem.getY());
            obj.addProperty("visible", elem.isVisible());
            if (elem.getCustomText() != null) {
                obj.addProperty("customText", elem.getCustomText());
            }

            elements.add(obj);
        }

        root.add("elements", elements);
        return root;
    }

    static OsdLayout fromJson(JsonObject root) {
        OsdLayout layout = new OsdLayout();
        JsonArray elements = root.getAsJsonArray("elements");
        if (elements == null) {
            return layout;
        } else {
            for (JsonElement je : elements) {
                JsonObject obj = je.getAsJsonObject();
                String name = obj.get("name").getAsString();
                if (OsdElementRegistry.get(name) != null) {
                    float x = obj.get("x").getAsFloat();
                    float y = obj.get("y").getAsFloat();
                    boolean visible = obj.get("visible").getAsBoolean();
                    OsdElementType type = OsdElementRegistry.get(name);
                    float maxX = 30 - type.getWidthChars();
                    float maxY = 16 - type.getHeightLines();
                    x = Math.max(0.0F, Math.min(maxX, x));
                    y = Math.max(0.0F, Math.min(maxY, y));
                    OsdElement elem = new OsdElement(name, x, y, visible);
                    if (obj.has("customText")) {
                        elem.setCustomText(obj.get("customText").getAsString());
                    }

                    layout.setElement(name, elem);
                }
            }

            return layout;
        }
    }

    private static String sanitizeFilename(String name) {
        return name != null && !name.isBlank() ? name.replaceAll("[^a-zA-Z0-9_\\-]", "_").trim() : "DRONE";
    }
}
