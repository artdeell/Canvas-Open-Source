package git.artdeell.skymodloader.ui.modmgr;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.*;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;
import git.artdeell.skymodloader.BR;
import git.artdeell.skymodloader.DialogY;
import git.artdeell.skymodloader.R;
import git.artdeell.skymodloader.elfmod.*;
import git.artdeell.skymodloader.updater.*;
import git.artdeell.skymodloader.ui.BaseViewModel;
import me.tatarka.bindingcollectionadapter2.ItemBinding;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static android.content.Context.BIND_AUTO_CREATE;

public class ModManagerViewModel extends BaseViewModel implements LoadingListener,ModUpdater  {
    private Boolean loading = true;
    public ObservableList<ElfModMetadata> items;
    public final ItemBinding<ElfModMetadata> itemBinding = ItemBinding.of(BR._item, R.layout.mod_list_element);

    private static final int REQUEST_MOD = 1024 * 121;
    @SuppressLint("StaticFieldLeak")
    public static ElfUIBackbone loader;
    public Activity context;
    private ModUpdaterDialogManager mDialogManager;


    @Bindable
    public Boolean getLoading() {
        return loading;
    }

    public void setLoading(Boolean bool) {
        loading = bool;
        notifyPropertyChanged(BR._all);
    }

    public void init(git.artdeell.skymodloader.ui.ModManagerActivity c) {
        context = c;
        initializeModUpdater();
        initializeLoader();
        itemBinding.bindExtra(BR.viewModel, this);

        runUpdater();
    }

    private void initializeLoader() {
        if (loader == null) {
            loader = new ElfUIBackbone(context, this);
            loader.addListener(this);
            loader.startLoadingAsync(new File(context.getFilesDir(), "mods"));
            mDialogManager.setLoader(loader);
        } else {
            handleLoading();
            handleUnsafeModRemoval();
            handleException();
            loader.addListener(this);
        }
    }

    public static InputStream getInputStreamFromURI(Context context, Uri uri) throws IOException {
        ContentResolver contentResolver = context.getContentResolver();
        return contentResolver.openInputStream(uri);
    }


    public void onAddMod(View view) {
        ((git.artdeell.skymodloader.ui.ModManagerActivity) view.getContext()).mGetContent.launch("*/*");
        notifyPropertyChanged(BR._all);
    }



    private void handleException() {
        Exception e = loader.getException();
        if (e == null) {
            return;
        }
        String message;
        if (e instanceof NoDependenciesException) {
            NoDependenciesException exc = (NoDependenciesException) e;
            StringBuilder stringBuilder = new StringBuilder();
            for (ElfModMetadata meta : exc.failedDependencies) {
                stringBuilder.append(context.getString(R.string.mod_add_missingdep, meta.name, meta.majorVersion, meta.minorVersion));
                stringBuilder.append('\n');
            }
            message = stringBuilder.toString();
        } else if (e instanceof InvalidModException) {
            message = context.getString(R.string.mod_add_wrongformat);
        } else if (e instanceof IOException) {
            message = e.getMessage();
        } else if (e instanceof ModExistsException) {
            message = context.getString(R.string.mod_add_exists);
        } else {
            e.printStackTrace();
            message = e.getMessage();
        }

        DialogY dialogY = DialogY.createFromActivity(context);
        dialogY.positiveButton.setVisibility(View.GONE);
        dialogY.title.setText(R.string.mod_add_unable);
        dialogY.content.setText(message);
        dialogY.negativeButton.setOnClickListener((v)->dialogY.dialog.dismiss());
        dialogY.dialog.setCancelable(true);
        dialogY.dialog.show();
    }

    public void runUpdater() {
        Intent updaterService = new Intent(context, CanvasUpdaterService.class);
        context.bindService(updaterService, new CanvasUpdaterConnection(context), BIND_AUTO_CREATE);
    }

    private void handleUnsafeModRemoval() {
        ElfUIBackbone.UnsafeRemovalMetadata metadata = loader.getUnsafeRemovalMetadata();
        if (metadata == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (ElfModUIMetadata meta : metadata.dependingMods) {
            sb.append(context.getString(R.string.mod_remove_dep, ModListAdapter.getVisibleModName(meta)));
            sb.append('\n');
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.mod_remove_unable);
        builder.setMessage(sb.toString());
        builder.setPositiveButton(android.R.string.ok, (d, w) -> loader.resetModRemovalMetadata());
        builder.setOnCancelListener((d) -> loader.resetModRemovalMetadata());
        builder.show();
    }

    private void handleLoading() {
        if (loader.getProgressBarState()) {
            setLoading(true);
        } else {
            setLoading(false);
        }
    }

    public boolean canUpdate(ElfModUIMetadata meta){
        String githubReleasesRegex = "https://api\\.github\\.com/repos/.+/releases/latest";
        return (meta.githubReleasesUrl != null && meta.githubReleasesUrl.matches(githubReleasesRegex));
    }

    @Override
    public void onCleared() {
        if (loader != null) {
            loader.removeListener();
            context.unbindService(mDialogManager);
        }

    }

    public int getUpdateText(ElfModUIMetadata metadata){
        String githubReleasesRegex = "https://api\\.github\\.com/repos/.+/releases/latest";
        if (metadata.githubReleasesUrl != null && metadata.githubReleasesUrl.matches(githubReleasesRegex)) {
            return R.string.check_for_updates;
        }else{
            return R.string.no_update_links;
        }
    }

    public void onCheckForUpdates(ElfModUIMetadata metadata) {
        loader.modUpdater.startModUpdater(metadata);
    }

    private void initializeModUpdater() {
        mDialogManager = new ModUpdaterDialogManager(context);
        // This will not do anything if the service isn't already started, and it's only
        // started when a mod is in the process of updating.
        context.bindService(new Intent(context, ModUpdaterService.class), mDialogManager, 0);
    }

    public void startModUpdater(ElfModUIMetadata metadata) {
        Log.i("MMA", "Starting mod update...");
        if(mDialogManager.isConnected()) {
            Toast.makeText(context, R.string.updater_busy, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent serviceStartIntent = new Intent(context, ModUpdaterService.class);
        serviceStartIntent.putExtra(ModUpdaterService.EXTRA_UPDATE_URL, metadata.getGithubReleasesUrl());
        serviceStartIntent.putExtra(ModUpdaterService.EXTRA_LIB_NAME, metadata.name);
        serviceStartIntent.putExtra(ModUpdaterService.EXTRA_VERSION_NUMBER,
                new VersionNumber(metadata.majorVersion, metadata.minorVersion, metadata.patchVersion)
        );
        context.startService(serviceStartIntent);
        context.bindService(new Intent(context, ModUpdaterService.class), mDialogManager, 0);
    }

    @Override
    public void refreshModList(int mode, int which) {
        notifyPropertyChanged(BR._all);
    }

    @Override
    public void onLoadingUpdated() {
        items = new ObservableArrayList<ElfModMetadata>();
        if (loader.getModsCount() != 0) {
            for (int i = 0; i <= (loader.getModsCount() - 1); i++) {
                ElfModUIMetadata metadata = loader.getMod(i);
                metadata.loader = loader;
                metadata.which = i;
                items.add(metadata);
            }
        }
        setLoading(false);
    }

    @Override
    public void signalModRemovalUnsafe() {
        handleUnsafeModRemoval();
    }

    @Override
    public void signalModRemovalError() {
        context.runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.mod_remove_unable);
            builder.setMessage(R.string.mod_ioe);
            builder.setPositiveButton(android.R.string.ok, (d, w) -> {
            });
            builder.show();
        });
    }

    @Override
    public void signalModAddException() {
        handleException();
    }


}