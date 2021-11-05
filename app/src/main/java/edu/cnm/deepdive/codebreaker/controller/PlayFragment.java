package edu.cnm.deepdive.codebreaker.controller;

import android.os.Bundle;
import android.view.View.OnClickListener;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.lifecycle.ViewModelProvider;
import edu.cnm.deepdive.codebreaker.R;
import edu.cnm.deepdive.codebreaker.databinding.FragmentPlayBinding;
import edu.cnm.deepdive.codebreaker.viewmodel.MainViewModel;
import org.jetbrains.annotations.NotNull;

public class PlayFragment extends Fragment {

  private MainViewModel viewModel;
  private FragmentPlayBinding binding;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    binding = FragmentPlayBinding.inflate(inflater, container, false);
    binding.submit.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        viewModel.submitGuess(binding.guess.getText().toString().trim());
      }
    });
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    viewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);
    viewModel.getThrowable().observe(getViewLifecycleOwner(), (throwable) -> {
      if (throwable != null) {
        Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
      }
    });
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}