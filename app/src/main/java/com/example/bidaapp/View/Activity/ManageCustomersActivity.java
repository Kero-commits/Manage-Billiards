package com.example.bidaapp.View.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bidaapp.View.Layout.AddCustomerDialogFragment;
import com.example.bidaapp.Controller.CustomerAdapter;
import com.example.bidaapp.Model.DatabaseHelper;
import com.example.bidaapp.Model.Customer;
import com.example.bidaapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ManageCustomersActivity extends BaseActivity {

    private RecyclerView recyclerViewCustomers;
    private FloatingActionButton btnAddCustomer;
    private Button btnSearch;
    private EditText etSearch;
    private DatabaseHelper databaseHelper;
    private int accountId;
    private CustomerAdapter customerAdapter;
    private ImageView backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_manage_customers, findViewById(R.id.content_frame));

        recyclerViewCustomers = findViewById(R.id.listViewCustomers);
        btnAddCustomer = findViewById(R.id.btnAddCustomer);
        btnSearch = findViewById(R.id.btnSearch);
        etSearch = findViewById(R.id.editTextSearch);
        backBtn = findViewById(R.id.backBtn);
        databaseHelper = new DatabaseHelper(this);

        recyclerViewCustomers.setLayoutManager(new LinearLayoutManager(this));

        // Lấy accountId từ Intent
        accountId = getIntent().getIntExtra("account_id", -1);
        Log.d("ManageCustomersActivity", "Account ID: " + accountId);

        if (accountId != -1) {
            loadCustomers();
        } else {
            Toast.makeText(this, "Invalid account ID", Toast.LENGTH_SHORT).show();
        }

        backBtn.setOnClickListener(v -> finish());

        btnAddCustomer.setOnClickListener(v -> {
            AddCustomerDialogFragment dialog = new AddCustomerDialogFragment(accountId);
            dialog.show(getSupportFragmentManager(), "AddCustomerDialog");
        });

        btnSearch.setOnClickListener(v -> {
            String query = etSearch.getText().toString().trim();
            loadCustomers(query);
        });
    }

    public void loadCustomers(String query) {
        try {
            List<Customer> customers;
            if (query.isEmpty()) {
                customers = databaseHelper.getCustomersByAccountId(accountId);
            } else {
                // Gọi phương thức tìm kiếm với accountId
                customers = databaseHelper.searchCustomers(query, accountId);
            }

            Log.d("ManageCustomersActivity", "Number of customers found: " + customers.size());

            if (customers.isEmpty()) {
                Toast.makeText(this, "Không có khách hàng nào", Toast.LENGTH_SHORT).show();
            } else {
                // Khởi tạo và thiết lập Adapter cho RecyclerView
                customerAdapter = new CustomerAdapter(this, customers);
                recyclerViewCustomers.setAdapter(customerAdapter);
            }
        } catch (Exception e) {
            // Xử lý ngoại lệ nếu có lỗi xảy ra khi lấy dữ liệu từ cơ sở dữ liệu
            Toast.makeText(this, "Error loading customers: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Phương thức để tải tất cả khách hàng
    public void loadCustomers() {
        loadCustomers(""); // Gọi với tham số trống để tải tất cả
    }
}
