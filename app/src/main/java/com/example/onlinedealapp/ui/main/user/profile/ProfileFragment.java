package com.example.onlinedealapp.ui.main.user.profile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.onlinedealapp.R;
import com.example.onlinedealapp.data.api.ApiConfig;
import com.example.onlinedealapp.data.api.UserService;
import com.example.onlinedealapp.data.model.ResponseModel;
import com.example.onlinedealapp.data.model.UserModel;
import com.example.onlinedealapp.databinding.FragmentProfileBinding;
import com.example.onlinedealapp.ui.main.auth.LoginActivity;
import com.example.onlinedealapp.util.Constants;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    SharedPreferences sharedPreferences;
    String userId, nama, telepon, email, kodePos;
    private UserService userService;
    AlertDialog progressDialog;
    private SharedPreferences.Editor editor;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        sharedPreferences = getContext().getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        userService = ApiConfig.getClient(requireContext()).create(UserService.class);
        userId = sharedPreferences.getString(Constants.SHARED_PREF_USER_ID, null);
        editor = sharedPreferences.edit();


        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getProfile();
        listener();
    }

    private void listener() {
        binding.btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();
            }
        });

        binding.btnUbahProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });
    }

    private void getProfile() {
        showProgressBar("Loading", "Loading data...", true);
        userService.getMyProfile(userId).enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    binding.tvNama.setText("Hai, " + response.body().getName());
                    nama = response.body().getName();
                    email = response.body().getEmail();
                    telepon = response.body().getPhoneNumber();
                    kodePos = response.body().getPostalCode();
                    showProgressBar("dsds", "Sdsd", false);
                } else {
                    showProgressBar("dsds", "Sdsd", false);
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                showProgressBar("dsds", "Sdsd", false);
                showToast("error", "No internet connection");

            }
        });
    }

    private void updateProfile() {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.layout_ubah_profile);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        EditText etNama, etPassword, etEmail, etTelepon, etKodePos;
        Button btnSimpan, btnBatal;
        btnBatal = dialog.findViewById(R.id.btnBatal);
        btnSimpan = dialog.findViewById(R.id.btnSimpan);
        etNama = dialog.findViewById(R.id.etNamaLengkap);
        etEmail = dialog.findViewById(R.id.etEmail);
        etPassword = dialog.findViewById(R.id.etPassword);
        etTelepon = dialog.findViewById(R.id.etTelepon);
        etKodePos = dialog.findViewById(R.id.etKodePos);

        etNama.setText(nama);
        etEmail.setText(email);
        etKodePos.setText(kodePos);
        etTelepon.setText(telepon);
        dialog.show();

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etNama.getText().toString().isEmpty()) {
                    etNama.setError("Name cannot be empty");
                    etNama.requestFocus();
                } else if (etTelepon.getText().toString().isEmpty()) {
                    etTelepon.setError("Phone no. cannot be empty");
                    etTelepon.requestFocus();
                } else if (etKodePos.getText().toString().isEmpty()) {
                    etKodePos.setError("Postal code cannot be empty");
                    etKodePos.requestFocus();
                } else {
                    showProgressBar("Loding", "Save profile...", true);
                    userService.updateProfile(userId, etPassword.getText().toString(), etNama.getText().toString(), etTelepon.getText().toString(), etKodePos.getText().toString()).enqueue(new Callback<ResponseModel>() {
                        @Override
                        public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                            if (response.isSuccessful() && response.body().getStatus() == 200) {
                                dialog.dismiss();
                                showProgressBar("ds", "sd", false);
                                getProfile();
                                showToast("success", "Successfully changed profile");
                            } else {
                                showProgressBar("ds", "sd", false);
                                showToast("error", "Failed to change profile");
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseModel> call, Throwable t) {
                            showProgressBar("ds", "sd", false);
                            showToast("error", "No internet connection");

                        }
                    });
                }
            }
        });


        btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }


    private void showProgressBar(String title, String message, boolean isLoading) {
        if (isLoading) {
            // Membuat progress dialog baru jika belum ada
            if (progressDialog == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(title);
                builder.setMessage(message);
                builder.setCancelable(false);
                progressDialog = builder.create();
            }
            progressDialog.show(); // Menampilkan progress dialog
        } else {
            // Menyembunyikan progress dialog jika ada
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    private void showToast(String jenis, String text) {
        if (jenis.equals("success")) {
            Toasty.success(getContext(), text, Toasty.LENGTH_SHORT).show();
        } else {
            Toasty.error(getContext(), text, Toasty.LENGTH_SHORT).show();
        }
    }

    private void logOut() {
        editor.clear().apply();
        startActivity(new Intent(getContext(), LoginActivity.class));
        getActivity().finish();

    }
}