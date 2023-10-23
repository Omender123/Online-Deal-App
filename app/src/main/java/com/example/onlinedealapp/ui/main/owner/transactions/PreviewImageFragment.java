package com.example.onlinedealapp.ui.main.owner.transactions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.onlinedealapp.databinding.FragmentPreviewImageBinding;
import com.example.onlinedealapp.util.Constants;

public class PreviewImageFragment extends Fragment {

    private FragmentPreviewImageBinding binding;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPreviewImageBinding.inflate(inflater, container, false);

        Glide.with(getContext())
                .load(Constants.URL_PRODUCT + getArguments().getString("image"))
                .into(binding.ivBukti);

        return binding.getRoot();
    }
}