package com.example.bidaapp.View.Layout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.bidaapp.Model.DatabaseHelper;
import com.example.bidaapp.R;

public class AddProductActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editTextProductName;
    private EditText editTextQuantity;
    private EditText editTextPrice;
    private EditText editTextImagePath;
    private Button buttonAddProduct;
    private Button buttonSelectImage;
    private DatabaseHelper databaseHelper;
    private int accountId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_product); // Đảm bảo layout đúng

        // Initialize views
        editTextProductName = findViewById(R.id.editTextProductName);
        editTextQuantity = findViewById(R.id.editTextQuantity);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextImagePath = findViewById(R.id.editTextImagePath);
        buttonAddProduct = findViewById(R.id.btnAddProduct); // Đảm bảo ID đúng
        buttonSelectImage = findViewById(R.id.btnSelectImage);
        databaseHelper = new DatabaseHelper(this);

        // Get accountId from Intent
        accountId = getIntent().getIntExtra("account_id", -1);

        // Set click listener for select image button
        buttonSelectImage.setOnClickListener(v -> openFileChooser());

        // Set click listener for add product button
        buttonAddProduct.setOnClickListener(v -> addProduct());
    }

    private void openFileChooser() {
        // Check for permission to read external storage
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            // Start an intent to select an image
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFileChooser();
            } else {
                Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            editTextImagePath.setText(imageUri.toString()); // Set the image URI in EditText
        } else {
            Toast.makeText(this, "Failed to select image", Toast.LENGTH_SHORT).show(); // Show error if selection fails
        }
    }

    private void addProduct() {
        String productName = editTextProductName.getText().toString().trim();
        String quantityStr = editTextQuantity.getText().toString().trim();
        String priceStr = editTextPrice.getText().toString().trim();
        String imagePath = editTextImagePath.getText().toString().trim();

        if (productName.isEmpty() || quantityStr.isEmpty() || priceStr.isEmpty() || imagePath.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityStr);
            double price = Double.parseDouble(priceStr);

            // Log giá trị để kiểm tra
            Log.d("AddProduct", "Account ID: " + accountId);
            Log.d("AddProduct", "Product Name: " + productName);
            Log.d("AddProduct", "Quantity: " + quantity);
            Log.d("AddProduct", "Price: " + price);
            Log.d("AddProduct", "Image Path: " + imagePath);

            // Add product to database
            boolean isInserted = databaseHelper.addProduct(accountId, productName, quantity, price, imagePath);
            if (isInserted) {
                Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show();
                finish(); // Close activity
            } else {
                Toast.makeText(this, "Failed to add product", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid quantity or price", Toast.LENGTH_SHORT).show();
        }
    }
}
