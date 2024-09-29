package com.example.bidaapp.View.Layout;



import android.widget.Button;
import android.widget.Button;





import android.view.View;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.bidaapp.Model.DatabaseHelper;
import com.example.bidaapp.R;
import com.example.bidaapp.View.Activity.ManageCustomersActivity;
public class AddCustomerDialogFragment extends DialogFragment {

    private int accountId;

    public AddCustomerDialogFragment(int accountId) {
        this.accountId = accountId;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_customer, null);

        EditText etName = view.findViewById(R.id.etName);
        EditText etPhone = view.findViewById(R.id.etPhone);
        EditText etAddress = view.findViewById(R.id.etAddress);

        Button btnAddCustomer = view.findViewById(R.id.btnAddCustomer);
        Button btnBack = view.findViewById(R.id.btnBack);

        // Thiết lập hành động cho nút thêm
        btnAddCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String address = etAddress.getText().toString().trim();

                // Kiểm tra tên khách hàng (ít nhất 1 ký tự chữ)
                if (name.isEmpty() || !name.matches(".*[a-zA-Z].*")) {
                    etName.setError("Tên khách hàng phải chứa ít nhất 1 ký tự chữ!");
                    return;
                }

                // Kiểm tra số điện thoại (phải bắt đầu bằng 0 và có từ 9 đến 12 ký tự số)
                if (!phone.matches("0\\d{8,11}")) {
                    etPhone.setError("Số điện thoại phải bắt đầu bằng 0 và từ 9 đến 12 số!");
                    return;
                }

                // Kiểm tra địa chỉ (ít nhất 1 ký tự)
                if (address.isEmpty()) {
                    etAddress.setError("Địa chỉ ít nhất phải có 1 ký tự!");
                    return;
                }

                DatabaseHelper db = new DatabaseHelper(getActivity());
                db.addCustomer(accountId, name, phone, address);

                ((ManageCustomersActivity) getActivity()).loadCustomers();
                dismiss(); // Đóng dialog sau khi thêm
            }
        });

        // Thiết lập hành động cho nút hủy
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss(); // Đóng dialog
            }
        });

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
    }
}
