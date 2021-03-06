package edu.cnm.deepdive.codebreaker14.service;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import io.reactivex.Completable;
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
                .requestIdToken("164373028747-566nokfb6dpkbqkemm7decqplcqj2udd.apps.googleusercontent.com")
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

    public Single<String> refreshBearerToken() {
        return refresh()
                .map(this::getBearerToken);
    }

    private String getBearerToken(GoogleSignInAccount account) {
        return String.format(BEARER_TOKEN_FORMAT, account.getIdToken());
    }

    public void startSignIn(ActivityResultLauncher<Intent> launcher) {
        launcher.launch(client.getSignInIntent());
    }

    public Single<GoogleSignInAccount> completeSignIn(ActivityResult result) {
        return Single
                .create((SingleEmitter<GoogleSignInAccount> emitter) -> {
                    try {
                        Task<GoogleSignInAccount> task =
                                GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        setAccount(account);
                        emitter.onSuccess(account);
                    } catch (ApiException e) {
                        emitter.onError(e);
                    }
                })
                .observeOn(Schedulers.io());
    }

    public Completable signOut() {
        return Completable
                .create((emitter) ->
                        client
                                .signOut()
                                .addOnSuccessListener((ignored) -> emitter.onComplete())
                                .addOnCompleteListener((ignored) -> setAccount(null))
                                .addOnFailureListener(emitter::onError)
                )
                .subscribeOn(Schedulers.io());
    }

    private void logAccount(GoogleSignInAccount account) {
        if (account != null) {
            Log.d(getClass().getSimpleName(),
                    (account.getIdToken() != null) ? getBearerToken(account) : "(none)");
        }
    }

    private void setAccount(GoogleSignInAccount account) {
        this.account = account;
        if (account != null) {
            Log.d(getClass().getSimpleName(),
                    (account.getIdToken() != null) ? getBearerToken(account) : "(none)");
        }
    }

    private static class InstanceHolder {

        private static final GoogleSignInRepository INSTANCE = new GoogleSignInRepository();

    }

}