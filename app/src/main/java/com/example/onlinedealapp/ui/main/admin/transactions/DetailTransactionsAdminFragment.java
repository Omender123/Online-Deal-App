package com.example.onlinedealapp.ui.main.admin.transactions;

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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.onlinedealapp.R;
import com.example.onlinedealapp.data.api.AdminService;
import com.example.onlinedealapp.data.api.ApiConfig;
import com.example.onlinedealapp.data.api.UserService;
import com.example.onlinedealapp.data.model.ResponseModel;
import com.example.onlinedealapp.data.model.TransactionsDetailModel;
import com.example.onlinedealapp.databinding.FragmentDetailTransactionsAdminBinding;
import com.example.onlinedealapp.ui.main.admin.adapter.TransactionsDetailAdapter;
import com.example.onlinedealapp.util.Constants;

import java.text.DecimalFormat;
import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailTransactionsAdminFragment extends Fragment {
    SharedPreferences sharedPreferences;
    UserService userService;
    AdminService adminService;
    private AlertDialog progressDialog;
    TransactionsDetailAdapter transactionsDetailAdapter;
    String transaction_id;
    LinearLayoutManager linearLayoutManager;
    List<TransactionsDetailModel> transactionsDetailModelList;


    private FragmentDetailTransactionsAdminBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDetailTransactionsAdminBinding.inflate(inflater, container, false);
        sharedPreferences = getContext().getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        userService = ApiConfig.getClient(requireContext()).create(UserService.class);
        transaction_id = getArguments().getString("transaction_id");
        adminService = ApiConfig.getClient(requireContext()).create(AdminService.class);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.tvCodeTransaksi.setText(getArguments().getString("code_transactions"));
        binding.tvStatus.setText(getArguments().getString("status"));
        binding.etKota.setText(getArguments().getString("kota"));
        binding.etTelepon.setText(getArguments().getString("telepon"));
        binding.etKodePos.setText(getArguments().getString("kode_pos"));
        binding.etTelepon.setText(getArguments().getString("telepon"));
        binding.etAlamat.setText(getArguments().getString("alamat"));
        binding.tvMetodePembayaran.setText(getArguments().getString("nama_bank"));
        binding.tvBeratBarang.setText(getArguments().getString("berat_total"));
        binding.tvJumlahProduk.setText(getArguments().getString("berat_total"));
        binding.etNamaLengkap.setText(getArguments().getString("nama_lengkap"));

        getFormatRupiah(binding.tvNominal, getArguments().getString("total"));
        getFormatRupiah(binding.tvTotalPembayaran, getArguments().getString("total"));


        if (getArguments().getString("bukti_tf").equals("")) {
            binding.ivBukti.setVisibility(View.GONE);
            binding.btnPesan.setVisibility(View.GONE);
        }else {
            binding.ivBukti.setVisibility(View.VISIBLE);
            Glide.with(getContext())
                    .load(Constants.URL_PRODUCT + getArguments().getString("bukti_tf"))
                    .fitCenter().into(binding.ivBukti);

            if (getArguments().getString("status").equals("BELUM KONFIRMASI")){
                binding.btnPesan.setText("Confirmation");

            }else if (getArguments().getString("status").equals("TERKONFIRMASI")) {
                binding.btnPesan.setText("Sent");
            }else if (getArguments().getString("status").equals("TERKIRIM")) {
                binding.btnPesan.setVisibility(View.GONE);
            }
        }

        if (getArguments().getString("nama_penerima").equals("")) {
            binding.lrPenerima.setVisibility(View.GONE);
        }else {
            binding.lrPenerima.setVisibility(View.VISIBLE);
            binding.tvNamaPenerima.setText(getArguments().getString("nama_penerima") + " | " + getArguments().getString("tgl_terima"));
        }

        getTransactionsDetail();





        listener();


    }

    private void listener() {
        binding.btnPesan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        binding.ivBukti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new PreviewImageFragment();
                Bundle bundle = new Bundle();
                bundle.putString("image", getArguments().getString("bukti_tf"));
                fragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameAdmin, fragment)
                        .addToBackStack(null).commit();
            }
        });

        if (getArguments().getString("status").equals("BELUM KONFIRMASI")) {
            binding.btnPesan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    konfirmasi();
                }
            });
        }else if (getArguments().getString("status").equals("TERKONFIRMASI")) {

                binding.btnPesan.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        terkirim();
                    }
                });
        }
    }


    private void getFormatRupiah(TextView tvText, String number) {
        DecimalFormat decimalFormat = new DecimalFormat("#,##0");
        String price = decimalFormat.format(Integer.parseInt(number));
        tvText.setText("Rs. " + price);
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

    private void konfirmasi() {
        showProgressBar("Loading", "Confirm transaction", true);
        adminService.konfirmasiTrans(transaction_id).enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                if (response.isSuccessful() && response.body().getStatus() == 200) {
                    showProgressBar("dsd","sd", false);
                    showToast("success", "Successfully confirmed the transaction");
                    getActivity().onBackPressed();
                }else {
                    showProgressBar("dsd","sd", false);
                    showToast("error", "Failed to confirm transaction");
                }
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                showProgressBar("dsd","sd", false);
                showToast("error", "No internet connection");

            }
        });



    }

    private void terkirim (){
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.layout_terkirim);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        EditText etPenerima;
        Button btnBatal, btnSimpan;
        etPenerima = dialog.findViewById(R.id.etNamaLengkap);
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
                if (etPenerima.getText().toString().isEmpty()) {
                    etPenerima.setError("Recipient's name Can not be empty");
                    etPenerima.requestFocus();
                }else {

                    showProgressBar("Loading", "Confirm transaction", true);
                    adminService.terkirimTransaction(transaction_id, etPenerima.getText().toString()).enqueue(new Callback<ResponseModel>() {
                        @Override
                        public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                            if (response.isSuccessful() && response.body().getStatus() == 200) {
                                showProgressBar("dsd","sd", false);
                                showToast("success", "Recipient verification successful");
                                getActivity().onBackPressed();
                                dialog.dismiss();
                            }else {
                                showProgressBar("dsd","sd", false);
                                showToast("error", "Recipient verification failed");
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseModel> call, Throwable t) {
                            showProgressBar("dsd","sd", false);
                            showToast("error", "No internet connection");

                        }
                    });

                }
            }
        });




    }

    private void getTransactionsDetail() {
        showProgressBar("Loading", "Loading data....", true);
        adminService.getDetailTransactions(transaction_id).enqueue(new Callback<List<TransactionsDetailModel>>() {
            @Override
            public void onResponse(Call<List<TransactionsDetailModel>> call, Response<List<TransactionsDetailModel>> response) {
                if (response.isSuccessful() && response.body().size() > 0) {
                    transactionsDetailModelList = response.body();
                    transactionsDetailAdapter = new TransactionsDetailAdapter(getContext(), transactionsDetailModelList);
                    linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                    binding.rvProduct.setLayoutManager(linearLayoutManager);
                    binding.rvProduct.setAdapter(transactionsDetailAdapter);
                    binding.rvProduct.setHasFixedSize(true);
                    showProgressBar("Sdd", "Dds", false);
                }else {
                    showProgressBar("Sds", "Dsds", false);
                    showToast("error", "Failed to load transaction details");
                }
            }

            @Override
            public void onFailure(Call<List<TransactionsDetailModel>> call, Throwable t) {
                showProgressBar("Sds", "Dsds", false);
                showToast("error", "No internet connection");

            }
        });
    }





}