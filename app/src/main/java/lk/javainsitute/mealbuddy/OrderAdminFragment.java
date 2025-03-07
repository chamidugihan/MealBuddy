package lk.javainsitute.mealbuddy;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class OrderAdminFragment extends Fragment {
    private ArrayList<Order> orderList;
    private OrderTrakingAdapter orderTrakingAdapter;
    private RecyclerView recyclerView;

    public static OrderAdminFragment newInstance() {
        return new OrderAdminFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_admin, container, false);

        recyclerView = view.findViewById(R.id.ordertrakingrcv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        orderList = new ArrayList<>();
        orderTrakingAdapter = new OrderTrakingAdapter(orderList);
        recyclerView.setAdapter(orderTrakingAdapter);

        loadOrders();

        return view;
    }

    private void loadOrders() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("order")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        orderList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Order order = document.toObject(Order.class);

                            // Ensure the order has required fields before adding
                            if (order.getOrderId() != null && order.getStatus() != null) {
                                orderList.add(order);
                            }
                        }

                        // 🔥 Fix: Ensure RecyclerView updates correctly
                        requireActivity().runOnUiThread(() -> orderTrakingAdapter.notifyDataSetChanged());
                        orderTrakingAdapter.setOnItemClickListener(order -> showOrderStatusDialog(order));
                    } else {
                        Log.e("FirestoreError", "Error getting orders", task.getException());
                    }
                });
    }

    private void showOrderStatusDialog(Order order) {
        Gson gson = new Gson();
        String orderJson = gson.toJson(order);

        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View userStatus = layoutInflater.inflate(R.layout.orderstatustemplate, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(userStatus);
        AlertDialog orderStatusAlertDialog = builder.create();
        orderStatusAlertDialog.show();

        RadioButton pending = userStatus.findViewById(R.id.radioButton7);
        RadioButton cooking = userStatus.findViewById(R.id.radioButton8);
        RadioButton packing = userStatus.findViewById(R.id.radioButton9);
        RadioButton delivering = userStatus.findViewById(R.id.radioButton10);
        RadioButton complete = userStatus.findViewById(R.id.radioButton11);

        // Set the checked status
        switch (order.getStatus()) {
            case "pending":
                pending.setChecked(true);
                break;
            case "cooking":
                cooking.setChecked(true);
                break;
            case "packing":
                packing.setChecked(true);
                break;
            case "delivering":
                delivering.setChecked(true);
                break;
            case "complete":
                complete.setChecked(true);
                break;
        }

        Button updateButton = userStatus.findViewById(R.id.update2);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        updateButton.setOnClickListener(view -> {
            String newStatus = "";
            if (pending.isChecked()) newStatus = "pending";
            if (cooking.isChecked()) newStatus = "cooking";
            if (packing.isChecked()) newStatus = "packing";
            if (delivering.isChecked()) newStatus = "delivering";
            if (complete.isChecked()) newStatus = "complete";

            firestore.collection("order").document(order.getOrderId())
                    .update("status", newStatus)
                    .addOnSuccessListener(aVoid -> {
                        orderStatusAlertDialog.dismiss();
                        loadOrders(); // Refresh the list
                    })
                    .addOnFailureListener(e -> Log.e("FirestoreError", "Failed to update status", e));
        });
    }
}
class OrderTrakingAdapter extends RecyclerView.Adapter<OrderTrakingAdapter.OrderTrackingViewHolder> {
    private ArrayList<Order> userArrayList;
    private OnItemClickListener onItemClickListener;

    public OrderTrakingAdapter(ArrayList<Order> userArrayList) {
        this.userArrayList = userArrayList;
    }

    class OrderTrackingViewHolder extends RecyclerView.ViewHolder {
        TextView orderId, status, totalPrice, dateTime;

        public OrderTrackingViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.textView55);
            status = itemView.findViewById(R.id.textView56);
            totalPrice = itemView.findViewById(R.id.textView57);
            dateTime = itemView.findViewById(R.id.textView58);
        }
    }

    @NonNull
    @Override
    public OrderTrackingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adminordertemplate, parent, false);
        return new OrderTrackingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderTrackingViewHolder holder, int position) {
        Order order = userArrayList.get(position);

        // Prevent Null Values
        holder.orderId.setText(order.getOrderId() != null ? order.getOrderId() : "N/A");
        holder.status.setText(order.getStatus() != null ? order.getStatus() : "Unknown");
        holder.totalPrice.setText(String.format(Locale.getDefault(), "%.2f", order.getTotalPrice()));

        // 🔥 Fix: Proper date handling
        if (order.getTimestamp() != null) {
            Timestamp timestamp = order.getTimestamp();
            Date date = timestamp.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault());
            holder.dateTime.setText(sdf.format(date));
        } else {
            holder.dateTime.setText("N/A");
        }

        holder.itemView.setOnClickListener(view -> {
            if (onItemClickListener != null) {
                onItemClickListener.onClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userArrayList.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onClick(Order order);
    }
}