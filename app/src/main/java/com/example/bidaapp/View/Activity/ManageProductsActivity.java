package com.example.bidaapp.View.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.net.Uri;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bidaapp.View.Layout.AddProductDialogFragment;
import com.example.bidaapp.Model.DatabaseHelper;
import com.example.bidaapp.Model.Product;
import com.example.bidaapp.Controller.ProductAdapter;
import com.example.bidaapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

public class ManageProductsActivity extends BaseActivity implements AddProductDialogFragment.OnProductAddedListener {

    private static final int REQUEST_CODE = 100; // Mã yêu cầu quyền
    private RecyclerView recyclerViewProducts;
    private FloatingActionButton btnAddProduct;

    private DatabaseHelper databaseHelper;
    private int accountId;
    private Button btnSearch;
    private EditText editTextSearch;
    private ImageView backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_manage_products, findViewById(R.id.content_frame));

        // Khởi tạo các thành phần
        recyclerViewProducts = findViewById(R.id.listViewCustomers);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        btnSearch = findViewById(R.id.btnSearch);
        editTextSearch = findViewById(R.id.editTextSearch);
        backBtn = findViewById(R.id.backBtn);

        databaseHelper = new DatabaseHelper(this);
        accountId = getIntent().getIntExtra("account_id", -1);

        // Kiểm tra và yêu cầu quyền truy cập
        checkPermissionsAndLoadProducts();

        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));

        btnAddProduct.setOnClickListener(v -> {
            AddProductDialogFragment dialog = new AddProductDialogFragment(accountId);
            dialog.show(getSupportFragmentManager(), "AddProductDialog");
        });

        btnSearch.setOnClickListener(v -> {
            String searchQuery = editTextSearch.getText().toString().trim();
            if (!searchQuery.isEmpty()) {
                loadProducts(searchQuery);
            } else {
                loadProducts(""); // Gọi lại hàm để tải tất cả sản phẩm khi không có tìm kiếm
                Toast.makeText(ManageProductsActivity.this, "Displaying all products", Toast.LENGTH_SHORT).show();
            }
        });

        // Thêm sự kiện cho nút quay lại
        backBtn.setOnClickListener(v -> finish()); // Hoặc sử dụng Intent nếu cần
    }

    private void checkPermissionsAndLoadProducts() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
        } else {
            loadProducts(""); // Nếu đã có quyền, tải sản phẩm
        }
    }

    @Override
    public void onProductAdded() {
        loadProducts(""); // Tải lại sản phẩm khi sản phẩm mới được thêm
    }

    public void loadProducts(String searchQuery) {
        List<Product> products = databaseHelper.searchProducts(accountId, searchQuery);
        ProductAdapter adapter = new ProductAdapter(this, products);
        recyclerViewProducts.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadProducts(""); // Nếu quyền được cấp, tải lại sản phẩm
            } else {
                Toast.makeText(this, "Permission denied to read your external storage", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
