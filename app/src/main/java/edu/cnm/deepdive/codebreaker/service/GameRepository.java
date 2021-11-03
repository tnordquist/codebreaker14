package edu.cnm.deepdive.codebreaker.service;

import android.util.Log;
import edu.cnm.deepdive.codebreaker.model.Game;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class GameRepository {

  private final WebServiceProxy proxy;

  public GameRepository() {
    this.proxy = WebServiceProxy.getInstance();
  }

  public Single<Game> startGame(String pool, int length) {
    return Single
        .fromCallable(() -> {
          Game game = new Game();
          game.setPool(pool);
          game.setLength(length);
          return game;
        })
        .flatMap((game) ->
            proxy.startGame(game)
                .subscribeOn(Schedulers.io()));
  }
}
