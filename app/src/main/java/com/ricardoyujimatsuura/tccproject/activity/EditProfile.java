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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ricardoyujimatsuura.tccproject.R;
import com.ricardoyujimatsuura.tccproject.config.FirebaseConfig;
import com.ricardoyujimatsuura.tccproject.models.User;

import java.util.Objects;

public class EditProfile extends AppCompatActivity {

    private FirebaseAuth auth;
    private User user;
    private String userId;
    private EditText editTextName, editTextEmail, editTextNickname;
    private String passwordSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        verifySignedInUser();
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.edit_profile);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        editTextName = findViewById(R.id.editTextNameEditProfile);
        editTextEmail = findViewById(R.id.editTextEmailEditProfile);
        editTextNickname = findViewById(R.id.editTextNicknameEditProfile);
        passwordSharedPreferences = getEncryptedSharedPreferences().getString("SHARED_PASSWORD", "Default value if null is returned or the key doesn't exist");

        if(auth != null) {
            getUserData();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu_edit_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menuItemSaveProfile) {
            saveUserData();
            return true;
        } else if(itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);

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

    public void getUserData() {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        userId = auth.getCurrentUser().getUid();

        firebase.child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                editTextName.setText(user.getName());
                editTextNickname.setText(user.getNickname());
                editTextEmail.setText(auth.getCurrentUser().getEmail());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public boolean saveUserData() {
        String name = editTextName.getText().toString();
        String email = editTextEmail.getText().toString();
        String nickname = editTextNickname.getText().toString();
        //re-autentica o usuario para fazer a mudança de email quando o usuario estiver logado a muito tempo
        AuthCredential credential = EmailAuthProvider.getCredential(auth.getCurrentUser().getEmail(), passwordSharedPreferences);


        if(name.isEmpty()) {
            Toast.makeText(EditProfile.this, R.string.empty_name_sign_up, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(email.isEmpty()) {
            Toast.makeText(EditProfile.this, R.string.empty_email_sign_up, Toast.LENGTH_SHORT).show();
            return false;
        }
        User user = new User();
        user.setName(name);
        user.setNickname(nickname);
        user.setIdFirebase(auth.getCurrentUser().getUid());
        auth.getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    auth.getCurrentUser().updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                user.saveToFirebase();
                                finish();
                                Toast.makeText(getApplicationContext(), R.string.profile_updated, Toast.LENGTH_LONG).show();
                            }
                            else {
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthInvalidCredentialsException e) {
                                    Toast.makeText(EditProfile.this, R.string.sign_up_invalid_email, Toast.LENGTH_LONG).show();
                                }
                                catch (FirebaseAuthUserCollisionException e) {
                                    Toast.makeText(EditProfile.this, R.string.sign_up_email_already_registered, Toast.LENGTH_LONG).show();
                                }
                                catch (Exception e) {
                                    Toast.makeText(EditProfile.this, getString(R.string.error_updating_email) + e.getMessage(), Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(EditProfile.this, R.string.error_authenticating_user, Toast.LENGTH_LONG).show();
                }

            }
        });

        return true;

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