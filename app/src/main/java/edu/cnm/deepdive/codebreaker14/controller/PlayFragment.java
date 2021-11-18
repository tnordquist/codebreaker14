package edu.cnm.deepdive.codebreaker14.controller;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.snackbar.Snackbar;
import edu.cnm.deepdive.codebreaker14.R;
import edu.cnm.deepdive.codebreaker14.adapter.GuessItemAdapter;
import edu.cnm.deepdive.codebreaker14.databinding.FragmentPlayBinding;
import edu.cnm.deepdive.codebreaker14.model.entity.Game;
import edu.cnm.deepdive.codebreaker14.viewmodel.PlayViewModel;

public class PlayFragment extends Fragment implements InputFilter {

  private static final String ILLEGAL_CHARACTERS_FORMAT = "[^%s]+";

  private PlayViewModel viewModel;
  private FragmentPlayBinding binding;
  private int codeLength;
  private String pool;
  private String illegalCharacters;

  @Override
  public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    binding = FragmentPlayBinding.inflate(inflater, container, false);
    binding.submit.setOnClickListener(
        (v) -> viewModel.submitGuess(binding.guess.getText().toString().trim()));
    binding.guess.setFilters(new InputFilter[]{this});
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    //noinspection ConstantConditions
    viewModel = new ViewModelProvider(getActivity()).get(PlayViewModel.class);
    getLifecycle().addObserver(viewModel);
    viewModel.getThrowable().observe(getViewLifecycleOwner(), new Observer<Throwable>() {
      @Override
      public void onChanged(Throwable throwable) {
        displayError(throwable);
      }
    });
    viewModel.getGame().observe(getViewLifecycleOwner(), new Observer<Game>() {
      @Override
      public void onChanged(Game game) {
        PlayFragment.this.update(game);
      }
    });
  }


  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.play_actions, menu);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    boolean handled;
    if (item.getItemId() == R.id.new_game) {
      handled = true;
      binding.guess.getText().clear();
      viewModel.startGame();
    } else {
      handled = super.onOptionsItemSelected(item);
    }
    return handled;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  @Override
  public CharSequence filter(
      CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
    String modifiedSource = source
        .subSequence(start, end)
        .toString()
        .toUpperCase()
        .replaceAll(illegalCharacters, "");
    StringBuilder builder = new StringBuilder(dest);
    builder.replace(dstart, dend, modifiedSource);
    if (builder.length() > codeLength) {
      modifiedSource = modifiedSource.substring(0, (builder.length() - codeLength));
    }
    int newLength = dest.length() - (dend - dstart) + modifiedSource.length();
    checkSubmitConditions(newLength);
    return modifiedSource;
  }

  private void update(Game game) {
    GuessItemAdapter adapter = new GuessItemAdapter(getContext(), game.getGuesses());
    binding.guesses.setAdapter(adapter);
    binding.guessContainer.setVisibility((game.isSolved() ? View.GONE : View.VISIBLE));
    codeLength = game.getLength();
    pool = game.getPool();
    illegalCharacters = String.format(ILLEGAL_CHARACTERS_FORMAT, pool);
    checkSubmitConditions(binding.guess.getText().toString().trim().length());
  }

  private void checkSubmitConditions(int length) {
    binding.submit.setEnabled(length == codeLength);
  }

  private void displayError(Throwable throwable) {
    if(throwable != null) {
      Snackbar snackbar = Snackbar.make(binding.getRoot(),
          getString(R.string.play_error_message, throwable.getMessage()),
          Snackbar.LENGTH_INDEFINITE);
      snackbar.setAction(R.string.error_dismiss, (v) -> snackbar.dismiss());
    }
  }

}