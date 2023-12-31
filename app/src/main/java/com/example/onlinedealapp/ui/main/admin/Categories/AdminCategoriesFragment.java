package com.example.onlinedealapp.ui.main.admin.Categories;

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
import com.example.onlinedealapp.data.model.CategoriesModel;
import com.example.onlinedealapp.data.model.ResponseModel;
import com.example.onlinedealapp.databinding.FragmentCategoriesAdminBinding;
import com.example.onlinedealapp.databinding.FragmentTransactionsAdminBinding;
import com.example.onlinedealapp.ui.main.admin.adapter.CategoriesAdapter;
import com.example.onlinedealapp.util.Constants;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminCategoriesFragment extends Fragment {

    LinearLayoutManager linearLayoutManager;
    CategoriesAdapter categoriesAdapter;
    private List<CategoriesModel> categoriesModelList;
    SharedPreferences sharedPreferences;
    AlertDialog progressDialog;
    AdminService adminService;
    String userId;

    private FragmentCategoriesAdminBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCategoriesAdminBinding.inflate(inflater, container, false);
        sharedPreferences = getContext().getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        userId = sharedPreferences.getString(Constants.SHARED_PREF_USER_ID, null);
        adminService = ApiConfig.getClient(requireContext()).create(AdminService.class);



        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getCategories();
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

    private void getCategories() {
        showProgressBar("Loading", "Load categories data", true);
        adminService.getCategories().enqueue(new Callback<List<CategoriesModel>>() {
            @Override
            public void onResponse(Call<List<CategoriesModel>> call, Response<List<CategoriesModel>> response) {
                if (response.isSuccessful() && response.body().size() > 0) {
                    categoriesModelList = response.body();
                    binding.tvEmpty.setVisibility(View.GONE);

                    categoriesAdapter = new CategoriesAdapter(getContext(), categoriesModelList);
                    linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                    binding.rvKategori.setLayoutManager(linearLayoutManager);
                    binding.rvKategori.setAdapter(categoriesAdapter);
                    binding.rvKategori.setHasFixedSize(true);
                    showProgressBar("sd", "dssd", false);
                }else {
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                    showProgressBar("adss", "sdsd", false);
                }
            }

            @Override
            public void onFailure(Call<List<CategoriesModel>> call, Throwable t) {
                binding.tvEmpty.setVisibility(View.VISIBLE);
                showProgressBar("adss", "sdsd", false);
                showToast("error", "No internet connection");

            }
        });
    }

    private void insertCategories() {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.layout_insert_categories);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        Button btnBatal, btnSimpan;
        EditText etNamaCategories = dialog.findViewById(R.id.etNamaCategory);
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
                if (etNamaCategories.getText().toString().isEmpty()) {
                    etNamaCategories.setError("Can not be empty");
                    etNamaCategories.requestFocus();
                }else {
                    showProgressBar("Loading", "Added new categories", true);
                    adminService.insertCategories(etNamaCategories.getText().toString())
                            .enqueue(new Callback<ResponseModel>() {
                                @Override
                                public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                                    showProgressBar("s", "s", false);
                                    if (response.isSuccessful() && response.body().getStatus() == 200) {
                                        showToast("success", "Successfully added a new category");
                                        getCategories();
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
        ArrayList<CategoriesModel> filteredList = new ArrayList<>();
        for (CategoriesModel item : categoriesModelList) {
            if (item.getCategoryName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }

            categoriesAdapter.filter(filteredList);
            if (filteredList.isEmpty()) {

            }else {
                categoriesAdapter.filter(filteredList);
            }
        }
    }
}