package com.example.onlinedealapp.ui.main.admin.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinedealapp.R;
import com.example.onlinedealapp.data.api.AdminService;
import com.example.onlinedealapp.data.api.ApiConfig;
import com.example.onlinedealapp.data.model.CategoriesModel;
import com.example.onlinedealapp.data.model.ResponseModel;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {
    Context context;
    List<CategoriesModel> categoriesModelList;
    private AdminService adminService;
    private AlertDialog progressDialog;

    public CategoriesAdapter(Context context, List<CategoriesModel> categoriesModelList) {
        this.context = context;
        this.categoriesModelList = categoriesModelList;
    }

    @NonNull
    @Override
    public CategoriesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_categories, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriesAdapter.ViewHolder holder, int position) {
        holder.tvCategories.setText(categoriesModelList.get(holder.getAdapterPosition()).getCategoryName());

    }

    @Override
    public int getItemCount() {
        return categoriesModelList.size();
    }

    public void filter(ArrayList<CategoriesModel> filteredList) {
        categoriesModelList = filteredList;
        notifyDataSetChanged();

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategories;
        ImageView btnDelete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategories = itemView.findViewById(R.id.tvCategory);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            adminService = ApiConfig.getClient(context).create(AdminService.class);

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    alert.setTitle("Warning").setMessage("Are you sure you want to delete this data?");
                    alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showProgressBar("Loading", "Deleting data...", true);
                            adminService.deleteCategories(categoriesModelList.get(getAdapterPosition()).getId())
                                    .enqueue(new Callback<ResponseModel>() {
                                        @Override
                                        public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                                            showProgressBar("s", "s", false);
                                            if (response.isSuccessful() && response.body().getStatus() == 200) {
                                                showToast("success", "Successfully deleted data");
                                                categoriesModelList.remove(getAdapterPosition());
                                                notifyDataSetChanged();

                                            }else {
                                                showToast("err", "There is an error");
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseModel> call, Throwable t) {

                                            showProgressBar("s", "s", false);
                                            showToast("err", "No internet connection");


                                        }
                                    });


                        }
                    })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    alert.show();
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
        }else {
            Toasty.error(context, text, Toasty.LENGTH_SHORT).show();
        }
    }
}
