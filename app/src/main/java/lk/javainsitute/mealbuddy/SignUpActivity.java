package lk.javainsitute.mealbuddy;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Button button = findViewById(R.id.button7);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, LoginAcitivity.class);
                startActivity(intent);
            }
        });
      EditText phone = findViewById(R.id.editTextPhone2);
      EditText firstName = findViewById(R.id.editTextText);
      EditText lastName = findViewById(R.id.editTextText2);
      EditText password = findViewById(R.id.editTextTextPassword2);
      EditText confirmPassword = findViewById(R.id.editTextTextPassword3);
      EditText email = findViewById(R.id.editTextTextEmailAddress);
      Button create = findViewById(R.id.button3);
      create.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {


                  if(phone.getText().toString().isEmpty() ){
                      CharSequence text = "please Enter Mobile Number";
                      int duration = Toast.LENGTH_SHORT;

                      Toast toast = Toast.makeText(SignUpActivity.this, text, duration);
                      toast.show();
                  }else if(phone.getText().toString().length()!=10){
                      CharSequence text = "please Enter Valid Mobile Number";
                      int duration = Toast.LENGTH_SHORT;
                      Toast toast = Toast.makeText(SignUpActivity.this, text, duration);
                      toast.show();
                  }else if(!Validation.validateMobileNumber(phone.getText().toString())){
                      CharSequence text = "please Enter Valid Mobile Number";
                      int duration = Toast.LENGTH_SHORT;
                      Toast toast = Toast.makeText(SignUpActivity.this, text, duration);
                      toast.show();
              } else if(firstName.getText().toString().isEmpty()){
                      CharSequence text = "please Enter First Name";
                      int duration = Toast.LENGTH_SHORT;
                      Toast toast = Toast.makeText(SignUpActivity.this, text, duration);
                      toast.show();

                  }else if(!Validation.validateName(firstName.getText().toString())){
                      CharSequence text = "please Enter Valid First Name";
                      int duration = Toast.LENGTH_SHORT;
                      Toast toast = Toast.makeText(SignUpActivity.this, text, duration);
                      toast.show();
                  } else if(lastName.getText().toString().isEmpty()){
                      CharSequence text = "please Enter Last Name";
                      int duration = Toast.LENGTH_SHORT;
                      Toast toast = Toast.makeText(SignUpActivity.this, text, duration);
                      toast.show();
                  }else if(!Validation.validateName(lastName.getText().toString())){
                      CharSequence text = "please Enter Valid Last Name";
                      int duration = Toast.LENGTH_SHORT;
                      Toast toast = Toast.makeText(SignUpActivity.this, text, duration);
                      toast.show();
                  } else if(password.getText().toString().isEmpty()){
                      CharSequence text = "please Enter Password";
                      int duration = Toast.LENGTH_SHORT;
                      Toast toast = Toast.makeText(SignUpActivity.this, text, duration);
                      toast.show();
                  }else if(!Validation.validatePassword(password.getText().toString())){
                      CharSequence text = "please Enter Valid Password";
                      int duration = Toast.LENGTH_SHORT;
                      Toast toast = Toast.makeText(SignUpActivity.this, text, duration);
                      toast.show();

                  } else if(confirmPassword.getText().toString().isEmpty()){
                      CharSequence text = "please Enter Confirm Password";
                      int duration = Toast.LENGTH_SHORT;
                      Toast toast = Toast.makeText(SignUpActivity.this, text, duration);
                      toast.show();
                  } else if(!Validation.validatePassword(confirmPassword.getText().toString())) {
                      CharSequence text = "please Enter Valid Confirm Password";
                      int duration = Toast.LENGTH_SHORT;
                      Toast toast = Toast.makeText(SignUpActivity.this, text, duration);
                      toast.show();
                  }else if (!password.getText().toString().equals(confirmPassword.getText().toString())) {
                      CharSequence text = "please Enter Same Password";
                      int duration = Toast.LENGTH_SHORT;
                      Toast toast = Toast.makeText(SignUpActivity.this, text, duration);
                      toast.show();
                  } else if (email.getText().toString().isEmpty()) {
                      CharSequence text = "please Enter Email";
                      int duration = Toast.LENGTH_SHORT;
                      Toast toast = Toast.makeText(SignUpActivity.this, text, duration);
                      toast.show();
                  }else if(!Validation.validateEmail(email.getText().toString())){
                      CharSequence text = "please Enter Valid Email";
                      int duration = Toast.LENGTH_SHORT;
                      Toast toast = Toast.makeText(SignUpActivity.this, text, duration);
                      toast.show();
                  }else{
                      FirebaseFirestore bd = FirebaseFirestore.getInstance();
                      bd.collection("user").whereEqualTo("mobile",phone.getText().toString()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                          @Override
                          public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.getResult().isEmpty()){
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("fname", firstName.getText().toString());
                                hashMap.put("lname", lastName.getText().toString());
                                hashMap.put("mobile", phone.getText().toString());
                                hashMap.put("password", password.getText().toString());
                                hashMap.put("email", email.getText().toString());
                                hashMap.put("status", "active");
                                bd.collection("user").add(hashMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                        Intent intent = new Intent(SignUpActivity.this, LoginAcitivity.class);
                                        startActivity(intent);
                                        Toast toast = Toast.makeText(SignUpActivity.this, "SignUp Success", Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                });
                            }else {
                                CharSequence text = "User Already Exist";
                                int duration = Toast.LENGTH_SHORT;
                                Toast toast = Toast.makeText(SignUpActivity.this, text, duration);
                                toast.show();
                            }
                          }
                      });
                  }

          }
      });

    }
}