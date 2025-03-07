package lk.javainsitute.mealbuddy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FlingAnimation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView v = findViewById(R.id.imageView);
        FlingAnimation flingAnimation = new FlingAnimation(v, DynamicAnimation.ROTATION);
        flingAnimation.setStartVelocity(330f);
        flingAnimation.setFriction(0.2f);
        flingAnimation.start();

        new Handler().postDelayed(() -> {
            if (!isNetworkAvailable()) {
                Toast.makeText(MainActivity.this, "No Internet. Please turn on mobile data.", Toast.LENGTH_SHORT).show();

                // Keep checking for internet connection
                checkInternetAndRestart();
            } else {
                proceedToNextActivity();
            }
        }, 2000);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void checkInternetAndRestart() {
        new Handler().postDelayed(() -> {
            if (isNetworkAvailable()) {
                Toast.makeText(MainActivity.this, "Internet is back. Restarting app...", Toast.LENGTH_SHORT).show();
                recreate(); // Restart the activity
            } else {
                checkInternetAndRestart(); // Keep checking
            }
        }, 3000); // Check every 3 seconds
    }

    private void proceedToNextActivity() {
        SharedPreferences preferences = getSharedPreferences("lk.javainsitute.mealbuddy", Context.MODE_PRIVATE);
        if (!preferences.contains("user")) {
            startActivity(new Intent(MainActivity.this, LoginAcitivity.class));
            finish();
        } else {
            String u = preferences.getString("user", null);
            Gson gson = new Gson();
            UserBean user = gson.fromJson(u, UserBean.class);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("user").whereEqualTo("mobile", user.getMobile()).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.getString("status").equals("active")) {
                                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                } else {
                                    Toast.makeText(MainActivity.this, "Your account is InActive", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(MainActivity.this, LoginAcitivity.class));
                                }
                                finish();
                            }
                        }
                    });
        }
    }
}
