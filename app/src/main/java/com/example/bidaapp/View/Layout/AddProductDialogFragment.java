package com.example.bidaapp.View.Layout;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.bidaapp.Model.DatabaseHelper;
import com.example.bidaapp.R;

public class AddProductDialogFragment extends DialogFragment {

    private int accountId;
    private static final int PICK_IMAGE_REQUEST = 1;
    private String imagePath;

    public AddProductDialogFragment() {
        // Required empty public constructor
    }

    public AddProductDialogFragment(int accountId) {
        this.accountId = accountId;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_product, null);

        EditText editTextProductName = view.findViewById(R.id.editTextProductName);
        EditText editTextQuantity = view.findViewById(R.id.editTextQuantity);
        EditText editTextPrice = view.findViewById(R.id.editTextPrice);
        EditText editTextImagePath = view.findViewById(R.id.editTextImagePath);
        Button btnSelectImage = view.findViewById(R.id.btnSelectImage);
        Button btnAddProduct = view.findViewById(R.id.btnAddProduct);
        Button btnAddCancel = view.findViewById(R.id.btnAddCCancel);
        ImageView imageViewProduct = view.findViewById(R.id.imageViewProduct);

        // Thiết lập TextWatcher cho các EditText
        editTextProductName.addTextChangedListener(createTextWatcher(editTextProductName));
        editTextQuantity.addTextChangedListener(createTextWatcher(editTextQuantity));
        editTextPrice.addTextChangedListener(createTextWatcher(editTextPrice));

        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        btnAddProduct.setOnClickListener(v -> {
            String productName = editTextProductName.getText().toString().trim();
            String quantityStr = editTextQuantity.getText().toString().trim();
            String priceStr = editTextPrice.getText().toString().trim();

            boolean isValid = true;

            // Kiểm tra tên sản phẩm
            if (productName.isEmpty() || !productName.matches(".*[a-zA-Z]+.*")) {
                editTextProductName.setError("Tên sản phẩm bắt buộc có chữ.");
                isValid = false;
            } else {
                editTextProductName.setError(null);
            }

            // Kiểm tra số lượng
            int quantity = 0;
            if (quantityStr.isEmpty()) {
                editTextQuantity.setError("Số lượng không được để trống.");
                isValid = false;
            } else {
                try {
                    quantity = Integer.parseInt(quantityStr);
                    if (quantity <= 0) {
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
            double price = 0.0;
            if (priceStr.isEmpty()) {
                editTextPrice.setError("Giá tiền không được để trống.");
                isValid = false;
            } else {
                try {
                    price = Double.parseDouble(priceStr);
                    if (price <= 0) {
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

            // Thêm sản phẩm vào cơ sở dữ liệu
            DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
            boolean result = databaseHelper.addProduct(accountId, productName, quantity, price, imagePath);

            if (result) {
                Toast.makeText(getActivity(), "Product added successfully", Toast.LENGTH_SHORT).show();
                if (getActivity() instanceof OnProductAddedListener) {
                    ((OnProductAddedListener) getActivity()).onProductAdded();
                }
                dismiss();
            } else {
                Toast.makeText(getActivity(), "Error adding product", Toast.LENGTH_SHORT).show();
            }
        });

        btnAddCancel.setOnClickListener(v -> dismiss());

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle("Add Product")
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == -1 && data != null) {
            Uri imageUri = data.getData();
            imagePath = getRealPathFromURI(imageUri);

            EditText editTextImagePath = getDialog().findViewById(R.id.editTextImagePath);
            editTextImagePath.setText(imagePath);

            ImageView imageViewProduct = getDialog().findViewById(R.id.imageViewProduct);
            Glide.with(this)
                    .load(imageUri)
                    .placeholder(R.drawable.coca)
                    .error(R.drawable.status_dot_yellow)
                    .into(imageViewProduct);
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getActivity().getContentResolver().query(contentUri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return null;
    }

    public interface OnProductAddedListener {
        void onProductAdded();
    }
}
