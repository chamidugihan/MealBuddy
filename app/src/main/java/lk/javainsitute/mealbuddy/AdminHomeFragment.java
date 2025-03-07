package lk.javainsitute.mealbuddy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;

public class AdminHomeFragment extends Fragment {

    private TextView usersize, userActive, userInActive;
    private TextView productsize, productActive, productInActive, productPending, productComplete;
    private FirebaseFirestore firestore;
    private PieChart pieChart1, pieChart2;

    public static AdminHomeFragment newInstance() {
        return new AdminHomeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);

        // Initialize Views
        TextView name = view.findViewById(R.id.admintest1);
        usersize = view.findViewById(R.id.admintest2);
        userActive = view.findViewById(R.id.admintest6);
        userInActive = view.findViewById(R.id.admintest7);
        productsize = view.findViewById(R.id.admintest3);
        productActive = view.findViewById(R.id.admintest8);
        productInActive = view.findViewById(R.id.admintest9);
        productPending = view.findViewById(R.id.admintest4);
        productComplete = view.findViewById(R.id.admintest5);
        Button logout = view.findViewById(R.id.button5);
        pieChart1 = view.findViewById(R.id.pieChart1);
        pieChart2 = view.findViewById(R.id.pieChart2);

        logout.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("lk.javainsitute.mealbuddy", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("admin");
            editor.apply();
            Intent intent = new Intent(getActivity(), LoginAcitivity.class);
            startActivity(intent);
            getActivity().finish();
        });

        // Load Admin Data
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("lk.javainsitute.mealbuddy", Context.MODE_PRIVATE);
        String adminjson = sharedPreferences.getString("admin", null);
        if (adminjson != null) {
            AdminBean admin = new Gson().fromJson(adminjson, AdminBean.class);
            name.setText(admin.getFname() + " " + admin.getLname());
        }

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Fetch Data from Firestore
        fetchUserData();
        fetchProductData();
        fetchOrderData();

        // Setup Pie Charts
        setupPieCharts();

        return view;
    }


    private void fetchUserData() {
        firestore.collection("user").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                usersize.setText("Error");
                userActive.setText("Error");
                userInActive.setText("Error");
                return;
            }

            int totalUsers = 0, activeUsers = 0, inactiveUsers = 0;

            for (QueryDocumentSnapshot doc : task.getResult()) {
                totalUsers++;
                String status = doc.getString("status");
                if ("active".equals(status)) {
                    activeUsers++;
                } else {
                    inactiveUsers++;
                }
            }

            usersize.setText(String.valueOf(totalUsers));
            userActive.setText(String.valueOf(activeUsers));
            userInActive.setText(String.valueOf(inactiveUsers));

            // Update User Activity Pie Chart
            updateUserPieChart(activeUsers, inactiveUsers);
        });
    }

    private void fetchProductData() {
        firestore.collection("product").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                productsize.setText("Error");
                productActive.setText("Error");
                productInActive.setText("Error");
                return;
            }

            int totalProducts = 0, activeProducts = 0, inactiveProducts = 0;

            for (QueryDocumentSnapshot doc : task.getResult()) {
                totalProducts++;
                String status = doc.getString("status");

                if ("active".equals(status)) {
                    activeProducts++;
                } else if ("inactive".equals(status)) {
                    inactiveProducts++;
                }
            }

            productsize.setText(String.valueOf(totalProducts));
            productActive.setText(String.valueOf(activeProducts));
            productInActive.setText(String.valueOf(inactiveProducts));
        });
    }

    private void fetchOrderData() {
        firestore.collection("order").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                productPending.setText("Error");
                productComplete.setText("Error");
                return;
            }

            int pendingOrders = 0, completedOrders = 0;

            for (QueryDocumentSnapshot doc : task.getResult()) {
                String orderStatus = doc.getString("status");
                if ("pending".equals(orderStatus)) {
                    pendingOrders++;
                } else if ("complete".equals(orderStatus)) {
                    completedOrders++;
                }
            }

            productPending.setText(String.valueOf(pendingOrders));
            productComplete.setText(String.valueOf(completedOrders));

            updateOrderPieChart(pendingOrders, completedOrders);
        });
    }

    private void setupPieCharts() {
        setupPieChart(pieChart1, "Order Status");
        setupPieChart(pieChart2, "User Activity");
    }

    private void setupPieChart(PieChart pieChart, String centerText) {
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(android.graphics.Color.WHITE);
        pieChart.setTransparentCircleRadius(50f);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setEntryLabelColor(android.graphics.Color.BLACK);
        pieChart.setCenterText(centerText);
        pieChart.setCenterTextSize(18f);
        pieChart.setDescription(null);
        pieChart.animateY(1000, Easing.EaseInOutCubic);
    }

    private void updateOrderPieChart(int pendingOrders, int completedOrders) {
        updatePieChart(pieChart1, pendingOrders, completedOrders, "Pending", "Complete", R.color.lb, R.color.db);
    }

    private void updateUserPieChart(int activeUsers, int inactiveUsers) {
        updatePieChart(pieChart2, activeUsers, inactiveUsers, "Active", "Inactive", R.color.green, R.color.red);
    }

    private void updatePieChart(PieChart pieChart, int value1, int value2, String label1, String label2, int color1, int color2) {
        ArrayList<PieEntry> pieEntryList = new ArrayList<>();
        pieEntryList.add(new PieEntry(value1, label1));
        pieEntryList.add(new PieEntry(value2, label2));

        PieDataSet pieDataSet = new PieDataSet(pieEntryList, label1 + " vs " + label2);
        ArrayList<Integer> colorArrayList = new ArrayList<>();
        colorArrayList.add(ContextCompat.getColor(requireContext(), color1));
        colorArrayList.add(ContextCompat.getColor(requireContext(), color2));
        pieDataSet.setColors(colorArrayList);

        PieData pieData = new PieData(pieDataSet);
        pieData.setValueTextSize(14f);
        pieData.setValueTextColor(Color.GRAY);

        pieChart.setData(pieData);
        pieChart.invalidate();
    }
}
