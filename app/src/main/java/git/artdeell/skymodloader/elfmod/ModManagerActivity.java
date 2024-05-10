package git.artdeell.skymodloader.elfmod;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

import git.artdeell.skymodloader.BuildConfig;
import git.artdeell.skymodloader.DialogY;
import git.artdeell.skymodloader.MainActivity;
import git.artdeell.skymodloader.R;
import git.artdeell.skymodloader.SMLApplication;
import git.artdeell.skymodloader.updater.CanvasUpdaterConnection;
import git.artdeell.skymodloader.updater.CanvasUpdaterService;
import git.artdeell.skymodloader.updater.ModUpdater;
import git.artdeell.skymodloader.updater.ModUpdaterService;
import git.artdeell.skymodloader.updater.VersionNumber;

public class ModManagerActivity extends Activity implements LoadingListener, ModUpdater {
    private static final int REQUEST_MOD = 1024 * 121;
    @SuppressLint("StaticFieldLeak")
    private static ElfUIBackbone loader;

    private RecyclerView modListView;
    private View addModButton;
    private View loadingBar;
    private Button btnLaunchLive;
    private Button btnLaunchBeta;
    private Button btnLaunchHuawei;
    private String skyPackageName;

    private SharedPreferences sharedPreferences;
    private ArrayList<String> skyPackages;
    private ModUpdaterDialogManager mDialogManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        runUpdater();
        setContentView(R.layout.mod_manager);

        modListView = findViewById(R.id.mm_modList);
        addModButton = findViewById(R.id.mm_addMod);
        loadingBar = findViewById(R.id.mm_loadBar);
        btnLaunchLive = findViewById(R.id.mm_launch_live);
        btnLaunchBeta = findViewById(R.id.mm_launch_beta);
        btnLaunchHuawei = findViewById(R.id.mm_launch_huawei);

        ((TextView) findViewById(R.id.mm_versionName)).setText(getString(R.string.mod_canvas_version, BuildConfig.VERSION_NAME));

        initializeSkyPackages();
        sharedPreferences = getSharedPreferences("package_configs", Context.MODE_PRIVATE);
        updateButtonTextColor();

        initializeModUpdater();
        initializeLoader();

        modListView.setLayoutManager(new LinearLayoutManager(this));
        modListView.setAdapter(new ModListAdapter(loader));

        setButtonClickListeners();
        setButtonLongClickListeners();
    }

    private void initializeModUpdater() {
        mDialogManager = new ModUpdaterDialogManager(this);
        // This will not do anything if the service isn't already started, and it's only
        // started when a mod is in the process of updating.
        bindService(new Intent(this, ModUpdaterService.class), mDialogManager, 0);
    }

    public void startModUpdater(ElfModUIMetadata metadata) {
        Log.i("MMA", "Starting mod update...");
        if(mDialogManager.isConnected()) {
            Toast.makeText(this, R.string.updater_busy, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent serviceStartIntent = new Intent(this, ModUpdaterService.class);
        serviceStartIntent.putExtra(ModUpdaterService.EXTRA_UPDATE_URL, metadata.getGithubReleasesUrl());
        serviceStartIntent.putExtra(ModUpdaterService.EXTRA_LIB_NAME, metadata.name);
        serviceStartIntent.putExtra(ModUpdaterService.EXTRA_VERSION_NUMBER,
                new VersionNumber(metadata.majorVersion, metadata.minorVersion, metadata.patchVersion)
        );
        startService(serviceStartIntent);
        bindService(new Intent(this, ModUpdaterService.class), mDialogManager, 0);
    }

    private void initializeSkyPackages() {
        skyPackages = new ArrayList<>();
        skyPackages.add("com.tgc.sky.android");
        skyPackages.add("com.tgc.sky.android.test.gold");
        skyPackages.add("com.tgc.sky.android.huawei");
        SMLApplication.skyPName = skyPackages.get(0);
    }

    private void updateButtonTextColor() {
        skyPackageName = sharedPreferences.getString("sky_package_name", null);
        if (skyPackageName == null) {
            skyPackageName = "com.tgc.sky.android";
            sharedPreferences.edit().putString("sky_package_name", skyPackageName).apply();
        }
        setButtonTextColor(btnLaunchLive, skyPackages.get(0));
        setButtonTextColor(btnLaunchBeta, skyPackages.get(1));
        setButtonTextColor(btnLaunchHuawei, skyPackages.get(2));
    }

    private void setButtonTextColor(Button button, String packageName) {
        if (skyPackageName != null && skyPackageName.equals(packageName)) {
            button.setTextColor(getColor(R.color.teal_700));
        } else {
            button.setTextColor(getColor(R.color.text));
        }
    }

    private void initializeLoader() {
        if (loader == null) {
            loader = new ElfUIBackbone(this, this);
            loader.addListener(this);
            loader.startLoadingAsync(new File(getFilesDir(), "mods"));
        } else {
            handleLoading();
            handleUnsafeModRemoval();
            handleException();
            loader.addListener(this);
        }
    }


    private void setButtonClickListeners() {
        btnLaunchLive.setOnClickListener(view -> {
            skyPackageName = skyPackages.get(0);
            launchGame();
        });

        btnLaunchBeta.setOnClickListener(view -> {
            skyPackageName = skyPackages.get(1);
            launchGame();
        });

        btnLaunchHuawei.setOnClickListener(view -> {
            skyPackageName = skyPackages.get(2);
            launchGame();
        });

    }

    private void setButtonLongClickListeners() {
        btnLaunchLive.setOnLongClickListener(view -> {
            setSkyPackageName(skyPackages.get(0));
            return true;
        });

        btnLaunchBeta.setOnLongClickListener(view -> {
            setSkyPackageName(skyPackages.get(1));
            return true;
        });

        btnLaunchHuawei.setOnLongClickListener(view -> {
            setSkyPackageName(skyPackages.get(2));
            return true;
        });
    }

    public void setSkyPackageName(String pkg) {
        if (findPackage(pkg)) {
            sharedPreferences.edit().putString("sky_package_name", pkg).apply();
            updateButtonTextColor();
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    getResources().getString(R.string.game_not_installed_warning),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    public void setSkipUpdates(boolean flag) {
        sharedPreferences.edit().putBoolean("skip_updates", flag).apply();
    }

    public void onAddMod(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_MOD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MOD && resultCode == Activity.RESULT_OK) {
            try {
                InputStream dataStream = getContentResolver().openInputStream(Objects.requireNonNull(data.getData()));
                loader.addModSafely(dataStream);
            } catch (FileNotFoundException e) {
                Toast.makeText(this, R.string.mod_ioe, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setLoadingStatus(boolean enable) {
        loadingBar.setVisibility(enable ? View.VISIBLE : View.GONE);
        addModButton.setEnabled(!enable);
        btnLaunchLive.setEnabled(!enable);
        btnLaunchBeta.setEnabled(!enable);
        btnLaunchHuawei.setEnabled(!enable);
        modListView.setClickable(!enable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loader != null) loader.removeListener();
        unbindService(mDialogManager);
    }

    @Override
    public void refreshModList(int mode, int which) {
        runOnUiThread(() -> {
            ModListAdapter adapter = (ModListAdapter) modListView.getAdapter();
            if (adapter != null) {
                switch (mode) {
                    case 0:
                        adapter.notifyItemRemoved(which);
                        break;
                    case 1:
                        adapter.notifyItemInserted(which);
                        break;
                    case 2:
                        adapter.notifyItemChanged(which);
                        break;
                }

            } else modListView.setAdapter(new ModListAdapter(loader));
        });
    }

    @Override
    public void onLoadingUpdated() {
        runOnUiThread(this::handleLoading);
    }

    @Override
    public void signalModRemovalUnsafe() {
        runOnUiThread(this::handleUnsafeModRemoval);
    }

    @Override
    public void signalModAddException() {
        runOnUiThread(this::handleException);
    }

    @Override
    public void signalModRemovalError() {
        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.mod_remove_unable);
            builder.setMessage(R.string.mod_ioe);
            builder.setPositiveButton(android.R.string.ok, (d, w) -> {
            });
            builder.show();
        });
    }

    private void handleException() {
        Exception e = loader.getException();
        if (e == null) return;
        String message;
        if (e instanceof NoDependenciesException) {
            NoDependenciesException exc = (NoDependenciesException) e;
            StringBuilder stringBuilder = new StringBuilder();
            for (ElfModMetadata meta : exc.failedDependencies) {
                stringBuilder.append(getString(R.string.mod_add_missingdep, meta.name, meta.majorVersion, meta.minorVersion));
                stringBuilder.append('\n');
            }
            message = stringBuilder.toString();
        } else if (e instanceof InvalidModException) {
            message = getString(R.string.mod_add_wrongformat);
        } else if (e instanceof IOException) {
            message = e.getMessage();
        } else if (e instanceof ModExistsException) {
            message = getString(R.string.mod_add_exists);
        } else {
            e.printStackTrace();
            message = e.getMessage();
        }

        DialogY dialogY = DialogY.createFromActivity(this);
        dialogY.positiveButton.setVisibility(View.GONE);
        dialogY.title.setText(R.string.mod_add_unable);
        dialogY.content.setText(message);
        dialogY.negativeButton.setOnClickListener((v)->dialogY.dialog.dismiss());
        dialogY.dialog.setCancelable(true);
        dialogY.dialog.show();
    }

    private void handleUnsafeModRemoval() {
        ElfUIBackbone.UnsafeRemovalMetadata metadata = loader.getUnsafeRemovalMetadata();
        if (metadata == null) return;
        StringBuilder sb = new StringBuilder();
        for (ElfModUIMetadata meta : metadata.dependingMods) {
            sb.append(getString(R.string.mod_remove_dep, ModListAdapter.getVisibleModName(meta)));
            sb.append('\n');
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.mod_remove_unable);
        builder.setMessage(sb.toString());
        builder.setPositiveButton(android.R.string.ok, (d, w) -> loader.resetModRemovalMetadata());
        builder.setOnCancelListener((d) -> loader.resetModRemovalMetadata());
        builder.show();
    }

    private void handleLoading() {
        if (loader.getProgressBarState()) {
            setLoadingStatus(true);
        } else {
            modListView.setAdapter(new ModListAdapter(loader));
            setLoadingStatus(false);
        }
    }

    public boolean findPackage(String packageName) {
        PackageManager packageManager = getPackageManager();
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_SHARED_LIBRARY_FILES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public void launchGame() {
        if (findPackage(skyPackageName)) {
            setSkyPackageName(skyPackageName);
            startActivity(new Intent(this, MainActivity.class));
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    getResources().getString(R.string.game_not_installed_warning),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    public void onExtraSettingsDialog(View view) {
        DialogY dialogY = DialogY.createFromActivity(this);
        dialogY.positiveButton.setVisibility(View.GONE);
        dialogY.content.setVisibility(View.GONE);
        dialogY.title.setText(R.string.settings_title);
        dialogY.negativeButton.setText(R.string.close);
        dialogY.negativeButton.setOnClickListener((v)->dialogY.dialog.dismiss());

        SwitchMaterial bypassUpdate = new SwitchMaterial(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        int marginPx = dpToPixels(10);
        layoutParams.setMargins(marginPx, 0, 0, marginPx);

        bypassUpdate.setTextSize(15);
        bypassUpdate.setLayoutParams(layoutParams);
        bypassUpdate.setText(R.string.switch_skip_updates);
        bypassUpdate.setChecked(sharedPreferences.getBoolean("skip_updates", false));

        bypassUpdate.setOnCheckedChangeListener((buttonView, isChecked) -> setSkipUpdates(isChecked));
        dialogY.container.addView(bypassUpdate, layoutParams);
        dialogY.dialog.show();
    }

    public void runUpdater() {
        Intent updaterService = new Intent(this, CanvasUpdaterService.class);
        bindService(updaterService, new CanvasUpdaterConnection(this), BIND_AUTO_CREATE);
    }

    private int dpToPixels(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
