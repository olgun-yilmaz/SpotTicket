package com.olgunyilmaz.spotticket.view.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.FragmentReauthenticateDialogBinding;
import com.olgunyilmaz.spotticket.view.activities.EmailPasswordActivity;
import com.olgunyilmaz.spotticket.view.activities.OnBoardingActivity;

public class ReAuthenticateDialogFragment extends DialogFragment {
    private EditText reAutheditTextPassword;
    private FirebaseUser user;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getActivity() == null) {
            throw new IllegalStateException("Activity cannot be null");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_reauthenticate_dialog, null);

        reAutheditTextPassword = view.findViewById(R.id.reAuthEditTextPassword);
        user = FirebaseAuth.getInstance().getCurrentUser();

        AlertDialog dialog = builder.setView(view)
                .setTitle("Hesap Silme")
                .setMessage("Hesabınızı silmek için şifrenizi girin")
                .setPositiveButton("Onayla", null)
                .setNegativeButton("İptal", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String password = reAutheditTextPassword.getText().toString().trim();
                if (!password.isEmpty()) {
                    reauthenticateAndDelete(password, dialog);
                } else {
                    Toast.makeText(getContext(), "Şifre boş olamaz", Toast.LENGTH_SHORT).show();
                }
            });
        });

        return dialog;
    }

    private void reauthenticateAndDelete(String password, AlertDialog dialog) {
        if (user != null && user.getEmail() != null && getActivity() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
            user.reauthenticate(credential)
                    .addOnSuccessListener(unused -> {
                        user.delete()
                                .addOnSuccessListener(aVoid -> {
                                    dialog.dismiss();
                                    Toast.makeText(getActivity(), 
                                            "Hesabınız başarıyla silindi!", 
                                            Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(getActivity(), OnBoardingActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    getActivity().finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getActivity(), 
                                            "Hesap silme başarısız: " + e.getMessage(), 
                                            Toast.LENGTH_LONG).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getActivity(), 
                                "Kimlik doğrulama başarısız: " + e.getMessage(), 
                                Toast.LENGTH_LONG).show();
                    });
        }
    }
}
