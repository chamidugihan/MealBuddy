package lk.javainsitute.mealbuddy.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.google.gson.Gson;

import java.util.ArrayList;

import lk.javainsitute.mealbuddy.AdminAddProductActivity;
import lk.javainsitute.mealbuddy.AdminProductUpdateActivity;
import lk.javainsitute.mealbuddy.model.AllProductAdapter;
import lk.javainsitute.mealbuddy.Product;
import lk.javainsitute.mealbuddy.R;
import lk.javainsitute.mealbuddy.databinding.FragmentDashboardBinding;

public class DashboardFragment extends Fragment {
    private RecyclerView recyclerView;
    private SearchView searchView;
    private AllProductAdapter allProductAdapter;
    private FirebaseFirestore db;
    private FragmentDashboardBinding binding;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = root.findViewById(R.id.adminproductrey);
        searchView = root.findViewById(R.id.searchView2);
        Button addproduct = root.findViewById(R.id.addproductadmin);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        db = FirebaseFirestore.getInstance();

        addproduct.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), AdminAddProductActivity.class);
            startActivity(intent);
        });

        loadAllProducts(); // Load products when the fragment starts

        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    searchProducts(query);
                } else {
                    loadAllProducts();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    loadAllProducts();
                }
                return false;
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAllProducts(); // Refresh product list when fragment resumes
    }

    private void searchProducts(String query) {
        db.collection("product")
                .orderBy("name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Product> filteredList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            product.setId(document.getId());
                            filteredList.add(product);
                        }
                        updateRecyclerView(filteredList);
                    } else {
                        Log.e("FirestoreError", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void loadAllProducts() {
        db.collection("product")
                .whereEqualTo("status", "active")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Product> allList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            product.setId(document.getId());
                            allList.add(product);
                        }
                        updateRecyclerView(allList);
                    } else {
                        Log.e("FirestoreError", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void updateRecyclerView(ArrayList<Product> products) {
        if (allProductAdapter == null) {
            allProductAdapter = new AllProductAdapter(products);
            recyclerView.setAdapter(allProductAdapter);

            allProductAdapter.setOnitemClickListener(product -> {
                Intent intent = new Intent(getActivity(), AdminProductUpdateActivity.class);
                String json = new Gson().toJson(product);
                intent.putExtra("product", json);
                startActivity(intent);
            });
        } else {
            allProductAdapter.updateProductList(products);
            allProductAdapter.notifyDataSetChanged();
        }
    }
}
