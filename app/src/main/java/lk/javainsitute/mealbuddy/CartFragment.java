package lk.javainsitute.mealbuddy;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.constraintlayout.utils.widget.ImageFilterButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.Item;
import lk.payhere.androidsdk.model.StatusResponse;
import com.google.firebase.Timestamp;

public class CartFragment extends Fragment implements CartAdapter.OnCartUpdatedListener {
    private static final String TAG = "PayhereDomain";

    // ActivityResultLauncher to handle the result of PayHere SDK
    private final ActivityResultLauncher payhereLauncher = registerForActivityResult(

            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
                        Serializable serializable = data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
                        if (serializable instanceof PHResponse) {
                            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) serializable;
                            if (response.isSuccess()) {
                                saveOrderToFirestore();
                                clearCart();
                                pushNotifi();
                                Toast.makeText(getActivity(), "Payment Successful!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Payment Failed. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    // Payment was canceled by the user
                    Toast.makeText(getContext(), "Payment Cancelled", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private RecyclerView recyclerView;
    private TextView priceTextView, qtyTextView;
    private Button checkoutButton;
    private ArrayList<CartItem> cartArrayList;
    private FirebaseFirestore db;
    private CartAdapter cartAdapter;
    private UserBean user;
    private SharedPreferences sharedPreferences;
    double total;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerView = view.findViewById(R.id.cartre);
        priceTextView = view.findViewById(R.id.textView131);
        qtyTextView = view.findViewById(R.id.textView111);
        checkoutButton = view.findViewById(R.id.button43);
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userJson = sharedPreferences.getString("user", null);
                Gson gson = new Gson();
                user = gson.fromJson(userJson, UserBean.class);
                if(user.getLine1() == null && user.getLine2() == null && user.getCity() == null){
                    Toast.makeText(getContext(), "Please add your address", Toast.LENGTH_SHORT).show();
                }else {
                    initiatePayment();
                }
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cartArrayList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

         sharedPreferences = getActivity().getSharedPreferences("lk.javainsitute.mealbuddy", Context.MODE_PRIVATE);
        String userJson = sharedPreferences.getString("user", null);
        Gson gson = new Gson();
        user = gson.fromJson(userJson, UserBean.class);

        loadCartItems();

        return view;
    }
    private void pushNotifi() {
        NotificationManager nm = getContext().getSystemService(NotificationManager.class);

        if (nm == null) {
            Log.e("NotificationError", "NotificationManager is null");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel nc = new NotificationChannel(
                    "1",
                    "Payment Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            nc.setDescription("Channel for payment success notifications");
            nm.createNotificationChannel(nc);
        }

        Notification notification = new Notification.Builder(getActivity().getApplicationContext(), "1")
                .setContentTitle("Payment")
                .setContentText("Payment Successful")
                .setSmallIcon(R.drawable.pngegg)
                .setAutoCancel(true)
                .build();

        // Add a unique notification ID
        nm.notify(1, notification); // '1' here is a unique ID for this notification
    }
    private void loadCartItems() {
        db.collection("cart").document(user.getId()).collection("items").get()
                .addOnCompleteListener(task -> {
                    if (task.getResult().isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        priceTextView.setText("Rs. 00");
                        qtyTextView.setText("0");
                        checkoutButton.setVisibility(View.GONE);

                        Toast.makeText(getContext(), "Cart is empty", Toast.LENGTH_SHORT).show();
                    } else {
                        cartArrayList.clear();
                        recyclerView.setVisibility(View.VISIBLE);
                        priceTextView.setVisibility(View.VISIBLE);
                        qtyTextView.setVisibility(View.VISIBLE);
                        checkoutButton.setVisibility(View.VISIBLE);

                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            CartItem cartItem = new CartItem();
                            cartItem.setProductId(documentSnapshot.getId());
                            cartItem.setName(documentSnapshot.getString("name"));
                            cartItem.setPrice(documentSnapshot.getString("price"));
                            cartItem.setQty(documentSnapshot.getString("qty"));
                            cartItem.setImage(documentSnapshot.getString("image"));
                            cartArrayList.add(cartItem);
                        }

                        cartAdapter = new CartAdapter(cartArrayList, this);
                        recyclerView.setAdapter(cartAdapter);
                        updateTotalPrice();
                    }
                });
    }

    @Override
    public void onCartUpdated() {
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        total = 0.0;
        for (CartItem item : cartArrayList) {
            double price = Double.parseDouble(item.getPrice());
            double qty = Double.parseDouble(item.getQty());
            total += price * qty;
        }
        priceTextView.setText("Rs. " + total );
        qtyTextView.setText(String.valueOf(cartArrayList.size()));
        checkoutButton.setVisibility(cartArrayList.size() == 0 ? View.GONE : View.VISIBLE);
    }

    private void initiatePayment() {
        Random random = new Random();
        int randomInt = random.nextInt();
        if (randomInt < 0) {
            randomInt = Math.abs(randomInt);  // Ensure positive order ID
        }

        InitRequest req = new InitRequest();
        req.setMerchantId("1229022"); // Replace with actual Merchant ID
        req.setCurrency("LKR");       // Currency
        req.setAmount(total);         // Total Amount to be charged
        req.setOrderId(String.valueOf(randomInt));  // Unique order ID
        req.setItemsDescription("MealBuddy Order"); // Item description
        req.setCustom1("Custom message 1");
        req.setCustom2("Custom message 2");
        req.getCustomer().setFirstName(user.getFname());
        req.getCustomer().setLastName(user.getLname());
        req.getCustomer().setEmail(user.getEmail());
        req.getCustomer().setPhone(user.getMobile());
        req.getCustomer().getAddress().setAddress(user.getLine1()+user.getLine2());
        req.getCustomer().getAddress().setCity(user.getCity());
        req.getCustomer().getAddress().setCountry("Sri Lanka");

        // Log values for debugging
        Log.d(TAG, "Payment Initiated with Order ID: " + req.getOrderId());
        Log.d(TAG, "Amount: " + req.getAmount());
        Log.d(TAG, "Currency: " + req.getCurrency());

        // Start PayHere Payment Activity
        Intent intent = new Intent(getContext(), PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL); // Set the sandbox URL

        payhereLauncher.launch(intent);
    }
    private void saveOrderToFirestore() {
        String orderId = String.valueOf(System.currentTimeMillis());

        Order order = new Order(orderId, user.getId(), cartArrayList, total, Timestamp.now(),"pending");
        db.collection("order").document(orderId)
                .set(order)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        startActivity(new Intent(getActivity(), HomeActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        getActivity().finish();


                    }
                });
    }

    private void clearCart() {
        db.collection("cart").document(user.getId()).collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }
                    cartArrayList.clear();
                    recyclerView.getAdapter().notifyDataSetChanged();
                    updateTotalPrice();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to clear cart", e));
    }
}

class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private ArrayList<CartItem> cartArrayList;
    private FirebaseFirestore db;
    private OnCartUpdatedListener listener;

    public CartAdapter(ArrayList<CartItem> cartArrayList, OnCartUpdatedListener listener) {
        this.cartArrayList = cartArrayList;
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, priceTextView, qtyTextView;
        ImageFilterButton increaseQtyButton, decreaseQtyButton, deleteItemButton;
        ImageView imageView;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.cart_type);
            priceTextView = itemView.findViewById(R.id.cart_price);
            qtyTextView = itemView.findViewById(R.id.cart_qty);
            increaseQtyButton = itemView.findViewById(R.id.cartimage2);
            decreaseQtyButton = itemView.findViewById(R.id.cartimage1);
            deleteItemButton = itemView.findViewById(R.id.cartimage3);
            imageView = itemView.findViewById(R.id.cartpizzaimage);
        }
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_template, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        SharedPreferences sharedPreferences = holder.itemView.getContext().getSharedPreferences("lk.javainsitute.mealbuddy", Context.MODE_PRIVATE);
        String userJson = sharedPreferences.getString("user", null);
        Gson gson = new Gson();
        UserBean user = gson.fromJson(userJson, UserBean.class);
        CartItem cartItem = cartArrayList.get(position);

        holder.nameTextView.setText(cartItem.getName());
        holder.priceTextView.setText("Rs. " + cartItem.getPrice() + ".00");
        holder.qtyTextView.setText(cartItem.getQty());

        holder.increaseQtyButton.setOnClickListener(view -> {
            int newQty = Integer.parseInt(cartItem.getQty()) + 1;
            cartItem.setQty(String.valueOf(newQty));
            notifyItemChanged(position);
            db.collection("cart").document(user.getId()).collection("items").document(cartItem.getProductId()).update("qty", cartItem.getQty());
            listener.onCartUpdated();  // Notify fragment
        });

        holder.decreaseQtyButton.setOnClickListener(view -> {
            int currentQty = Integer.parseInt(cartItem.getQty());
            if (currentQty > 1) {
                int newQty = currentQty - 1;
                cartItem.setQty(String.valueOf(newQty));
                notifyItemChanged(position);
                db.collection("cart").document(user.getId()).collection("items").document(cartItem.getProductId()).update("qty", cartItem.getQty());
                listener.onCartUpdated();  // Notify fragment
            } else {
                Toast.makeText(view.getContext(), "Quantity cannot be less than 1", Toast.LENGTH_SHORT).show();
            }
        });

        holder.deleteItemButton.setOnClickListener(view -> {
            db.collection("cart").document(user.getId()).collection("items").document(cartItem.getProductId()).delete();
            cartArrayList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, cartArrayList.size());
            listener.onCartUpdated();  // Notify fragment
        });

        if (cartItem.getImage() != null && !cartItem.getImage().isEmpty()) {
            try {
                byte[] imageBytes = Base64.decode(cartItem.getImage(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                holder.imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                holder.imageView.setImageResource(R.drawable.default_image);
            }
        } else {
            holder.imageView.setImageResource(R.drawable.default_image);
        }
    }

    @Override
    public int getItemCount() {
        return cartArrayList.size();
    }

    public interface OnCartUpdatedListener {
        void onCartUpdated();
    }

}
