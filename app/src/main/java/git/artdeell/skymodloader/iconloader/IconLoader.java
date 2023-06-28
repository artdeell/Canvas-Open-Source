package git.artdeell.skymodloader.iconloader;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import git.artdeell.skymodloader.SMLApplication;

public class IconLoader {
    private static final int IMAGE_START_LEN = "image=\"".length();
    private static final int IMAGE_UV_LEN = "uv=\"".length();
    public static void findIcons() {
        try {
            AssetManager skyAssets = SMLApplication.skyRes.getAssets();
            InputStream iconInputStream = skyAssets.open("Data/Resources/UIPackedAtlas.lua");
            BufferedReader reader = new BufferedReader(new InputStreamReader(iconInputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] lineSplit = line.split(" ");
                if (lineSplit.length > 3 && lineSplit[0].equals("resource")) {
                    String iconName = lineSplit[2].replace("\"", "");
                    String lineSpaceless = line.replaceAll("\\s+", "");
                    int iconStart = lineSpaceless.indexOf('{');
                    if (iconStart == -1) continue;
                    int imageStart = lineSpaceless.indexOf("image=\"", iconStart), uvStart = lineSpaceless.indexOf("uv={", iconStart);
                    if (imageStart == -1 || uvStart == -1) continue;
                    imageStart = imageStart + IMAGE_START_LEN;
                    uvStart = uvStart + IMAGE_UV_LEN;
                    String atlasName = lineSpaceless.substring(imageStart, lineSpaceless.indexOf('"', imageStart));
                    String uv = lineSpaceless.substring(uvStart, lineSpaceless.indexOf('}', uvStart));
                    if(uv.contains("/")) continue;
                    String[] uvs = uv.split(",");
                    addIcon(iconName, atlasName, Float.parseFloat(uvs[0]), Float.parseFloat(uvs[1]), Float.parseFloat(uvs[2]), Float.parseFloat(uvs[3]));
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static native void addIcon(String name, String atlasName, float u0, float v0, float u1, float v1);
}
