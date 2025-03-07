package lk.javainsitute.mealbuddy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {
    ImageView profile;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;
    private UserBean user;
    private String base64Image = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profile = view.findViewById(R.id.profileimageview);
        TextView fname = view.findViewById(R.id.editprofilefname);
        TextView lname = view.findViewById(R.id.editprofilelname);
        TextView email = view.findViewById(R.id.editprofileemail);
        TextView mobile = view.findViewById(R.id.editprofilemobile);
        EditText line1 = view.findViewById(R.id.editprofileline1);
        EditText line2 = view.findViewById(R.id.editTextText5);
        Spinner spinner = view.findViewById(R.id.spinner2);
        ArrayList<String> cities = new ArrayList<>();
        Button save = view.findViewById(R.id.profilesavebutton);

        fname.setEnabled(false);
        lname.setEnabled(false);
        sharedPreferences = getActivity().getSharedPreferences("lk.javainsitute.mealbuddy", Context.MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();

        Gson gson = new Gson();
        String json = sharedPreferences.getString("user", null);
        user = gson.fromJson(json, UserBean.class);

        fname.setText(user.getFname());
        lname.setText(user.getLname());
        email.setText(user.getEmail());
        mobile.setText(user.getMobile());

        if (user.getLine1() != null) line1.setText(user.getLine1());
        if (user.getLine2() != null) line2.setText(user.getLine2());

        loadProfileImage();

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        profile.setImageURI(uri);
                        base64Image = encodeImageToBase64(uri);
                    }
                });

        profile.setOnClickListener(v -> {
            ImagePicker.Companion.with(ProfileFragment.this)
                    .galleryOnly()
                    .crop()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .createIntent(intent -> {
                        imagePickerLauncher.launch(intent);
                        return null;
                    });
        });


        db.collection("city").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    cities.add(document.getString("name"));
                }
                CityAdapter adapter = new CityAdapter(getActivity(), R.layout.city_tamplate, cities);
                spinner.setAdapter(adapter);


                if (user.getCity() != null) {
                    int index = cities.indexOf(user.getCity());
                    if (index != -1) spinner.setSelection(index);
                }
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProfile(email, mobile, line1, line2, spinner);
            }
        });


        return view;
    }

    private void saveProfile(TextView email, TextView mobile, EditText line1, EditText line2, Spinner spinner) {
        if (email.getText().toString().isEmpty() || mobile.getText().toString().isEmpty() || line1.getText().toString().isEmpty() || line2.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        } else if (!Validation.validateMobileNumber(mobile.getText().toString())) {
            Toast.makeText(getContext(), "Invalid Mobile Number", Toast.LENGTH_SHORT).show();
            return;
        } else if (!Validation.validateEmail(email.getText().toString())) {
            Toast.makeText(getContext(), "Invalid Email", Toast.LENGTH_SHORT).show();
            return;
        }

        user.setEmail(email.getText().toString());
        user.setMobile(mobile.getText().toString());
        user.setLine1(line1.getText().toString());
        user.setLine2(line2.getText().toString());
        user.setCity(spinner.getSelectedItem().toString());

        if (base64Image != null) {
            user.setImage(base64Image);
            sharedPreferences.edit().putString("profile_image", base64Image).apply();
        }

        Gson gson = new Gson();
        sharedPreferences.edit().putString("user", gson.toJson(user)).apply();

        db.collection("user").document(user.getId())
                .update("email", user.getEmail(), "mobile", user.getMobile(), "line1", user.getLine1(), "line2", user.getLine2(), "city", user.getCity(), "image", user.getImage())
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Profile Updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show());
    }

    private void loadProfileImage() {
        String base64Image = sharedPreferences.getString("profile_image", null);
        if (base64Image != null) {
            profile.setImageBitmap(decodeBase64ToBitmap(base64Image));
            return;
        }
        db.collection("user").document(user.getId()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fetchedImage = documentSnapshot.getString("image");
                        if (fetchedImage != null) {
                            sharedPreferences.edit().putString("profile_image", fetchedImage).apply();
                            profile.setImageBitmap(decodeBase64ToBitmap(fetchedImage));
                        } else {
                            profile.setImageResource(R.drawable.twotone_account_circle_24);
                        }
                    }
                })
                .addOnFailureListener(e -> profile.setImageResource(R.drawable.twotone_account_circle_24));
    }

    private String encodeImageToBase64(Uri imageUri) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(imageUri));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap decodeBase64ToBitmap(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}
class CityAdapter extends ArrayAdapter<String> {
    List<String> list1;
    int layout;

    public CityAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
        this.list1 = objects;
        this.layout = resource;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater i = LayoutInflater.from(parent.getContext());
        View view = i.inflate(layout, parent, false);
        TextView textView = view.findViewById(R.id.citytemplatetext);
        textView.setText(list1.get(position));
        return view;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getDropDownView(position, convertView, parent);
    }
}