package git.artdeell.skymodloader;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * This class is intended for sending characters used in chat via the virtual keyboard
 */
public class ImGUITextInput extends androidx.appcompat.widget.AppCompatEditText {
    public ImGUITextInput(@NonNull Context context) {
        this(context, null);
    }
    public ImGUITextInput(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }
    public ImGUITextInput(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        imm = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);
        setup();
    }
    private final InputMethodManager imm;
    private boolean mIsDoingInternalChanges = false;


    /**
     * When we change from app to app, the keyboard gets disabled.
     * So, we disable the object
     */
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        disable();
    }

    /**
     * Intercepts the back key to disable focus
     * Does not affect the rest of the activity.
     */
    @Override
    public boolean onKeyPreIme(final int keyCode, final KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            disable();
        }
        return super.onKeyPreIme(keyCode, event);
    }

    /**
     * Set the soft keyboard state
     * @param state the state (true=visible)
     */
    public void setKeyboardState(boolean state) {
        if(state) {
            enable();
            requestFocus();
            imm.showSoftInput(this, InputMethodManager.SHOW_FORCED);
        }else  {
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
            clear();
            disable();
        }
    }


    /**
     * Clear the EditText from any leftover inputs
     * It does not affect the in-game input
     */
    @SuppressLint("SetTextI18n")
    public void clear(){
        mIsDoingInternalChanges = true;
        //Braille space, doesn't trigger keyboard auto-complete
        //replacing directly the text without though setText avoids notifying changes
        setText("                              ");
        setSelection(getText().length());
        mIsDoingInternalChanges = false;
    }

    /** Regain ability to exist, take focus and have some text being input */
    public void enable(){
        setEnabled(true);
        setFocusable(true);
        setVisibility(VISIBLE);
        requestFocus();

    }

    /** Lose ability to exist, take focus and have some text being input */
    public void disable(){
        clear();
        setVisibility(GONE);
        clearFocus();
        setEnabled(false);
    }

    /** Send the enter key. */
    private void sendEnter(){
        ImGUI.submitKeyEvent(KeyEvent.KEYCODE_ENTER, true);
        ImGUI.submitKeyEvent(KeyEvent.KEYCODE_ENTER, false);
        clear();
    }

    /** This function deals with anything that has to be executed when the constructor is called */
    private void setup(){
        setOnEditorActionListener((textView, i, keyEvent) -> {
            sendEnter();
            clear();
            disable();
            return false;
        });
        clear();
        disable();
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            /**
             * We take the new chars, and send them to the game.
             * If less chars are present, remove some.
             * The text is always cleaned up.
             */
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(mIsDoingInternalChanges)return;
                if (count != 0)
                    ImGUI.submitUnicodeEvent(s.charAt(start + before));
                else {
                    ImGUI.submitKeyEvent(KeyEvent.KEYCODE_DEL, true);
                    ImGUI.submitKeyEvent(KeyEvent.KEYCODE_DEL, false);
                }
                //Reset the keyboard state
                clear();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
