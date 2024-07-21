package git.artdeell.skymodloader.ui.modmgr;

import android.app.Activity;
import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import git.artdeell.skymodloader.R;
import git.artdeell.skymodloader.databinding.FragmentModManagerBinding;
import git.artdeell.skymodloader.databinding.ModManagerBinding;
import git.artdeell.skymodloader.ui.ModManagerActivity;

public class ModManagerFragment extends Fragment {

    private ModManagerViewModel mViewModel;
    FragmentModManagerBinding binding;

    public static ModManagerFragment newInstance() {
        return new ModManagerFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(ModManagerViewModel.class);
        mViewModel.init((ModManagerActivity) container.getContext());
        binding = FragmentModManagerBinding.inflate(inflater, container, false);
        binding.setLifecycleOwner(this);
        binding.setViewModel(mViewModel);
        return binding.getRoot();

    }

}