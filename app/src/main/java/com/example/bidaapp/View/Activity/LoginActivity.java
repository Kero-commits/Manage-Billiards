package com.example.bidaapp.View.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bidaapp.Model.DatabaseHelper;
import com.example.bidaapp.R;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText passwordEditText;
    private DatabaseHelper databaseHelper;
    private Button login_button;
    private Button registerButton; // Khai báo nút đăng ký
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        int accountId = preferences.getInt("account_id", -1);
        if (accountId != -1) {
            navigateToMainActivity();
        }

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        databaseHelper = new DatabaseHelper(this);
        login_button = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.registerButton); // Khởi tạo nút đăng ký

        passwordEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (passwordEditText.getRight() - passwordEditText.getCompoundDrawables()[2].getBounds().width())) {
                    togglePasswordVisibility();
                    return true;
                }
            }
            return false;
        });

        login_button.setOnClickListener(v -> {
            if (validateInputs()) {
                login();
            }
        });

        // Sự kiện cho nút đăng ký
        registerButton.setOnClickListener(v -> openRegisterActivity());

        updatePasswordToggleIcon();
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordEditText.setInputType(129); // 129 = TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_PASSWORD
            isPasswordVisible = false;
        } else {
            passwordEditText.setInputType(144); // 144 = TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            isPasswordVisible = true;
        }
        passwordEditText.setSelection(passwordEditText.getText().length());
        updatePasswordToggleIcon();
    }

    private void updatePasswordToggleIcon() {
        int drawableId = isPasswordVisible ? R.drawable.mat_mo : R.drawable.mat_nham;
        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableId, 0);
    }

    private boolean validateInputs() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        boolean isValid = true;

        if (!isUsernameValid(username)) {
            usernameEditText.setError("Tài khoản phải từ 3 ký tự, bao gồm chữ và số.");
            isValid = false;
        } else {
            usernameEditText.setError(null);
        }

        if (!isPasswordValid(password)) {
            passwordEditText.setError("Mật khẩu phải có ít nhất 6 ký tự, bao gồm chữ hoa, số và ký tự đặc biệt.");
            isValid = false;
        } else {
            passwordEditText.setError(null);
        }

        return isValid;
    }

    private boolean isUsernameValid(String username) {
        return username.length() >= 3 && username.matches("^(?=.*[a-zA-Z])(?=.*[0-9]).+$");
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6 && password.matches("^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#$%^&+=]).+$");
    }

    public void login() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đăng nhập...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                int accountId = databaseHelper.checkLogin(username, password);
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    if (accountId != -1) {
                        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt("account_id", accountId);
                        editor.apply();
                        navigateToMainActivity();
                    } else {
                        passwordEditText.setError("Tên đăng nhập hoặc mật khẩu không đúng.");
                    }
                });
            } catch (InterruptedException e) {
                runOnUiThread(progressDialog::dismiss);
            }
        }).start();
    }

    private void openRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
