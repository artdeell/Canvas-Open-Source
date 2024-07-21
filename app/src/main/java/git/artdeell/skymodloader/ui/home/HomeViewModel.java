package git.artdeell.skymodloader.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;
import git.artdeell.skymodloader.BR;
import git.artdeell.skymodloader.MainActivity;
import git.artdeell.skymodloader.R;
import git.artdeell.skymodloader.SMLApplication;
import git.artdeell.skymodloader.ui.BaseViewModel;
import me.tatarka.bindingcollectionadapter2.ItemBinding;

import java.util.ArrayList;

public class HomeViewModel extends BaseViewModel {
    private ArrayList<String> skyPackages;
    private String skyPackageName;
    public ObservableList<BaseDevItem> items;
    public final ItemBinding<BaseDevItem> itemBinding = ItemBinding.of(BR.item, R.layout.item_dev);

    public HomeViewModel(){
        initializeSkyPackages();
        initializeDev();
        itemBinding.bindExtra(BR.viewModel,this);
    }

    private void initializeSkyPackages() {
        skyPackages = new ArrayList<>();
        skyPackages.add("com.tgc.sky.android");
        skyPackages.add("com.tgc.sky.android.test.gold");
        skyPackages.add("com.tgc.sky.android.huawei");
        SMLApplication.skyPName = skyPackages.get(0);
    }

    private void initializeDev(){
        items = new ObservableArrayList<BaseDevItem>();
        items.add(new BaseDevItem("artdeell","https://github.com/artdeell",null));
        items.add(new BaseDevItem("RomanChamelo","https://github.com/RomanChamelo","https://t.me/RomanChameloo"));
        items.add(new BaseDevItem("Kiojeen","https://github.com/Kiojeen",null));
        items.add(new BaseDevItem("gxost","https://github.com/gxosty",null));
        items.add(new BaseDevItem("achqing","https://github.com/RaioxySu","https://t.me/achqing0769"));
    }

    public void normalBoot(View v){
        v.getContext().startActivity(new Intent(v.getContext(), MainActivity.class));
    }

    public int getButtonColor(Context context,int pack) {
        SharedPreferences prefs = context.getSharedPreferences("package_configs", 0);
        int color;
        if(skyPackages.get(pack).equals(prefs.getString("sky_package_name","com.tgc.sky.android"))) {
            color = context.getColor(R.color.teal_700);
        } else {
            color = context.getColor(R.color.text);
        }
        return color;
    }

    public void onButtonClick(View view,int pack) {
        skyPackageName = skyPackages.get(pack);
        launchGame(view.getContext());
    }

    public void launchGame(Context context) {
        if (findPackage(context,skyPackageName)) {
            setSkyPackageName(context,skyPackageName);
            context.startActivity(new Intent(context, MainActivity.class));
        } else {
            Toast.makeText(
                    context.getApplicationContext(),
                    context.getResources().getString(R.string.game_not_installed_warning),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    public void setSkyPackageName(Context context,String pkg) {
        if (findPackage(context,pkg)) {
            context.getSharedPreferences("package_configs", 0).edit().putString("sky_package_name", pkg).apply();
        } else {
            Toast.makeText(
                    context.getApplicationContext(),
                    context.getResources().getString(R.string.game_not_installed_warning),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    public boolean findPackage(Context context,String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_SHARED_LIBRARY_FILES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


    public boolean onDefaultChange(View view,int pack) {
        setSkyPackageName(view.getContext(),skyPackages.get(pack));
        notifyPropertyChanged(BR._all);
        return true;
    }

    public void onLinkPressed(View view,String link){
        Context context = view.getContext();
        Uri webpage = Uri.parse(link);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

}