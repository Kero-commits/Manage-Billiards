package com.example.bidaapp.View.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bidaapp.Controller.ThongKeAdapter;
import com.example.bidaapp.Model.DatabaseHelper;
import com.example.bidaapp.Model.Invoice;
import com.example.bidaapp.R;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Manage_ThongKe_Activity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ThongKeAdapter adapter;
    private List<Invoice> invoiceList;
    private DatabaseHelper dbHelper;
    private int accountId;
    private Button  btnSelectDate, btnMonth, btnYear;
    private Spinner sapXepSpinner;
    private ImageView backBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_activity_manage_thongke);

        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        accountId = preferences.getInt("account_id", -1);

        recyclerView = findViewById(R.id.list_Thong_ke);
        dbHelper = new DatabaseHelper(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> finish());
        invoiceList = getInvoicesFromDatabase(accountId);
        adapter = new ThongKeAdapter(this, invoiceList);
        recyclerView.setAdapter(adapter);



        btnSelectDate = findViewById(R.id.select_Date);
        btnMonth = findViewById(R.id.btnMonth);
        btnYear = findViewById(R.id.btnYear);
        sapXepSpinner = findViewById(R.id.Sap_Xep);

        setupSortingSpinner();
        setupButtonListeners();
    }

    private List<Invoice> getInvoicesFromDatabase(int accountId) {
        List<Invoice> invoices = new ArrayList<>();
        Cursor cursor = dbHelper.get_thong_ke_ForAccount(accountId);
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

    private void setupSortingSpinner() {
        List<String> sapXepOptions = new ArrayList<>();
        sapXepOptions.add("Cao tới thấp");
        sapXepOptions.add("Thấp tới cao");

        ArrayAdapter<String> sapXepAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, sapXepOptions);
        sapXepAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sapXepSpinner.setAdapter(sapXepAdapter);

        sapXepSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = parent.getItemAtPosition(position).toString();
                if (selectedOption.equals("Cao tới thấp")) {
                    adapter.sortInvoices(false);
                } else if (selectedOption.equals("Thấp tới cao")) {
                    adapter.sortInvoices(true);
                }
                Toast.makeText(Manage_ThongKe_Activity.this, "Chọn: " + selectedOption, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupButtonListeners() {

        btnSelectDate.setOnClickListener(v -> showDatePickerDialog());
        btnMonth.setOnClickListener(v -> showMonthYearPickerDialog());
        btnYear.setOnClickListener(v -> showYearPickerDialog());
    }

    private void createPDF() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return;
        }

        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/Invoices.pdf";
        try {
            PdfWriter writer = new PdfWriter(new FileOutputStream(filePath));
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");
            document.add(new Paragraph("Danh Sách Hóa Đơn").setFontSize(18).setFont(boldFont));
            document.add(new Paragraph("\n"));

            for (Invoice invoice : invoiceList) {
                document.add(new Paragraph("ID Hóa Đơn: " + invoice.getIdHoaDon()));
                document.add(new Paragraph("Tổng Tiền: " + invoice.getTongTien()));
                document.add(new Paragraph("Ngày Lập: " + invoice.getNgayLap()));
                document.add(new Paragraph("\n"));
            }

            document.close();
            Toast.makeText(this, "PDF đã được tạo: " + filePath, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi tạo PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    Toast.makeText(this, "Ngày đã chọn: " + selectedDate, Toast.LENGTH_SHORT).show();
                    updateInvoicesDisplay(selectedDate, "date");
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showMonthYearPickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.a_dialog_month_year_picker, null);

        Spinner monthSpinner = view.findViewById(R.id.monthSpinner);
        Spinner yearSpinner = view.findViewById(R.id.yearSpinner);

        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this,
                R.array.months_array, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);

        List<Integer> years = new ArrayList<>();
        for (int i = 2020; i <= 2030; i++) {
            years.add(i);
        }

        ArrayAdapter<Integer> yearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        builder.setView(view)
                .setTitle("Chọn tháng và năm")
                .setPositiveButton("OK", (dialog, which) -> {
                    int selectedMonth = monthSpinner.getSelectedItemPosition(); // Vị trí tháng
                    int selectedYear = (Integer) yearSpinner.getSelectedItem();

                    // Chuyển đổi tháng về định dạng phù hợp
                    if (selectedMonth == 0) { // Nếu chọn "Tháng ALL"
                        updateInvoicesDisplay(String.valueOf(selectedYear), "all_months");
                        Toast.makeText(this, "Tất cả tháng của năm: " + selectedYear, Toast.LENGTH_SHORT).show();
                    } else {
                        String monthYear = String.format("%04d-%02d", selectedYear, selectedMonth);
                        updateInvoicesDisplay(monthYear, "month");
                        Toast.makeText(this, "Tháng/Năm đã chọn: " + monthYear, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void showYearPickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.a_dialog_year_picker, null);

        Spinner yearSpinner = view.findViewById(R.id.yearSpinner);
        List<String> years = new ArrayList<>();
        years.add("Tất cả năm"); // Thêm tùy chọn "Tất cả năm"
        for (int i = 2020; i <= 2030; i++) {
            years.add(String.valueOf(i));
        }

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        builder.setView(view)
                .setTitle("Chọn năm")
                .setPositiveButton("OK", (dialog, which) -> {
                    String selectedYear = (String) yearSpinner.getSelectedItem();
                    Toast.makeText(this, "Năm đã chọn: " + selectedYear, Toast.LENGTH_SHORT).show();

                    if (selectedYear.equals("Tất cả năm")) {
                        updateInvoicesDisplay("", "all_years");
                    } else {
                        updateInvoicesDisplay(selectedYear, "year");
                    }
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }
    private void updateInvoicesDisplay(String value, String type) {
        List<Invoice> invoices = new ArrayList<>();
        Cursor cursor;

        switch (type) {
            case "date":
                cursor = dbHelper.getTotalInvoicesByDate(accountId, value);
                break;
            case "month":
                cursor = dbHelper.getTotalInvoicesByMonth(accountId, value);
                break;
            case "year":
                cursor = dbHelper.getTotalInvoicesByYear(accountId, value); // Gọi phương thức lấy dữ liệu theo năm
                break;
            case "all_months":
                cursor = dbHelper.getTotalInvoicesByAllMonths(accountId, Integer.parseInt(value)); // Gọi đúng phương thức
                break;
            case "all_years":
                cursor = dbHelper.getTotalInvoicesByAllYears(accountId);
                break;
            default:
                return;
        }

        if (cursor.moveToFirst()) {
            do {
                double totalThoiGian = cursor.getDouble(cursor.getColumnIndex("total_thoigian"));
                double totalSanPham = cursor.getDouble(cursor.getColumnIndex("total_sanpham"));
                double total = cursor.getDouble(cursor.getColumnIndex("total"));
                String displayDate;

                switch (type) {
                    case "date":
                        displayDate = value; // Hiển thị ngày cụ thể
                        break;
                    case "month":
                        displayDate = value; // Hiển thị tháng
                        break;
                    case "year":
                        displayDate = value; // Hiển thị năm
                        break;
                    case "all_months":
                        displayDate = cursor.getString(cursor.getColumnIndex("month_year")); // Lấy month_year
                        break;
                    case "all_years":
                        displayDate = cursor.getString(cursor.getColumnIndex("year")); // Hiển thị năm từ cursor
                        break;
                    default:
                        displayDate = ""; // Trường hợp không hợp lệ
                        break;
                }

                invoices.add(new Invoice(0, totalThoiGian, totalSanPham, total, displayDate));
            } while (cursor.moveToNext());
        }
        cursor.close();

        adapter.updateData(invoices); // Cập nhật dữ liệu mới
    }

}
