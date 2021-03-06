package com.stanfy.enroscar.async.internal;

import com.stanfy.enroscar.async.Async;
import com.stanfy.enroscar.async.AsyncObserver;

import java.util.ArrayList;

/**
 * Base implementation for {@link com.stanfy.enroscar.async.Async}.
 * Supports multiple observers. Not thread safe: {@code Async} methods like {@code subscribe} and
 * {@code cancel} are supposed to be invoked in the main thread by {@code Loader}.
 * @param <D> data type
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
abstract class BaseAsync<D> implements Async<D> {

  /**
   * Observers collection.
   * Initial capacity is based on an assumption that we have a really small number of observers
   * created in activities/fragments.
   */
  private final ArrayList<AsyncObserver<D>> observers = new ArrayList<>(3);

  @Override
  public final void subscribe(final AsyncObserver<D> observer) {
    observers.add(observer);
    if (observers.size() == 1) {
      onTrigger();
    }
  }

  @Override
  public final void cancel() {
    observers.clear();
    onCancel();
  }

  protected void onTrigger() {
    // nothing
  }

  protected final void postResult(final D data) {
    int count = observers.size();
    //noinspection ForLoopReplaceableByForEach
    for (int i = 0; i < count; i++) {
      observers.get(i).onResult(data);
    }
  }

  protected final void postError(final Throwable error) {
    int count = observers.size();
    //noinspection ForLoopReplaceableByForEach
    for (int i = 0; i < count; i++) {
      observers.get(i).onError(error);
    }
  }

  protected final void postReset() {
    int count = observers.size();
    //noinspection ForLoopReplaceableByForEach
    for (int i = 0; i < count; i++) {
      observers.get(i).onReset();
    }
  }

  /**
   * Invoked when async operation is canceled.
   */
  protected void onCancel() {
    // nothing
  }

}
