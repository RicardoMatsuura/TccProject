package com.ricardoyujimatsuura.tccproject.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.ricardoyujimatsuura.tccproject.R;
import com.ricardoyujimatsuura.tccproject.config.FirebaseConfig;
import com.ricardoyujimatsuura.tccproject.models.User;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {


    private EditText editTextEmail, editTextPassword;
    private User user;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.login);

        editTextEmail = findViewById(R.id.editTextEmailLogin);
        editTextPassword = findViewById(R.id.editTextPasswordLogin);
        Button buttonSignIn = findViewById(R.id.buttonSignIn);



        //botão de login

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();

                if(!email.isEmpty()) {
                    if(!password.isEmpty()) {

                        user = new User();
                        user.setEmail(email);
                        user.setPassword(password);
                        getEncryptedSharedPreferences().edit().putString("SHARED_PASSWORD", password).apply();
                        signInUser();
                    }
                    else {
                        Toast.makeText(LoginActivity.this, R.string.please_write_password, Toast.LENGTH_SHORT).show();
                    }
                }
                else {

                    Toast.makeText(LoginActivity.this, R.string.please_write_email, Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    public void openSignUpActivity(View view) {
          Intent i = new Intent(LoginActivity.this, SignUpActivity.class);
          startActivity(i);
    }

    //método para logar o usuário
    public void signInUser () {
        auth = FirebaseConfig.getFirebaseAuth();
        auth.signInWithEmailAndPassword(user.getEmail(),user.getPassword()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, R.string.sign_in_success, Toast.LENGTH_LONG).show();
                    goToGameActivity();
                }
                else {
                    try {
                        throw task.getException();
                    }
                    catch (FirebaseAuthInvalidUserException e) {
                        Toast.makeText(LoginActivity.this, R.string.login_invalid_password_or_email, Toast.LENGTH_LONG).show();
                    }
                    catch(FirebaseAuthInvalidCredentialsException e) {
                        Toast.makeText(LoginActivity.this, R.string.login_invalid_password_or_email, Toast.LENGTH_LONG).show();
                    }
                    catch (Exception e) {
                        Toast.makeText(LoginActivity.this, getString(R.string.sign_in_error) + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //método para trocar o usuário para a tela principal
    public void goToGameActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    MasterKey getMasterKey() {
        try {
            KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                    "_androidx_security_master_key_",
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build();

            return new MasterKey.Builder(getApplicationContext())
                    .setKeyGenParameterSpec(spec)
                    .build();
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), getString(R.string.error_master_key), e);
        }
        return null;
    }

    private SharedPreferences getEncryptedSharedPreferences() {
        try {
            return EncryptedSharedPreferences.create(
                    Objects.requireNonNull(getApplicationContext()),
                    "encrypted_shared_preferences",
                    getMasterKey(), // calling the method above for creating MasterKey
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), getString(R.string.error_getting_encrypted_shared_pref), e);
        }
        return null;
    }

}