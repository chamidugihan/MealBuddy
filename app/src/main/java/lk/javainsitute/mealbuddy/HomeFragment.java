package lk.javainsitute.mealbuddy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codebyashish.autoimageslider.AutoImageSlider;
import com.codebyashish.autoimageslider.Enums.ImageAnimationTypes;
import com.codebyashish.autoimageslider.Models.ImageSlidesModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;


public class HomeFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        AutoImageSlider autoImageSlider = view.findViewById(R.id.autoImageSlider);
        Button logout = view.findViewById(R.id.Homefagmentbutton1);
        Button button = view.findViewById(R.id.button42);
        ArrayList<ImageSlidesModel> autoImageList = new ArrayList<>();
        autoImageList.add(new ImageSlidesModel(R.drawable.images));
       // autoImageList.add(new ImageSlidesModel("https://cdn.pixabay.com/photo/2015/04/23/22/00/new-year-background-736885_1280.jpg"));
        autoImageList.add(new ImageSlidesModel(R.drawable.pizza1));
        autoImageSlider.setImageList(autoImageList);
//        autoImageSlider.setDefaultAnimation();
        autoImageSlider.setSlideAnimation(ImageAnimationTypes.BACKGROUND_TO_FOREGROUND);
        RecyclerView recyclerView = view.findViewById(R.id.itemview1);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
        ArrayList<Product> productArrayList = new ArrayList<>();
        FirebaseFirestore bd = FirebaseFirestore.getInstance();
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("lk.javainsitute.mealbuddy", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("user");
                editor.apply();
                Intent intent = new Intent(getActivity(), LoginAcitivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                getActivity().startActivity(intent);
                Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            }
        });
        bd.collection("product")
                .whereEqualTo("status", "active")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            productArrayList.clear(); // Clear old data to prevent duplication
                            ArrayList<Product> tempList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.i("TAG", document.getId() + " => " + document.getData());
                                Product productClass = new Product();
                                productClass.setId(document.getId());
                                productClass.setName(document.getString("name"));
                                productClass.setDescription(document.getString("description"));
                                productClass.setSprice(document.getString("sprice"));
                                productClass.setLprice(document.getString("lprice"));
                                productClass.setImage(document.getString("image"));
                                productClass.setDatetime(document.getTimestamp("datetime"));
                                tempList.add(productClass);
                            }
// Sort manually by date_time descending
                            Collections.sort(tempList, (a, b) -> b.getDatetime().compareTo(a.getDatetime()));

                            // Limit to the latest 4 products
                            productArrayList.addAll(tempList.subList(0, Math.min(tempList.size(), 4)));
                            // Initialize the adapter and set click listener BEFORE setting it to RecyclerView
                            ProductAdapter productAdapter = new ProductAdapter(productArrayList);
                            productAdapter.setOnitemClickListener(new ProductAdapter.OnitemClickListener() {
                                @Override
                                public void onClick(Product c) {
                                    Intent i = new Intent(getActivity(),SingleActivity.class);
                                    Gson gson = new Gson();
                                    String json = gson.toJson(c);
                                    i.putExtra("product",json);
                                    startActivity(i);
                                    Toast.makeText(getActivity(), c.getName(), Toast.LENGTH_SHORT).show();

                                }
                            });

                            recyclerView.setAdapter(productAdapter); // Set adapter AFTER setting click listener
                        } else {
                            Log.e("FirestoreError", "Error getting documents: ", task.getException());
                        }
                    }
                });
    button.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.homefagment, new ProductFragment()); // Change to your container ID
            transaction.addToBackStack(null); // Allows back navigation
            transaction.commit();
        }
    });
        return  view;
    }
}
class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.UserViewHolder>{
    OnitemClickListener onitemClickListener;
    class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        TextView textView2;
        ImageView imageView;


        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView9);
            textView2 = itemView.findViewById(R.id.textView12);
            imageView = itemView.findViewById(R.id.imageView5);
        }
    }

    public ArrayList<Product> userArrayList;

    public ProductAdapter(ArrayList<Product> userArrayList){
        this.userArrayList = userArrayList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.product_template, parent, false);
        UserViewHolder userViewHolder = new UserViewHolder(view);
        return userViewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.textView.setText(userArrayList.get(position).getName());
        holder.textView2.setText(userArrayList.get(position).getSprice());
        Product productClass = userArrayList.get(position);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("product").document(userArrayList.get(position).getId()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fetchedImage = documentSnapshot.getString("image");
                        if (fetchedImage != null) {
                            holder.imageView.setImageBitmap(decodeBase64ToBitmap(fetchedImage));

                        } else {
                            holder.imageView.setImageResource(R.drawable.twotone_account_circle_24);
                        }
                    }
                })
                .addOnFailureListener(e -> holder.imageView.setImageResource(R.drawable.twotone_account_circle_24));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onitemClickListener.onClick(productClass);

            }
        });

    }
    public void setOnitemClickListener(OnitemClickListener onitemClickListener) {
        this.onitemClickListener = onitemClickListener;
    }

    public interface OnitemClickListener{
        void onClick(Product c);
    }
    @Override
    public int getItemCount() {
        return this.userArrayList.size();
    }
    private Bitmap decodeBase64ToBitmap(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}