package lk.javainsitute.mealbuddy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.HashMap;

public class LoginAcitivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        EditText editTextPhone = findViewById(R.id.editTextPhone);
        EditText editTextPassword = findViewById(R.id.editTextTextPassword);
        Button button = findViewById(R.id.button);
        SharedPreferences preferences = getSharedPreferences("lk.javainsitute.mealbuddy", Context.MODE_PRIVATE);
        TextView textView = findViewById(R.id.textView7);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginAcitivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
        Button b = findViewById(R.id.loginbutton1);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (editTextPhone.getText().toString().isEmpty()) {
                    CharSequence text = "please Enter Mobile Number";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(LoginAcitivity.this, text, duration);
                    toast.show();
                } else if (!Validation.validateMobileNumber(editTextPhone.getText().toString())) {
                    CharSequence text = "please Enter Valid Mobile Number";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(LoginAcitivity.this, text, duration);
                    toast.show();
                } else {

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("user").whereEqualTo("mobile", editTextPhone.getText().toString()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (!task.getResult().isEmpty()) {
                                CharSequence text = "Please Check Your Email and Enter the validation code. Then reset the password";
                                int duration = Toast.LENGTH_SHORT;
                                Toast toast = Toast.makeText(LoginAcitivity.this, text, duration);
                                toast.show();

                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    CharSequence text1 = "give you a otp and new password";
                                    int duration1 = Toast.LENGTH_LONG;
                                    Toast toast1 = Toast.makeText(LoginAcitivity.this, text1, duration1);
                                    toast1.show();
                                    LayoutInflater layoutInflater = LayoutInflater.from(LoginAcitivity.this);
                                    View forgotPasswordAlertView = layoutInflater.inflate(R.layout.forgotalert, null);

                                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginAcitivity.this);
                                    builder.setView(forgotPasswordAlertView);
                                    AlertDialog resetPasswordAlertDialog = builder.create();
                                    resetPasswordAlertDialog.show();

                                    int code = (int) (Math.random() * 1000000);



//                                    JavaMailAPI javaMailAPI = new JavaMailAPI( document.getString("email"), "Password Reset Validation Code", "Your Verification Code : " + code );
//                                    javaMailAPI.execute();
                                    String emailBody = "<p>Your Verification Code : <span style='color:red; font-weight:bold;'>" + code + "</span></p>";

                                    JavaMailAPI javaMailAPI = new JavaMailAPI(
                                            document.getString("email"),
                                            "Password Reset Validation Code",
                                            emailBody
                                    );
                                    javaMailAPI.execute();


                                    EditText validationCode = forgotPasswordAlertView.findViewById(R.id.editTextText3);
                                    EditText newPassword = forgotPasswordAlertView.findViewById(R.id.editTextText4);
                                    Button b6 = forgotPasswordAlertView.findViewById(R.id.button6);
                                    b6.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (validationCode.getText().toString().isEmpty()) {

                                                CharSequence text = "Please Enter Validation Code";
                                                int duration = Toast.LENGTH_SHORT;
                                                Toast toast = Toast.makeText(LoginAcitivity.this, text, duration);
                                                toast.show();
                                            }else if(validationCode.getText().toString().equals(String.valueOf(code))){
                                                CharSequence text = "Invalid Validation Code";
                                                int duration = Toast.LENGTH_SHORT;
                                                Toast toast = Toast.makeText(LoginAcitivity.this, text, duration);
                                                toast.show();

                                            } else if (newPassword.getText().toString().isEmpty()) {

                                                CharSequence text = "Please Enter New Password";
                                                int duration = Toast.LENGTH_SHORT;
                                                Toast toast = Toast.makeText(LoginAcitivity.this, text, duration);
                                                toast.show();

                                            } else if(!Validation.validatePassword(newPassword.getText().toString())){

                                                CharSequence text = "Please Enter Valid Password";
                                                int duration = Toast.LENGTH_SHORT;
                                                Toast toast = Toast.makeText(LoginAcitivity.this, text, duration);
                                                toast.show();
                                            }  else {

                                                if(validationCode.getText().toString().equals(String.valueOf(code))){

                                                    db.collection("user").document(document.getId()).update("password", newPassword.getText().toString());
                                                    CharSequence text = "Password Reset Successful";
                                                    int duration = Toast.LENGTH_SHORT;
                                                    Toast toast = Toast.makeText(LoginAcitivity.this, text, duration);
                                                    toast.show();
                                                    resetPasswordAlertDialog.dismiss();


                                                }else{
                                                    CharSequence text = "Invalid Validation Code";
                                                    int duration = Toast.LENGTH_SHORT;
                                                    Toast toast = Toast.makeText(LoginAcitivity.this, text, duration);
                                                    toast.show();
                                                }

                                            }

                                        }
                                    });

                                }

                            }else{
                                CharSequence text = "Invalid Credentials";
                                int duration = Toast.LENGTH_SHORT;
                                Toast toast = Toast.makeText(LoginAcitivity.this, text, duration);
                                toast.show();
                            }


                        }
                    });
                }
            }
        });

        if (!preferences.contains("user")) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (editTextPhone.getText().toString().isEmpty()) {
                        CharSequence text = "please Enter Mobile Number";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(LoginAcitivity.this, text, duration);
                        toast.show();
                    } else if (editTextPhone.getText().toString().length() != 10) {
                        CharSequence text = "please Enter Valid Mobile Number";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(LoginAcitivity.this, text, duration);
                        toast.show();
                    } else if (!Validation.validateMobileNumber(editTextPhone.getText().toString())) {
                        CharSequence text = "please Enter Valid Mobile Number";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(LoginAcitivity.this, text, duration);
                        toast.show();
                    } else if (editTextPassword.getText().toString().isEmpty()) {
                        CharSequence text = "please Enter Password";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(LoginAcitivity.this, text, duration);
                        toast.show();
                    } else if (!Validation.validatePassword(editTextPassword.getText().toString())) {
                        CharSequence text = "please Enter Valid Password";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(LoginAcitivity.this, text, duration);
                        toast.show();
                    } else {
                        FirebaseFirestore bd = FirebaseFirestore.getInstance();
                        bd.collection("admin").where(Filter.and(
                                Filter.equalTo("mobile", editTextPhone.getText().toString()),
                                Filter.equalTo("password", editTextPassword.getText().toString()))).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.getResult().isEmpty()) {
                                    CharSequence text = "Welcome user";
                                    int duration = Toast.LENGTH_SHORT;
                                    Toast toast = Toast.makeText(LoginAcitivity.this, text, duration);
                                    toast.show();
                                    bd.collection("user").
                                            where(
                                                    Filter.and(
                                                            Filter.equalTo("mobile", editTextPhone.getText().toString()),
                                                            Filter.equalTo("password", editTextPassword.getText().toString()))).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.getResult().isEmpty()) {
                                                        CharSequence text = "User Not Found";
                                                        int duration = Toast.LENGTH_SHORT;
                                                        Toast toast = Toast.makeText(LoginAcitivity.this, text, duration);
                                                        toast.show();
                                                    } else {
                                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                                            UserBean userBean = new UserBean();
                                                            userBean.setFname(document.getString("fname"));
                                                            userBean.setLname(document.getString("lname"));
                                                            userBean.setMobile(document.getString("mobile"));
                                                            userBean.setEmail(document.getString("email"));
                                                            userBean.setId(document.getId());
                                                            userBean.setImage(document.getString("image"));
                                                            userBean.setLine1(document.getString("line1"));
                                                            userBean.setLine2(document.getString("line2"));
                                                            userBean.setCity(document.getString("city"));

                                                            Gson gson = new Gson();
                                                            String user = gson.toJson(userBean);
                                                            if (document.getString("status").equals("active")) {
                                                                preferences.edit().putString("user", user).apply();
                                                                Intent intent = new Intent(LoginAcitivity.this, HomeActivity.class);
                                                                startActivity(intent);
                                                                finish();
                                                            } else {
                                                                CharSequence text = "your account is InActive";
                                                                int duration = Toast.LENGTH_SHORT;
                                                                Toast toast = Toast.makeText(LoginAcitivity.this, text, duration);
                                                                toast.show();
                                                            }

                                                        }
                                                    }
                                                }
                                            });
                                } else {
                                    CharSequence text = "Welcome Admin!";
                                    int duration = Toast.LENGTH_SHORT;
                                    Toast toast = Toast.makeText(LoginAcitivity.this, text, duration);
                                    toast.show();
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        AdminBean adminBean = new AdminBean();
                                        adminBean.setId(document.getId());
                                        adminBean.setFname(document.getString("fname"));
                                        adminBean.setLname(document.getString("lname"));
                                        adminBean.setMobile(document.getString("mobile"));
                                        adminBean.setEmail(document.getString("email"));
                                        Gson gson = new Gson();
                                        String admin = gson.toJson(adminBean);
                                        preferences.edit().putString("admin", admin).apply();
                                        Intent intent = new Intent(LoginAcitivity.this, AdminHomeActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears the back stack
                                        startActivity(intent);
                                        finish();
                                        break;

                                    }
//                                    int code = (int) (Math.random() * 1000000);
//                                    for (QueryDocumentSnapshot document : task.getResult()) {
//                                        JavaMailAPI javaMailAPI = new JavaMailAPI(document.getString("email"),
//                                                "Reset Password", "Account Verification" +
//                                                "<h3 style=\"color:#6482AD;\">Your Verification Code : " + code + "</h3>");
//                                        javaMailAPI.execute();
//                                        CharSequence text = "OTP Sent";
//                                        int duration = Toast.LENGTH_SHORT;
//                                        Toast toast = Toast.makeText(LoginAcitivity.this, text, duration);
//                                        toast.show();
//                                        LayoutInflater layoutInflater = LayoutInflater.from(LoginAcitivity.this);
//                                        View forgotPasswordAlertView = layoutInflater.inflate(R.layout.verification_template, null);
//
//                                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginAcitivity.this);
//                                        builder.setView(forgotPasswordAlertView);
//                                        AlertDialog resetPasswordAlertDialog = builder.create();
//                                        resetPasswordAlertDialog.show();
//                                        EditText otp = forgotPasswordAlertView.findViewById(R.id.verificationtestbox);
//
//                                        Button send = forgotPasswordAlertView.findViewById(R.id.verificationbutton);
//                                        send.setOnClickListener(new View.OnClickListener() {
//                                            @Override
//                                            public void onClick(View view) {
//                                                if (otp.getText().toString().isEmpty()) {
//                                                    CharSequence text = "please Enter OTP";
//                                                    int duration = Toast.LENGTH_SHORT;
//                                                    Toast toast = Toast.makeText(LoginAcitivity.this, text, duration);
//                                                    toast.show();
//                                                } else if (!otp.getText().toString().equals(String.valueOf(code))) {
//                                                    CharSequence text = "please Enter Valid OTP";
//                                                    int duration = Toast.LENGTH_SHORT;
//                                                    Toast toast = Toast.makeText(LoginAcitivity.this, text, duration);
//                                                    toast.show();
//                                                } else {
//                                                    AdminBean adminBean = new AdminBean();
//                                                    adminBean.setId(document.getId());
//                                                    adminBean.setFname(document.getString("fname"));
//                                                    adminBean.setLname(document.getString("lname"));
//                                                    adminBean.setMobile(document.getString("mobile"));
//                                                    adminBean.setEmail(document.getString("email"));
//                                                    Gson gson = new Gson();
//                                                    String admin = gson.toJson(adminBean);
//                                                    preferences.edit().putString("admin", admin).apply();
//
//                                                }
//                                            }
//                                        });
//                                    }
                                }
                            }
                        });


                    }
                }
            });
        } else {
            String u = preferences.getString("user", null);
            Gson gson = new Gson();
            UserBean user = gson.fromJson(u, UserBean.class);
            FirebaseFirestore bd = FirebaseFirestore.getInstance();
            bd.collection("user").whereEqualTo("mobile", user.getMobile()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if (document.getString("status").equals("active")) {
                            Intent intent = new Intent(LoginAcitivity.this, HomeActivity.class);
                            startActivity(intent);
                        } else {
                            CharSequence text = "your account is InActive";
                            int duration = Toast.LENGTH_SHORT;
                            Toast toast = Toast.makeText(LoginAcitivity.this, text, duration);
                            toast.show();

                        }
                    }
                }
            });
        }
    }
}