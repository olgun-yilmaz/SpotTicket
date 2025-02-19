package com.olgunyilmaz.spotticket.view;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FirebaseAuth auth;
    private String email;
    private String password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        auth = FirebaseAuth.getInstance();
    }

    public void signUp(View view){

        email = binding.emailText.getText().toString();
        password = binding.passwordText.getText().toString();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Başarıyla kayıt oldunuz");
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null){
                                goToMainActivity(user);
                            }
                        } else {
                            Log.w(TAG, "Kayıt olunamadı!", task.getException());
                            Toast.makeText(LoginActivity.this, "Kayıt olunamadı!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    public void login(View view){

        email = binding.emailText.getText().toString();
        password = binding.passwordText.getText().toString();

        if (!email.isEmpty() && !password.isEmpty()){

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                System.out.println("baiaşrılı");
                                Log.d(TAG, "Kullanıcı girişi başarılı");
                                FirebaseUser currentUser = auth.getCurrentUser();

                                if (currentUser != null){
                                    Toast.makeText(LoginActivity.this,"Hoşgeldin : "+currentUser.getEmail()
                                            ,Toast.LENGTH_LONG).show();
                                    goToMainActivity(currentUser);
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LoginActivity.this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                        }
                    });

        }else{
            Toast.makeText(LoginActivity.this,"Lütfen email ve parolanızı girin.",Toast.LENGTH_LONG).show();
        }
    }

    private void goToMainActivity(FirebaseUser user){
        System.out.println(user.getEmail());
        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

}