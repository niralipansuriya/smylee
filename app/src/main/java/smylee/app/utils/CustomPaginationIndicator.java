package smylee.app.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import java.util.Objects;

import smylee.app.R;

public class CustomPaginationIndicator {
    private Dialog dialog;

    public CustomPaginationIndicator(Context context) {
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
            Objects.requireNonNull(dialog.getWindow()).setGravity(Gravity.BOTTOM);
            WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
            // layoutParams.x = 100; // left margin
            layoutParams.y = 150; // bottom margin
            dialog.getWindow().setAttributes(layoutParams);

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
            dialog.cancel();
            dialog.dismiss();
        }
    }
}