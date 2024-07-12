package git.artdeell.skymodloader.ui;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import git.artdeell.skymodloader.R;
import git.artdeell.skymodloader.databinding.ActivityMainBinding;

import java.io.IOException;

import static git.artdeell.skymodloader.ui.modmgr.ModManagerViewModel.getInputStreamFromURI;
import static git.artdeell.skymodloader.ui.modmgr.ModManagerViewModel.loader;

public class ModManagerActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    public ActivityResultLauncher<String> mGetContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showMainUI();
    }

    private void showMainUI(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        BottomNavigationView bottomNavigationView = binding.bottomNavigation;
        NavController navController = Navigation.findNavController(this, R.id.main_nav_host);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    try {
                        if(uri!=null) {
                            loader.addModSafely(getInputStreamFromURI(loader.activity, uri));
                        }
                    } catch (IOException e) {
                        runOnUiThread(() -> Toast.makeText(loader.activity, R.string.mod_ioe, Toast.LENGTH_SHORT).show());
                    }
                });
    }


}