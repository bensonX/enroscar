package com.stanfy.stviews.test;

import java.util.concurrent.Semaphore;

import android.app.Instrumentation;
import android.os.Bundle;
import android.test.TouchUtils;
import android.view.View;

/**
 * List view tests.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class ListViewTest extends BaseTestActivity<ListViewActivity> {

  public ListViewTest() {
    super(ListViewActivity.class);
  }

  /**
   * Test initialization.
   */
  public void testPreConditions() {
    assertNotNull(getActivity().getListAdapter());
  }

  /**
   * Test saving state.
   */
  public synchronized void testSavingState() throws Exception {
    final ListViewActivity activity = getActivity();
    final Instrumentation instrumentation = getInstrumentation();

    TouchUtils.dragQuarterScreenUp(this, activity);
    final long sleep = 500;
    Thread.sleep(sleep);
    final View old = activity.getListView();
    final int pos = activity.getListView().getFirstVisiblePosition();
    assertTrue(pos > 0);
    final Bundle b = new Bundle();
    instrumentation.callActivityOnSaveInstanceState(activity, b);
    final ListViewActivity a = activity;
    final Semaphore s = new Semaphore(0);
    final Runnable r = new Runnable() {
      @Override
      public void run() {
        instrumentation.callActivityOnCreate(a, b);
        instrumentation.callActivityOnStart(a);
        instrumentation.callActivityOnRestoreInstanceState(a, b);
        instrumentation.callActivityOnResume(a);
        s.release();
      }
    };
    activity.runOnUiThread(r);
    s.acquire();
    Thread.sleep(sleep);
    assertFalse(activity.getListView() == old);
    assertEquals(pos, activity.getListView().getFirstVisiblePosition());
  }

}
