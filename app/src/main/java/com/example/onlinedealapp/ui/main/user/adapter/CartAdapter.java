package com.example.onlinedealapp.ui.main.user.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.onlinedealapp.R;
import com.example.onlinedealapp.data.api.ApiConfig;
import com.example.onlinedealapp.data.api.UserService;
import com.example.onlinedealapp.data.model.CartModel;
import com.example.onlinedealapp.data.model.ResponseModel;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    Context context;
    List<CartModel> cartModelList;
    AlertDialog progressDialog;
    UserService userService;

    private CartAdapter.OnButtonClickListener onButtonClickListener;


    public void setOnButtonClickListener(CartAdapter.OnButtonClickListener listener) {
        this.onButtonClickListener = listener;
    }


    public CartAdapter(Context context, List<CartModel> cartModelList) {
        this.context = context;
        this.cartModelList = cartModelList;
    }

    @NonNull
    @Override
    public CartAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartAdapter.ViewHolder holder, int position) {
        holder.tvNamaProduct.setText(cartModelList.get(holder.getAdapterPosition()).getProductName());
        holder.tvJumlah.setText("X" + String.valueOf(cartModelList.get(holder.getAdapterPosition()).getQuantity()));

        DecimalFormat decimalFormat = new DecimalFormat("#,##0");
        String price = decimalFormat.format(cartModelList.get(holder.getAdapterPosition()).getTotal());
        holder.tvHarga.setText("Rs. " + price);

        Glide.with(context).load(cartModelList.get(holder.getAdapterPosition()).getPhotos()).diskCacheStrategy(DiskCacheStrategy.NONE).centerCrop().centerInside().skipMemoryCache(true).into(holder.ivProduct);

    }

    @Override
    public int getItemCount() {
        return cartModelList.size();
    }

    public void filter(ArrayList<CartModel> filterList) {
        cartModelList = filterList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNamaProduct, tvJumlah, tvHarga;
        ImageView ivProduct;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvNamaProduct = itemView.findViewById(R.id.tvNamaProduct);
            tvJumlah = itemView.findViewById(R.id.tvJumlah);
            tvHarga = itemView.findViewById(R.id.tvPrice);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            userService = ApiConfig.getClient(context).create(UserService.class);

            listener();
        }

        private void listener() {
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showProgressBar("Loading", "Deleting a product...", false);
                    userService.deleteCart(cartModelList.get(getAdapterPosition()).getCartId(), cartModelList.get(getAdapterPosition()).getProductId(), cartModelList.get(getAdapterPosition()).getQuantity()).enqueue(new Callback<ResponseModel>() {
                        @Override
                        public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                            if (response.isSuccessful() && response.body().getStatus() == 200) {
                                showProgressBar("ssds", "sds", false);
                                if (onButtonClickListener != null) {
                                    onButtonClickListener.onButtonClicked();
                                }
                                cartModelList.remove(getAdapterPosition());
                                notifyDataSetChanged();
                                showToast("success", "Successfully deleted the product");

                            } else {
                                showToast("error", "There is an error");
                                showProgressBar("sds", "Sds", false);

                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseModel> call, Throwable t) {
                            showToast("error", "No internet connection");
                            showProgressBar("sds", "Sds", false);

                        }
                    });
                }


            });
        }
    }


    private void showProgressBar(String title, String message, boolean isLoading) {
        if (isLoading) {
            // Membuat progress dialog baru jika belum ada
            if (progressDialog == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
            Toasty.success(context, text, Toasty.LENGTH_SHORT).show();
        } else {
            Toasty.error(context, text, Toasty.LENGTH_SHORT).show();
        }
    }

    public interface OnButtonClickListener {
        void onButtonClicked();
    }
}
