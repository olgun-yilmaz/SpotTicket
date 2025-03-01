package com.olgunyilmaz.spotticket.view.fragments;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.databinding.FragmentReauthenticateDialogBinding;
import com.olgunyilmaz.spotticket.view.activities.EmailPasswordActivity;
import com.olgunyilmaz.spotticket.view.activities.OnBoardingActivity;

public class ReAuthenticateDialogFragment extends DialogFragment {
    private EditText reAutheditTextPassword;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private Runnable runnable;
    private Handler handler;
    int counter = 0;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getActivity() == null) {
            throw new IllegalStateException("Activity cannot be null");
        }
        db = FirebaseFirestore.getInstance();

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

    private void deleteProfileImage(String email, AlertDialog dialog){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        String dir_name = "pp";
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child("images").child(dir_name).child(email + ".jpg");

        imageRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                deleteAccount(dialog); // success -> optional
            }
        });
    }

    private void deleteFavoriteEvents(String email, AlertDialog dialog) {
        String collection = email + "_Events";
        db.collection(collection).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        db.collection(collection).document(document.getId())
                                .delete();
                    }
                    deleteUserData(email, dialog);
                }else{
                    deleteAccount(dialog); // anyway delete account
                }
            }
        });
    }

    private void deleteUserData(String email, AlertDialog dialog) {
        String collection = "Users";
        db.collection(collection)
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) { // update
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        String documentId = documentSnapshot.getId();

                        db.collection("Users").document(documentId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    deleteProfileImage(email, dialog);
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        deleteAccount(dialog); //anyway delete account
                    }
                });
    }

    private void goBackToOnBoarding() {
        handler.removeCallbacks(runnable);
        Toast.makeText(requireActivity(),"Hesabınız başarıyla silindi!",Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getActivity(), OnBoardingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void reauthenticateAndDelete(String password, AlertDialog dialog) {
        if (user != null && user.getEmail() != null && getActivity() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
            user.reauthenticate(credential)
                    .addOnSuccessListener(unused -> {
                        dialog.setTitle("Veriler Siliniyor");
                        updateLoadingText(dialog);
                        deleteFavoriteEvents(user.getEmail(), dialog);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getActivity(),
                                "Kimlik doğrulama başarısız: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void deleteAccount(AlertDialog dialog){
        user.delete()
                .addOnSuccessListener(aVoid -> {
                    dialog.dismiss();
                    goBackToOnBoarding();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(),
                            "Hesap silme başarısız: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void updateLoadingText(AlertDialog dialog) {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                counter++;
                int numPoint = counter % 4;
                String numPointText = ". ".repeat(numPoint) + "  ".repeat(4 - numPoint);
                dialog.setMessage("Siliniyor " + numPointText);
                handler.postDelayed(runnable, 1000);
            }
        };
        handler.post(runnable);
    }
}
