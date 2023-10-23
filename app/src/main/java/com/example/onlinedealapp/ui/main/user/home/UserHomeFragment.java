package com.example.onlinedealapp.ui.main.user.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.onlinedealapp.R;
import com.example.onlinedealapp.data.api.ApiConfig;
import com.example.onlinedealapp.data.api.UserService;
import com.example.onlinedealapp.data.model.CartModel;
import com.example.onlinedealapp.data.model.CategoriesModel;
import com.example.onlinedealapp.data.model.ProductModel;
import com.example.onlinedealapp.data.model.UserModel;
import com.example.onlinedealapp.databinding.FragmentUserHomeBinding;
import com.example.onlinedealapp.ui.main.user.adapter.Categories_Adapter;
import com.example.onlinedealapp.ui.main.user.adapter.ProductAdapter;
import com.example.onlinedealapp.ui.main.user.cart.CartFragment;
import com.example.onlinedealapp.ui.main.user.product.ProductKategoriFragment;
import com.example.onlinedealapp.util.Constants;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserHomeFragment extends Fragment implements ProductAdapter.OnButtonClickListener, Categories_Adapter.OnCategoriesItemListener {

    private FragmentUserHomeBinding binding;
    List<ProductModel> productModelList;
    List<CategoriesModel> categoriesModelList;
    GridLayoutManager gridLayoutManager;
    ProductAdapter productAdapter;
    Categories_Adapter categoriesAdapter;
    SharedPreferences sharedPreferences;
    UserService userService;
    AlertDialog progressDialog;
    private String nama, userId;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentUserHomeBinding.inflate(inflater, container, false);
        sharedPreferences = getContext().getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        userService = ApiConfig.getClient(requireContext()).create(UserService.class);
        userId = sharedPreferences.getString(Constants.SHARED_PREF_USER_ID, null);


        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getAllProduct();
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
        getProfile();
        getTotalCart();

        listener();
    }

    private void listener() {
        binding.btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replace(new CartFragment());
            }
        });

    }

    private void getAllProduct() {
        showProgressBar("Loading", "Loading data...", true);
        userService.getAllProduct().enqueue(new Callback<List<ProductModel>>() {
            @Override
            public void onResponse(Call<List<ProductModel>> call, Response<List<ProductModel>> response) {
                if (response.isSuccessful() && response.body().size() > 0) {
                    productModelList = response.body();
                    productAdapter = new ProductAdapter(getContext(), productModelList);
                    gridLayoutManager = new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false);
                    binding.rvProduct.setLayoutManager(gridLayoutManager);
                    productAdapter.setOnButtonClickListener(UserHomeFragment.this);
                    binding.rvProduct.setAdapter(productAdapter);
                    binding.rvProduct.setHasFixedSize(true);
                    showProgressBar("sdd", "dsd", false);
                } else {
                    showProgressBar("sdsd", "sds", false);
                }
            }

            @Override
            public void onFailure(Call<List<ProductModel>> call, Throwable t) {
                showProgressBar("sdsd", "sds", false);
                showToast("error", "No internet connection");

            }
        });
        userService.getCategories().enqueue(new Callback<List<CategoriesModel>>() {
            @Override
            public void onResponse(Call<List<CategoriesModel>> call, Response<List<CategoriesModel>> response) {
                if (response.isSuccessful() && response.body().size() > 0) {
                    categoriesModelList = response.body();
                    Log.e("categoriesModelList", categoriesModelList.toString());
                    categoriesAdapter = new Categories_Adapter(getContext(), categoriesModelList);
                    binding.rvCate.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                    binding.rvCate.setAdapter(categoriesAdapter);
                    categoriesAdapter.setOnButtonClickListener(UserHomeFragment.this);
                    binding.rvCate.setHasFixedSize(true);
                    showProgressBar("sdd", "dsd", false);
                } else {
                    showProgressBar("sdsd", "sds", false);
                }
            }

            @Override
            public void onFailure(Call<List<CategoriesModel>> call, Throwable t) {
                showProgressBar("sdsd", "sds", false);
                showToast("error", "No internet connection");

            }
        });


    }

    private void filter(String text) {
        ArrayList<ProductModel> filteredList = new ArrayList<>();
        for (ProductModel item : productModelList) {
            if (item.getProduct_name().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }

            productAdapter.filter(filteredList);
            if (filteredList.isEmpty()) {

            } else {
                productAdapter.filter(filteredList);
            }
        }

    }

    private void getProfile() {
        showProgressBar("Loading", "Loading data...", true);
        userService.getMyProfile(userId).enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    binding.tvnama.setText("Hai, " + response.body().getName());
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


    public void getTotalCart() {
        showProgressBar("Loading", "Loading data...", true);
        userService.getMyCart(userId).enqueue(new Callback<List<CartModel>>() {
            @Override
            public void onResponse(Call<List<CartModel>> call, Response<List<CartModel>> response) {
                if (response.isSuccessful() && response.body().size() > 0) {
                    binding.tvTotalNotif.setText(String.valueOf(response.body().size()));
                    showProgressBar("sds", "sdsd", false);
                } else {
                    binding.tvTotalNotif.setText("0");
                    showProgressBar("sds", "sdsd", false);

                }
            }

            @Override
            public void onFailure(Call<List<CartModel>> call, Throwable t) {
                binding.tvTotalNotif.setText("0");
                showProgressBar("sds", "sdsd", false);
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
        } else {
            Toasty.error(getContext(), text, Toasty.LENGTH_SHORT).show();
        }
    }

    private void replace(Fragment fragment) {
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameUsers, fragment).addToBackStack(null).commit();
    }

    private void replaceKategori(String id) {
        Fragment fragment = new ProductKategoriFragment();
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        fragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameUsers, fragment).addToBackStack(null).commit();
    }

    @Override
    public void onButtonClicked() {
        getTotalCart();

    }

    @Override
    public void onCategoriesItemClickListener(List<CategoriesModel> data, int position) {
        replaceKategori(data.get(position).getId());
    }
}