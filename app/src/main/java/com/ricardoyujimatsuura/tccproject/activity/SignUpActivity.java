package com.ricardoyujimatsuura.tccproject.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.ricardoyujimatsuura.tccproject.R;
import com.ricardoyujimatsuura.tccproject.config.FirebaseConfig;
import com.ricardoyujimatsuura.tccproject.models.User;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextName, editTextPassword, editTextNickname;
    private Button buttonSignUp;
    private FirebaseAuth auth;
    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.register);

        //Campos de texto da página de cadastro

        editTextEmail = findViewById(R.id.editTextEmailSignUp);
        editTextName = findViewById(R.id.editTextNameSignUp);
        editTextNickname = findViewById(R.id.editTextNicknameSignUp);
        editTextPassword = findViewById(R.id.editTextPasswordSignUp);
        buttonSignUp = findViewById(R.id.buttonSignUp);

        //botão de cadastrar

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editTextName.getText().toString();
                String email = editTextEmail.getText().toString();
                String nickname = editTextNickname.getText().toString();
                String password = editTextPassword.getText().toString();

                //validação
                    if(!name.isEmpty()) {
                        if(!email.isEmpty()) {
                            if(!password.isEmpty()) {

                                user = new User();
                                user.setName(name);
                                user.setEmail(email);
                                user.setNickname(nickname);
                                user.setPassword(password);
                                signUpUser();
                            }
                            else {
                                Toast.makeText(SignUpActivity.this, R.string.empty_password_sign_up, Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {

                            Toast.makeText(SignUpActivity.this, R.string.empty_email_sign_up, Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(SignUpActivity.this, R.string.empty_name_sign_up, Toast.LENGTH_SHORT).show();
                    }

            }
        });


    }

    public void signUpUser() {
        auth = FirebaseConfig.getFirebaseAuth();
        auth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(SignUpActivity.this, R.string.success_message_sign_up, Toast.LENGTH_SHORT).show();
                    FirebaseUser registeredUser = task.getResult().getUser();
                    user.setIdFirebase(registeredUser.getUid());
                    user.saveToFirebase();
                    finish();
                }
                else {
                    try {
                        throw task.getException();
                    }
                    catch (FirebaseAuthWeakPasswordException e) {
                        Toast.makeText(SignUpActivity.this, R.string.sign_up_weak_password, Toast.LENGTH_LONG).show();
                    }
                    catch (FirebaseAuthInvalidCredentialsException e) {
                        Toast.makeText(SignUpActivity.this, R.string.sign_up_invalid_email, Toast.LENGTH_LONG).show();
                    }
                    catch (FirebaseAuthUserCollisionException e) {
                        Toast.makeText(SignUpActivity.this, R.string.sign_up_email_already_registered, Toast.LENGTH_LONG).show();
                    }
                    catch (Exception e) {
                        Toast.makeText(SignUpActivity.this, getString(R.string.sign_up_error) + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                }
            }
        });

    }
}