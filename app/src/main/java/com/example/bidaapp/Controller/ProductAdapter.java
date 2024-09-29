package com.example.bidaapp.Controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.bidaapp.View.Layout.Dialog_delete_product;
import com.example.bidaapp.Model.DatabaseHelper;
import com.example.bidaapp.Model.Product;
import com.example.bidaapp.R;
import com.example.bidaapp.View.Layout.EditProductDialogFragment;
import java.util.List;
import android.widget.ImageView;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products;
    private DatabaseHelper databaseHelper;
    private Context context;

    public ProductAdapter(Context context, List<Product> products) {
        this.context = context;
        this.products = products;
        this.databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);

        holder.textViewProductName.setText(product.getName());
        holder.textViewQuantity.setText(String.valueOf(product.getQuantity()));
        holder.textViewPrice.setText(String.valueOf(product.getPrice()));

        // Hiển thị hình ảnh bằng Glide
        Glide.with(context)
                .load(product.getImagePath()) // Sử dụng trực tiếp đường dẫn từ Product
                .placeholder(R.drawable.coca) // Placeholder
                .error(R.drawable.status_dot_yellow) // Hình ảnh lỗi
                .into(holder.imageViewProduct);

        // Xử lý sự kiện edit và delete
        holder.btnEdit.setOnClickListener(v -> {
            EditProductDialogFragment dialog = new EditProductDialogFragment(
                    product.getId(),
                    product.getName(),
                    product.getQuantity(),
                    product.getPrice(),
                    product.getImagePath()
            );
            dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "EditProductDialog");
        });



        holder.btnDelete.setOnClickListener(v -> {
            Dialog_delete_product confirmDeleteDialog = new Dialog_delete_product(context, new Dialog_delete_product.DeleteConfirmationListener() {
                @Override
                public void onDeleteConfirmed() {
                    boolean result = databaseHelper.deleteProduct(product.getId());
                    if (result) {
                        Toast.makeText(context, "Product deleted", Toast.LENGTH_SHORT).show();
                        products.remove(position);
                        notifyItemRemoved(position);
                    } else {
                        Toast.makeText(context, "Error deleting product", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            confirmDeleteDialog.show();
        });

    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView textViewProductName, textViewQuantity, textViewPrice;
        AppCompatImageButton btnEdit, btnDelete;
        ImageView imageViewProduct;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
        }
    }
}
