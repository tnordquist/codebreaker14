package edu.cnm.deepdive.codebreaker14;

import android.app.Application;
import com.facebook.stetho.Stetho;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import edu.cnm.deepdive.codebreaker14.service.CodebreakerDatabase;
import edu.cnm.deepdive.codebreaker14.service.GoogleSignInRepository;
import io.reactivex.schedulers.Schedulers;

public class CodebreakerApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    Stetho.initializeWithDefaults(this);
    CodebreakerDatabase.setContext(this);
    GoogleSignInRepository.setContext(this);
    CodebreakerDatabase
        .getInstance()
        .getGameDao()
        .delete()
        .subscribeOn(Schedulers.io())
        .subscribe();
  }

}