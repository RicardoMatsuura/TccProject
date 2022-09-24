package com.ricardoyujimatsuura.tccproject.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.ricardoyujimatsuura.tccproject.R;
import com.ricardoyujimatsuura.tccproject.config.FirebaseConfig;
import com.ricardoyujimatsuura.tccproject.models.User;

public class LoginActivity extends AppCompatActivity {

    /*private FirebaseAuth user = FirebaseAuth.getInstance();
    private DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference users = reference.child("usuario");*/

    private EditText editTextEmail, editTextPassword;
    private Button buttonSignIn;
    private User user;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.editTextEmailLogin);
        editTextPassword = findViewById(R.id.editTextPasswordLogin);
        buttonSignIn = findViewById(R.id.buttonSignIn);

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
                        signInUser();
                    }
                    else {
                        Toast.makeText(LoginActivity.this, "Please write a password", Toast.LENGTH_SHORT).show();
                    }
                }
                else {

                    Toast.makeText(LoginActivity.this, "Please write an e-mail.", Toast.LENGTH_SHORT).show();
                }

            }
        });


        //Cadastro de usuário

        /*user.createUserWithEmailAndPassword("teste@teste.com", "12345").addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.i("CreateUser", "Sucesso");
                } else {
                    Log.i("CreateUSer", "falha");
                }
            }
        });

        //Verifica usuario logado
        if (user.getCurrentUser() != null) {
            Log.i("CreateUSer", "logado");
        } else {
            Log.i("CreateUSer", "não logado");
        }

        //deslogar user

        user.signOut();

        //logar usuario

        user.signInWithEmailAndPassword("teste@teste.com", "12345").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.i("CreateUser", "usuario logado");
                } else {
                    Log.i("CreateUSer", "falha no login");
                }
            }
        });

        //gerar identificador unico (ID) no firebase

        users.push().setValue("");

        //query firebase
        Query userSearch = users.orderByChild("game").equalTo("valorant");
        Query userSearch2 = users.orderByKey().limitToFirst(2); //limit as 2 primeiras linhas
        Query userSearch3 = users.orderByChild("idade").startAt(18).endAt(22); //pesquisa com maior ou igual
        Query userSearch4 = users.orderByChild("nome").startAt("J").endAt("J" + "\uf8ff"); //pesquisa com letras, adicionar o \uf8ff para isso
        //criar indice no firebase
        /*Regras > Exemplo no JSON : "usuarios" : {
                                            ".indexOn": ["nome", "email"]
                                                 }*/
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
}