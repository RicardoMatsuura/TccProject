package com.ricardoyujimatsuura.tccproject.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.ricardoyujimatsuura.tccproject.R;
import com.ricardoyujimatsuura.tccproject.config.FirebaseConfig;

import java.util.Objects;

public class ChangePasswordActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText editTextNewPassword, editTextConfirmNewPassword;
    private String passwordSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        verifySignedInUser();

        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.change_password);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        passwordSharedPreferences = getEncryptedSharedPreferences().getString("SHARED_PASSWORD", "Default value if null is returned or the key doesn't exist");

        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextConfirmNewPassword = findViewById(R.id.editTextConfirmPassword);
    }

    public void verifySignedInUser() {
        auth = FirebaseConfig.getFirebaseAuth();

        //auth.signOut();
        if ( auth.getCurrentUser() == null) {
            goToLoginActivity();
        }
    }

    public void goToLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu_change_password, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if(itemId == R.id.menuItemSaveNewPassword) {
            saveNewPassword();
            return true;

        } else if(itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean saveNewPassword() {
        String newPassword = editTextNewPassword.getText().toString();
        String confirmNewPassword = editTextConfirmNewPassword.getText().toString();

        //para fazer a mudança de senha
        AuthCredential credential = EmailAuthProvider.getCredential(auth.getCurrentUser().getEmail(), passwordSharedPreferences);

        if(newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            Toast.makeText(ChangePasswordActivity.this, R.string.password_empty, Toast.LENGTH_LONG).show();
            return false;

        }

        if(!newPassword.equals(confirmNewPassword)) {
            Toast.makeText(ChangePasswordActivity.this, R.string.password_dont_match, Toast.LENGTH_LONG).show();
            return false;
        }

        auth.getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    auth.getCurrentUser().updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                getEncryptedSharedPreferences().edit().putString("SHARED_PASSWORD", newPassword).apply();
                                finish();
                                Toast.makeText(getApplicationContext(), R.string.password_updated, Toast.LENGTH_LONG).show();
                            }
                            else {
                                try{
                                    throw task.getException();
                                }
                                catch (FirebaseAuthWeakPasswordException e) {
                                    Toast.makeText(ChangePasswordActivity.this, R.string.sign_up_weak_password, Toast.LENGTH_LONG).show();
                                }
                                catch(Exception e ) {
                                    Toast.makeText(ChangePasswordActivity.this, getString(R.string.error_changing_password) + e.getMessage(), Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
                else {
                }
            }
        });
        return true;

    }

    //métodos para encrypt shared preferences
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