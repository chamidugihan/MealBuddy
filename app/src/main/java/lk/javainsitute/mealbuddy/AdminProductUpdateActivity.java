package lk.javainsitute.mealbuddy;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;

public class AdminProductUpdateActivity extends AppCompatActivity {

    private EditText description, largePrice, smallPrice;
    private TextView name;
    private RadioButton radioActive, radioInactive;
    private Button updateButton;
    private ImageView imageView;
    private FirebaseFirestore firestore;
    private String productId;
    private Product product;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private String base64Image = null;

    private static final String TAG = "AdminProductUpdate"; // Log Tag for debugging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_product_update);

        // Handle Edge-to-Edge UI (Check if main exists)
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Get Views
        name = findViewById(R.id.adminproductupdatetext1);
        description = findViewById(R.id.admindescription);
        largePrice = findViewById(R.id.adminlageprice);
        smallPrice = findViewById(R.id.adminsmallprice);
        updateButton = findViewById(R.id.adminupdatebutton);
        radioActive = findViewById(R.id.radioButton3);
        radioInactive = findViewById(R.id.radioButton4);
        imageView = findViewById(R.id.imageView8);


        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        imageView.setImageURI(uri);
                        base64Image = encodeImageToBase64(uri);
                    }
                });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.Companion.with(AdminProductUpdateActivity.this)
                        .galleryOnly()
                        .crop()
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .createIntent(intent -> {
                            imagePickerLauncher.launch(intent);
                            return null;
                        });
            }
        });


        String productJson = getIntent().getStringExtra("product");
        if (productJson != null) {
            Gson gson = new Gson();
            product = gson.fromJson(productJson, Product.class);
            populateProductData();
        } else {
            Toast.makeText(this, "Error: Product data not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        updateButton.setOnClickListener(view -> updateProductData());
    }

    private void populateProductData() {
        name.setText(product.getName());
        description.setText(product.getDescription());
        largePrice.setText(product.getLprice());
        smallPrice.setText(product.getSprice());
        productId = product.getId();

        if ("active".equals(product.getStatus())) {
            radioActive.setChecked(true);
        } else {
            radioInactive.setChecked(true);
        }


        if (product.getImage() != null && !product.getImage().isEmpty()) {
            try {
                byte[] imageBytes = Base64.decode(product.getImage(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                imageView.setImageResource(R.drawable.default_image);
            }
        } else {
            imageView.setImageResource(R.drawable.default_image);
        }
    }


    private void updateProductData() {
        if (description.getText().toString().isEmpty() ||
                largePrice.getText().toString().isEmpty() ||
                smallPrice.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        product.setDescription(description.getText().toString());
        product.setLprice(largePrice.getText().toString());
        product.setSprice(smallPrice.getText().toString());
        product.setStatus(radioActive.isChecked() ? "active" : "inactive");
        product.setImage(base64Image);

        firestore.collection("product").document(productId)
                .update(
                        "description", product.getDescription(),
                        "sprice", product.getSprice(),
                        "lprice", product.getLprice(),
                        "status", product.getStatus(),
                        "image", product.getImage()
                )
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Product Updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show());
    }

    private String encodeImageToBase64(Uri imageUri) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
