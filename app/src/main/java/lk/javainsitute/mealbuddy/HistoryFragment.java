package lk.javainsitute.mealbuddy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private HistoryAdapter orderAdapter;
    private ArrayList<Order> orderArrayList;
    private FirebaseFirestore firestore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestore = FirebaseFirestore.getInstance(); // Initialize Firestore
        orderArrayList = new ArrayList<>(); // Initialize list
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        recyclerView = view.findViewById(R.id.historyri);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        orderAdapter = new HistoryAdapter(orderArrayList);
        recyclerView.setAdapter(orderAdapter);

        loadOrders(); // Fetch orders
        return view;
    }

    private void loadOrders() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("lk.javainsitute.mealbuddy", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("user", null);
        UserBean user = gson.fromJson(json, UserBean.class);

        if (user == null || user.getId() == null) {
            Toast.makeText(getContext(), "User not found. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("order") // Ensure this matches your Firestore collection
                .whereEqualTo("userId", user.getId())
                .whereEqualTo("status", "complete")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            orderArrayList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Order order = document.toObject(Order.class);
                                orderArrayList.add(order);
                            }

                            // Sort orders by timestamp (latest first)
                            Collections.sort(orderArrayList, new Comparator<Order>() {
                                @Override
                                public int compare(Order o1, Order o2) {
                                    if (o1.getTimestamp() == null || o2.getTimestamp() == null) {
                                        return 0; // Avoid crash if timestamp is null
                                    }
                                    return o2.getTimestamp().compareTo(o1.getTimestamp());
                                }
                            });

                            if (orderArrayList.isEmpty()) {
                                Toast.makeText(getContext(), "No orders found.", Toast.LENGTH_SHORT).show();
                            }

                            orderAdapter.notifyDataSetChanged(); // Refresh RecyclerView
                        } else {
                            Log.e("FirestoreError", "Error getting orders: ", task.getException());
                        }
                    }
                });
    }
}
class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private ArrayList<Order> orderArrayList;

    public HistoryAdapter(ArrayList<Order> orderArrayList) {
        this.orderArrayList = orderArrayList;
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdText, timestampText, totalPriceText, statusText;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdText = itemView.findViewById(R.id.ordertext1);
            timestampText = itemView.findViewById(R.id.ordertext2);
            totalPriceText = itemView.findViewById(R.id.ordertest3);
            statusText = itemView.findViewById(R.id.ordertext4);
        }
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.order_template, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        Order order = orderArrayList.get(position);
        holder.orderIdText.setText(order.getOrderId());

        // Format timestamp correctly
        if (order.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.timestampText.setText(sdf.format(order.getTimestamp().toDate()));
        } else {
            holder.timestampText.setText("N/A");
        }

        holder.totalPriceText.setText("Rs. " + order.getTotalPrice());
        holder.statusText.setText(order.getStatus());
    }

    @Override
    public int getItemCount() {
        return orderArrayList.size();
    }
}