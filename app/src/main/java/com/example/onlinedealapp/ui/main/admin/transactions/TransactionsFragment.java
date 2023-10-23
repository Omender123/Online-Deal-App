package com.example.onlinedealapp.ui.main.admin.transactions;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.onlinedealapp.data.api.AdminService;
import com.example.onlinedealapp.data.api.ApiConfig;
import com.example.onlinedealapp.data.model.TransactionsModel;
import com.example.onlinedealapp.databinding.FragmentTransactionsAdminBinding;
import com.example.onlinedealapp.databinding.FragmentTransactionsBinding;
import com.example.onlinedealapp.ui.main.admin.adapter.TransactionsAdminAdapter;
import com.example.onlinedealapp.util.Constants;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionsFragment extends Fragment {

    List<TransactionsModel> transactionsModelList;
    LinearLayoutManager linearLayoutManager;
    TransactionsAdminAdapter transactionsAdminAdapter;
    SharedPreferences sharedPreferences;
    AlertDialog progressDialog;
    AdminService adminService;
    String userId;

    private FragmentTransactionsAdminBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTransactionsAdminBinding.inflate(inflater, container, false);
        sharedPreferences = getContext().getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        userId = sharedPreferences.getString(Constants.SHARED_PREF_USER_ID, null);
        adminService = ApiConfig.getClient(requireContext()).create(AdminService.class);

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("All"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Not yet confirmed"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Confirmed"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Sent"));


        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getTransactions("all");
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

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    binding.rvTransactions.setAdapter(null);
                    getTransactions("all");
                }else if (tab.getPosition() == 1) {
                    binding.rvTransactions.setAdapter(null);
                    getTransactions("BELUM KONFIRMASI");

                }else if (tab.getPosition() == 2) {
                    binding.rvTransactions.setAdapter(null);
                    getTransactions("TERKONFIRMASI");

                }else if (tab.getPosition() == 3) {
                    binding.rvTransactions.setAdapter(null);
                    getTransactions("TERKIRIM");

                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void getTransactions(String status) {
        showProgressBar("Loading", "Load transaction data", true);
        adminService.getAllTransactionsByStatus(status).enqueue(new Callback<List<TransactionsModel>>() {
            @Override
            public void onResponse(Call<List<TransactionsModel>> call, Response<List<TransactionsModel>> response) {
                if (response.isSuccessful() && response.body().size() > 0) {
                    transactionsModelList = response.body();
                    binding.tvEmpty.setVisibility(View.GONE);

                    transactionsAdminAdapter = new TransactionsAdminAdapter(getContext(), transactionsModelList);
                    linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                    binding.rvTransactions.setLayoutManager(linearLayoutManager);
                    binding.rvTransactions.setAdapter(transactionsAdminAdapter);
                    binding.rvTransactions.setHasFixedSize(true);
                    showProgressBar("sd", "dssd", false);
                }else {
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                    showProgressBar("adss", "sdsd", false);
                }
            }

            @Override
            public void onFailure(Call<List<TransactionsModel>> call, Throwable t) {
                binding.tvEmpty.setVisibility(View.VISIBLE);
                showProgressBar("adss", "sdsd", false);
                showToast("error", "No internet connection");

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
        ArrayList<TransactionsModel> filteredList = new ArrayList<>();
        for (TransactionsModel item : transactionsModelList) {
            if (item.getCode().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }

            transactionsAdminAdapter.filter(filteredList);
            if (filteredList.isEmpty()) {

            }else {
                transactionsAdminAdapter.filter(filteredList);
            }
        }
    }
}