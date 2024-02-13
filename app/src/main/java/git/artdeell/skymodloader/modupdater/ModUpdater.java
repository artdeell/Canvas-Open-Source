package git.artdeell.skymodloader.modupdater;

import git.artdeell.skymodloader.modupdater.ModUpdateInfo;
import git.artdeell.skymodloader.modupdater.ModUpdaterService;
import git.artdeell.skymodloader.elfmod.ElfModMetadata;

import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.content.ServiceConnection;

import android.os.Build;
import android.os.IBinder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;

import java.util.stream.Collectors;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.lang.Exception;

import org.json.JSONObject;
import org.json.JSONException;

public class ModUpdater implements ServiceConnection {
	private static ModUpdater instance = null;

	private Context context;
	private ModUpdaterListener listener = null;

	// Info about mod to update is temporarily stored here
	private ElfModMetadata elfMod = null;
	private String elfModUrl = null;

	public static ModUpdater getInstance(Context context) {
		if (ModUpdater.instance == null) {
			ModUpdater.instance = new ModUpdater(context);
		}

		return ModUpdater.instance;
	}

	public ModUpdater(Context context) {
		this.context = context;
	}

	public interface ModUpdaterListener {
		public void onModUpdateAvailable(ElfModMetadata elfMod, ModUpdateInfo info);
		public void onModUpToDate(ElfModMetadata elfMod, ModUpdateInfo info);
		public void onModDownloadProgress(ElfModMetadata elfMod, int downloadedSize, int fileSize);
		public void onModDownloadComplete(ElfModMetadata elfMod);
		public void onModDownloadFailure(ElfModMetadata elfMod, Exception exception);
	}

	public void setModUpdaterListener(ModUpdaterListener listener) {
		// Set listener.
		if (this.listener == null) {
			this.listener = listener;
		}
	}

	public void checkForModUpdate(ElfModMetadata elfMod) throws IOException, JSONException {
		/* !!! This must be called in a separate Thread !!! */
		// Checks for mod update.
		String url = elfMod.getGithubReleasesUrl();

		if (url == null) {
			if (this.listener != null) {
				this.listener.onModUpToDate(elfMod, null);
			}

			return;
		}

		InputStream inputStream = new URL(url).openStream();
		JSONObject updateData = new JSONObject(new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n")));

		if (this.listener != null) {
			ModUpdateInfo modUpdateInfo = new ModUpdateInfo(updateData);

			if (this.versionCompare(elfMod, modUpdateInfo.tag)) {
				this.listener.onModUpdateAvailable(elfMod, modUpdateInfo);
			} else {
				this.listener.onModUpToDate(elfMod, modUpdateInfo);
			}
		}
	}

	public static boolean isDownloading() {
		// Returns `true` if any update is being downloaded, otherwise `false`.
		// Guaranteed to always return correct result.
		return ModUpdaterService.isDownloading();
	}

	public ElfModMetadata getCurrent() {
		return ModUpdaterService.getLastUpdatedMod();
	}

	public void updateMod(ElfModMetadata elfMod, String modUrl) {
		// Call this function to start update process
		this.elfMod = elfMod;
		this.elfModUrl = modUrl;
		this.startModUpdaterService("Updating " + elfMod.getName());
	}

	private void startModUpdaterService(String notifContent) {
		Intent modUpdaterServiceIntent = new Intent(this.context, ModUpdaterService.class);

		modUpdaterServiceIntent.putExtra("notif_content", notifContent);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			this.context.startForegroundService(modUpdaterServiceIntent);
		} else {
			this.context.startService(modUpdaterServiceIntent);
		}

		this.context.bindService(modUpdaterServiceIntent, this, this.context.BIND_AUTO_CREATE);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder binder) {
		ModUpdaterService service = ((ModUpdaterService.LocalBinder)binder).getService();
		service.downloadModThenStop(this.elfMod, this.elfModUrl, this.listener);
		this.context.unbindService(this);
		this.elfMod = null;
		this.elfModUrl = null;
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		this.elfMod = null;
		this.elfModUrl = null;
	}

	private Boolean versionCompare(ElfModMetadata elfMod, String tag) {
		// Compares version numbers.
		Pattern pattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
		Matcher matcher = pattern.matcher(tag);

		if (matcher.find()) {
			int majorVersion = Integer.parseInt(matcher.group(1));
			int minorVersion = Integer.parseInt(matcher.group(2));
			int patchVersion = Integer.parseInt(matcher.group(3));

			if (majorVersion > elfMod.getMajorVersion()) {
				return true;
			}

			if (minorVersion > elfMod.getMinorVersion()) {
				return true;
			}

			if (patchVersion > elfMod.getPatchVersion()) {
				return true;
			}
		}

		return false;
	}
}