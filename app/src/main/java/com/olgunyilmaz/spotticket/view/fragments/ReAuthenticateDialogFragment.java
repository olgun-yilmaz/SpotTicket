/*
 * Project: SpotTicket
 * Copyright 2025 Olgun Yılmaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.olgunyilmaz.spotticket.view.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.olgunyilmaz.spotticket.util.LocalDataManager;
import com.olgunyilmaz.spotticket.R;
import com.olgunyilmaz.spotticket.util.UserManager;
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
        db = FirebaseFirestore.getInstance();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_reauthenticate_dialog, null);

        reAutheditTextPassword = view.findViewById(R.id.reAuthEditTextPassword);
        user = FirebaseAuth.getInstance().getCurrentUser();

        AlertDialog dialog = builder.setView(view)
                .setTitle(getString(R.string.delete_account_text))
                .setMessage(getString(R.string.enter_password_for_delete_account))
                .setPositiveButton(getString(R.string.answer_confirm), null)
                .setNegativeButton(getString(R.string.answer_cancel), null)
                .create();

        dialog.setOnShowListener(dialogInterface -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String password = reAutheditTextPassword.getText().toString().trim();
            if (!password.isEmpty()) {
                reauthenticateAndDelete(password, dialog);
            } else {
                Toast.makeText(getContext(),
                        getString(R.string.enter_password_for_delete_account), Toast.LENGTH_SHORT).show();
            }
        }));

        return dialog;
    }

    private void deleteProfileImage(String email, AlertDialog dialog) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        String dir_name = "pp";
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child("images").child(dir_name).child(email + ".jpg");

        imageRef.delete().addOnCompleteListener(task -> {
            deleteAccount(dialog); // success -> optional
        });
    }

    private void deleteFavoriteEvents(String email, AlertDialog dialog) {
        String collection = email + getString(R.string.my_events_key);
        db.collection(collection).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    db.collection(collection).document(document.getId())
                            .delete();
                }
                deleteUserData(email, dialog);
            } else {
                deleteAccount(dialog); // anyway delete account
            }
        });
    }

    private void deleteUserData(String email, AlertDialog dialog) {
        db.collection(getString(R.string.users_collection_key))
                .whereEqualTo(getString(R.string.email_key), email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) { // update
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        String documentId = documentSnapshot.getId();

                        db.collection(getString(R.string.users_collection_key)).document(documentId)
                                .delete()
                                .addOnSuccessListener(aVoid -> deleteProfileImage(email, dialog));
                    }
                }).addOnFailureListener(e -> {
                    deleteAccount(dialog); //anyway delete account
                });
    }

    private void goBackToOnBoarding() {
        handler.removeCallbacks(runnable);
        Toast.makeText(requireActivity(), getString(R.string.successfully_delete_account_text), Toast.LENGTH_LONG).show();
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
                        dialog.setTitle(getString(R.string.deleting_data_text));
                        updateLoadingText(dialog);
                        deleteFavoriteEvents(user.getEmail(), dialog);
                    })
                    .addOnFailureListener(e -> Toast.makeText(requireActivity(),
                            getString(R.string.validation_error_text),
                            Toast.LENGTH_LONG).show());
        }
    }

    private void deleteAccount(AlertDialog dialog) {
        user.delete()
                .addOnSuccessListener(aVoid -> {

                    LocalDataManager localDataManager = new LocalDataManager(requireActivity());
                    localDataManager.deleteAllData();
                    UserManager.getInstance().profileImage = null;

                    dialog.dismiss();
                    goBackToOnBoarding();
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(),
                        getString(R.string.delete_account_error_text),
                        Toast.LENGTH_LONG).show());
    }

    private void updateLoadingText(AlertDialog dialog) {
        handler = new Handler();
        runnable = () -> {
            if (isAdded()) {
                counter++;
                int numPoint = counter % 4;
                String numPointText = " .".repeat(numPoint) + "  ".repeat(4 - numPoint);
                dialog.setMessage(getString(R.string.deleting_text) + numPointText);
                handler.postDelayed(runnable, 1000);
            }
        };
        handler.post(runnable);
    }
}