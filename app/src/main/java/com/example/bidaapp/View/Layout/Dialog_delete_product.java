package com.example.bidaapp.View.Layout;



import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;

import com.example.bidaapp.R;

public class Dialog_delete_product {
    private Dialog dialog;

    public Dialog_delete_product(@NonNull Context context, DeleteConfirmationListener listener) {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_delete_product);

        TextView textViewMessage = dialog.findViewById(R.id.textViewMessage);
        Button buttonCancel = dialog.findViewById(R.id.buttonCancel);
        Button buttonConfirm = dialog.findViewById(R.id.buttonConfirm);

        // Đặt thông điệp nếu cần
        textViewMessage.setText("Bạn có chắc chắn muốn xóa sản phẩm này?");

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        buttonConfirm.setOnClickListener(v -> {
            listener.onDeleteConfirmed();
            dialog.dismiss();
        });
    }

    public void show() {
        dialog.show();
    }

    public interface DeleteConfirmationListener {
        void onDeleteConfirmed();
    }
}
