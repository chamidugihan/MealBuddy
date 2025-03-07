package lk.javainsitute.mealbuddy.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import lk.javainsitute.mealbuddy.Product;
import lk.javainsitute.mealbuddy.R;


public class AllProductAdapter extends RecyclerView.Adapter<AllProductAdapter.ProductViewHolder> {
    OnitemClickListener onitemClickListener;
    public class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        TextView textView2;
        ImageView imageView;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView9);
            textView2 = itemView.findViewById(R.id.textView12);
            imageView = itemView.findViewById(R.id.imageView5);
        }
    }

    public ArrayList<Product> allProductArrayList;

    public AllProductAdapter(ArrayList<Product> allProductArrayList) {
        this.allProductArrayList = allProductArrayList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.product_template, parent, false);
        ProductViewHolder productViewHolder = new ProductViewHolder(view);
        return productViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.textView.setText(allProductArrayList.get(position).getName());
        holder.textView2.setText(allProductArrayList.get(position).getSprice());
        Product productClass = allProductArrayList.get(position);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("product").document(allProductArrayList.get(position).getId()).get()
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

    @Override
    public int getItemCount() {
        return this.allProductArrayList.size();
    }

    public void setOnitemClickListener(OnitemClickListener onitemClickListener) {
        this.onitemClickListener = onitemClickListener;
    }

    public interface OnitemClickListener {
        void onClick(Product c);
    }
    private Bitmap decodeBase64ToBitmap(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
    public void updateProductList(ArrayList<Product> newProductList) {
        this.allProductArrayList.clear(); // Clear old list
        this.allProductArrayList.addAll(newProductList); // Add new products
        notifyDataSetChanged(); // Notify adapter of changes
    }

}

