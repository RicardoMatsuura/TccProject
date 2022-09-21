package com.ricardoyujimatsuura.tccproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth user = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Cadastro de usuário

        user.createUserWithEmailAndPassword("teste@teste.com", "12345").addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Log.i("CreateUser", "Sucesso");
                }
                else {
                    Log.i("CreateUSer", "falha");
                }
            }
        });

        //Verifica usuario logado
        if(user.getCurrentUser() != null) {
            Log.i("CreateUSer", "logado");
        }
        else {
            Log.i("CreateUSer", "não logado");
        }

        //deslogar user

        user.signOut();

        //logar usuario

        user.signInWithEmailAndPassword("teste@teste.com", "12345").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Log.i("CreateUser", "usuario logado");
                }
                else {
                    Log.i("CreateUSer", "falha no login");
                }
            }
        });

        //gerar identificador unico (ID) no firebase

        user.push().setValue();

        //
    }
}