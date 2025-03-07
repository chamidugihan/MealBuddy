package lk.javainsitute.mealbuddy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

public class SingleActivity extends AppCompatActivity {

    int tprice;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_single);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    String i = getIntent().getStringExtra("product");
    Gson gson = new Gson();
       Product productClass =gson.fromJson(i,Product.class);
        RadioButton radioButton = findViewById(R.id.radioButton);
        RadioButton radioButton2 = findViewById(R.id.radioButton2);
        TextView textView3 = findViewById(R.id.textView27);
        CheckBox checkBox = findViewById(R.id.checkBox);
        Button addtoCart = findViewById(R.id.button35);
        Button back = findViewById(R.id.button49);
        ImageView imageView2 = findViewById(R.id.singleproductimage);
        EditText qtyInput = findViewById(R.id.editTextNumberDecimal);
        textView3.setText("Rs. "+productClass.getSprice()+".00");
        tprice = Integer.parseInt(productClass.getSprice());
        radioButton2.setChecked(true);

        back.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(SingleActivity.this, HomeActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });

        radioButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkBox.isChecked()){
                    int price = Integer.parseInt(productClass.getSprice());
                    int topping = 500;
                    int totalPrice = price + topping;
                    textView3.setText("Rs. "+totalPrice+".00");
                    tprice = totalPrice;
                }else{
                    textView3.setText("Rs. "+productClass.getSprice()+".00");
                    tprice = Integer.parseInt(productClass.getSprice());
                }

            }
        });
radioButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        if(checkBox.isChecked()){
            int price = Integer.parseInt(productClass.getLprice());
            int topping = 500;
            int totalPrice = price + topping;
            textView3.setText("Rs. "+totalPrice+".00");
            tprice = totalPrice;
        }else{
            textView3.setText("Rs. "+productClass.getLprice()+".00");
            tprice = Integer.parseInt(productClass.getLprice());
        }

    }
});
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                int basePrice;
                if (radioButton.isChecked()) {
                    basePrice = Integer.parseInt(productClass.getLprice());
                } else {
                    basePrice = Integer.parseInt(productClass.getSprice());
                }

                int finalPrice = isChecked ? basePrice + 500 : basePrice;
                textView3.setText("Rs. " + finalPrice + ".00");
                tprice = finalPrice;
            }
        });

        ImageView imageView = findViewById(R.id.singleproductimage);
        TextView textView = findViewById(R.id.textView22);
        TextView textView2 = findViewById(R.id.textView26);

        textView.setText(productClass.getName());
        textView2.setText(productClass.getDescription());
//        textView3.setText("Rs. "+productClass.getPrice()+".00");

        if (productClass.getImage() != null && !productClass.getImage().isEmpty()) {
            try {
                byte[] imageBytes = Base64.decode(productClass.getImage(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                imageView.setImageResource(R.drawable.default_image);
            }
        } else {
            imageView.setImageResource(R.drawable.default_image);
        }
        addtoCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredQtyStr = qtyInput.getText().toString().trim();

                if (enteredQtyStr.isEmpty()) {
                    Toast.makeText(SingleActivity.this, "Please enter a quantity", Toast.LENGTH_SHORT).show();
                    return;
                }

                int enteredQty;
                try {
                    enteredQty = Integer.parseInt(enteredQtyStr);
                    if (enteredQty <= 0) {
                        Toast.makeText(SingleActivity.this, "Quantity must be greater than zero", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(SingleActivity.this, "Invalid quantity entered", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Determine selected pizza size
                String size;
                int basePrice;
                if (radioButton.isChecked()) {
                    size = "Large";
                    basePrice = Integer.parseInt(productClass.getLprice());
                } else {
                    size = "Small";
                    basePrice = Integer.parseInt(productClass.getSprice());
                }

                // Check if toppings are added
                boolean hasToppings = checkBox.isChecked();
                int finalPrice = hasToppings ? basePrice + 500 : basePrice;
                tprice = finalPrice;

                // Generate a unique cart item ID based on selection
                String cartItemId = productClass.getId() + "_" + size + (hasToppings ? "_toppings" : "");

                SharedPreferences sp = getSharedPreferences("lk.javainsitute.mealbuddy", Context.MODE_PRIVATE);
                String u = sp.getString("user", null);
                Gson gson = new Gson();
                UserBean user = gson.fromJson(u, UserBean.class);

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference cartItemRef = db.collection("cart").document(String.valueOf(user.getId()))
                        .collection("items").document(cartItemId);

                cartItemRef.get().addOnSuccessListener(cartSnapshot -> {
                    int newQty = enteredQty;
                    if (cartSnapshot.exists()) {
                        // Get existing quantity and add new qty
                        String existingQtyStr = cartSnapshot.getString("qty");
                        int existingQty = (existingQtyStr != null && !existingQtyStr.isEmpty()) ? Integer.parseInt(existingQtyStr) : 0;
                        newQty += existingQty;
                    }

                    // Create a CartItem object
                    CartItem cartItem = new CartItem(
                            cartItemId,
                            productClass.getName() + " (" + size + (hasToppings ? " + Toppings" : "") + ")",
                            String.valueOf(tprice),
                            productClass.getImage(),
                            String.valueOf(newQty)
                    );

                    cartItemRef.set(cartItem)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(SingleActivity.this, "Added to cart!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FirestoreError", "Error adding to cart", e);
                                Toast.makeText(SingleActivity.this, "Failed to add to cart", Toast.LENGTH_SHORT).show();
                            });
                }).addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Failed to retrieve cart item", e);
                });
            }
        });

    }
}