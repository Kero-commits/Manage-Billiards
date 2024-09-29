package com.example.bidaapp.View.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.view.ViewGroup;
import android.text.InputType;

import com.example.bidaapp.Model.DatabaseHelper;
import com.example.bidaapp.Model.Product;
import com.example.bidaapp.Model.Table;
import com.example.bidaapp.R;
import android.os.Handler;
import android.app.ProgressDialog;

public class OrderActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper; // Trợ giúp cơ sở dữ liệu
    private int idBan; // ID bàn
    private int accountId; // ID tài khoản
    private String thoiGianBatDau; // Thời gian bắt đầu
    private String thoiGianKetThuc; // Thời gian kết thúc
    private int tongThoiGian; // Tổng thời gian chơi (giây)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        dbHelper = new DatabaseHelper(this);
        idBan = getIntent().getIntExtra("id_ban", -1);
        String trangThai = dbHelper.getTableStatus(idBan);
        Toast.makeText(this, "Trạng thái bàn: " + trangThai, Toast.LENGTH_SHORT).show();

        // Lấy accountId từ SharedPreferences
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        accountId = preferences.getInt("account_id", -1);
        Log.d("OrderActivity", "Received Account ID: " + accountId);

        TextView timeStartTextView = findViewById(R.id.timeStartTextView);
        TextView accountIdTextView = findViewById(R.id.accountIdTextView);

        // Nhận thời gian bắt đầu từ Intent
        thoiGianBatDau = getIntent().getStringExtra("thoi_gian_bat_dau");
        Log.d("OrderActivity", "Thời gian bắt đầu: " + thoiGianBatDau);
        updateTimeStartTextView(timeStartTextView);

        // Hiển thị Account ID
        if (accountId != -1) {
            accountIdTextView.setText("Account ID: " + accountId);
        } else {
            accountIdTextView.setText("Account ID: Chưa thiết lập");
        }

        Button orderButton = findViewById(R.id.orderButton);
        orderButton.setOnClickListener(v -> showProductDialog());

        Button endButton = findViewById(R.id.endButton);
        endButton.setOnClickListener(v -> finishSession());
    }

    private void updateTimeStartTextView(TextView timeStartTextView) {
        if (thoiGianBatDau != null && !thoiGianBatDau.isEmpty()) {
            timeStartTextView.setText("Thời gian bắt đầu: " + thoiGianBatDau);
        } else {
            timeStartTextView.setText("Thời gian bắt đầu: Chưa bắt đầu");
        }
    }

    private void showProductDialog() {
        List<Product> productList = dbHelper.getProductsByAccountId(accountId);

        if (productList.isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm nào!", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayAdapter<Product> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, productList);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Danh sách sản phẩm")
                .setAdapter(adapter, (dialog, which) -> {
                    Product selectedProduct = productList.get(which);
                    showQuantityDialog(selectedProduct);
                })
                .setNegativeButton("Đóng", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void showQuantityDialog(Product product) {
        int availableQuantity = dbHelper.getAvailableQuantity(product.getId());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nhập số lượng cho " + product.getName());

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_quantity, null);
        builder.setView(dialogView);

        EditText quantityEditText = dialogView.findViewById(R.id.quantityEditText);
        TextView availableQuantityTextView = dialogView.findViewById(R.id.availableQuantityTextView);
        TextView priceTextView = dialogView.findViewById(R.id.priceTextView);
        TextView totalPriceTextView = dialogView.findViewById(R.id.totalPriceTextView);

        availableQuantityTextView.setText("Số lượng hiện có: " + availableQuantity);
        priceTextView.setText("Giá: " + product.getPrice());
        quantityEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String quantityStr = s.toString();
                if (!quantityStr.isEmpty()) {
                    int quantity = Integer.parseInt(quantityStr);
                    if (quantity > availableQuantity) {
                        quantityEditText.setError("Số lượng vượt quá số lượng hiện có!");
                        totalPriceTextView.setText("Tổng giá: 0");
                    } else {
                        quantityEditText.setError(null);
                        double totalPrice = product.getPrice() * quantity;
                        totalPriceTextView.setText("Tổng giá: " + totalPrice);
                    }
                } else {
                    totalPriceTextView.setText("Tổng giá: 0");
                    quantityEditText.setError(null);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        builder.setPositiveButton("Đặt hàng", (dialog, which) -> {
            String quantityStr = quantityEditText.getText().toString();
            if (!quantityStr.isEmpty()) {
                int selectedQuantity = Integer.parseInt(quantityStr);
                if (selectedQuantity > availableQuantity) {
                    Toast.makeText(OrderActivity.this, "Số lượng không được vượt quá " + availableQuantity, Toast.LENGTH_SHORT).show();
                } else {
                    placeOrder(product, selectedQuantity);
                }
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void placeOrder(Product product, int quantity) {
        double totalPrice = product.getPrice() * quantity;
        int availableQuantity = dbHelper.getAvailableQuantity(product.getId());
        int newQuantity = availableQuantity - quantity;

        boolean updateSuccess = dbHelper.updateProductQuantity(product.getId(), newQuantity);

        if (updateSuccess) {
            boolean orderSuccess = dbHelper.insertOrderDetail(idBan, product.getId(), accountId, quantity, totalPrice, getCurrentTime());

            if (orderSuccess) {
                Toast.makeText(this, "Đặt hàng thành công! Tổng giá: " + totalPrice, Toast.LENGTH_SHORT).show();
                displayOrders();

                if (thoiGianBatDau == null || thoiGianBatDau.isEmpty()) {
                    thoiGianBatDau = getCurrentTime();
                }
                updateTimeStartTextView((TextView) findViewById(R.id.timeStartTextView));
            } else {
                Toast.makeText(this, "Lưu chi tiết hóa đơn thất bại", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Cập nhật số lượng thất bại", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayOrders() {
        String endTime = getCurrentTime();
        List<Table.OrderDetail> orderDetails = dbHelper.getOrderDetailsByTableId2(idBan, thoiGianBatDau, endTime);

        ArrayAdapter<Table.OrderDetail> adapter = new ArrayAdapter<Table.OrderDetail>(this, android.R.layout.simple_list_item_1, orderDetails) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                Table.OrderDetail orderDetail = getItem(position);
                textView.setText(orderDetail.getTenSanPham() + "                        " + orderDetail.getSoLuong() + "               " + orderDetail.getTongGia());
                return view;
            }
        };

        ListView orderListView = findViewById(R.id.orderListView);
        orderListView.setAdapter(adapter);
    }

    private void finishSession() {
        Toast.makeText(this, "Kết thúc phiên chơi!", Toast.LENGTH_SHORT).show();

        // Lưu thời gian kết thúc
        thoiGianKetThuc = getCurrentTime();
        dbHelper.updateEndTime(idBan, thoiGianKetThuc);



        // Tính toán thời gian chơi
        long durationInMillis = getDurationInMillis(thoiGianBatDau, thoiGianKetThuc);
        tongThoiGian = (int) (durationInMillis / 1000); // Chuyển đổi milliseconds sang giây

        if (tongThoiGian <= 0) {
            Toast.makeText(this, "Thời gian chơi không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }
        int idThoigian = dbHelper.getIdThoigianByBanId(idBan);
        dbHelper.updateTongThoiGian(idThoigian, tongThoiGian);

        showDialogNhapGiaTien(); // Gọi hàm để nhập giá trị tiền
    }

    private void showDialogNhapGiaTien() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nhập giá trị tiền");

        EditText priceEditText = new EditText(this);
        priceEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        priceEditText.setHint("Nhập giá trị tiền để nhân với số giây");

        builder.setView(priceEditText);

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            String priceStr = priceEditText.getText().toString();
            if (!priceStr.isEmpty()) {
                // Hiện ProgressDialog
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Đang xử lý, vui lòng đợi...");
                progressDialog.setCancelable(false); // Ngăn người dùng tắt dialog
                progressDialog.show();

                new Thread(() -> {
                    try {
                        // Thực hiện các tác vụ nặng
                        double pricePerSecond = Double.parseDouble(priceStr);
                        double tongTienThoiGian = tongThoiGian * pricePerSecond;

                        // Chắc chắn rằng `thoiGianKetThuc` không null
                        double tongTienSanPham = dbHelper.getTotalProductCostForTable(idBan, thoiGianBatDau, thoiGianKetThuc);
                        int idHoaDon = dbHelper.insertHoaDon(accountId, idBan, tongTienThoiGian, tongTienSanPham);

                        // Thêm một chút delay để thấy được ProgressDialog
                        Thread.sleep(1000); // Trì hoãn 1 giây

                        runOnUiThread(() -> {
                            if (idHoaDon > 0) {
                                Toast.makeText(this, "Hóa đơn đã được tạo với ID: " + idHoaDon, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Không thể tạo hóa đơn", Toast.LENGTH_SHORT).show();
                            }

                            // Cập nhật trạng thái bàn và reset thời gian bắt đầu
                            dbHelper.updateTableStatus(idBan, "Trống");
                            dbHelper.resetStartTime(idBan);
                            displayOrders();

                            // Đóng ProgressDialog
                            progressDialog.dismiss();
                            finish(); // Hoặc bạn có thể sử dụng Intent để chuyển đến Activity khác
                        });
                    } catch (Exception e) {
                        // Xử lý ngoại lệ nếu cần
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Có lỗi xảy ra: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        });
                    }
                }).start(); // Bắt đầu thread để thực hiện các tác vụ nặng

            } else {
                Toast.makeText(this, "Vui lòng nhập giá", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }


    private long getDurationInMillis(String startTime, String endTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date startDate = sdf.parse(startTime);
            Date endDate = sdf.parse(endTime);
            return endDate.getTime() - startDate.getTime();
        } catch (Exception e) {
            Log.e("OrderActivity", "Lỗi khi phân tích ngày", e);
            return 0;
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}

