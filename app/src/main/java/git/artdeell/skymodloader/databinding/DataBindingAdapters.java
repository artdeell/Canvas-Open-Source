package git.artdeell.skymodloader.databinding;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.databinding.BindingAdapter;

import java.util.List;

import git.artdeell.skymodloader.R;

public class DataBindingAdapters {

    @BindingAdapter("gone")
    public static void setGone(View view, Boolean gone) {
        if(gone){
            view.setVisibility(View.GONE);
        }else{
            view.setVisibility(View.VISIBLE);
        }
    }

    @BindingAdapter("srcCompat")
    public static void setImageResource(ImageView view, Drawable drawable) {
        view.setImageDrawable(drawable);
    }

    @BindingAdapter("front")
    public static void setFront(ImageView view, Boolean bool) {
        view.bringToFront();
    }

    @BindingAdapter("srcCompat")
    public static void setImageResource(ImageView view, int id) {
        view.setImageResource(id);
    }

    @BindingAdapter("setText")
    public static void setText(TextView view, int id) {
        if(id!=0)
        view.setText(id);
    }

    @BindingAdapter("source")
    public static void setBitmap(ImageView view, Bitmap bm) {
        if (bm != null && bm.getWidth() != 0 && bm.getHeight() != 0) {
            view.setImageBitmap(bm);
        }else{
            view.setImageResource(R.drawable.icon_black_round);
        }
    }

    @BindingAdapter("setDescription")
    public static void setDescription(TextView view, int id) {
        if(view.getContext().getString(id).equals("")){
            view.setVisibility(View.GONE);
        }else{
            view.setText(id);
        }
    }

    @BindingAdapter("isEnabled")
    public void setEnabled(View view, boolean isEnabled) {
        view.setEnabled(isEnabled);
    }
}
