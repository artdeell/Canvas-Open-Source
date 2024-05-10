package git.artdeell.skymodloader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Button;

import java.util.Locale;

public class DialogY implements View.OnLayoutChangeListener, DialogInterface.OnShowListener {
    public final AlertDialog dialog;
    public final TextView title;
    public final TextView content;
    public final Button positiveButton;
    public final Button negativeButton;
    public final LinearLayout container;
    private final View mProgressBox;
    private final ProgressBar mProgressBar;
    private final TextView mProgressText;

    private double mMaxProgress, mCurrentProgress;
    private double mProgressBarMax = 0;
    private boolean mIndeterminate;

    public static DialogY createFromActivity(Activity activity) {
        ViewGroup root = null;
        View contentView = activity.findViewById(android.R.id.content);
        if(contentView == null){
            contentView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        }
        if(contentView instanceof ViewGroup) root = (ViewGroup) contentView;
        return new DialogY(activity, root);
    }

    public DialogY(Context context, ViewGroup root) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.dialog_x, root, false);
        title = rootView.findViewById(R.id.dialog_title);
        content = rootView.findViewById(R.id.dialog_description);
        positiveButton = rootView.findViewById(R.id.dialog_button_positive);
        negativeButton = rootView.findViewById(R.id.dialog_button_negative);
        container = rootView.findViewById(R.id.dialog_empty_space);
        mProgressBox = rootView.findViewById(R.id.dialog_progress_box);
        mProgressBar = rootView.findViewById(R.id.dialog_progress);
        mProgressText = rootView.findViewById(R.id.dialog_progress_text);
        mProgressBar.addOnLayoutChangeListener(this);
        dialog = new AlertDialog.Builder(context)
                .setView(rootView)
                .create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setOnShowListener(this);
    }

    public void setProgressMax(long max) {
        double dmax = (double) max;
        if(dmax == mMaxProgress) return;
        mMaxProgress = dmax;
        updateProgressBar();

    }
    public void setProgress(long progress) {
        double dcur = (double) progress;
        if(dcur == mCurrentProgress) return;
        mCurrentProgress = dcur;
        updateProgressBar();
    }

    private void updateProgressBar() {
        if(mIndeterminate) return;
        double progress = mCurrentProgress / mMaxProgress;
        mProgressBar.setProgress((int)(progress * mProgressBarMax));
        mProgressText.setText(
            String.format(Locale.getDefault(),"%.2f%%", progress * 100d)
        );
    }

    public void setProgressIndeterminate(boolean indeterminate) {
        if(mIndeterminate == indeterminate) return;
        mIndeterminate = indeterminate;
        mProgressBar.setIndeterminate(mIndeterminate);
        if(mIndeterminate) {
            mProgressText.setText("--%");
        }else {
            updateProgressBar();
        }
    }

    public void setProgressVisibility(boolean visible) {
        mProgressBox.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
        if(!view.equals(mProgressBar)) return;
        int smoothMax = mProgressBar.getWidth() * 4;
        mProgressBar.setMax(smoothMax);
        mProgressBarMax = smoothMax;
    }

    private void setDialogBackground() {
        Window dialogWindow = dialog.getWindow();
        if(dialogWindow == null) {
            container.post(this::setDialogBackground);
            return;
        }
        dialogWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public void onShow(DialogInterface dialogInterface) {
        setDialogBackground();
    }
}
