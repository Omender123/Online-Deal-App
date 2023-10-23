package com.example.onlinedealapp.ui.main.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.onlinedealapp.R;
import com.example.onlinedealapp.data.api.ApiConfig;
import com.example.onlinedealapp.data.api.AuthService;
import com.example.onlinedealapp.data.model.AuthModel;
import com.example.onlinedealapp.ui.main.admin.AdminMainActivity;
import com.example.onlinedealapp.ui.main.owner.OwnerMainActivity;
import com.example.onlinedealapp.ui.main.user.UserMainActivity;
import com.example.onlinedealapp.util.Constants;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    EditText etEmail, etPassword;
    Button btnLogin;
    AuthService authService;
    TextView tvDaftar;
    AlertDialog progressDialog;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        authService = ApiConfig.getClient(this).create(AuthService.class);
        sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvDaftar = findViewById(R.id.tvDaftar);

        if (sharedPreferences.getBoolean(Constants.SHARED_PREF_LOGGED, false)) {
            if (sharedPreferences.getString(Constants.SHARED_PREF_ROLE, null).equals("USER")) {
                startActivity(new Intent(LoginActivity.this, UserMainActivity.class));
                finish();

            }else if (sharedPreferences.getString(Constants.SHARED_PREF_ROLE, null).equals("ADMIN")) {
                startActivity(new Intent(LoginActivity.this, AdminMainActivity.class));
                finish();
            }else if (sharedPreferences.getString(Constants.SHARED_PREF_ROLE, null).equals("OWNER")) {
                startActivity(new Intent(LoginActivity.this, OwnerMainActivity.class));
                finish();
            }
        }

        listener();
    }

    private void listener() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etEmail.getText().toString().isEmpty()) {
                    etEmail.setError("Email Can not be empty");
                    etEmail.requestFocus();
                }else  if (etPassword.getText().toString().isEmpty()) {
                    etPassword.setError("Password Can not be empty");
                    etPassword.requestFocus();
                }else {
                    login();
                }
            }
        });

        tvDaftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));

            }
        });
    }

    private void login() {
        showProgressBar("Loading", "Authentication...", true);
        authService.login(etEmail.getText().toString(), etPassword.getText().toString()).enqueue(new Callback<AuthModel>() {
            @Override
            public void onResponse(Call<AuthModel> call, Response<AuthModel> response) {
                if (response.isSuccessful() && response.body().getStatus() == 200) {

                  if (response.body().getRole().equals("USER")) {
                      editor.putBoolean(Constants.SHARED_PREF_LOGGED, true);
                      editor.putString(Constants.SHARED_PREF_USER_ID, response.body().getUserId());
                      editor.putString(Constants.SHARED_PREF_ROLE, response.body().getRole());
                      editor.apply();
                      startActivity(new Intent(LoginActivity.this, UserMainActivity.class));
                      finish();

                      showProgressBar("dsd", "Dsd", false);
                  }else if (response.body().getRole().equals("ADMIN")) {
                      editor.putBoolean(Constants.SHARED_PREF_LOGGED, true);
                      editor.putString(Constants.SHARED_PREF_USER_ID, response.body().getUserId());
                      editor.putString(Constants.SHARED_PREF_ROLE, response.body().getRole());
                      editor.apply();
                      startActivity(new Intent(LoginActivity.this, AdminMainActivity.class));
                      finish();

                      showProgressBar("dsd", "Dsd", false);
                  }else if (response.body().getRole().equals("OWNER")) {
                      editor.putBoolean(Constants.SHARED_PREF_LOGGED, true);
                      editor.putString(Constants.SHARED_PREF_USER_ID, response.body().getUserId());
                      editor.putString(Constants.SHARED_PREF_ROLE, response.body().getRole());
                      editor.apply();
                      startActivity(new Intent(LoginActivity.this, OwnerMainActivity.class));
                      finish();

                      showProgressBar("dsd", "Dsd", false);
                  }

                }else {
                    showProgressBar("sdsd", "sdds", false);
                    showToast("error", response.body().getMessage());
                }
            }

            @Override
            public void onFailure(Call<AuthModel> call, Throwable t) {
                showProgressBar("sdsd", "sdds", false);
                showToast("error", "No internet connection");

            }
        });
    }

    private void showProgressBar(String title, String message, boolean isLoading) {
        if (isLoading) {
            // Membuat progress dialog baru jika belum ada
            if (progressDialog == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
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
            Toasty.success(LoginActivity.this, text, Toasty.LENGTH_SHORT).show();
        }else {
            Toasty.error(LoginActivity.this, text, Toasty.LENGTH_SHORT).show();
        }
    }
}