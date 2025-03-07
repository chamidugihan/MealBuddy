package lk.javainsitute.mealbuddy;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;

import lk.javainsitute.mealbuddy.model.AllProductAdapter;


public class ProductFragment extends Fragment {
    private RecyclerView recyclerView;
    private SearchView searchView;

    AllProductAdapter allProductAdapter;
    private ArrayList<Product> allproductArrayList = new ArrayList<>();
    private FirebaseFirestore db;
    String catrgory;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product, container, false);

        Spinner spinner = view.findViewById(R.id.spinner3);
        recyclerView = view.findViewById(R.id.allproduct);
        searchView = view.findViewById(R.id.searchView);
        Button button = view.findViewById(R.id.button2);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));


            db = FirebaseFirestore.getInstance();

            loadAllProducts();
            ArrayList<String> categoryList = new ArrayList<>();
            categoryList.add("Select");
            db.collection("category").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            categoryList.add(document.getString("name"));
                        }
                        CityAdapter adapter = new CityAdapter(getContext(), R.layout.city_tamplate, categoryList);
                        spinner.setAdapter(adapter);
                    }
                }
            });

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Query q = db.collection("product").whereEqualTo("status", "active");

                    // Apply category filter if selected
                    if (!catrgory.equals("Select")) {
                        q = q.whereEqualTo("category", catrgory);
                    }

                    q.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            allproductArrayList.clear();
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                Product product = new Product();
                                product.setId(document.getId());
                                product.setName(document.getString("name"));
                                product.setDescription(document.getString("description"));
                                product.setSprice(document.getString("sprice"));
                                product.setLprice(document.getString("lprice"));
                                product.setImage(document.getString("image"));
                                allproductArrayList.add(product);
                            }

                            // Update RecyclerView
                            updateRecyclerView(allproductArrayList);
                        }
                    }).addOnFailureListener(e -> {
                        Log.e("FirestoreError", "Error fetching filtered products: ", e);
                    });
                    ;
                }
            });

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    catrgory = categoryList.get(i);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });


            searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (!query.isEmpty()) {
                        searchProducts(query);
                    } else {
                        loadAllProducts(); // Load all if search is empty
                    }
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (newText.isEmpty()) {
                        loadAllProducts(); // Also load all products when clearing the text
                    }
                    return false; // Optional: live search while typing
                }
            });

        return view;
    }

    private void searchProducts(String query) {
        if (query.isEmpty()) {
            loadAllProducts(); // If the query is empty, load all products
        } else {
            db.collection("product")
                    .orderBy("name") // Ensure 'name' is indexed in Firestore
                    .startAt(query)
                    .endAt(query + "\uf8ff") // Match any text starting with the query string
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            ArrayList<Product> filteredList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Product product = new Product();
                                product.setId(document.getId());
                                product.setName(document.getString("name"));
                                product.setDescription(document.getString("description"));
                                product.setSprice(document.getString("sprice"));
                                product.setLprice(document.getString("lprice"));
                                product.setImage(document.getString("image"));
                                filteredList.add(product);
                            }

                            // Update RecyclerView with the filtered results
                            updateRecyclerView(filteredList);
                        } else {
                            Log.e("FirestoreError", "Error getting documents: ", task.getException());
                        }
                    });
        }
    }

    private void loadAllProducts() {

        db.collection("product")
                .whereEqualTo("status", "active")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Product> allList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = new Product();
                            product.setId(document.getId());
                            product.setName(document.getString("name"));
                            product.setDescription(document.getString("description"));
                            product.setSprice(document.getString("sprice"));
                            product.setLprice(document.getString("lprice"));
                            product.setImage(document.getString("image"));
                            allList.add(product);
                        }

                        // Update RecyclerView with all products
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

            allProductAdapter.setOnitemClickListener(new AllProductAdapter.OnitemClickListener() {
                @Override
                public void onClick(Product product) {
                    Intent intent = new Intent(getActivity(), SingleActivity.class);
                    String json = new Gson().toJson(product);
                    intent.putExtra("product", json);
                    startActivity(intent);
                    Toast.makeText(getActivity(), product.getName(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // If the adapter is already created, just update the data
            allProductAdapter.updateProductList(products);
        }
    }


}