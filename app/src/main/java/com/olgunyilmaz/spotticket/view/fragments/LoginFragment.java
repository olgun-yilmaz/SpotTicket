package com.olgunyilmaz.spotticket.view.fragments;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.FragmentLoginBinding;
import com.olgunyilmaz.spotticket.view.activities.MainActivity;

public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    FragmentManager fragmentManager;
    private FirebaseAuth auth;
    private Runnable runnable;
    private Handler handler;
    int counter = 0;

    public LoginFragment() {}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(getLayoutInflater(),container,false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fragmentManager = requireActivity().getSupportFragmentManager();
        auth = FirebaseAuth.getInstance();
        auth.setLanguageCode("tr");

        binding.loginButton.setOnClickListener(v -> login());

        binding.loginSignUpButton.setOnClickListener(v -> signUp());

        binding.resetPasswordText.setOnClickListener(v -> resetPassword());
    }

    private void normalMode(){
        binding.resetPasswordText.setText("- - - Şifreni mi unuttun? - - -");
        binding.resetPasswordText.setEnabled(true);

        binding.loginUsernameText.setVisibility(View.VISIBLE);
        binding.loginSignUpButton.setVisibility(View.VISIBLE);
        binding.loginButton.setVisibility(View.VISIBLE);
        binding.loginPasswordText.setVisibility(View.VISIBLE);

        handler.removeCallbacks(runnable);

    }

    private void loadingMode(){
        updateLoadingText();
        binding.resetPasswordText.setEnabled(false);

        binding.loginUsernameText.setVisibility(View.INVISIBLE);
        binding.loginSignUpButton.setVisibility(View.INVISIBLE);
        binding.loginButton.setVisibility(View.INVISIBLE);
        binding.loginPasswordText.setVisibility(View.INVISIBLE);
    }

    private void login(){
        String email = binding.loginUsernameText.getText().toString();
        String password = binding.loginPasswordText.getText().toString();

        if (!email.isEmpty() && !password.isEmpty()){
            loadingMode();

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Kullanıcı girişi başarılı");
                                FirebaseUser currentUser = auth.getCurrentUser();

                                if (currentUser != null){

                                    String msg;

                                    if(currentUser.isEmailVerified()){
                                        Intent intent = new Intent(getContext(), MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        requireActivity().finish();
                                        msg = "Hoşgeldin : ";
                                    }else{
                                        normalMode();
                                        msg = "Giriş yapabilmek için gönderilen linkten e-posta adresinizi doğrulamanız gerekmektedir.";
                                    }
                                    Toast.makeText(getContext(),msg+currentUser.getDisplayName()
                                            ,Toast.LENGTH_LONG).show();


                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            normalMode();
                            Toast.makeText(getContext(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                        }
                    });

        }else{
            Toast.makeText(getContext(),"Lütfen email ve parolanızı girin.",Toast.LENGTH_LONG).show();
        }
    }

    private void updateLoadingText() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                counter++;
                int numPoint = counter % 4;
                String numPointText = ". ".repeat(numPoint) + "  ".repeat(4 - numPoint);
                binding.resetPasswordText.setText("Lütfen Bekleyiniz " + numPointText);
                handler.postDelayed(runnable, 1000);
            }
        };
        handler.post(runnable);
    }


    private void signUp(){
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        SignUpFragment signUpFragment = new SignUpFragment();
        fragmentTransaction.replace(R.id.loginFragmentContainer, signUpFragment).commit();

    }

    private void resetPassword(){
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ResetPasswordFragment resetPasswordFragment = new ResetPasswordFragment();
        fragmentTransaction.replace(R.id.loginFragmentContainer, resetPasswordFragment).commit();
    }

}