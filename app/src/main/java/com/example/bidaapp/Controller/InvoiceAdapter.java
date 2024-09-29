package com.example.bidaapp.Controller;

import java.util.Comparator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bidaapp.Model.Invoice;
import com.example.bidaapp.R;
import java.util.List;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder> {
    private final Context context;
    private List<Invoice> invoices;
    private OnInvoiceClickListener onInvoiceClickListener;

    public interface OnInvoiceClickListener {
        void onInvoiceClick(Invoice invoice);
    }

    public InvoiceAdapter(Context context, List<Invoice> invoices, OnInvoiceClickListener listener) {
        this.context = context;
        this.invoices = invoices;
        this.onInvoiceClickListener = listener;
    }

    public static class InvoiceViewHolder extends RecyclerView.ViewHolder {
        public TextView idTextView;
        public TextView totalTimeTextView;
        public TextView totalProductTextView;
        public TextView totalTextView;
        public TextView dateTextView;

        public InvoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            idTextView = itemView.findViewById(R.id.id_invoice_text_view);
            totalTimeTextView = itemView.findViewById(R.id.total_time_text_view);
            totalProductTextView = itemView.findViewById(R.id.total_product_text_view);
            totalTextView = itemView.findViewById(R.id.total_text_view);
            dateTextView = itemView.findViewById(R.id.invoice_date_text_view);
        }
    }

    @NonNull
    @Override
    public InvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_invoice, parent, false);
        return new InvoiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceViewHolder holder, int position) {
        Invoice invoice = invoices.get(position);

        holder.idTextView.setText("" + invoice.getIdHoaDon());
        holder.totalTimeTextView.setText(String.valueOf(invoice.getTongTienThoiGian()));
        holder.totalProductTextView.setText(String.valueOf(invoice.getTongTienSanPham()));
        holder.totalTextView.setText(String.valueOf(invoice.getTongTien()));
        holder.dateTextView.setText(invoice.getNgayLap());

        holder.itemView.setOnClickListener(v -> {
            if (onInvoiceClickListener != null) {
                onInvoiceClickListener.onInvoiceClick(invoice);
            }
        });
    }

    public void sortInvoices(boolean ascending) {
        if (ascending) {
            invoices.sort(Comparator.comparingDouble(Invoice::getTongTien));
        } else {
            invoices.sort(Comparator.comparingDouble(Invoice::getTongTien).reversed());
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return invoices.size();
    }

    // Method to update data in the adapter
    public void updateData(List<Invoice> newInvoices) {
        this.invoices.clear();
        this.invoices.addAll(newInvoices);
        notifyDataSetChanged(); // Notify the adapter to refresh the RecyclerView
    }
}
