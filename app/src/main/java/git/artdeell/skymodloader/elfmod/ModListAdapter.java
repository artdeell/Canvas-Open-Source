package git.artdeell.skymodloader.elfmod;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import git.artdeell.skymodloader.R;

public class ModListAdapter extends RecyclerView.Adapter<ModListAdapter.ViewHolder> {
    final ElfUIBackbone loader;
    public ModListAdapter(ElfUIBackbone loader) {
        this.loader = loader;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.mod_list_element,parent, false));
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
        TextView nameView;
        TextView versionView;
        View removeButton;
        View infoButton;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.mml_modName);
            versionView = itemView.findViewById(R.id.mml_version);
             defaultColors = versionView.getTextColors();
            removeButton = itemView.findViewById(R.id.mml_deleteButton);
            infoButton = itemView.findViewById(R.id.mml_infoButton);
            removeButton.setOnClickListener(this);
            infoButton.setOnClickListener(this);
        }
        void bindMod(int which) {
            this.which = which;
            ElfModUIMetadata metadata = loader.getMod(which);
            nameView.setText(getVisibleModName(itemView.getContext(),metadata));
            infoButton.setVisibility(metadata.description==null?View.GONE:View.VISIBLE);
            if(metadata.modIsValid) {
                versionView.setTextColor(defaultColors);
                versionView.setText(itemView.getContext().getString(R.string.mod_version,metadata.majorVersion, metadata.minorVersion, metadata.patchVersion));
            }else{
                versionView.setTextColor(Color.RED);
                versionView.setText(R.string.mod_invalid);
            }

        }

        @Override
        public void onClick(View v) {
            if(v.equals(removeButton)) loader.removeModSafelyAsync(which);
            else if(v.equals(infoButton)) {
                ElfModUIMetadata metadata = loader.getMod(which);
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle(getVisibleModName(v.getContext(),metadata));
                builder.setMessage(metadata.description);
                builder.setPositiveButton(android.R.string.ok,(d,w)->{});
                builder.show();
            }
        }
    }
    public static String getVisibleModName(Context c, ElfModUIMetadata metadata) {
        if(metadata.displayName != null) {
            return c.getString(R.string.mod_name,metadata.displayName, metadata.name);
        }else{
            return metadata.name;
        }
    }
}
