package edu.cnm.deepdive.codebreaker14.service;

import android.app.Application;
import android.util.Log;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.schedulers.Schedulers;

public class GoogleSignInRepository {

  private static final String BEARER_TOKEN_FORMAT = "Bearer %s";

  private static Application context;

  private final GoogleSignInClient client;

  private GoogleSignInAccount account;

  private GoogleSignInRepository() {
    GoogleSignInOptions options = new GoogleSignInOptions.Builder()
        .requestEmail()
        .requestId()
        .requestProfile()
//        .requestIdToken()
        .build();
    client = GoogleSignIn.getClient(context, options);
  }

  public static void setContext(Application context) {
    GoogleSignInRepository.context = context;
  }

  public static GoogleSignInRepository getInstance() {
    return InstanceHolder.INSTANCE;
  }

  public Single<GoogleSignInAccount> refresh() {
    return Single
        .create((SingleEmitter<GoogleSignInAccount> emitter) ->
            client
                .silentSignIn()
                .addOnSuccessListener(this::setAccount)
                .addOnSuccessListener(emitter::onSuccess)
                .addOnFailureListener(emitter::onError)
        )
        .observeOn(Schedulers.io());
  }

  private void setAccount(GoogleSignInAccount account) {
    this.account = account;
    if (account != null) {
      Log.d(getClass().getSimpleName(),
          (account.getIdToken() != null) ? getBearerToken(account) : "(none)");
    }
  }

  private String getBearerToken(GoogleSignInAccount account) {
    return String.format(BEARER_TOKEN_FORMAT, account.getIdToken());
  }

  private static class InstanceHolder {

    private static final GoogleSignInRepository INSTANCE = new GoogleSignInRepository();
  }

}