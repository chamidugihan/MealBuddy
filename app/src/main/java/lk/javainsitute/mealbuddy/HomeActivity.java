package lk.javainsitute.mealbuddy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

public class HomeActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    ImageButton buttonDrawerToggle;
    NavigationView navigationView;
    ImageView userImage;
    private SharedPreferences preferences;
    private UserBean user;
    private String base64Image = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_home);
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.homefagment, new HomeFragment())
                    .commit();
            drawerLayout = findViewById(R.id.drawer_layout);
            buttonDrawerToggle = findViewById(R.id.buttonDrawerToggle);
            navigationView = findViewById(R.id.navigationview);
            buttonDrawerToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    drawerLayout.open();
                }
            });

            View headerView = navigationView.getHeaderView(0);
            preferences = getSharedPreferences("lk.javainsitute.mealbuddy", Context.MODE_PRIVATE);
            String u = preferences.getString("user", null);
            Gson gson = new Gson();
            user = gson.fromJson(u, UserBean.class);


            userImage = headerView.findViewById(R.id.userImage);
            TextView textUsername = headerView.findViewById(R.id.textUsername);
            TextView textEmail = headerView.findViewById(R.id.textEmail);

            loadProfileImage();

            textUsername.setText(user.getFname() + " " + user.getLname());
            textEmail.setText(user.getEmail());


            userImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(HomeActivity.this, textUsername.getText(), Toast.LENGTH_SHORT).show();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.homefagment, new ProfileFragment())
                            .commit();
                }
            });
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == R.id.navMenu) {
                        Toast.makeText(HomeActivity.this, "Home", Toast.LENGTH_SHORT).show();
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.homefagment, new HomeFragment())
                                .commit();
                    }
                    if (itemId == R.id.navCart) {
                        Toast.makeText(HomeActivity.this, "Cart", Toast.LENGTH_SHORT).show();
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.homefagment, new CartFragment())
                                .commit();
                    }
                    if (itemId == R.id.naveProduct) {
                        Toast.makeText(HomeActivity.this, "Producs", Toast.LENGTH_SHORT).show();
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.homefagment, new ProductFragment())
                                .commit();
                    }

//                    if (itemId == R.id.navFavorites) {
//                        Toast.makeText(HomeActivity.this, "Favorites", Toast.LENGTH_SHORT).show();
////                    FragmentManager fragmentManager = getSupportFragmentManager();
////                    fragmentManager.beginTransaction()
////                            .replace(R.id.homefagment, new CartFragment())
////                            .commit();
//                    }

                    if (itemId == R.id.navOrders) {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.homefagment, new OrderFragment())
                                .commit();
                        Toast.makeText(HomeActivity.this, "Orders", Toast.LENGTH_SHORT).show();
                    }

                    if (itemId == R.id.navHistory) {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.homefagment, new HistoryFragment())
                                .commit();
                        Toast.makeText(HomeActivity.this, "History", Toast.LENGTH_SHORT).show();
                    }

//                    if (itemId == R.id.navFeadback) {
//
//                        Toast.makeText(HomeActivity.this, "Feadback ", Toast.LENGTH_SHORT).show();
//                    }
                    if (itemId == R.id.navTerms) {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.homefagment, new termsFragment())
                                .commit();
                        Toast.makeText(HomeActivity.this, "Terms and Conditions", Toast.LENGTH_SHORT).show();
                    }
                    if (itemId == R.id.navContact) {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.homefagment, new ContactFragment())
                                .commit();
                        Toast.makeText(HomeActivity.this, "Contact Us", Toast.LENGTH_SHORT).show();
                    }
                    if (itemId == R.id.navShear) {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.homefagment, new ShareFragment())
                                .commit();
                        Toast.makeText(HomeActivity.this, "Shear", Toast.LENGTH_SHORT).show();
                    }
                    drawerLayout.close();
                    return false;
                }
            });

    }

    private void loadProfileImage() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String base64Image = preferences.getString("profile_image", null);
        if (base64Image != null) {
            userImage.setImageBitmap(decodeBase64ToBitmap(base64Image));
            return;
        }
        db.collection("user").document(user.getId()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fetchedImage = documentSnapshot.getString("image");
                        if (fetchedImage != null) {
                            preferences.edit().putString("profile_image", fetchedImage).apply();
                            userImage.setImageBitmap(decodeBase64ToBitmap(fetchedImage));
                        } else {
                            userImage.setImageResource(R.drawable.twotone_account_circle_24);
                        }
                    }
                })
                .addOnFailureListener(e -> userImage.setImageResource(R.drawable.twotone_account_circle_24));
    }
    private Bitmap decodeBase64ToBitmap(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}