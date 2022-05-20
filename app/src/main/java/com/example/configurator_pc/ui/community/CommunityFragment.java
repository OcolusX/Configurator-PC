package com.example.configurator_pc.ui.community;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.configurator_pc.databinding.FragmentCommunityBinding;

public class CommunityFragment extends Fragment {
    private FragmentCommunityBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCommunityBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    public void onDestroyView() {
        super.onDestroyView();
        this.binding = null;
    }
}
