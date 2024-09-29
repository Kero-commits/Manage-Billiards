package com.example.bidaapp.Model;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import java.util.List;
import java.util.ArrayList;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Locale;
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "bidaapp.db";
    private static final int DATABASE_VERSION = 3;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }




    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables
        String createTableAccount = "CREATE TABLE account (id_account INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, password TEXT, role TEXT)";
        String createTableBan = "CREATE TABLE ban (id_ban INTEGER PRIMARY KEY AUTOINCREMENT, id_account INTEGER, ten_ban TEXT, tinh_trang TEXT)";
        String createTableSanPham = "CREATE TABLE san_pham (id_san_pham INTEGER PRIMARY KEY AUTOINCREMENT, id_account INTEGER, ten_san_pham TEXT, so_luong INTEGER, gia REAL,  image_san_pham TEXT )";
        String createTableKhachHang = "CREATE TABLE khach_hang (id_khach_hang INTEGER PRIMARY KEY AUTOINCREMENT, id_account INTEGER, ten_khach TEXT, so_dien_thoai TEXT, dia_chi TEXT)";
        String createTableHoaDon = "CREATE TABLE hoadon (id_hoadon INTEGER PRIMARY KEY AUTOINCREMENT, id_thoigian INTEGER, id_account INTEGER, tong_tien_thoigian REAL, tong_tien_sanpham REAL, tong_tien REAL, ngay_lap TEXT DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (id_thoigian) REFERENCES thoigianchoi(id_thoigian), FOREIGN KEY (id_account) REFERENCES account(id_account))";
        String createTableThoiGianChoi = "CREATE TABLE thoigianchoi (id_thoigian INTEGER PRIMARY KEY AUTOINCREMENT, id_ban INTEGER, id_account INTEGER, thoi_gian_bat_dau TEXT, thoi_gian_ket_thuc TEXT, tong_thoi_gian INTEGER, FOREIGN KEY (id_ban) REFERENCES ban(id_ban), FOREIGN KEY (id_account) REFERENCES account(id_account))";
        String createTableChiTietHoaDon = "CREATE TABLE chitiethoadon (id_chitiet INTEGER PRIMARY KEY AUTOINCREMENT, id_ban INTEGER, id_sanpham INTEGER, id_account INTEGER, so_luong INTEGER, tong_gia REAL, ngay_order TEXT DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (id_ban) REFERENCES ban(id_ban), FOREIGN KEY (id_sanpham) REFERENCES san_pham(id_san_pham), FOREIGN KEY (id_account) REFERENCES account(id_account))";

        // Execute table creation queries
        db.execSQL(createTableAccount);
        db.execSQL(createTableBan);
        db.execSQL(createTableSanPham);
        db.execSQL(createTableKhachHang);
        db.execSQL(createTableHoaDon);
        db.execSQL(createTableThoiGianChoi);
        db.execSQL(createTableChiTietHoaDon);

        // Insert sample data into tables
        String insertAccount = "INSERT INTO account (username, password, role) VALUES ('admin', 'admin123', 'Admin'), ('user1', 'user123', 'User'), ('bogame', '123123', 'User')";
        String insertBan = "INSERT INTO ban (id_account, ten_ban, tinh_trang) VALUES (1, 'Ban 1', 'Trống'), (1, 'Ban 2', 'Có khách')";
        String insertSanPham = "INSERT INTO san_pham (id_account, ten_san_pham, so_luong, gia, image_san_pham) VALUES (1, 'Bida', 5, 100000, '/storage/emulated/0/Download0000~2.png'), (1, 'Cà phê', 10, 15000, '/storage/e0000~3.png')";
        String insertKhachHang = "INSERT INTO khach_hang (id_account, ten_khach, so_dien_thoai, dia_chi) VALUES (1, 'Nguyen Van A', '0909123456', '123 Đường ABC'), (1, 'Le Thi B', '0909876543', '456 Đường XYZ')";
        String insertHoaDon = "INSERT INTO hoadon (id_thoigian, id_account, tong_tien_thoigian, tong_tien_sanpham, tong_tien) VALUES (1, 1, 200000, 150000, 350000)";
        String insertThoiGianChoi = "INSERT INTO thoigianchoi (id_ban, id_account, thoi_gian_bat_dau, thoi_gian_ket_thuc, tong_thoi_gian) VALUES (1, 1, '2024-09-17 10:00:00', '2024-09-17 12:00:00', 120)";
        String insertChiTietHoaDon = "INSERT INTO chitiethoadon (id_ban, id_sanpham, id_account, so_luong, tong_gia) VALUES (1, 1, 1, 2, 200000)";

        // Execute insert queries
        db.execSQL(insertAccount);
        db.execSQL(insertBan);
        db.execSQL(insertSanPham);
        db.execSQL(insertKhachHang);
        db.execSQL(insertHoaDon);
        db.execSQL(insertThoiGianChoi);
        db.execSQL(insertChiTietHoaDon);
    }


    public double getTotalProductCostForTable(int tableId, String startTime, String endTime) {
        double totalCost = 0;
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT SUM(tong_gia) FROM chitiethoadon WHERE id_ban = ? AND ngay_order BETWEEN ? AND ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(tableId), startTime, endTime});

        if (cursor.moveToFirst()) {
            totalCost = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return totalCost;
    }


    public List<Invoice> getInvoicesByTotal(int accountId, double totalAmount) {
        List<Invoice> invoices = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Truy vấn để tìm hóa đơn theo tổng tiền
        Cursor cursor = db.rawQuery("SELECT * FROM hoadon WHERE id_account = ? AND tong_tien = ?", new String[]{String.valueOf(accountId), String.valueOf(totalAmount)});

        if (cursor.moveToFirst()) {
            do {
                int idHoaDon = cursor.getInt(cursor.getColumnIndex("id_hoadon"));
                double tongTienThoiGian = cursor.getDouble(cursor.getColumnIndex("tong_tien_thoigian"));
                double tongTienSanPham = cursor.getDouble(cursor.getColumnIndex("tong_tien_sanpham"));
                double tongTien = cursor.getDouble(cursor.getColumnIndex("tong_tien"));
                String ngayLap = cursor.getString(cursor.getColumnIndex("ngay_lap"));

                invoices.add(new Invoice(idHoaDon, tongTienThoiGian, tongTienSanPham, tongTien, ngayLap));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return invoices;
    }





    public List<Table.OrderDetail> getOrderDetailsByTableId2(int tableId, String startTime, String endTime) {
        List<Table.OrderDetail> orderDetails = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT s.ten_san_pham, c.so_luong, c.tong_gia " +
                        "FROM chitiethoadon c " +
                        "JOIN san_pham s ON c.id_sanpham = s.id_san_pham " +
                        "WHERE c.id_ban = ? AND c.ngay_order BETWEEN ? AND ?",
                new String[]{String.valueOf(tableId), startTime, endTime}
        );

        if (cursor.moveToFirst()) {
            do {
                Table.OrderDetail orderDetail = new Table.OrderDetail(
                        cursor.getString(cursor.getColumnIndex("ten_san_pham")),
                        cursor.getInt(cursor.getColumnIndex("so_luong")),
                        cursor.getDouble(cursor.getColumnIndex("tong_gia"))
                );
                orderDetails.add(orderDetail);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orderDetails;
    }

    public List<Table.OrderDetail> getOrderDetailsByTableIdAndTime(int tableId, String startTime) {
        List<Table.OrderDetail> orderDetails = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT s.ten_san_pham, c.so_luong, c.tong_gia " +
                        "FROM chitiethoadon c " +
                        "JOIN san_pham s ON c.id_sanpham = s.id_san_pham " +
                        "JOIN thoigianchoi t ON c.id_ban = t.id_ban " +
                        "WHERE c.id_ban = ? AND t.thoi_gian_bat_dau >= ?",
                new String[]{String.valueOf(tableId), startTime}
        );

        if (cursor.moveToFirst()) {
            do {
                Table.OrderDetail orderDetail = new Table.OrderDetail(
                        cursor.getString(cursor.getColumnIndex("ten_san_pham")),
                        cursor.getInt(cursor.getColumnIndex("so_luong")),
                        cursor.getDouble(cursor.getColumnIndex("tong_gia"))
                );
                orderDetails.add(orderDetail);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orderDetails;
    }


    public boolean insertOrderDetail(int idBan, int idSanPham, int idAccount, int soLuong, double tongGia, String ngayOrder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_ban", idBan);
        values.put("id_sanpham", idSanPham);
        values.put("id_account", idAccount);
        values.put("so_luong", soLuong);
        values.put("tong_gia", tongGia);
        values.put("ngay_order", ngayOrder); // Lưu ngày order

        long result = db.insert("chitiethoadon", null, values);
        return result != -1; // Trả về true nếu thêm thành công
    }


    public String getEndTime(int idBan) {
        String endTime = null;
        SQLiteDatabase db = this.getReadableDatabase();

        // Truy vấn thời gian kết thúc cho id_ban cụ thể
        Cursor cursor = db.rawQuery("SELECT thoi_gian_ket_thuc FROM thoigianchoi WHERE id_ban = ?", new String[]{String.valueOf(idBan)});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                endTime = cursor.getString(cursor.getColumnIndex("thoi_gian_ket_thuc"));
            }
            cursor.close();
        }

        return endTime; // Trả về thời gian kết thúc hoặc null nếu không tìm thấy
    }


    public boolean isTableOccupied(int tableId) {


        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM thoigianchoi WHERE idBan = ? AND thoi_gian_bat_dau IS NOT NULL";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(tableId)});
        cursor.moveToFirst();
        boolean isOccupied = cursor.getInt(0) > 0;
        cursor.close();
        return isOccupied;
    }
    public long insertStartTime(int idBan, String thoiGianBatDau) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("idBan", idBan);
        values.put("thoiGianBatDau", thoiGianBatDau); // Assuming "thoiGianBatDau" is the column name

        // Insert the new row and get the ID of the new row
        long newRowId = db.insert("thoigianchoi", null, values);

        db.close(); // Close the database connection
        return newRowId; // Return the ID of the newly inserted row, or -1 if there was an error
    }

    public boolean insertBill(int idThoiGian, int idAccount, double tongTienThoiGian, double tongTienSanPham, double tongTien) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("id_thoigian", idThoiGian);
        values.put("id_account", idAccount);
        values.put("tong_tien_thoigian", tongTienThoiGian);
        values.put("tong_tien_sanpham", tongTienSanPham);
        values.put("tong_tien", tongTien);

        long result = db.insert("hoadon", null, values);
        return result != -1; // Trả về true nếu việc chèn thành công
    }

//số lượng sản phẩm - số lượng đã đặt

    public boolean updateProductQuantity(int productId, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("so_luong", newQuantity);  // Đặt số lượng mới

        try {
            int rowsAffected = db.update("san_pham", values, "id_san_pham = ?", new String[]{String.valueOf(productId)});
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Lỗi khi cập nhật số lượng sản phẩm: ", e);
            return false;
        }
    }

    public void updateStartTime(int idBan, String thoiGianBatDau) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("thoi_gian_bat_dau", thoiGianBatDau);
        db.update("thoigianchoi", values, "id_ban = ?", new String[]{String.valueOf(idBan)});
        db.close();
    }
    public long insertThoiGianChoi(int idBan, int idAccount, String thoiGianBatDau) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_account", idAccount); // Lưu id_account đúng
        values.put("id_ban", idBan);
        values.put("thoi_gian_bat_dau", thoiGianBatDau);
        values.put("tong_thoi_gian", 0); // Khởi tạo giá trị

        long result = db.insert("thoigianchoi", null, values);
        db.close();
        return result;
    }

    public int getIdThoigianByBanId(int idBan) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int idThoigian = -1;

        try {
            String query = "SELECT id_thoigian FROM thoigianchoi WHERE id_ban = ? ORDER BY thoi_gian_bat_dau DESC LIMIT 1";
            cursor = db.rawQuery(query, new String[]{String.valueOf(idBan)});

            if (cursor != null && cursor.moveToFirst()) {
                idThoigian = cursor.getInt(cursor.getColumnIndex("id_thoigian"));
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error while trying to get id_thoigian", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return idThoigian;
    }


    public void updateTongThoiGian(int idThoigian, int tongThoiGian) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("tong_thoi_gian", tongThoiGian);

        // Cập nhật trong bảng thoigianchoi cho phiên tương ứng
        db.update("thoigianchoi", values, "id_thoigian = ?", new String[]{String.valueOf(idThoigian)});
        db.close();
    }



    public void checkRecords(int idBan) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM thoigianchoi WHERE id_ban = ?", new String[]{String.valueOf(idBan)});

        if (cursor.moveToFirst()) {
            do {
                // Log thông tin bản ghi
                Log.d("Database", "Bản ghi: " + cursor.getInt(cursor.getColumnIndex("id_thoigian")) +
                        ", ID Bàn: " + cursor.getInt(cursor.getColumnIndex("id_ban")) +
                        ", Tổng Thời Gian: " + cursor.getInt(cursor.getColumnIndex("tong_thoi_gian")));
            } while (cursor.moveToNext());
        } else {
            Log.d("Database", "Không có bản ghi nào cho bàn: " + idBan);
        }
        cursor.close();
    }


//    public void updateTongThoiGian(int idBan, int tongThoiGian) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put("tong_thoi_gian", tongThoiGian);
//
//        // Cập nhật trong bảng thoigianchoi cho bàn tương ứng
//        int rowsAffected = db.update("thoigianchoi", values, "id_ban = ?", new String[]{String.valueOf(idBan)});
//        Log.d("Database", "Rows affected: " + rowsAffected);
//    }



    public void updateEndTime(int idBan, String thoiGianKetThuc) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("thoi_gian_ket_thuc", thoiGianKetThuc);
        // Cập nhật hàng của bàn cụ thể mà có thời gian bắt đầu
        db.update("thoigianchoi", values, "id_ban = ? AND thoi_gian_ket_thuc IS NULL", new String[]{String.valueOf(idBan)});
        db.close();
    }



    // hiển thị sp đã đặt
    public List<Table.OrderDetail> getOrderDetailsByTableId1(int idBan) {
        List<Table.OrderDetail> orderDetails = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM chitiethoadon WHERE id_ban = ?", new String[]{String.valueOf(idBan)});

        while (cursor.moveToNext()) {
            int idSanPham = cursor.getInt(cursor.getColumnIndex("id_sanpham"));
            int soLuong = cursor.getInt(cursor.getColumnIndex("so_luong"));
            double tongGia = cursor.getDouble(cursor.getColumnIndex("tong_gia"));
            String ngayOrder = cursor.getString(cursor.getColumnIndex("ngay_order"));

            // Lấy tên sản phẩm từ bảng sản phẩm
            String tenSanPham = getProductNameById(idSanPham); // Phương thức cần được định nghĩa để lấy tên sản phẩm

            // Kiểm tra xem sản phẩm đã tồn tại trong danh sách chưa
            boolean found = false;
            for (Table.OrderDetail detail : orderDetails) {
                if (detail.getTenSanPham().equals(tenSanPham)) {
                    detail.addQuantity(soLuong, tongGia);
                    found = true;
                    break;
                }
            }

            if (!found) {
                orderDetails.add(new Table.OrderDetail(tenSanPham, soLuong, tongGia));
            }
        }
        cursor.close();
        return orderDetails;
    }

    // Phương thức để lấy tên sản phẩm
    private String getProductNameById(int idSanPham) {
        // Thực hiện truy vấn để lấy tên sản phẩm từ bảng san_pham
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT ten_san_pham FROM san_pham WHERE id_san_pham = ?", new String[]{String.valueOf(idSanPham)});
        String tenSanPham = null;
        if (cursor.moveToFirst()) {
            tenSanPham = cursor.getString(cursor.getColumnIndex("ten_san_pham"));
        }
        cursor.close();
        return tenSanPham;
    }



    //so luong san pham
    public int getAvailableQuantity(int productId) {
        int quantity = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT so_luong FROM san_pham WHERE id_san_pham = ?", new String[]{String.valueOf(productId)});

        if (cursor.moveToFirst()) {
            quantity = cursor.getInt(cursor.getColumnIndex("so_luong"));
        }
        cursor.close();
        return quantity;
    }



    public String getTableStatus(int idBan) {
        SQLiteDatabase db = this.getReadableDatabase();
        String status = null;

        // Truy vấn để lấy trạng thái bàn dựa trên idBan
        Cursor cursor = db.rawQuery("SELECT tinh_trang FROM ban WHERE id_ban = ?", new String[]{String.valueOf(idBan)});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                // Lấy trạng thái bàn từ cột tinh_trang
                status = cursor.getString(cursor.getColumnIndex("tinh_trang"));
            }
            cursor.close();
        }

        return status; // Trả về trạng thái bàn hoặc null nếu không tìm thấy
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if exist
        db.execSQL("DROP TABLE IF EXISTS account");
        db.execSQL("DROP TABLE IF EXISTS ban");
        db.execSQL("DROP TABLE IF EXISTS san_pham");
        db.execSQL("DROP TABLE IF EXISTS khach_hang");
        db.execSQL("DROP TABLE IF EXISTS hoadon");
        db.execSQL("DROP TABLE IF EXISTS thoigianchoi");
        onCreate(db);
    }
    //--------------------------------------------------------
    // ----- Check Login -----
    @SuppressLint("Range")
    public int checkLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT id_account FROM account WHERE username = ? AND password = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{username, password});

        int accountId = -1;
        if (cursor.moveToFirst()) {
            accountId = cursor.getInt(cursor.getColumnIndex("id_account"));
        }
        cursor.close();
        return accountId;
    }

    // Check if username already exists
    public boolean usernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM account WHERE username = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{username});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Add a new account
    public boolean addAccount(String username, String password, String role) {
        if (usernameExists(username)) {
            return false; // Username already exists
        }

        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "INSERT INTO account (username, password, role) VALUES (?, ?, ?)";
        SQLiteStatement statement = db.compileStatement(sql);
        statement.bindString(1, username);
        statement.bindString(2, password);
        statement.bindString(3, role);
        return statement.executeInsert() > 0;
    }
    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }


    public void updateTableStatus(int idBan, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Tạo nội dung để cập nhật
        ContentValues contentValues = new ContentValues();
        contentValues.put("tinh_trang", newStatus);

        // Cập nhật trạng thái bàn
        int rowsAffected = db.update("ban", contentValues, "id_ban = ?", new String[]{String.valueOf(idBan)});

        if (rowsAffected > 0) {
            Log.d("DB Update", "Cập nhật trạng thái bàn thành công!");
        } else {
            Log.d("DB Update", "Không có bản ghi nào bị ảnh hưởng. Kiểm tra ID bàn.");
        }

        db.close();
    }

    public void insertOrder(int idBan, int productId, int quantity, double totalPrice) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Thêm đơn hàng vào bảng chitiethoadon
        ContentValues values = new ContentValues();
        values.put("id_ban", idBan);
        values.put("id_sanpham", productId);
        values.put("so_luong", quantity);
        values.put("tong_gia", totalPrice);


        long result = db.insert("chitiethoadon", null, values);
        if (result == -1) {
            // Xử lý lỗi
            Log.e("Database Error", "Failed to insert order");
        } else {
            // Đơn hàng đã được thêm thành công
            Log.d("Database Success", "Order inserted with ID: " + result);
        }

        db.close();
    }


    public List<Product> getProductsByAccountId(int accountId) {
        List<Product> productList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query("san_pham",
                new String[]{"id_san_pham", "ten_san_pham", "so_luong", "gia"},
                "id_account = ?", new String[]{String.valueOf(accountId)},
                null, null, null);

        Log.d("DatabaseHelper", "Account ID: " + accountId + ", Số lượng sản phẩm: " + cursor.getCount());

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                int quantity = cursor.getInt(2);
                double price = cursor.getDouble(3);
                Product product = new Product(id, name, quantity, price);
                productList.add(product);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return productList;
    }




    // ----- Delete Table -----
    public boolean deleteTable(int tableId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "DELETE FROM ban WHERE id_ban = ?";
        SQLiteStatement statement = db.compileStatement(sql);
        statement.bindLong(1, tableId);
        return statement.executeUpdateDelete() > 0;
    }
    public void updateTable(int tableId, String tableName, String tableStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ten_ban", tableName);
        values.put("trang_thai", tableStatus);

        db.update("ban", values, "id_ban = ?", new String[]{String.valueOf(tableId)});
        db.close();
    }


    // ----- Add Product -----
    public boolean addProduct(int accountId, String productName, int quantity, double price, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id_account", accountId);
        contentValues.put("ten_san_pham", productName);
        contentValues.put("so_luong", quantity);
        contentValues.put("gia", price);
        contentValues.put("image_san_pham", imagePath);

        long result = db.insert("san_pham", null, contentValues);
        db.close();
        return result != -1; // Trả về true nếu thêm thành công
    }


    // ----- Update Product -----
    // ----- Update Product -----
    public boolean updateProduct(int id, String name, int quantity, double price, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ten_san_pham", name);
        values.put("so_luong", quantity);
        values.put("gia", price);
        values.put("image_san_pham", imagePath);

        long result = db.update("san_pham", values, "id_san_pham = ?", new String[]{String.valueOf(id)});
        return result != -1; // Trả về true nếu cập nhật thành công
    }


    // ----- Delete Product -----
    public boolean deleteProduct(int productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "DELETE FROM san_pham WHERE id_san_pham = ?";
        SQLiteStatement statement = db.compileStatement(sql);
        statement.bindLong(1, productId);
        return statement.executeUpdateDelete() > 0;
    }

    public List<Product> searchProducts(int accountId, String searchQuery) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM san_pham WHERE id_account = ? AND ten_san_pham LIKE ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(accountId), "%" + searchQuery + "%"});

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id_san_pham"));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("ten_san_pham"));
                @SuppressLint("Range") int quantity = cursor.getInt(cursor.getColumnIndex("so_luong"));
                @SuppressLint("Range") double price = cursor.getDouble(cursor.getColumnIndex("gia"));
                @SuppressLint("Range") String imagePath = cursor.getString(cursor.getColumnIndex("image_san_pham")); // Lấy đường dẫn hình ảnh

                products.add(new Product(id, name, quantity, price, imagePath)); // Thêm imagePath vào Product
            } while (cursor.moveToNext());
        }
        cursor.close();
        return products;
    }

    public boolean addTable(int accountId, String tableName, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_account", accountId);
        values.put("ten_ban", tableName);
        values.put("tinh_trang", status);

        // Thêm vào cơ sở dữ liệu và kiểm tra kết quả
        long result = db.insert("ban", null, values);  // 'banbida1' là tên bảng
        db.close();

        // Kiểm tra kết quả, nếu kết quả là -1 thì việc chèn thất bại
        return result != -1;
    }

    public List<Table> getTablesByAccountId(int accountId) {
        List<Table> tableList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM ban WHERE id_account = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(accountId)});

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_ban"));
                        String tableName = cursor.getString(cursor.getColumnIndexOrThrow("ten_ban"));
                        String status = cursor.getString(cursor.getColumnIndexOrThrow("tinh_trang"));

                        Table table = new Table(id, tableName, status);
                        tableList.add(table);
                    } while (cursor.moveToNext());
                } else {
                    Log.e("DatabaseHelper", "Cursor is empty");
                }
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Error reading data", e);
            } finally {
                cursor.close();
            }
        }
        db.close();
        return tableList;
    }
    public List<Customer> searchCustomers(String query, int accountId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM khach_hang WHERE (ten_khach LIKE ? OR dia_chi LIKE ? OR so_dien_thoai LIKE ?) AND id_account = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{"%" + query + "%", "%" + query + "%", "%" + query + "%", String.valueOf(accountId)});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex("id_khach_hang"));
                String name = cursor.getString(cursor.getColumnIndex("ten_khach"));
                String phone = cursor.getString(cursor.getColumnIndex("so_dien_thoai"));
                String address = cursor.getString(cursor.getColumnIndex("dia_chi"));
                customers.add(new Customer(id, name, phone, address));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return customers;
    }



    // ----- KHACH HANG------------------------------------------
    public List<Customer> getCustomersByPhoneNumber(String phoneNumber, int accountId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM khach_hang WHERE so_dien_thoai LIKE ? AND id_account = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{"%" + phoneNumber + "%", String.valueOf(accountId)});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex("id_khach_hang"));
                String name = cursor.getString(cursor.getColumnIndex("ten_khach"));
                String phone = cursor.getString(cursor.getColumnIndex("so_dien_thoai"));
                String address = cursor.getString(cursor.getColumnIndex("dia_chi"));
                customers.add(new Customer(id, name, phone, address));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return customers;
    }


    /// THOI GIAN CHOi

    public boolean isGameStarted(int idBan) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Kiểm tra nếu có bản ghi trong bảng thoigianchoi với id_ban và thoi_gian_ket_thuc là NULL
        Cursor cursor = db.query("thoigianchoi",
                new String[]{"id_thoigian"},
                "id_ban=? AND thoi_gian_ket_thuc IS NULL",
                new String[]{String.valueOf(idBan)},
                null, null, null);

        boolean isStarted = (cursor != null && cursor.getCount() > 0);

        if (cursor != null) {
            cursor.close();
        }
        return isStarted;
    }


//    public long insertThoiGianChoi(int idBan, String thoiGianBatDau) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put("id_ban", idBan);
//        values.put("thoi_gian_bat_dau", thoiGianBatDau);
//
//        long id = db.insert("thoigianchoi", null, values);
//        return id; // Phải trả về ID của bản ghi
//    }

    public String getStartTime(int tableId) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Truy vấn chỉ lấy bản ghi có thoi_gian_ket_thuc là null
        String query = "SELECT thoi_gian_bat_dau FROM thoigianchoi WHERE id_ban = ? AND thoi_gian_ket_thuc IS NULL ORDER BY thoi_gian_bat_dau DESC LIMIT 1";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(tableId)});

        if (cursor.moveToFirst()) {
            String startTime = cursor.getString(0);
            cursor.close();
            return startTime;
        }

        cursor.close();
        return null; // Không tìm thấy phiên nào đang diễn ra
    }
    public void endSession(int tableId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("thoi_gian_ket_thuc", getCurrentTime()); // Lưu thời gian hiện tại

        db.update("thoigianchoi", values, "id_ban = ? AND thoi_gian_ket_thuc IS NULL", new String[]{String.valueOf(tableId)});
    }



    public List<Customer> getCustomersByAccountId(int accountId) {
        List<Customer> customerList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM khach_hang WHERE id_account = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(accountId)});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex("id_khach_hang"));
                String name = cursor.getString(cursor.getColumnIndex("ten_khach"));
                String phoneNumber = cursor.getString(cursor.getColumnIndex("so_dien_thoai"));
                String address = cursor.getString(cursor.getColumnIndex("dia_chi"));

                // Tạo đối tượng Customer với bốn tham số
                Customer customer = new Customer(id, name, phoneNumber, address);
                customerList.add(customer);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return customerList;
    }
    public boolean addCustomer(int accountId, String customerName, String phoneNumber, String address) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "INSERT INTO khach_hang (id_account, ten_khach, so_dien_thoai, dia_chi) VALUES (?, ?, ?, ?)";
        SQLiteStatement statement = db.compileStatement(sql);
        statement.clearBindings();
        statement.bindLong(1, accountId);
        statement.bindString(2, customerName);
        statement.bindString(3, phoneNumber);
        statement.bindString(4, address);
        long result = statement.executeInsert();
        db.close(); // Đừng quên đóng cơ sở dữ liệu khi không sử dụng
        return result != -1;
    }

    // ----- Update Customer -----
    public boolean updateCustomer(int customerId, String customerName, String phoneNumber, String address) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "UPDATE khach_hang SET ten_khach = ?, so_dien_thoai = ?, dia_chi = ? WHERE id_khach_hang = ?";
        SQLiteStatement statement = db.compileStatement(sql);
        statement.clearBindings();
        statement.bindString(1, customerName);
        statement.bindString(2, phoneNumber);
        statement.bindString(3, address);
        statement.bindLong(4, customerId);
        int rowsAffected = statement.executeUpdateDelete();
        db.close(); // Đừng quên đóng cơ sở dữ liệu khi không sử dụng
        return rowsAffected > 0;
    }


    // ----- Delete Customer -----
    public boolean deleteCustomer(int customerId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "DELETE FROM khach_hang WHERE id_khach_hang = ?";
        SQLiteStatement statement = db.compileStatement(sql);
        statement.clearBindings();
        statement.bindLong(1, customerId);
        int rowsDeleted = statement.executeUpdateDelete();
        db.close(); // Đừng quên đóng cơ sở dữ liệu khi không sử dụng
        return rowsDeleted > 0;
    }


    // ----- Update Invoice -----
    public boolean updateInvoice(int invoiceId, double totalTime, double totalProducts, double total) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "UPDATE hoadon SET tong_tien_thoigian = ?, tong_tien_sanpham = ?, tong_tien = ? WHERE id_hoadon = ?";
        SQLiteStatement statement = db.compileStatement(sql);
        statement.bindDouble(1, totalTime);
        statement.bindDouble(2, totalProducts);
        statement.bindDouble(3, total);
        statement.bindLong(4, invoiceId);
        return statement.executeUpdateDelete() > 0;
    }

    // ----- Delete Invoice -----
    public boolean deleteInvoice(int invoiceId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "DELETE FROM hoadon WHERE id_hoadon = ?";
        SQLiteStatement statement = db.compileStatement(sql);
        statement.bindLong(1, invoiceId);
        return statement.executeUpdateDelete() > 0;
    }

    // ----- Add Time -----
    public boolean addTime(int tableId, int accountId, String startTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "INSERT INTO thoigianchoi (id_ban, id_account, thoi_gian_bat_dau) VALUES (?, ?, ?)";
        SQLiteStatement statement = db.compileStatement(sql);
        statement.bindLong(1, tableId);
        statement.bindLong(2, accountId);
        statement.bindString(3, startTime);
        return statement.executeInsert() > 0;
    }

    // ----- Update Time -----
    public boolean updateTime(int timeId, String endTime, int totalTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "UPDATE thoigianchoi SET thoi_gian_ket_thuc = ?, tong_thoi_gian = ? WHERE id_thoigian = ?";
        SQLiteStatement statement = db.compileStatement(sql);
        statement.bindString(1, endTime);
        statement.bindLong(2, totalTime);
        statement.bindLong(3, timeId);
        return statement.executeUpdateDelete() > 0;
    }

    // ----- Delete Time -----
    public boolean deleteTime(int timeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "DELETE FROM thoigianchoi WHERE id_thoigian = ?";
        SQLiteStatement statement = db.compileStatement(sql);
        statement.bindLong(1, timeId);
        return statement.executeUpdateDelete() > 0;
    }

    // ----- Get Invoice by Time ID -----
    public Cursor getInvoiceByTimeId(int timeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM hoadon WHERE id_thoigian = ?";
        return db.rawQuery(query, new String[]{String.valueOf(timeId)});
    }

    public int getTongThoiGian(int idBan) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT tong_thoi_gian FROM thoigianchoi WHERE id_ban = ?", new String[]{String.valueOf(idBan)});
        int tongThoiGian = 0;

        if (cursor.moveToFirst()) {
            tongThoiGian = cursor.getInt(cursor.getColumnIndex("tong_thoi_gian"));
        }
        cursor.close();
        return tongThoiGian;
    }



    public int insertHoaDon(int idAccount, int idBan, String thoiGianBatDau, String thoiGianKetThuc) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Tính tổng thời gian chơi từ bảng thoigianchoi
        int tongThoiGian = getTongThoiGian(idBan);
        double tongTienThoiGian = tongThoiGian * 20; // Tổng tiền thời gian = tổng thời gian chơi * 20

        // Tính tổng tiền sản phẩm trong phiên chơi
        double tongTienSanPham = getTotalProductCostForTable(idBan, thoiGianBatDau, thoiGianKetThuc);

        // Tính tổng tiền
        double tongTien = tongTienThoiGian + tongTienSanPham;

        // Lưu vào hóa đơn
        ContentValues values = new ContentValues();
        values.put("id_thoigian", idBan); // Giả sử idThoiGian là idBan ở đây
        values.put("id_account", idAccount);
        values.put("tong_tien_thoigian", tongTienThoiGian);
        values.put("tong_tien_sanpham", tongTienSanPham);
        values.put("tong_tien", tongTien);
        values.put("ngay_lap", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

        long id = db.insert("hoadon", null, values);
        return (int) id; // Trả về ID hóa đơn vừa được tạo
    }



    //HOA DON
    public int insertHoaDon(int idAccount, int idThoiGian, double tongTienThoiGian, double tongTienSanPham) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_thoigian", idThoiGian);
        values.put("id_account", idAccount);
        values.put("tong_tien_thoigian", tongTienThoiGian);
        values.put("tong_tien_sanpham", tongTienSanPham);
        values.put("tong_tien", tongTienThoiGian + tongTienSanPham);
        values.put("ngay_lap", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));


        long id = db.insert("hoadon", null, values);
        return (int) id; // Trả về ID hóa đơn vừa được tạo
    }

    public String getStartTimeHOADON(int idBan) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("thoigianchoi", new String[]{"thoi_gian_ket_thuc"}, "id_ban = ?", new String[]{String.valueOf(idBan)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String startTime = cursor.getString(0);
            cursor.close();
            return startTime;
        }
        return null;
    }

    public void resetStartTime(int idBan) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("thoi_gian_bat_dau", (String) null);
        values.put("thoi_gian_ket_thuc", (String) null);
        // Đặt giá trị của start_time là null
        db.update("ban", values, "id = ?", new String[]{String.valueOf(idBan)});
        db.close();
    }
    public void resetEndTime(int idBan) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("thoi_gian_ket_thuc", (String) null); // Đặt giá trị của start_time là null
        db.update("ban", values, "id = ?", new String[]{String.valueOf(idBan)});
        db.close();
    }
    public int insertHoaDon(int accountId, int tableId, double totalTimeCost, double productTotalCost, double totalAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("id_account", accountId);
        values.put("id_ban", tableId);
        values.put("tong_tien_thoigian", totalTimeCost);
        values.put("tong_tien_sanpham", productTotalCost);
        values.put("tong_tien", totalAmount);

        long id = db.insert("hoadon", null, values);
        db.close();
        return (int) id;
    }

    public int getThoiGianId(int idBan) {
        int idThoiGian = -1; // Giá trị mặc định nếu không tìm thấy
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id_thoigian FROM thoigianchoi WHERE id_ban = ? AND thoi_gian_ket_thuc IS NULL", new String[]{String.valueOf(idBan)});

        if (cursor.moveToFirst()) {
            idThoiGian = cursor.getInt(cursor.getColumnIndex("id_thoigian"));
        }

        cursor.close();
        return idThoiGian;
    }


    //HIỂN THỊ DANH SÁCH HÓA ĐƠN
    public Cursor getInvoicesForAccount(int accountId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM hoadon WHERE id_account = ?", new String[]{String.valueOf(accountId)});
    }


    //HIỂN THỊ DANH SÁCH HÓA ĐƠN

    // HIỂN THỊ DANH SÁCH HÓA ĐƠN

    public Cursor get_thong_ke_ForAccount(int accountId) {

            SQLiteDatabase db = this.getReadableDatabase();
            return db.rawQuery("SELECT * FROM hoadon WHERE id_account = ?", new String[]{String.valueOf(accountId)});
        }



//
//
//    public Cursor getInvoicesByDate(int accountId, String date) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        return db.rawQuery("SELECT * FROM hoadon WHERE id_account = ? AND strftime('%Y-%m-%d', ngay_lap) = ?", new String[]{String.valueOf(accountId), date});
//    }
//
//
//    public Cursor getInvoicesByMonth(int accountId, String monthYear) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        return db.rawQuery("SELECT * FROM hoadon WHERE id_account = ? AND strftime('%Y-%m', ngay_lap) = ?", new String[]{String.valueOf(accountId), monthYear});
//    }
//
//    public Cursor getInvoicesByYear(int accountId, String year) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        return db.rawQuery("SELECT * FROM hoadon WHERE id_account = ? AND strftime('%Y', ngay_lap) = ?", new String[]{String.valueOf(accountId), year});
//    }

    public Cursor getTotalInvoicesByYear(int accountId, String year) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT strftime('%Y', ngay_lap) AS year, " +
                        "SUM(tong_tien_thoigian) AS total_thoigian, " +
                        "SUM(tong_tien_sanpham) AS total_sanpham, " +
                        "SUM(tong_tien) AS total " +
                        "FROM hoadon WHERE id_account = ? AND strftime('%Y', ngay_lap) = ? " +
                        "GROUP BY year",
                new String[]{String.valueOf(accountId), year});
    }


//Tháng
    public Cursor getTotalInvoicesByMonth(int accountId, String monthYear) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT strftime('%Y-%m-%d', ngay_lap) AS date, " +
                        "SUM(tong_tien_thoigian) AS total_thoigian, " +
                        "SUM(tong_tien_sanpham) AS total_sanpham, " +
                        "SUM(tong_tien) AS total " +
                        "FROM hoadon WHERE id_account = ? AND strftime('%Y-%m', ngay_lap) = ? " +
                        "GROUP BY date ORDER BY date",
                new String[]{String.valueOf(accountId), monthYear});
    }


    //Ngày
    public Cursor getTotalInvoicesByDate(int accountId, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT SUM(tong_tien_thoigian) AS total_thoigian, " +
                        "SUM(tong_tien_sanpham) AS total_sanpham, " +
                        "SUM(tong_tien) AS total " +
                        "FROM hoadon " +
                        "WHERE id_account = ? AND strftime('%Y-%m-%d', ngay_lap) = ?",
                new String[]{String.valueOf(accountId), date});
    }





    public Cursor getTotalInvoicesByAllYears(int accountId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT strftime('%Y', ngay_lap) AS year, " +
                        "SUM(tong_tien_thoigian) AS total_thoigian, " +
                        "SUM(tong_tien_sanpham) AS total_sanpham, " +
                        "SUM(tong_tien) AS total " +
                        "FROM hoadon WHERE id_account = ? " +
                        "GROUP BY year ORDER BY year",
                new String[]{String.valueOf(accountId)});
    }

    public Cursor getTotalInvoicesByAllYears1(int accountId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT strftime('%Y-%m', ngay_lap) AS month_year, " + // Sửa tên alias cho đúng
                        "SUM(tong_tien_thoigian) AS total_thoigian, " +
                        "SUM(tong_tien_sanpham) AS total_sanpham, " +
                        "SUM(tong_tien) AS total " +
                        "FROM hoadon WHERE id_account = ? " +
                        "GROUP BY month_year ORDER BY month_year",
                new String[]{String.valueOf(accountId)});
    }





    public Cursor getTotalInvoicesByAllMonths(int accountId, int year) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT strftime('%Y-%m', ngay_lap) AS month_year, " +
                        "SUM(tong_tien_thoigian) AS total_thoigian, " +
                        "SUM(tong_tien_sanpham) AS total_sanpham, " +
                        "SUM(tong_tien) AS total " +
                        "FROM hoadon WHERE id_account = ? AND strftime('%Y', ngay_lap) = ? " +
                        "GROUP BY month_year ORDER BY month_year",
                new String[]{String.valueOf(accountId), String.valueOf(year)});
    }

}
