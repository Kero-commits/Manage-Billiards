package com.example.bidaapp.View.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bidaapp.Model.DatabaseHelper;
import com.example.bidaapp.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private DatabaseHelper dbHelper;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        dbHelper = new DatabaseHelper(this);

        // Thêm sự kiện cho EditText mật khẩu
        edtPassword.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (edtPassword.getRight() - edtPassword.getCompoundDrawables()[2].getBounds().width())) {
                    togglePasswordVisibility();
                    return true;
                }
            }
            return false;
        });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            edtPassword.setInputType(129); // 129 = TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_PASSWORD
            edtPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.mat_nham, 0); // Icon ẩn
        } else {
            edtPassword.setInputType(144); // 144 = TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            edtPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.mat_mo, 0); // Icon hiện
        }
        edtPassword.setSelection(edtPassword.getText().length());
        isPasswordVisible = !isPasswordVisible;
    }

    public void register(View view) {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // Reset lỗi trước khi kiểm tra
        edtUsername.setError(null);
        edtPassword.setError(null);

        if (username.isEmpty() || password.isEmpty()) {
            if (username.isEmpty()) {
                edtUsername.setError("Vui lòng nhập tên tài khoản");
            }
            if (password.isEmpty()) {
                edtPassword.setError("Vui lòng nhập mật khẩu");
            }
            return;
        }

        if (!isUsernameValid(username)) {
            edtUsername.setError("Tài khoản phải từ 3 ký tự, bao gồm chữ và số.");
            return;
        }

        if (!isPasswordValid(password)) {
            edtPassword.setError("Mật khẩu phải có ít nhất 6 ký tự, bao gồm chữ hoa, số và ký tự đặc biệt.");
            return;
        }

        String role = "user"; // Hoặc giá trị mặc định khác bạn muốn

        // Hiện ProgressDialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đăng ký...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(() -> {
            try {
                // Giả lập thời gian xử lý
                Thread.sleep(1000);

                // Thực hiện đăng ký
                boolean isRegistered = dbHelper.addAccount(username, password, role);
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    if (isRegistered) {
                        Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                        finish(); // Đóng activity đăng ký
                    } else {
                        edtUsername.setError("Tên tài khoản đã tồn tại");
                    }
                });
            } catch (InterruptedException e) {
                runOnUiThread(progressDialog::dismiss);
            }
        }).start();
    }

    private boolean isUsernameValid(String username) {
        return username.length() >= 3 && username.matches("^(?=.*[a-zA-Z])(?=.*[0-9]).+$");
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6 && password.matches("^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#$%^&+=]).+$");
    }

    public void login(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent); // Chuyển hướng đến LoginActivity
    }
}
