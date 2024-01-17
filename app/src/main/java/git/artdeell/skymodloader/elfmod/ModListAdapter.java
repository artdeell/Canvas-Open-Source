package git.artdeell.skymodloader.elfmod;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import git.artdeell.skymodloader.R;
import git.artdeell.skymodloader.SMLApplication;
import git.artdeell.skymodloader.databinding.ModListElementBinding;

public class ModListAdapter extends RecyclerView.Adapter<ModListAdapter.ViewHolder> {
    final ElfUIBackbone loader;
    public ModListAdapter(ElfUIBackbone loader) {
        this.loader = loader;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ModListElementBinding binding = DataBindingUtil.inflate(LayoutInflater.from(loader.activity), R.layout.mod_list_element, null, false);
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
            binding.setItem(metadata);
        }

        @Override
        public void onClick(View v) {}
    }
    public static String getVisibleModName(Context c, ElfModUIMetadata metadata) {
        if(metadata.displayName != null) {
            return c.getString(R.string.mod_name,metadata.displayName, metadata.name);
        }else{
            return metadata.name;
        }
    }
}
