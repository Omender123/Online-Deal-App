package com.example.onlinedealapp.ui.main.admin.rekening;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.onlinedealapp.R;
import com.example.onlinedealapp.data.api.AdminService;
import com.example.onlinedealapp.data.api.ApiConfig;
import com.example.onlinedealapp.data.model.RekeningModel;
import com.example.onlinedealapp.data.model.ResponseModel;
import com.example.onlinedealapp.databinding.FragmentCategoriesAdminBinding;
import com.example.onlinedealapp.databinding.FragmentRekeningAdminBinding;
import com.example.onlinedealapp.ui.main.admin.adapter.RekeningAdapter;
import com.example.onlinedealapp.util.Constants;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRekeningFragment extends Fragment {

    LinearLayoutManager linearLayoutManager;
    RekeningAdapter rekeningAdapter;
    private List<RekeningModel> rekeningModelList;
    SharedPreferences sharedPreferences;
    AlertDialog progressDialog;
    AdminService adminService;
    String userId;

    private FragmentRekeningAdminBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRekeningAdminBinding.inflate(inflater, container, false);
        sharedPreferences = getContext().getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        userId = sharedPreferences.getString(Constants.SHARED_PREF_USER_ID, null);
        adminService = ApiConfig.getClient(requireContext()).create(AdminService.class);



        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getRekening();
        listener();

    }

    private void listener() {

        binding.searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return false;
            }
        });
        binding.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertCategories();

            }
        });



    }

    private void getRekening() {
        showProgressBar("Loading", "Load categories data", true);
        adminService.getAllRekening().enqueue(new Callback<List<RekeningModel>>() {
            @Override
            public void onResponse(Call<List<RekeningModel>> call, Response<List<RekeningModel>> response) {
                if (response.isSuccessful() && response.body().size() > 0) {
                    rekeningModelList = response.body();
                    binding.tvEmpty.setVisibility(View.GONE);

                    rekeningAdapter = new RekeningAdapter(getContext(), rekeningModelList);
                    linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                    binding.rvRekening.setLayoutManager(linearLayoutManager);
                    binding.rvRekening.setAdapter(rekeningAdapter);
                    binding.rvRekening.setHasFixedSize(true);
                    showProgressBar("sd", "dssd", false);
                }else {
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                    showProgressBar("adss", "sdsd", false);
                }
            }

            @Override
            public void onFailure(Call<List<RekeningModel>> call, Throwable t) {
                binding.tvEmpty.setVisibility(View.VISIBLE);
                showProgressBar("adss", "sdsd", false);
                showToast("error", "No internet connection");

            }
        });
    }

    private void insertCategories() {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.layout_insert_rekening);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        Button btnBatal, btnSimpan;
        EditText etBankName, etNoRek, etNama;
        etBankName = dialog.findViewById(R.id.etNamaBank);
        etNoRek = dialog.findViewById(R.id.etNoRek);
        etNama = dialog.findViewById(R.id.etNama);
        btnSimpan = dialog.findViewById(R.id.btnSimpan);
        btnBatal = dialog.findViewById(R.id.btnBatal);
        dialog.show();

        btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etBankName.getText().toString().isEmpty()) {
                    etBankName.setError("Can not be empty");
                    etBankName.requestFocus();
                }else  if (etNoRek.getText().toString().isEmpty()) {
                    etNoRek.setError("Can not be empty");
                    etNoRek.requestFocus();
                }else  if (etNama.getText().toString().isEmpty()) {
                    etNama.setError("Can not be empty");
                    etNama.requestFocus();
                }
                else {
                    showProgressBar("Loading", "Added new categories", true);
                    adminService.insertRekening(
                            etBankName.getText().toString(),
                    etNoRek.getText().toString(),
                    etNama.getText().toString()
                            )
                            .enqueue(new Callback<ResponseModel>() {
                                @Override
                                public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                                    showProgressBar("s", "s", false);
                                    if (response.isSuccessful() && response.body().getStatus() == 200) {
                                        showToast("success", "Successfully added new account");
                                        getRekening();
                                        dialog.dismiss();

                                    }else {
                                        showToast("error", "There is an error");

                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseModel> call, Throwable t) {
                                    showProgressBar("s", "s", false);
                                    showToast("error", "No internet connection");



                                }
                            });
                }
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
        }else {
            Toasty.error(getContext(), text, Toasty.LENGTH_SHORT).show();
        }
    }

    private void filter(String text) {
        ArrayList<RekeningModel> filteredList = new ArrayList<>();
        for (RekeningModel item : rekeningModelList) {
            if (item.getBankName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }

            rekeningAdapter.filter(filteredList);
            if (filteredList.isEmpty()) {

            }else {
                rekeningAdapter.filter(filteredList);
            }
        }
    }
}