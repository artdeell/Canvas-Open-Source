package git.artdeell.skymodloader.elfmod;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import git.artdeell.skymodloader.R;
import git.artdeell.skymodloader.databinding.ModListElementBinding;
import git.artdeell.skymodloader.modupdater.ModUpdater;

public class ModListAdapter extends RecyclerView.Adapter<ModListAdapter.ViewHolder> {
    final ElfUIBackbone loader;

    public ModListAdapter(ElfUIBackbone loader) {
        this.loader = loader;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ModListElementBinding binding = DataBindingUtil.inflate(LayoutInflater.from(loader.activity), R.layout.mod_list_element, parent, false);
        ViewHolder vh = new ViewHolder(binding.getRoot());
        vh.binding = binding;
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindMod(position);
    }

    @Override
    public int getItemCount() {
        return loader.getModsCount();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ColorStateList defaultColors;
        int which;
        View myView;
        public ModListElementBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            myView = itemView;
        }

        void bindMod(int which) {
            this.which = which;
            ElfModUIMetadata metadata = loader.getMod(which);
            metadata.loader = loader;
            metadata.which = which;

            LinearLayout checkForUpdatesLayout = myView.findViewById(R.id.check_for_updates);

            String githubReleasesRegex = "https://api\\.github\\.com/repos/.+/releases/latest";

            if (metadata.githubReleasesUrl != null && metadata.githubReleasesUrl.matches(githubReleasesRegex)) {
                checkForUpdatesLayout.setVisibility(View.VISIBLE);
            } else {
                checkForUpdatesLayout.setVisibility(View.GONE);
            }

            myView.findViewById(R.id.check_for_updates).setOnClickListener(v -> {
                if (ModUpdater.isDownloading()) {
                    Toast.makeText(myView.getContext(), R.string.updater_busy, Toast.LENGTH_SHORT).show();
                    return;
                }
                
                ModManagerActivity.dialogX = new DialogX(
                        myView.getContext(),
                        ModManagerActivity.alertDialog,
                        loader.activity.getString(R.string.check_updates_title, metadata.displayName),
                        loader.activity.getString(R.string.check_updates_message),
                        loader.activity.getString(R.string.check_button_text),
                        () -> {
                            Executor executor = Executors.newSingleThreadExecutor();
                            executor.execute(() -> {
                                try {
                                    loader.modUpdater.checkForModUpdate(metadata);
                                } catch (IOException | JSONException e) {
                                    loader.activity.runOnUiThread(() -> {
                                        ModManagerActivity.dialogX = new DialogX(
                                                myView.getContext(),
                                                ModManagerActivity.alertDialog,
                                                loader.activity.getString(R.string.check_updates_failed_title),
                                                e.getMessage(),
                                                null,
                                                null
                                        );
                                        ModManagerActivity.dialogX.buildDialogEx(view -> view.findViewById(R.id.dialog_button_positive).setVisibility(View.GONE));
                                        ModManagerActivity.dialogX.setCancelable(false);
                                        ModManagerActivity.dialogX.show();
                                    });
                                }
                            });
                        }
                );
                ModManagerActivity.dialogX.buildDialog();
                ModManagerActivity.dialogX.setCancelable(false);
                ModManagerActivity.dialogX.show();
            });
            binding.setItem(metadata);
        }


        @Override
        public void onClick(View v) {
        }
    }

    public static String getVisibleModName(Context c, ElfModUIMetadata metadata) {

        if (metadata.displayName != null) {
            return c.getString(R.string.mod_name, metadata.displayName, metadata.name);
        } else {
            return metadata.name;
        }
    }
}
