package com.example.bidaapp.View.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;

import android.widget.Button;

import com.example.bidaapp.R;
import com.example.bidaapp.a_TEST;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends BaseActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private int accountId;
    private TextView accountIdTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Nạp giao diện của Activity vào content_frame của BaseActivity
        getLayoutInflater().inflate(R.layout.activity_main, findViewById(R.id.content_frame));

        requestPermissions();

        // Lấy thông tin account_id từ SharedPreferences
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        accountId = preferences.getInt("account_id", -1);

        // Liên kết TextView để hiển thị Account ID
        accountIdTextView = findViewById(R.id.accountIdTextView);
        if (accountId != -1) {
            accountIdTextView.setText("Account ID: " + accountId);
        } else {
            accountIdTextView.setText("Account ID: Not set");
        }

        // Các LinearLayout đại diện cho các mục
        LinearLayout manageTablesLayout = findViewById(R.id.manageTablesLayout);
        LinearLayout manageProductsLayout = findViewById(R.id.manageProductsLayout);
        LinearLayout manageCustomersLayout = findViewById(R.id.manageCustomersLayout);
        LinearLayout manageInvoicesLayout = findViewById(R.id.manageInvoicesLayout);
        LinearLayout manageThongKeLayout = findViewById(R.id.btnThongKe); // Thêm nút Thống Kê
        Button testBangButton = findViewById(R.id.test_bang);

        // Set OnClickListener cho mỗi mục
        manageTablesLayout.setOnClickListener(v -> {
            startActivity(new Intent(this, ManageTablesActivity.class).putExtra("account_id", accountId));
        });

        manageProductsLayout.setOnClickListener(v -> {
            startActivity(new Intent(this, ManageProductsActivity.class).putExtra("account_id", accountId));
        });

        manageCustomersLayout.setOnClickListener(v -> {
            startActivity(new Intent(this, ManageCustomersActivity.class).putExtra("account_id", accountId));
        });

        manageInvoicesLayout.setOnClickListener(v -> {
            startActivity(new Intent(this, ManageInvoicesActivity.class).putExtra("account_id", accountId));
        });

        // Set OnClickListener cho nút Thống Kê
        manageThongKeLayout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Manage_ThongKe_Activity.class);
            intent.putExtra("account_id", accountId);
            startActivity(intent);
        });

        testBangButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, a_TEST.class);
            intent.putExtra("account_id", accountId);
            startActivity(intent);
        });

        // Thêm chức năng đăng xuất cho TextView
        TextView logoutTextView = findViewById(R.id.textView4); // Liên kết TextView "Đăng xuất"

        logoutTextView.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Xác nhận đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.remove("account_id");
                        editor.apply();

                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish(); // Đóng activity hiện tại
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void requestPermissions() {
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        };

        // Kiểm tra quyền
        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        // Nếu chưa được cấp quyền, yêu cầu quyền
        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        } else {
            // Tất cả quyền đã được cấp, thực hiện tiếp
            onPermissionsGranted();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                // Tất cả quyền đã được cấp
                onPermissionsGranted();
            } else {
                // Một hoặc nhiều quyền chưa được cấp
                // Có thể hiển thị thông báo cho người dùng
            }
        }
    }

    private void onPermissionsGranted() {
        // Thực hiện các thao tác sau khi quyền đã được cấp
    }
}
