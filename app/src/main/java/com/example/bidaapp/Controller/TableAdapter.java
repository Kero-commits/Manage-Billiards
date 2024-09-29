package com.example.bidaapp.Controller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;

import com.example.bidaapp.Model.DatabaseHelper;
import com.example.bidaapp.Model.Table;
import com.example.bidaapp.View.Activity.OrderActivity;
import com.example.bidaapp.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TableAdapter extends RecyclerView.Adapter<TableAdapter.TableViewHolder> {
    private Context context;
    private List<Table> tableList;
    private DatabaseHelper dbHelper;
    private String thoiGianBatDau;
    public TableAdapter(Context context, List<Table> tableList) {
        this.context = context;
        this.tableList = tableList;
        this.dbHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public TableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_table, parent, false);
        return new TableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TableViewHolder holder, int position) {
        Table table = tableList.get(position);
        holder.bind(table);
    }

    @Override
    public int getItemCount() {
        return tableList.size();
    }

    class TableViewHolder extends RecyclerView.ViewHolder {
        TextView tableNameTextView;
        View statusDot;
        Button actionButton;
        Button deleteButton;

        public TableViewHolder(@NonNull View itemView) {
            super(itemView);
            tableNameTextView = itemView.findViewById(R.id.tableName);
            statusDot = itemView.findViewById(R.id.statusDot);
            actionButton = itemView.findViewById(R.id.action_button);
            deleteButton = itemView.findViewById(R.id.delete_button);

        }
        public void bind(Table table) {
            tableNameTextView.setText(table.getTableName());

            if ("Có khách".equals(table.getStatus())) {
                statusDot.setBackgroundResource(R.drawable.status_dot_yellow);
                actionButton.setText("Chi tiết");
                actionButton.setOnClickListener(v -> showTableDetails(table));
                deleteButton.setVisibility(View.GONE); // Ẩn nút xóa
            } else {
                statusDot.setBackgroundResource(R.drawable.status_dot);
                actionButton.setText("Bắt đầu");
                actionButton.setOnClickListener(v -> startNewSession(table));
                deleteButton.setVisibility(View.VISIBLE); // Hiện nút xóa
            }
        }


        private void showTableDetails(Table table) {
            // Lấy thông tin chi tiết về bàn
            String tableName = table.getTableName();
            String tableStatus = table.getStatus();

            // Lấy thời gian bắt đầu từ cơ sở dữ liệu
            String startTime = dbHelper.getStartTime(table.getId());

            // Cập nhật thoiGianBatDau
            if (startTime != null && !startTime.isEmpty()) {
                thoiGianBatDau = startTime; // Cập nhật thoiGianBatDau
            }

            // Tạo nội dung cho hộp thoại
            String message = "Tên bàn: " + tableName + "\n" +
                    "Trạng thái: " + tableStatus + "\n" +
                    "Thời gian bắt đầu: " + (startTime != null && !startTime.isEmpty() ? startTime : "Chưa bắt đầu");

            // Tạo hộp thoại hiển thị chi tiết
            new AlertDialog.Builder(context)
                    .setTitle("Chi tiết bàn")
                    .setMessage(message)
                    .setPositiveButton("Xem thêm", (dialog, which) -> {
                        dialog.dismiss();
                        // Khởi chạy OrderActivity sau khi đóng hộp thoại
                        Intent intent = new Intent(context, OrderActivity.class);
                        intent.putExtra("id_ban", table.getId()); // Gửi ID bàn vào OrderActivity
                        intent.putExtra("table_name", table.getTableName());
                        intent.putExtra("table_status", table.getStatus());
                        intent.putExtra("thoi_gian_bat_dau", thoiGianBatDau); // Gửi thời gian bắt đầu vào OrderActivity
                        context.startActivity(intent);
                    })
                    .show();
        }

    private void startNewSession(Table table) {
        String thoiGianBatDau = getCurrentTime();




        // Lấy id_account từ SharedPreferences
        SharedPreferences preferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        int idAccount = preferences.getInt("account_id", -1);

        // Kiểm tra xem idAccount có hợp lệ không
        if (idAccount == -1) {
            Log.e("StartNewSession", "ID Account không hợp lệ: " + idAccount);
            Toast.makeText(context, "ID tài khoản không hợp lệ", Toast.LENGTH_SHORT).show();
            return; // Dừng nếu không hợp lệ
        }

        long id_thoigian = dbHelper.insertThoiGianChoi(table.getId(), idAccount, thoiGianBatDau);

        if (id_thoigian != -1) {
            Log.d("StartNewSession", "ID Thoi Gian: " + id_thoigian);
        } else {
            Log.e("StartNewSession", "Không thể lưu thời gian chơi");
        }

        dbHelper.updateTableStatus(table.getId(), "Có khách");
        table.setStatus("Có khách");

        Toast.makeText(context, "Bắt đầu phiên chơi thành công!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(context, OrderActivity.class);
        intent.putExtra("id_ban", table.getId());
        intent.putExtra("thoi_gian_bat_dau", thoiGianBatDau);
        context.startActivity(intent);
    }

    private void startSession(int idBan) {
        if (dbHelper.isTableOccupied(idBan)) {
            Toast.makeText(context, "Bàn này đang được sử dụng!", Toast.LENGTH_SHORT).show();
            return; // Không cho phép bắt đầu phiên chơi
        }

        // Nếu bàn không bị chiếm, tiếp tục bắt đầu phiên chơi
        String thoiGianBatDau = getCurrentTime();
        dbHelper.insertStartTime(idBan, thoiGianBatDau); // Ghi thời gian bắt đầu vào cơ sở dữ liệu
        Toast.makeText(context, "Bắt đầu phiên chơi thành công!", Toast.LENGTH_SHORT).show();
    }

    private void confirmDeleteTable(Table table) {
        new AlertDialog.Builder(context)
                .setTitle("Xóa bàn")
                .setMessage("Bạn có chắc chắn muốn xóa bàn này không?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteTable(table))
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteTable(Table table) {
        boolean success = dbHelper.deleteTable(table.getId());
        if (success) {
            tableList.remove(table);
            notifyDataSetChanged();
            Toast.makeText(context, "Xóa bàn thành công!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Xóa bàn thất bại!", Toast.LENGTH_SHORT).show();
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

}}
