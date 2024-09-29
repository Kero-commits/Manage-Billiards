package com.example.bidaapp.View.Layout;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.bidaapp.Model.DatabaseHelper;
import com.example.bidaapp.R;
import com.example.bidaapp.View.Activity.ManageProductsActivity;

public class EditProductDialogFragment extends DialogFragment {

    private int productId;
    private String productName;
    private int quantity;
    private double price;
    private String imagePath;

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageViewProduct;
    private EditText editTextImagePath;

    public EditProductDialogFragment(int productId, String productName, int quantity, double price, String imagePath) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.imagePath = imagePath;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_product, null);

        EditText editTextProductName = view.findViewById(R.id.editTextProductName);
        EditText editTextQuantity = view.findViewById(R.id.editTextQuantity);
        EditText editTextPrice = view.findViewById(R.id.editTextPrice);
        editTextImagePath = view.findViewById(R.id.editTextImagePath);
        imageViewProduct = view.findViewById(R.id.imageViewProduct);
        Button btnSelectImage = view.findViewById(R.id.buttonSelectImage);
        Button btnSave = view.findViewById(R.id.buttonSave);
        Button btnCancel = view.findViewById(R.id.buttonCancel);

        // Gán giá trị ban đầu
        editTextProductName.setText(productName);
        editTextQuantity.setText(String.valueOf(quantity));
        editTextPrice.setText(String.valueOf(price));
        editTextImagePath.setText(imagePath);

        // Hiển thị hình ảnh hiện tại
        Glide.with(requireContext()).load(imagePath).into(imageViewProduct);

        // Thiết lập TextWatcher cho các EditText
        editTextProductName.addTextChangedListener(createTextWatcher(editTextProductName));
        editTextQuantity.addTextChangedListener(createTextWatcher(editTextQuantity));
        editTextPrice.addTextChangedListener(createTextWatcher(editTextPrice));

        // Xử lý sự kiện nhấn nút chọn hình ảnh
        btnSelectImage.setOnClickListener(v -> openFileChooser());

        // Xử lý sự kiện cho nút Save
        btnSave.setOnClickListener(v -> {
            String updatedName = editTextProductName.getText().toString().trim();
            String updatedQuantityStr = editTextQuantity.getText().toString().trim();
            String updatedPriceStr = editTextPrice.getText().toString().trim();

            boolean isValid = true;

            // Kiểm tra tên sản phẩm
            if (updatedName.isEmpty() || !updatedName.matches(".*[a-zA-Z]+.*")) {
                editTextProductName.setError("Tên sản phẩm bắt buộc có chữ.");
                isValid = false;
            } else {
                editTextProductName.setError(null);
            }

            // Kiểm tra số lượng
            int updatedQuantity = 0;
            if (updatedQuantityStr.isEmpty()) {
                editTextQuantity.setError("Số lượng không được để trống.");
                isValid = false;
            } else {
                try {
                    updatedQuantity = Integer.parseInt(updatedQuantityStr);
                    if (updatedQuantity <= 0) {
                        editTextQuantity.setError("Số lượng phải lớn hơn 0.");
                        isValid = false;
                    } else {
                        editTextQuantity.setError(null);
                    }
                } catch (NumberFormatException e) {
                    editTextQuantity.setError("Số lượng không hợp lệ.");
                    isValid = false;
                }
            }

            // Kiểm tra giá tiền
            double updatedPrice = 0.0;
            if (updatedPriceStr.isEmpty()) {
                editTextPrice.setError("Giá tiền không được để trống.");
                isValid = false;
            } else {
                try {
                    updatedPrice = Double.parseDouble(updatedPriceStr);
                    if (updatedPrice <= 0) {
                        editTextPrice.setError("Giá tiền phải lớn hơn 0.");
                        isValid = false;
                    } else {
                        editTextPrice.setError(null);
                    }
                } catch (NumberFormatException e) {
                    editTextPrice.setError("Giá tiền không hợp lệ.");
                    isValid = false;
                }
            }

            if (!isValid) return;

            // Cập nhật sản phẩm vào cơ sở dữ liệu
            DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
            boolean result = databaseHelper.updateProduct(productId, updatedName, updatedQuantity, updatedPrice, imagePath);

            // Thông báo kết quả
            if (result) {
                Toast.makeText(getActivity(), "Product updated", Toast.LENGTH_SHORT).show();
                if (getActivity() instanceof ManageProductsActivity) {
                    ((ManageProductsActivity) getActivity()).loadProducts("");
                }
                dismiss();
            } else {
                Toast.makeText(getActivity(), "Error updating product", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý sự kiện cho nút Cancel
        btnCancel.setOnClickListener(v -> dismiss());

        return new AlertDialog.Builder(requireActivity())
                .setView(view)
                .setTitle("Edit Product")
                .create();
    }

    private TextWatcher createTextWatcher(EditText editText) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editText.setError(null); // Xóa thông báo lỗi khi có thay đổi
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == -1 && data != null) {
            Uri imageUri = data.getData();
            imagePath = imageUri.toString();
            editTextImagePath.setText(imagePath);
            Glide.with(requireContext()).load(imageUri).into(imageViewProduct);
        }
    }
}
