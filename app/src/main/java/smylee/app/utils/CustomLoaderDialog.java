package smylee.app.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.view.WindowManager;

import java.util.Objects;

import smylee.app.R;


public class CustomLoaderDialog {

    private Dialog dialog;

    public CustomLoaderDialog(Context context) {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(dialog.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    /**
     * This method use for show progress dialog
     *
     * @param isCancelable set true if you set cancel progressDialog by user event
     */
    public void show(Boolean isCancelable) {
        dialog.setCancelable(isCancelable);
        dialog.setContentView(R.layout.progress_layout);
        try {
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Boolean isShowing() {
        return dialog.isShowing();
    }

    public void hide() {
        if (dialog != null) {
            try {
                dialog.cancel();
                dialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
