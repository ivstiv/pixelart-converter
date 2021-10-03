package core;

import com.dajudge.colordiff.RgbColor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class ColorPalette {

    private final List<RgbColor> palette;
    private final DrednotColor defaultColor; // value used for transparency

    public ColorPalette(List<RgbColor> palette, DrednotColor defaultColor) {
        this.palette = palette;
        this.defaultColor = defaultColor;
    }

    public static ColorPalette fromJson(String jsonData) throws IllegalStateException {
        JsonParser parser = new JsonParser();
        JsonElement tradeElement = parser.parse(jsonData);
        JsonObject config = tradeElement.getAsJsonObject();

        JsonObject defaultColorJson = config.getAsJsonObject("defaultColor");
        DrednotColor defaultColor = new DrednotColor(
                defaultColorJson.get("R").getAsInt(),
                defaultColorJson.get("G").getAsInt(),
                defaultColorJson.get("B").getAsInt(),
                defaultColorJson.get("ID").getAsString()
        );

        JsonArray colorsJson = config.getAsJsonArray("palette");
        List<RgbColor> colors = new ArrayList<>();
        for(JsonElement el : colorsJson) {
            String id = el.getAsJsonObject().get("ID").getAsString();
            int r = el.getAsJsonObject().get("R").getAsInt();
            int g = el.getAsJsonObject().get("G").getAsInt();
            int b = el.getAsJsonObject().get("B").getAsInt();
            colors.add(new DrednotColor(r,g,b,id));
        }
        return new ColorPalette(colors, defaultColor);
    }

    public List<RgbColor> getPalette() {
        return palette;
    }

    public DrednotColor getDefaultColor() {
        return defaultColor;
    }
}
