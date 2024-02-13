package git.artdeell.skymodloader.modupdater;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class ModUpdateInfo {
	public String name;               // Release name
	public String description;        // Changelog / Release body
	public String tag;                // Release tag. Guaranteed to be unique for each release
	public String url;                // Direct url to mod file
	public int size;                  // Mod file size
	public int downloadCount;         // Number of times the mod file has been downloaded

	private Boolean valid = false;    // `true` if all fields above is filled, otherwise `false`

	ModUpdateInfo(JSONObject updateData) {
		try {
			this.name = updateData.getString("name");
			this.description = updateData.getString("body");
			this.tag = updateData.getString("tag_name");

			JSONArray assets = updateData.getJSONArray("assets");

			if (assets.length() > 0) {
				JSONObject asset = assets.getJSONObject(0);

				this.url = asset.getString("browser_download_url");
				this.size = asset.getInt("size");
				this.downloadCount = asset.getInt("download_count");

				this.valid = true;
			}
		} catch (JSONException ex) {
			ex.printStackTrace();
		}
	}

	public Boolean isValid() {
		return this.valid;
	}
}