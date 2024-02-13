package git.artdeell.skymodloader.modupdater;

import android.app.Service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import android.content.Intent;
import android.content.Context;
import android.content.pm.ServiceInfo;

import android.os.Bundle;
import android.os.Binder;
import android.os.IBinder;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.lang.Exception;
import java.lang.Thread;

import git.artdeell.skymodloader.R;
import git.artdeell.skymodloader.SMLApplication;
import git.artdeell.skymodloader.elfmod.ElfModMetadata;
import git.artdeell.skymodloader.elfmod.ElfModUIMetadata;
import git.artdeell.skymodloader.elfmod.ElfUIBackbone;
import git.artdeell.skymodloader.elfmod.InvalidModException;
import git.artdeell.skymodloader.modupdater.ModUpdater;

public class ModUpdaterService extends Service {
	private static Boolean downloading = false;
	private static ElfModMetadata lastUpdatedMod = null;

	private final int notifId = 999;
	private Notification notif;

	public static Boolean isDownloading() {
		return ModUpdaterService.downloading;
	}

	public static ElfModMetadata getLastUpdatedMod() {
		return ModUpdaterService.lastUpdatedMod;
	}

	public class LocalBinder extends Binder {
		public ModUpdaterService getService() {
			return ModUpdaterService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new LocalBinder();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
			return Service.START_NOT_STICKY;
		}

		Bundle bundle = intent.getExtras();
		String notifContent = (String) bundle.get("notif_content");

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, SMLApplication.MOD_UPDATER_SERVICE_NOTIFICATION_CHANNEL)
				.setSmallIcon(R.drawable.icon_fg)
				.setContentTitle("Mod Updater")
				.setContentText(notifContent)
				.setOngoing(true);

		this.notif = builder.build();

		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			notificationManager.notify(this.notifId, this.notif);
			this.startForeground(this.notifId, this.notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
		} else {
			this.startForeground(this.notifId, this.notif);
		}

		return Service.START_NOT_STICKY;
	}

	public void stop() {
		this.stopSelf();
		NotificationManager notifManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
		notifManager.cancel(this.notifId);
	}

	public void downloadMod(ElfModMetadata elfMod, String modUrl, ModUpdater.ModUpdaterListener listener) {
		// Download process
		ModUpdaterService.lastUpdatedMod = elfMod;
		ModUpdaterService.downloading = true;

		String modFilePath = elfMod.modFile.getPath();

		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(modUrl).openConnection();
			conn.connect();
			int fileSize = conn.getContentLength();
			InputStream is = conn.getInputStream();
			File newModFile = new File(getCacheDir(), "libtemp.so.download");
			FileOutputStream out = new FileOutputStream(newModFile);
			byte[] buffer = new byte[8192];

			int len;
			int downloadedSize = 0;
			while ((len = is.read(buffer)) != -1) {
				out.write(buffer, 0, len);
				downloadedSize += len;
				listener.onModDownloadProgress(elfMod, downloadedSize, fileSize);
				updateNotification(elfMod, downloadedSize, fileSize);
			}

			ElfUIBackbone loader = ((ElfModUIMetadata)elfMod).loader;

			File modFile2 = new File(modFilePath + ".old");
			elfMod.modFile.renameTo(modFile2);
			elfMod.modFile = modFile2;

			ElfModUIMetadata newElfMod = loader.updateElfMod(
					loader.getModIndex(elfMod),
					newModFile
			);
			
			elfMod.modFile.delete();

			listener.onModDownloadComplete(newElfMod);

		} catch (IOException | InvalidModException ex) {
			ex.printStackTrace();
			elfMod.modFile.renameTo(new File(modFilePath)); // rename back the original lib
			listener.onModDownloadFailure(elfMod, ex);
		}
		ModUpdaterService.downloading = false;
	}

	private void updateNotification(ElfModMetadata elfMod, int progress, int maxProgress) {
		if (notif != null) {
			NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

			int progressPercent = (int) ((progress * 100.0f) / maxProgress);

			NotificationCompat.Builder builder = new NotificationCompat.Builder(this, SMLApplication.MOD_UPDATER_SERVICE_NOTIFICATION_CHANNEL)
					.setSmallIcon(R.drawable.icon_fg)
					.setContentTitle("Mod Updater")
					.setContentText("Downloading: " + elfMod.getLibName() + " " + progressPercent + "%")
					.setOngoing(true)
					.setProgress(maxProgress, progress, false);

			notificationManager.notify(notifId, builder.build());
		}
	}


	public void downloadModThenStop(ElfModMetadata elfMod, String modUrl, ModUpdater.ModUpdaterListener listener) {
		new Thread(() -> {
			this.downloadMod(elfMod, modUrl, listener);
			this.stop();
		}).start();
	}
}