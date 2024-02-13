package git.artdeell.skymodloader.elfmod;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import git.artdeell.skymodloader.R;


public class DialogX {

    Context context;
    String title;
    String description;
    String positiveButtonText;

    DialogCallback onPositiveClick;

    String negativeButtonText;
    DialogCallback onNegativeClick;

    public AlertDialog alertDialog;
    private View view;

    public interface DialogCallback {
        void onClick();
    }

    public interface DialogEditCallback {
        void onDialogEdit(View view);
    }


    public DialogX(
            Context context,
            AlertDialog alertDialog,
            String title,
            String description,
            String positiveButtonText,
            DialogCallback onPositiveClick
    ) {
        this.context = context;
        this.alertDialog = alertDialog;
        this.title = title;
        this.description = description;
        this.positiveButtonText = positiveButtonText;
        this.onPositiveClick = onPositiveClick;

        // get view
        View parentView = ((Activity) context).findViewById(android.R.id.content);
        ConstraintLayout dialog_constraintlayout = parentView.findViewById(R.id.dialog_constraintlayout);
        this.view = LayoutInflater.from(context).inflate(R.layout.dialog_x, dialog_constraintlayout);

    }

    public DialogX(
            Context context,
            AlertDialog alertDialog,
            String title,
            String description,
            String positiveButtonText,
            DialogCallback onPositiveClick,
            String negativeButtonText,
            DialogCallback onNegativeClick
    ) {
        this(context, alertDialog, title, description, positiveButtonText, onPositiveClick);
        this.negativeButtonText = negativeButtonText;
        this.onNegativeClick = onNegativeClick;
    }

    public void setButtons(View view) {

    }

    public void buildDialog() {
        if (this.alertDialog == null) {
            view.findViewById(R.id.dialog_progress_box).setVisibility(View.GONE);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(this.view);
            this.alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            if (alertDialog.getWindow() != null)
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

        }

        TextView dialog_title = view.findViewById(R.id.dialog_title);
        TextView dialog_description = view.findViewById(R.id.dialog_description);

        Button btnNegative = view.findViewById(R.id.dialog_button_negative);
        Button btnPositive = view.findViewById(R.id.dialog_button_positive);


        dialog_title.setVisibility(this.title != null ? View.VISIBLE : View.GONE);
        dialog_title.setText(this.title);

        dialog_description.setVisibility(this.description != null ? View.VISIBLE : View.GONE);
        dialog_description.setText(this.description);

        btnPositive.setVisibility((this.positiveButtonText != null && !this.positiveButtonText.isEmpty()) ? View.VISIBLE : View.GONE);
        btnPositive.setText(this.positiveButtonText);

        if (this.negativeButtonText != null && !this.negativeButtonText.isEmpty())
            btnNegative.setText(this.negativeButtonText);

        btnNegative.setOnClickListener(view1 -> {
            if (context instanceof Activity && !((Activity) context).isFinishing()) {
                if (onNegativeClick != null) {
                    onNegativeClick.onClick();
                }
                alertDialog.dismiss();
            }
        });

        btnPositive.setOnClickListener(v -> {
            if (context instanceof Activity && !((Activity) context).isFinishing()) {
                if (onPositiveClick != null) {
                    onPositiveClick.onClick();
                }
                alertDialog.dismiss();
            }
        });
    }

    public void buildDialogEx(DialogEditCallback dialogEditCallback) {
        this.buildDialog();

        if (dialogEditCallback != null) {
            dialogEditCallback.onDialogEdit(this.view);
        }
    }

    public void show() {
        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            alertDialog.show();
        }
    }

    public void edit(DialogEditCallback dialogEditCallback) {
        if (dialogEditCallback != null) {
            dialogEditCallback.onDialogEdit(this.view);
        }
    }

    public void close() {
        if (this.alertDialog != null)
            alertDialog.dismiss();
    }

    public void setCancelable(boolean flag) {
        alertDialog.setCancelable(flag);
    }

}
