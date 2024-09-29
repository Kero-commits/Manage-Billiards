package com.example.bidaapp.Model;

public class Product {
    private int id; // ID sản phẩm
    private String name; // Tên sản phẩm
    private int quantity; // Số lượng sản phẩm
    private double price; // Giá sản phẩm
    private String imagePath; // Đường dẫn hình ảnh sản phẩm

    // Hàm khởi tạo không tham số
    public Product() {
        this.id = 0;
        this.name = "";
        this.quantity = 0;
        this.price = 0.0;
        this.imagePath = ""; // Đường dẫn hình ảnh mặc định
    }
    // Phương thức để cập nhật thông tin sản phẩm
    public void updateProduct(String name, int quantity, double price, String imagePath) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.imagePath = imagePath; // Cập nhật đường dẫn hình ảnh
    }
    // Hàm khởi tạo với tham số
    public Product(int id, String name, int quantity, double price) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.imagePath = ""; // Đặt giá trị mặc định cho imagePath
    }

    public Product(int id, String name, int quantity, double price, String imagePath) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.imagePath = imagePath; // Khởi tạo đường dẫn hình ảnh
    }

    // Getter và Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    @Override
    public String toString() {
        return name;
    }
}
