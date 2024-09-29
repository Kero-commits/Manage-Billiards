package com.example.bidaapp.View.Layout;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.bidaapp.Model.DatabaseHelper;
import com.example.bidaapp.R;

public class AddTableDialogFragment extends DialogFragment {

    private EditText editTextTableName;
    private Button btnAddTable;
    private DatabaseHelper databaseHelper;
    private int accountId;
    private OnTableAddedListener mListener;

    // Constructor nhận accountId
    public AddTableDialogFragment(int accountId) {
        this.accountId = accountId;
    }

    public interface OnTableAddedListener {
        void onTableAdded();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (OnTableAddedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnTableAddedListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_table, container, false);

        editTextTableName = view.findViewById(R.id.editTextTableName);
        btnAddTable = view.findViewById(R.id.btnAddTable);
        databaseHelper = new DatabaseHelper(getActivity());

        btnAddTable.setOnClickListener(v -> {
            String tableName = editTextTableName.getText().toString().trim();

            if (TextUtils.isEmpty(tableName)) {
                Toast.makeText(getActivity(), "Vui lòng nhập tên bàn", Toast.LENGTH_SHORT).show();
                return;
            }

            // Thêm bàn vào cơ sở dữ liệu (mặc định trạng thái hoặc không cần nhập)
            databaseHelper.addTable(accountId, tableName, "Trống");  // Giả sử mặc định trạng thái là "Available"
            Toast.makeText(getActivity(), "Thêm bàn thành công", Toast.LENGTH_SHORT).show();

            if (mListener != null) {
                mListener.onTableAdded();  // Thông báo cho Activity để cập nhật danh sách
            }

            dismiss();  // Đóng dialog
        });

        return view;
    }
}
