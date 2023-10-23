package com.example.onlinedealapp.ui.main.user.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinedealapp.data.model.CategoriesModel;
import com.example.onlinedealapp.databinding.CustomLayoutCategoryBinding;

import java.util.List;
public class Categories_Adapter extends RecyclerView.Adapter<Categories_Adapter.ViewHolder>  {

    Context context;
    List<CategoriesModel>allCategoriesResponses;
    private  OnCategoriesItemListener onCategoriesItemListener;



    public void setOnButtonClickListener(OnCategoriesItemListener onCategoriesItemListener) {
        this.onCategoriesItemListener = onCategoriesItemListener;
    }

    public Categories_Adapter(Context context, List<CategoriesModel>allCategoriesResponses) {
        this.context = context;
        this.allCategoriesResponses = allCategoriesResponses;
    }

    public Categories_Adapter() {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        CustomLayoutCategoryBinding binding = CustomLayoutCategoryBinding.inflate(layoutInflater, parent, false);
        //  View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.categories_layout, parent, false);

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.binding.txt.setText(allCategoriesResponses.get(position).getCategoryName());
        holder.binding.txtCate.setText(allCategoriesResponses.get(position).getCategoryName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCategoriesItemListener.onCategoriesItemClickListener(allCategoriesResponses,position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return allCategoriesResponses.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CustomLayoutCategoryBinding binding;

        public ViewHolder( CustomLayoutCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;


        }
    }

    public interface OnCategoriesItemListener {
        void onCategoriesItemClickListener(List<CategoriesModel> data, int position);
    }

}