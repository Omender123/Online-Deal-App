package com.example.onlinedealapp.ui.main.user.transactions;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.onlinedealapp.R;
import com.example.onlinedealapp.data.api.ApiConfig;
import com.example.onlinedealapp.data.api.UserService;
import com.example.onlinedealapp.data.model.TransactionsModel;
import com.example.onlinedealapp.databinding.FragmentTransactionsBinding;
import com.example.onlinedealapp.ui.main.user.adapter.TransactionsAdapter;
import com.example.onlinedealapp.util.Constants;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionsFragment extends Fragment {

    List<TransactionsModel> transactionsModelList;
    LinearLayoutManager linearLayoutManager;
    TransactionsAdapter transactionsAdapter;
    SharedPreferences sharedPreferences;
    AlertDialog progressDialog;
    UserService userService;
    String userId;

    private FragmentTransactionsBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTransactionsBinding.inflate(inflater, container, false);
        sharedPreferences = getContext().getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        userId = sharedPreferences.getString(Constants.SHARED_PREF_USER_ID, null);
        userService = ApiConfig.getClient(requireContext()).create(UserService.class);


        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getTransactions();
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

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    private void getTransactions() {
        showProgressBar("Loading", "Load transaction data", true);
        userService.getMyTransactions(userId).enqueue(new Callback<List<TransactionsModel>>() {
            @Override
            public void onResponse(Call<List<TransactionsModel>> call, Response<List<TransactionsModel>> response) {
                if (response.isSuccessful() && response.body().size() > 0) {
                    transactionsModelList = response.body();
                    binding.tvEmpty.setVisibility(View.GONE);

                    transactionsAdapter = new TransactionsAdapter(getContext(), transactionsModelList);
                    linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                    binding.rvTransactions.setLayoutManager(linearLayoutManager);
                    binding.rvTransactions.setAdapter(transactionsAdapter);
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

            transactionsAdapter.filter(filteredList);
            if (filteredList.isEmpty()) {

            }else {
                transactionsAdapter.filter(filteredList);
            }
        }
    }
}