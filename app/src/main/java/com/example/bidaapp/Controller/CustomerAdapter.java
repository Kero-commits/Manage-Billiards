package com.example.bidaapp.Controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bidaapp.Model.DatabaseHelper;
import com.example.bidaapp.View.Layout.EditCustomerDialogFragment;
import com.example.bidaapp.View.Activity.ManageCustomersActivity;
import com.example.bidaapp.Model.Customer;
import com.example.bidaapp.R;

import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {

    private Context context;
    private List<Customer> customers;
    private DatabaseHelper databaseHelper;

    public CustomerAdapter(Context context, List<Customer> customers) {
        this.context = context;
        this.customers = customers;
        this.databaseHelper = new DatabaseHelper(context);
    }

    @Override
    public CustomerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate layout for each item
        View view = LayoutInflater.from(context).inflate(R.layout.item_customer, parent, false);
        return new CustomerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomerViewHolder holder, int position) {
        // Bind data to ViewHolder
        final Customer customer = customers.get(position);
        holder.nameTextView.setText(customer.getName());
        holder.phoneTextView.setText(customer.getPhoneNumber());
        holder.addressTextView.setText(customer.getAddress());

        // Handle click event for "Edit" button
        holder.btnEdit.setOnClickListener(v -> {
            EditCustomerDialogFragment dialog = new EditCustomerDialogFragment(customer);
            dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "EditCustomerDialog");
        });

        // Handle click event for "Delete" button
        holder.btnDelete.setOnClickListener(v -> {
            // Inflate custom layout for delete confirmation dialog
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_delete_customer, null);
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setView(dialogView)
                    .create();

            // Initialize buttons in the dialog
            Button btnYes = dialogView.findViewById(R.id.btnYes);
            Button btnNo = dialogView.findViewById(R.id.btnNo);

            // Handle click event for "Yes" button
            btnYes.setOnClickListener(view -> {
                // Delete customer from database
                databaseHelper.deleteCustomer(customer.getId());
                // Reload customer list after deletion
                ((ManageCustomersActivity) context).loadCustomers();
                dialog.dismiss(); // Dismiss dialog
            });

            // Handle click event for "No" button
            btnNo.setOnClickListener(view -> dialog.dismiss()); // Dismiss dialog

            // Show the dialog
            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return customers.size();
    }

    // ViewHolder class for RecyclerView
    public static class CustomerViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView phoneTextView;
        public TextView addressTextView;
        public AppCompatImageButton btnEdit;
        public AppCompatImageButton btnDelete;

        public CustomerViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            phoneTextView = itemView.findViewById(R.id.phoneTextView);
            addressTextView = itemView.findViewById(R.id.addressTextView);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
