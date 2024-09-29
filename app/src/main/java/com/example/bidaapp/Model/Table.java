package com.example.bidaapp.Model;

public class Table {
    private int id;
    private String tableName;
    private String status;

    private int accountId;

    public Table(int id, String tableName, String status) {
        this.id = id;
        this.tableName = tableName;
        this.status = status;
    }
    public int getAccountId() {
        return accountId;
    }
    public int getId() {
        return id;
    }

    public String getTableName() {
        return tableName;
    }

    public String getStatus() {
        return status;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public static class OrderDetail {
        private String tenSanPham;
        private int soLuong;
        private double tongGia;

        public OrderDetail(String tenSanPham, int soLuong, double tongGia) {
            this.tenSanPham = tenSanPham;
            this.soLuong = soLuong;
            this.tongGia = tongGia;
        }

        public String getTenSanPham() {
            return tenSanPham;
        }

        public int getSoLuong() {
            return soLuong;
        }

        public double getTongGia() {
            return tongGia;
        }

        public void addQuantity(int quantity, double price) {
            this.soLuong += quantity;
            this.tongGia += price;
        }
    }
}
