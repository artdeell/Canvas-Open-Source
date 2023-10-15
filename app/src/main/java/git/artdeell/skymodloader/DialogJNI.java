package git.artdeell.skymodloader;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.tgc.sky.GameActivity;
import com.tgc.sky.SystemUI_android;
import com.tgc.sky.ui.Utils;

import java.util.concurrent.atomic.AtomicBoolean;

public class DialogJNI {
    public static final int TEXTBOX_TYPE_TEXT = 0;
    public static final int TEXTBOX_TYPE_NUMBER = 1;
    private static GameActivity activity;
    private static Handler uiThreadHandler = new Handler(Looper.getMainLooper());
    private ConstraintLayout layout;
    private ConstraintSet constraintSet;
    private int hookViewHorizontal;
    private int hookViewVertical;
    private boolean direction = true;
    private String title;
    private boolean callingStdfn = false;
    private void addView(View view) {
        int viewId = View.generateViewId();
        view.setId(viewId);
        layout.addView(view);
        if(hookViewHorizontal == 0 && hookViewVertical == 0) {
            constraintSet.clone(layout);
            constraintSet.connect(ConstraintSet.PARENT_ID, ConstraintSet.START, view.getId(), ConstraintSet.START);
            constraintSet.connect(ConstraintSet.PARENT_ID, ConstraintSet.TOP, view.getId(), ConstraintSet.TOP);
            constraintSet.applyTo(layout);
            hookViewVertical = hookViewHorizontal = viewId;
            return;
        }
        constraintSet.clone(layout);
        if(direction) { //TRUE - top to bottom
            constraintSet.connect(viewId, ConstraintSet.TOP, hookViewVertical, ConstraintSet.BOTTOM);
            constraintSet.connect(viewId, ConstraintSet.START, hookViewVertical, ConstraintSet.START);
            hookViewHorizontal = hookViewVertical = viewId;
        }else{
            constraintSet.connect(viewId, ConstraintSet.START, hookViewHorizontal, ConstraintSet.END);
            constraintSet.connect(viewId, ConstraintSet.BOTTOM, hookViewHorizontal, ConstraintSet.BOTTOM);
            constraintSet.connect(viewId, ConstraintSet.TOP, hookViewHorizontal, ConstraintSet.TOP);
            hookViewHorizontal = viewId;
        }
        constraintSet.applyTo(layout);
    }
    DialogJNI(String title) {
        this.title = title;
        AtomicBoolean hasCreationCompleted = new AtomicBoolean(false);
        uiThreadHandler.post(()->{
            layout = new ConstraintLayout(activity);
            constraintSet = new ConstraintSet();
            hasCreationCompleted.set(true);
        });
        while(!hasCreationCompleted.get()) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public static void setActivity(GameActivity activity) {
        DialogJNI.activity = activity;
    }
    public static Object BeginDialog(String dialogTitle) {
        return new DialogJNI(dialogTitle);
    }

    public View PutButton(String text, long cbid) {
        Button button = new Button(activity);
        button.setText(text);
        if(cbid != 0) button.setOnClickListener(v->fireCallback(cbid, callingStdfn));
        uiThreadHandler.post(()->addView(button));
        return button;
    }

    public TextView PutText(String text) {
        TextView button = new TextView(activity);
        button.setText(text);
        uiThreadHandler.post(()->addView(button));
        return button;
    }

    public Switch PutSwitch(String text, boolean state, long cbid) {
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch _switch = new Switch(activity);
        _switch.setText(text);
        if(cbid != 0) _switch.setOnCheckedChangeListener((v, c)->fireCallbackBoolean(cbid, c, callingStdfn));
        uiThreadHandler.post(()-> {
            _switch.setChecked(state);
            addView(_switch);
        });
        return _switch;
    }

    public TextView PutTextBox(String text, String hint, int type) {
        EditText editText = new EditText(activity);
        editText.setInputType(type);
        editText.setText(text);
        editText.setHint(hint);
        uiThreadHandler.post(()->addView(editText));
        return editText;
    }

    public static String GetTextBoxText(TextView textBox) {
        return textBox.getText().toString();
    }

    public static boolean GetSwitchValue(@SuppressLint("UseSwitchCompatOrMaterialCode") Switch textBox) {
        return textBox.isChecked();
    }

    public static void ChangeVisibility(View element, int visibility) {
        uiThreadHandler.post(()->(element).setVisibility(visibility));
    }

    public static void ChangeTextualText(TextView textual, String text) {
        uiThreadHandler.post(()->(textual).setText(text));
    }

    public void ShowDialog(String positiveText, long onPositive, String negativeText, long onNegative, String neutralText, long onNeutral) {
        uiThreadHandler.post(()->{
            AlertDialog.Builder bldr = new AlertDialog.Builder(activity);
            if(title != null && !title.isEmpty()) bldr.setTitle(title);
            bldr.setView(layout);
            if(positiveText != null && !positiveText.isEmpty() && onPositive != 0) {
                bldr.setPositiveButton(positiveText, (d,w)->fireCallback(onPositive, callingStdfn));
            }
            if(negativeText != null && !negativeText.isEmpty() && onNegative != 0) {
                bldr.setNegativeButton(negativeText, (d,w)->fireCallback(onNegative, callingStdfn));
            }
            if(neutralText != null && !neutralText.isEmpty() && onNeutral != 0) {
                bldr.setNeutralButton(neutralText, (d,w)->fireCallback(onNeutral, callingStdfn));
            }
            bldr.show();
        });
    }

    public static native void fireCallback(long cbid, boolean stdfn);
    public static native void fireCallbackBoolean(long cbid, boolean state, boolean stdfn);
}
