package com.example.bidaapp.Model;


public class Invoice {
    private int idHoaDon;
    private double tongTienThoiGian;
    private double tongTienSanPham;
    private double tongTien;
    private String ngayLap;

    public Invoice(int idHoaDon, double tongTienThoiGian, double tongTienSanPham, double tongTien, String ngayLap) {
        this.idHoaDon = idHoaDon;
        this.tongTienThoiGian = tongTienThoiGian;
        this.tongTienSanPham = tongTienSanPham;
        this.tongTien = tongTien;
        this.ngayLap = ngayLap;
    }

    // Getter methods
    public int getIdHoaDon() { return idHoaDon; }
    public double getTongTienThoiGian() { return tongTienThoiGian; }
    public double getTongTienSanPham() { return tongTienSanPham; }
    public double getTongTien() { return tongTien; }
    public String getNgayLap() { return ngayLap; }

}
