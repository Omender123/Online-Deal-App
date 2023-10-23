package com.example.onlinedealapp.ui.main.user.cart;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.onlinedealapp.R;
import com.example.onlinedealapp.data.api.ApiConfig;
import com.example.onlinedealapp.data.api.UserService;
import com.example.onlinedealapp.data.model.CartModel;
import com.example.onlinedealapp.data.model.InformationModel;
import com.example.onlinedealapp.data.model.RekeningModel;
import com.example.onlinedealapp.data.model.ResponseModel;
import com.example.onlinedealapp.data.model.UserModel;
import com.example.onlinedealapp.databinding.FragmentCartBinding;
import com.example.onlinedealapp.ui.main.user.adapter.CartAdapter;
import com.example.onlinedealapp.ui.main.user.adapter.SpinnerRekeningAdapter;
import com.example.onlinedealapp.ui.main.user.transactions.TransactionsFragment;
import com.example.onlinedealapp.util.Constants;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartFragment extends Fragment implements CartAdapter.OnButtonClickListener {
    private FragmentCartBinding binding;
    List<CartModel> cartModelList;
    CartAdapter cartAdapter;
    List<RekeningModel> rekeningModelList;
    LinearLayoutManager linearLayoutManager;
    UserService userService;
    SpinnerRekeningAdapter spinnerRekeningAdapter;
    String [] opsiKota = {"New Delhi", "Delhi NCR"};
    String userId;
    SharedPreferences sharedPreferences;
    AlertDialog progressDialog;
    Integer totalPrice, weightTotal;
    String kota, rekening;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCartBinding.inflate(inflater, container, false);
        sharedPreferences = getContext().getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        userId = sharedPreferences.getString(Constants.SHARED_PREF_USER_ID, null);
        userService = ApiConfig.getClient(requireContext()).create(UserService.class);

        ArrayAdapter adapterKota = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, opsiKota);
        adapterKota.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spKota.setAdapter(adapterKota);

        return binding.getRoot();
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        getCart();
        listener();
        getProfile();
        getAllRekening();

    }

    private void listener() {
        binding.spKota.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                kota = opsiKota[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        binding.icArrowDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.lrAlamat.setVisibility(View.VISIBLE);
                binding.icArrowDown.setVisibility(View.GONE);
                binding.icArrowUp.setVisibility(View.VISIBLE);
            }
        });

        binding.icArrowUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.lrAlamat.setVisibility(View.GONE);
                binding.icArrowUp.setVisibility(View.GONE);
                binding.icArrowDown.setVisibility(View.VISIBLE);
            }
        });
        binding.spPembayaran.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                rekening = String.valueOf(spinnerRekeningAdapter.getRekeningId(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        binding.btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.etAlamat.getText().toString().isEmpty()) {
                    binding.etAlamat.setError("The address cannot be empty");
                    binding.etAlamat.requestFocus();
                }else if (binding.etTelepon.getText().toString().isEmpty()) {
                    binding.etTelepon.setError("The phone cannot be empty");
                    binding.etTelepon.requestFocus();
                }else if (binding.etKodePos.getText().toString().isEmpty()) {
                    binding.etKodePos.setError("Postal Code cannot be empty");
                    binding.etKodePos.requestFocus();
                }else {
                    updateAlamat();
                }
            }
        });

        binding.btnPesan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.etAlamat.getText().toString().isEmpty()) {
                    binding.lrAlamat.setVisibility(View.VISIBLE);
                    binding.etAlamat.setError("The address cannot be empty");
                    binding.etTelepon.requestFocus();
                }else if (kota == null) {
                    binding.lrAlamat.setVisibility(View.VISIBLE);
                    showToast("error", "The city cannot be empty");
                }

                else {
                    checkOut();
                }
            }
        });
    }

    private void getCart() {
        showProgressBar("Loading", "Loading data...", true);
        userService.getMyCart(userId).enqueue(new Callback<List<CartModel>>() {
            @Override
            public void onResponse(Call<List<CartModel>> call, Response<List<CartModel>> response) {
                if (response.isSuccessful() && response.body().size() > 0) {
                    binding.tvEmpty.setVisibility(View.GONE);
                    cartModelList = response.body();
                    cartAdapter = new CartAdapter(getContext(), cartModelList);
                    cartAdapter.setOnButtonClickListener(CartFragment.this);
                    linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                    binding.rvProduct.setLayoutManager(linearLayoutManager);
                    binding.rvProduct.setAdapter(cartAdapter);
                    binding.rvProduct.setHasFixedSize(true);
                    getInformationOrder();
                    binding.btnPesan.setEnabled(true);
                    binding.lrBody.setVisibility(View.VISIBLE);
                    showProgressBar("adasd", "ssds", false);
                }else {
                    showProgressBar("Sds", "dsd", false);
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                    binding.btnPesan.setVisibility(View.GONE);
                    binding.lrBody.setVisibility(View.GONE);
                    binding.tvTotalPembayaran.setText("-");


                }
            }

            @Override
            public void onFailure(Call<List<CartModel>> call, Throwable t) {
                showProgressBar("Sds", "dsd", false);
                binding.tvEmpty.setVisibility(View.GONE);
                binding.lrBody.setVisibility(View.GONE);
                showToast("error", "No internet connection");
                binding.btnPesan.setEnabled(false);


            }
        });


    }

    private void getAllRekening() {
        showProgressBar("Loading", "Loading data...", true);
        userService.getallrekening().enqueue(new Callback<List<RekeningModel>>() {
            @Override
            public void onResponse(Call<List<RekeningModel>> call, Response<List<RekeningModel>> response) {
                if (response.isSuccessful() && response.body().size() > 0) {
                    rekeningModelList = response.body();
                    spinnerRekeningAdapter = new SpinnerRekeningAdapter(getContext(), rekeningModelList);
                    binding.spPembayaran.setAdapter(spinnerRekeningAdapter);
                    showProgressBar("sds", "sds", false);
                    binding.btnPesan.setEnabled(true);
                }else {
                    showProgressBar("sds", "sds", false);
                    binding.btnPesan.setEnabled(false);
                    showToast("d", "Unable to load payment method");
                }
            }

            @Override
            public void onFailure(Call<List<RekeningModel>> call, Throwable t) {
                showProgressBar("sds", "sds", false);
                binding.btnPesan.setEnabled(false);
                showToast("d", "No internet connection");

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
        ArrayList<CartModel> filteredList = new ArrayList<>();
        for (CartModel item : cartModelList) {
            if (item.getProductName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }

            cartAdapter.filter(filteredList);
            if (filteredList.isEmpty()) {

            }else {
                cartAdapter.filter(filteredList);
            }
        }
    }

    private void getProfile() {
        showProgressBar("Loading", "Loading data...", true);
        userService.getMyProfile(userId).enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (response.isSuccessful() && response.body() != null) {

                    if (response.body().getAddress() != null) {
                        String replacedString = response.body().getAddress().replaceAll("<p>|</p>|&nbsp;|\n", "");
                        binding.etAlamat.setText(replacedString);
                    }


                    binding.etTelepon.setText(response.body().getPhoneNumber());
                    binding.etKodePos.setText(response.body().getPostalCode());
                    showProgressBar("dsds", "Sdsd",false);
                }else {
                    showProgressBar("dsds", "Sdsd",false);
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                showProgressBar("dsds", "Sdsd",false);
                showToast("error", "No internet connection");

            }
        });
    }

    private void getInformationOrder() {
        showProgressBar("Loading", "Loading order information...", true);
        userService.getInformationOrder(userId).enqueue(new Callback<InformationModel>() {
            @Override
            public void onResponse(Call<InformationModel> call, Response<InformationModel> response) {
                if (response.isSuccessful() && response.body() != null) {

                    binding.tvJumlahProduk.setText(response.body().getQty() + " Product");
                    binding.tvBeratBarang.setText(response.body().getBerat() + " Quantity");
                    getFormatRupiah(binding.tvNominal, response.body().getHargaTotal());
                    getFormatRupiah(binding.tvTotalPembayaran, response.body().getHargaTotal());
                    binding.btnPesan.setEnabled(true);
                    weightTotal = Integer.parseInt(response.body().getBerat() + "000");
                    totalPrice = response.body().getHargaTotal();

                    // sembunyikan
                    if (response.body().getQty() <= 99){
                        binding.lrProdukDibawah100.setVisibility(View.VISIBLE);
                    }else if (response.body().getQty() >= 100) {
                        binding.lrProdukDiatas100.setVisibility(View.VISIBLE);
                    }
                    showProgressBar("sds", "Sd", false);
                }else {
                    showProgressBar("sds", "Sd", false);
                    showToast("er", "Failed to load purchase information");
                    binding.btnPesan.setEnabled(false);

                }
            }

            @Override
            public void onFailure(Call<InformationModel> call, Throwable t) {
                showProgressBar("sds", "Sd", false);
                showToast("er", "No internet connection");
                binding.btnPesan.setEnabled(false);

            }
        });

    }

    private void getFormatRupiah(TextView tvText, Integer nominal) {
        DecimalFormat decimalFormat = new DecimalFormat("#,##0");
        String price = decimalFormat.format(nominal);
        tvText.setText("Rs. " + price);
    }

    @Override
    public void onButtonClicked() {
        getCart();
    }

    private void updateAlamat() {
        showProgressBar("Loading", "Saving changes...", true);
        userService.updateAlamat(
                userId,
                binding.etAlamat.getText().toString(),
                binding.etTelepon.getText().toString(),
                binding.etKodePos.getText().toString()
        ).enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                if (response.isSuccessful() && response.body().getStatus() == 200) {
                    showProgressBar("sd", "Sds", false);
                    showToast("success", "Successfully changed address");
                    getProfile();

                }else {
                    showProgressBar("sd", "Sds", false);
                    showToast("error", "There is an error");
                }
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                showProgressBar("sd", "Sds", false);
                showToast("error", "No internet connection");

            }
        });
    }

    private void checkOut() {

        showProgressBar("Loading", "Transaction process", true);
        userService.checkOut(
                userId,
                totalPrice,
                kota,
                Integer.parseInt(rekening),
                weightTotal
        ).enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                if (response.isSuccessful() && response.body().getStatus() == 200) {
                    showProgressBar("sds", "dssd", false);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frameUsers, new TransactionsFragment()).commit();
                    showToast("success", "Successfully created a new order");

                }else {
                    showProgressBar("Dsd", "sdsd", false);
                    showToast("error", "Failed to place order");

                }

            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                showProgressBar("Dsd", "sdsd", false);
                showToast("error", "Check your internet connection");

            }
        });


    }
}