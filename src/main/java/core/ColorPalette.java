package core;

import com.dajudge.colordiff.RgbColor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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

    public static ColorPalette fromJson(String jsonData) {
        JsonParser parser = new JsonParser();
        JsonElement tradeElement = parser.parse(jsonData);
        JsonArray colorsJson = tradeElement.getAsJsonArray();

        List<RgbColor> colors = new ArrayList<>();
        for(JsonElement el : colorsJson) {
            String id = el.getAsJsonObject().get("ID").getAsString();
            int r = el.getAsJsonObject().get("R").getAsInt();
            int g = el.getAsJsonObject().get("G").getAsInt();
            int b = el.getAsJsonObject().get("B").getAsInt();
            colors.add(new DrednotColor(r,g,b,id));
        }
        return new ColorPalette(colors, new DrednotColor(187,187,187, "FA")); // TO-DO: fix this dummy value
    }

    public List<RgbColor> getPalette() {
        return palette;
    }

    public DrednotColor getDefaultColor() {
        return defaultColor;
    }
}
