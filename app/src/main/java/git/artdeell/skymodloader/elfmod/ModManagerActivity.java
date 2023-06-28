package git.artdeell.skymodloader.elfmod;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import git.artdeell.skymodloader.BuildConfig;
import git.artdeell.skymodloader.R;

public class ModManagerActivity extends Activity implements LoadingListener {
    private static final int REQUEST_MOD = 1024*121;
    private static ElfUIBackbone loader;
    RecyclerView modListView;
    View addModButton;
    View loadingBar;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch enableBetaSwitch;
    SharedPreferences beta_enabler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_manager);
        modListView = findViewById(R.id.mm_modList);
        addModButton = findViewById(R.id.mm_addMod);
        loadingBar = findViewById(R.id.mm_loadBar);
        beta_enabler = getSharedPreferences("beta_enabler", Context.MODE_PRIVATE);
        enableBetaSwitch = findViewById(R.id.mm_enableSkyBeta);
        enableBetaSwitch.setOnCheckedChangeListener(this::onBetaChecked);
        enableBetaSwitch.setChecked(beta_enabler.getBoolean("enable_beta", false));
        ((TextView)findViewById(R.id.mm_versionName)).setText(getString(R.string.mod_canvas_version, BuildConfig.VERSION_NAME));
        if(loader == null) {
            loader = new ElfUIBackbone();
            loader.addListener(this);
            loader.startLoadingAsync(new File(getFilesDir(),"mods"));
        }else{
            handleLoading();
            handleUnsafeModRemoval();
            handleException();
            loader.addListener(this);
        }
        modListView.setLayoutManager(new LinearLayoutManager(this));
        modListView.setAdapter(new ModListAdapter(loader));
    }

    public void onAddMod(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_MOD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_MOD && resultCode == Activity.RESULT_OK) {
            try {
                InputStream dataStream = getContentResolver().openInputStream(data.getData());
                loader.addModSafely(dataStream);
            }catch (FileNotFoundException e) {
                Toast.makeText(this,R.string.mod_ioe, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setLoadingStatus(boolean enable) {
        loadingBar.setVisibility(enable?View.VISIBLE:View.GONE);
        addModButton.setEnabled(!enable);
        modListView.setClickable(!enable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(loader != null) loader.removeListener();
    }

    @Override
    public void refreshModList(boolean mode, int which) {
        runOnUiThread(()->{
            ModListAdapter adapter = (ModListAdapter) modListView.getAdapter();
            if(adapter != null) {
                if(mode) adapter.notifyItemInserted(which);
                else adapter.notifyItemRemoved(which);
            }
            else modListView.setAdapter(new ModListAdapter(loader));
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
        runOnUiThread(()-> {
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
        if(e == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.mod_add_unable);
        if (e instanceof NoDependenciesException) {
            NoDependenciesException exc = (NoDependenciesException) e;
            StringBuilder stringBuilder = new StringBuilder();
            for (ElfModMetadata meta : exc.failedDependencies) {
                stringBuilder.append(getString(R.string.mod_add_missingdep, meta.name, meta.majorVersion, meta.minorVersion));
                stringBuilder.append('\n');
            }
            builder.setMessage(stringBuilder.toString());
        } else if (e instanceof InvalidModException) {
            builder.setMessage(R.string.mod_add_wrongformat);
        } else if (e instanceof IOException) {
            builder.setMessage(e.getMessage());
        } else if(e instanceof ModExistsException) {
            builder.setMessage(getString(R.string.mod_add_exists));
        }else{
            e.printStackTrace();
            builder.setMessage(R.string.mod_gf);
        }
        builder.setPositiveButton(android.R.string.ok, (d, w) -> loader.resetException());
        builder.setOnCancelListener((d)->loader.resetException());
        builder.show();
    }

    private void handleUnsafeModRemoval() {
        ElfUIBackbone.UnsafeRemovalMetadata metadata = loader.getUnsafeRemovalMetadata();
        if(metadata == null) return;
        StringBuilder sb = new StringBuilder();
        for(ElfModUIMetadata meta : metadata.dependingMods) {
            sb.append(getString(R.string.mod_remove_dep,ModListAdapter.getVisibleModName(this, meta)));
            sb.append('\n');
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.mod_remove_unable);
        builder.setMessage(sb.toString());
        builder.setPositiveButton(android.R.string.ok,(d,w)->loader.reserModRemovalMetadata());
        builder.setOnCancelListener((d)->loader.reserModRemovalMetadata());
        builder.show();
    }
    private void handleLoading() {
        if(loader.getProgressBarState()) {
            setLoadingStatus(true);
        }else{
            modListView.setAdapter(new ModListAdapter(loader));
            setLoadingStatus(false);
        }
    }

    public void onBetaChecked(CompoundButton buttonView, boolean isChecked) {
        beta_enabler.edit()
                .putBoolean("enable_beta", isChecked)
                .apply();
    }
}
