package com.example.bidaapp.View.Activity;

import android.Manifest;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bidaapp.Controller.InvoiceAdapter;
import com.example.bidaapp.Model.DatabaseHelper;
import com.example.bidaapp.Model.Invoice;
import com.example.bidaapp.R;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.font.PdfFont;
import android.content.pm.PackageManager;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ManageInvoicesActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private InvoiceAdapter adapter;
    private List<Invoice> invoiceList;
    private DatabaseHelper dbHelper;
    private int accountId;
    private ImageView backBtn;
    private EditText editTextSearchTotal; // Đổi tên cho EditText tìm kiếm tổng tiền

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_manage_invoices, findViewById(R.id.content_frame));

        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        accountId = preferences.getInt("account_id", -1);
        backBtn = findViewById(R.id.backBtn);
        editTextSearchTotal = findViewById(R.id.editTextSearchTotal); // Sử dụng trường nhập tổng tiền
        recyclerView = findViewById(R.id.listViewCustomers);
        dbHelper = new DatabaseHelper(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        backBtn.setOnClickListener(v -> finish());

        invoiceList = getInvoicesFromDatabase(accountId);
        adapter = new InvoiceAdapter(this, invoiceList, this::onInvoiceClick);
        recyclerView.setAdapter(adapter);

        // Xử lý sự kiện nhấn nút tìm kiếm
        findViewById(R.id.buttonSearch).setOnClickListener(v -> {
            String totalAmountStr = editTextSearchTotal.getText().toString().trim(); // Lấy giá trị từ EditText tìm kiếm tổng tiền

            if (TextUtils.isEmpty(totalAmountStr)) {
                Toast.makeText(this, "Vui lòng nhập tổng tiền để tìm kiếm", Toast.LENGTH_SHORT).show();
                return;
            }

            // Xóa danh sách hiện tại
            invoiceList.clear();

            try {
                double totalAmount = Double.parseDouble(totalAmountStr);
                // Tìm hóa đơn theo tổng tiền
                List<Invoice> foundInvoices = dbHelper.getInvoicesByTotal(accountId, totalAmount);
                if (foundInvoices.isEmpty()) {
                    Toast.makeText(this, "Không tìm thấy hóa đơn cho tổng tiền này", Toast.LENGTH_SHORT).show();
                } else {
                    invoiceList.addAll(foundInvoices);
                }
                adapter.notifyDataSetChanged(); // Cập nhật adapter
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Giá trị nhập vào không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onInvoiceClick(Invoice invoice) {
        // Hiển thị hộp thoại để nhập tên file và đường dẫn cho PDF
        showExportDialog(invoice);
    }

    private void showExportDialog(Invoice invoice) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xuất Hóa Đơn");

        // Thiết lập input
        final EditText input = new EditText(this);
        input.setHint("Nhập tên file");
        input.setText("Invoice_" + invoice.getIdHoaDon() + ".pdf");
        builder.setView(input);

        // Thiết lập các nút
        builder.setPositiveButton("Xuất", (dialog, which) -> {
            String fileName = input.getText().toString();
            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + fileName;

            createPDF(invoice, filePath);
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void createPDF(Invoice invoice, String filePath) {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return;
        }

        try {
            PdfWriter writer = new PdfWriter(new FileOutputStream(filePath));
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            // Tạo font đậm
            PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");
            document.add(new Paragraph("Hóa Đơn Chi Tiết")
                    .setFontSize(18)
                    .setFont(boldFont));
            document.add(new Paragraph("\n"));

            // Thêm chi tiết hóa đơn vào PDF
            document.add(new Paragraph("ID Hóa Đơn: " + invoice.getIdHoaDon()));
            document.add(new Paragraph("Tổng Tiền Thời Gian: " + invoice.getTongTienThoiGian()));
            document.add(new Paragraph("Tổng Tiền Sản Phẩm: " + invoice.getTongTienSanPham()));
            document.add(new Paragraph("Tổng Tiền: " + invoice.getTongTien()));
            document.add(new Paragraph("Ngày Lập: " + invoice.getNgayLap()));
            document.add(new Paragraph("\n"));

            document.close();
            Toast.makeText(this, "PDF đã được tạo: " + filePath, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi tạo PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private List<Invoice> getInvoicesFromDatabase(int accountId) {
        List<Invoice> invoices = new ArrayList<>();
        Cursor cursor = dbHelper.getInvoicesForAccount(accountId);
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
}
