package com.example.bidaapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class a_TEST extends AppCompatActivity {

    Button btnAccount, btnBan, btnKhachHang, btnHoaDon, btnThoiGianChoi, btnChiTietHoaDon, btnProduct;
    ListView listViewData;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);

        // Kết nối các button và ListView
        btnAccount = findViewById(R.id.account);
        btnBan = findViewById(R.id.ban);
        btnKhachHang = findViewById(R.id.khachhang);
        btnHoaDon = findViewById(R.id.hoadon);
        btnThoiGianChoi = findViewById(R.id.thoigianchoi);
        btnChiTietHoaDon = findViewById(R.id.chitiethoadon);
        btnProduct = findViewById(R.id.product); // Thêm nút sản phẩm
        listViewData = findViewById(R.id.listViewData);

        // Mở hoặc tạo cơ sở dữ liệu
        db = openOrCreateDatabase("bidaapp.db", MODE_PRIVATE, null);

        // Set onClickListener cho từng nút
        btnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData("account");
            }
        });

        btnBan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData("ban");
            }
        });

        btnKhachHang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData("khach_hang");
            }
        });

        btnHoaDon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData("hoadon");
            }
        });

        btnThoiGianChoi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData("thoigianchoi");
            }
        });

        btnChiTietHoaDon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData("chitiethoadon");
            }
        });

        // Set onClickListener cho nút sản phẩm
        btnProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData("san_pham");
            }
        });
    }

    // Hàm truy vấn dữ liệu từ bảng và hiển thị lên ListView
    private void loadData(String tableName) {
        ArrayList<String> dataList = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName, null);
        if (cursor.moveToFirst()) {
            do {
                // Thêm dữ liệu vào dataList (dạng chuỗi)
                String rowData = "";
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    rowData += cursor.getString(i) + " ";
                }
                dataList.add(rowData);
            } while (cursor.moveToNext());
        }
        cursor.close();

        // Đưa dữ liệu vào ListView qua ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);
        listViewData.setAdapter(adapter);
    }
}
