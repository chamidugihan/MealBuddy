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
import android.widget.Spinner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class AdminAddProductActivity extends AppCompatActivity {
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private String base64Image = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_product);

        Button addButton = findViewById(R.id.adminproductaddpagebutton);
        EditText titleEditText = findViewById(R.id.editTextText6);
        EditText descriptionEditText = findViewById(R.id.editTextText7);
        EditText largePriceEditText = findViewById(R.id.editTextText8);
        EditText smallPriceEditText = findViewById(R.id.editTextText9);
        ImageView imageView = findViewById(R.id.imageView7);
        Button button4 = findViewById(R.id.button4);

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
        Intent intent = new Intent(AdminAddProductActivity.this, CategoryActivity.class);
        startActivity(intent);
            }
        });

        ArrayList<String> item = new ArrayList<>();
        Spinner spinner1 = findViewById(R.id.spinner);
        FirebaseFirestore db1 = FirebaseFirestore.getInstance();
        db1.collection("category").orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            item.add(document.getString("name"));
                        }
                        CityAdapter adapter = new CityAdapter(AdminAddProductActivity.this,R.layout.city_tamplate, item);
                        spinner1.setAdapter(adapter);

                    }
                });


        // Image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        imageView.setImageURI(uri);
                        base64Image = encodeImageToBase64(uri);
                    }
                });

        // Image view click to pick image from gallery
        imageView.setOnClickListener(view -> {
            ImagePicker.Companion.with(AdminAddProductActivity.this)
                    .galleryOnly()  // Allow only gallery selection, no camera
                    .crop()          // Enable image cropping
                    .compress(1024)  // Compress image to 1024 KB
                    .maxResultSize(1080, 1080) // Maximum resolution of the image
                    .createIntent(intent -> {
                        imagePickerLauncher.launch(intent); // Launch image picker intent
                        return null;
                    });
        });

        // Add button click listener
        addButton.setOnClickListener(view -> {
            if (titleEditText.getText().toString().isEmpty()) {
                titleEditText.setError("Please enter title");
            } else if (descriptionEditText.getText().toString().isEmpty()) {
                descriptionEditText.setError("Please enter description");
            } else if (largePriceEditText.getText().toString().isEmpty()) {
                largePriceEditText.setError("Please enter large price");
            } else if (smallPriceEditText.getText().toString().isEmpty()) {
                smallPriceEditText.setError("Please enter small price");
            } else {
                Product product = new Product();
                String productId = String.valueOf(System.currentTimeMillis());
                product.setName(titleEditText.getText().toString());
                product.setDescription(descriptionEditText.getText().toString());
                product.setLprice(largePriceEditText.getText().toString());
                product.setSprice(smallPriceEditText.getText().toString());
        product.setDatetime(Timestamp.now());
                product.setImage(base64Image);
                product.setId(productId);
                product.setStatus("active");
                product.setCategory(spinner1.getSelectedItem().toString());

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("product").document(productId).set(product);
                finish();
                startActivity(new Intent(AdminAddProductActivity.this, AdminHomeActivity.class));
            }
        });
    }

    // Encode image to Base64
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

    // Decode Base64 string to Bitmap
    private Bitmap decodeBase64ToBitmap(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}
