package com.example.callback;

import static android.content.ContentValues.TAG;
import static com.example.callback.MainActivity.firebaseAuth;
import static com.example.callback.MainActivity.userDatabase;
import static com.example.callback.MainActivity.vehicleDatabase;

import static java.lang.String.*;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.Random;

public class Login extends AppCompatActivity {
    EditText nameEditText, emailEditText, phoneNumberEditText, passwordEditText, confirmPasswordEditText, busPlateNumberEditText;
    CheckBox driver;
    boolean isDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findViewById(R.id.signUpButton).setOnClickListener(view -> startSignUp());
        emailEditText = findViewById(R.id.editTextEmailAddress);
        passwordEditText = findViewById(R.id.editTextPassword);

        findViewById(R.id.signInButton).setOnClickListener(view -> loginUser());
    }

    void loginUser() {
        String emailLogin = emailEditText.getText().toString();
        String passwordLogin = passwordEditText.getText().toString();
//        boolean detailsValidated = areValid(emailLogin, passwordLogin);
        loginUserInFirebase(emailLogin, passwordLogin);
        }

    void loginUserInFirebase(String emailLogin, String passwordLogin) {
        firebaseAuth.signInWithEmailAndPassword(emailLogin, passwordLogin).addOnCompleteListener(
                task -> {
                    if (task.isSuccessful()) {
                        if (Objects.requireNonNull(firebaseAuth.getCurrentUser()).isEmailVerified()) {
                            Toast.makeText(Login.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Login.this, MainActivity.class));
                        } else {
                            Toast.makeText(Login.this, "You need to verify your email", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(Login.this, Objects.requireNonNull(task.getException()).getLocalizedMessage(), Toast.LENGTH_LONG).show();

                    }
                }
        );
    }



    private void startSignUp(){
        setContentView(R.layout.signup_file);
        driver = findViewById(R.id.checkBox);
        isDriver = driver.isChecked();
        nameEditText = findViewById(R.id.editTextName);
        phoneNumberEditText = findViewById(R.id.editTextPhone);
        emailEditText = findViewById(R.id.emailAddress3);
        passwordEditText = findViewById(R.id.editTextTextPassword2);
        confirmPasswordEditText = findViewById(R.id.editTextConfirmPassword);
        busPlateNumberEditText = findViewById(R.id.editTextVehicleRegistration);
        if (!driver.isChecked())
            busPlateNumberEditText.setEnabled(false);
        findViewById(R.id.createAccountButton).setOnClickListener(view -> CreateUser());

    }

    void CreateUser() {
        String name = nameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String phone = phoneNumberEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();
        String vehiclePlate = null;
        if (isDriver) {
            vehiclePlate = busPlateNumberEditText.getText().toString();
        }


        boolean isValidated = validateData(email, phone, password, confirmPassword);
        if (!isValidated) {
            return;
        }
        if (vehiclePlate==null)
            isDriver = false;
        createAccountInFirebase(name, email, phone, password, vehiclePlate);


    }

    void createAccountInFirebase(String name, String email, String phone, String password, @Nullable String vehiclePlate) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(Login.this,
                task -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(Login.this, "Account Created Successfully, verify your Email", Toast.LENGTH_SHORT).show();
                        Objects.requireNonNull(firebaseAuth.getCurrentUser()).sendEmailVerification();

                        String myVehicleCode = null;
                        if (isDriver && !Objects.equals(vehiclePlate, "")) {
                            myVehicleCode = generateVehicleCode(vehiclePlate, firebaseAuth.getCurrentUser().getUid());
                        }
                        Users user = new Users(firebaseAuth.getCurrentUser().getUid(), name, email, phone, isDriver, vehiclePlate, myVehicleCode);

                        userDatabase.child(firebaseAuth.getCurrentUser().getUid()).setValue(user);
                        userDatabase.child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Users newUser = snapshot.getValue(Users.class);
                                assert newUser != null;
                                Log.i(TAG, "onDataChange: successful retrieval " + newUser.email);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                                Log.i(TAG, "onCancelled: there is an error " + error.getMessage());
                            }
                        });
                        firebaseAuth.signOut();

                    }
                    else{
                        Toast.makeText(Login.this, Objects.requireNonNull(task.getException()).getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
        setContentView(R.layout.activity_login);
    }

    private String generateVehicleCode(String vehiclePlate, String userId) {
        int len = 6;
        String chars = "123456789ABCDEFGHIJKLMNPQRSTUVWXYZ";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        vehicleDatabase.orderByChild("VehicleCode").equalTo(valueOf(sb)).addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                if(dataSnapshot.exists()) {
                    Log.i(TAG, "onDataChange: new vehicle code is  "+ sb);
                    generateVehicleCode(vehiclePlate, userId);


                }
                else{//String vehicleId =
                    vehicleDatabase.push().getKey();
                    MyVehicle myVehicle = new MyVehicle(userId, vehiclePlate, valueOf(sb));
                    vehicleDatabase.child(valueOf(sb)).setValue(myVehicle);



                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i(TAG, "onCancelled: There is an error");
            }  });

        return valueOf(sb);
    }

    boolean validateData(String email,String phone,String password, String confirmPassword){
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailEditText.setError("Wrong Email Format");
            return false;
        }
        if(!Patterns.PHONE.matcher(phone).matches()){
            phoneNumberEditText.setError("Wrong Phone Format");
            return false;
        }
        if (password.length()<6){
            passwordEditText.setError("Password too short!");
            return false;
        }
        if (!password.equals(confirmPassword)){
            confirmPasswordEditText.setError("Passwords do not match!");
            return false;
        }
        return true;
    }
}