package com.example.bidaapp.View.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bidaapp.View.Layout.AddTableDialogFragment;
import com.example.bidaapp.Controller.TableAdapter;
import com.example.bidaapp.Model.DatabaseHelper;
import com.example.bidaapp.Model.Table;
import com.example.bidaapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class ManageTablesActivity extends BaseActivity implements AddTableDialogFragment.OnTableAddedListener {
    private RecyclerView recyclerView;
    private TableAdapter tableAdapter;
    private DatabaseHelper databaseHelper;
    private int accountId;
    private List<Table> currentTableList;
    private ImageView backBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_manage_tables);
        // Nạp giao diện của Activity vào content_frame của BaseActivity
        getLayoutInflater().inflate(R.layout.activity_manage_tables, findViewById(R.id.content_frame));

        // Nhận accountId từ Intent
        Intent intent = getIntent();
        accountId = intent.getIntExtra("account_id", -1);
        backBtn = findViewById(R.id.backBtn);
        // Khởi tạo DatabaseHelper
        databaseHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.listViewInvoices);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Kiểm tra nếu accountId hợp lệ và lấy danh sách bàn từ cơ sở dữ liệu
        if (accountId != -1) {
            loadTableList();
        } else {
            Toast.makeText(this, "Account ID không hợp lệ!", Toast.LENGTH_SHORT).show();
        }

        // Thiết lập FloatingActionButton
        FloatingActionButton addTableButton = findViewById(R.id.btnAddCustomer);
        addTableButton.setOnClickListener(v -> {
            AddTableDialogFragment dialog = new AddTableDialogFragment(accountId);
            dialog.show(getSupportFragmentManager(), "AddTableDialog");
        });
        backBtn.setOnClickListener(v -> finish()); // Hoặc sử dụng Intent nếu cần
    }

    private void loadTableList() {
        List<Table> newTableList = databaseHelper.getTablesByAccountId(accountId);

        // So sánh danh sách mới và danh sách hiện tại
        if (currentTableList == null || !currentTableList.equals(newTableList)) {
            currentTableList = newTableList; // Cập nhật danh sách hiện tại
            tableAdapter = new TableAdapter(this, currentTableList);
            recyclerView.setAdapter(tableAdapter);  // Cập nhật adapter
        }
    }

    @Override
    public void onTableAdded() {
        loadTableList();  // Tải lại danh sách bàn khi bàn mới được thêm
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTableList();  // Tải lại danh sách bàn khi Activity trở lại
    }
}
