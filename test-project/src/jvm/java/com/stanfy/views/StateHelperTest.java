package com.stanfy.views;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

import android.content.Context;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.stanfy.test.AbstractEnroscarTest;
import com.stanfy.views.StateHelper.StateViewCreator;

/**
 * {@link com.stanfy.views.StateHelper} test.
 * @author Vladislav Lipskiy - Stanfy (http://www.stanfy.com)
 */
public class StateHelperTest extends AbstractEnroscarTest {

  /** Test helper state. */
  private static final int STATE_TEST = 1;

  /** Test state helper. */
  private final StateHelper stateHelper = new StateHelper() {

    @Override
    protected StateViewCreator[] constructCreatorsArray() {
      // The only state (except STATE_NORMAL = 0) is test state
      return new StateViewCreator[STATE_TEST + 1];
    }

  };

  private static void measureAndLayout(final ViewGroup parent, final int width, final int height) {
    final int widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
    final int heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
    parent.measure(widthMeasureSpec, heightMeasureSpec);
    parent.layout(0, 0, parent.getMeasuredWidth(), parent.getMeasuredHeight());
  }

  /**
   * Test case 1.
   * State view wants to match parent, but parent has not been measured and layout.
   * Expecting MATCH_PARENT in state view layout params as a result.
   */
  @Test
  public void testStretchWithoutParentSize() {
    final Context context = getApplication();

    final FrameLayout parent = new FrameLayout(context);

    // Configure state helper
    stateHelper.setStateViewCreator(STATE_TEST, new BaseTestStateViewCreator() {
      @Override
      protected LayoutParams createLayoutParams() {
        return new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
      }
    });

    final View stateView = stateHelper.getCustomStateView(STATE_TEST, context, null, parent);
    assertThat(stateView, notNullValue());

    final ViewGroup.LayoutParams lp = stateView.getLayoutParams();
    assertThat(lp, notNullValue());

    assertThat(lp.width, equalTo(LayoutParams.MATCH_PARENT));
    assertThat(lp.height, equalTo(LayoutParams.MATCH_PARENT));
  }

  /**
   * Test case 2.
   * State view wants to match parent, parent has been measured and layout.
   * Expecting that state view layout params width and height will be the same as specified.
   */
  @Test
  public void testStretchWithParentSize() {
    final Context context = getApplication();

    final int width = 480;
    final int height = 800;

    final FrameLayout parent = new FrameLayout(context);
    measureAndLayout(parent, width, height);

    // Configure state helper
    stateHelper.setStateViewCreator(STATE_TEST, new BaseTestStateViewCreator() {
      @Override
      protected LayoutParams createLayoutParams() {
        return new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
      }
    });

    final View stateView = stateHelper.getCustomStateView(STATE_TEST, context, null, parent);
    assertThat(stateView, notNullValue());

    final ViewGroup.LayoutParams lp = stateView.getLayoutParams();
    assertThat(lp, notNullValue());

    assertThat(lp.width, equalTo(width));
    assertThat(lp.height, equalTo(height));
  }

  /**
   * Test case 3.
   * Similar to case 2, but additionally state view size must be recalculated
   * if parent size changes.
   */
  @Test
  public void testStretchWithParentSizeSubsequentCalls() {
    final Context context = getApplication();

    final int width = 480;
    final int height = 800;

    final FrameLayout parent = new FrameLayout(context);
    measureAndLayout(parent, width, height);

    // Configure state helper
    stateHelper.setStateViewCreator(STATE_TEST, new BaseTestStateViewCreator() {
      @Override
      protected LayoutParams createLayoutParams() {
        return new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
      }
    });

    View stateView = stateHelper.getCustomStateView(STATE_TEST, context, null, parent);
    assertThat(stateView, notNullValue());

    ViewGroup.LayoutParams lp = stateView.getLayoutParams();
    assertThat(lp, notNullValue());

    assertThat(lp.width, equalTo(width));
    assertThat(lp.height, equalTo(height));

    // Step 2 - change parent size
    final int newWidth = 240;
    final int newHeight = 320;

    measureAndLayout(parent, newWidth, newHeight);

    stateView = stateHelper.getCustomStateView(STATE_TEST, context, null, parent);
    assertThat(stateView, notNullValue());

    lp = stateView.getLayoutParams();
    assertThat(lp, notNullValue());

    assertThat(lp.width, equalTo(newWidth));
    assertThat(lp.height, equalTo(newHeight));

    // Step 3 - fall back to MATCH_PARENT
    measureAndLayout(parent, 0, 0);

    stateView = stateHelper.getCustomStateView(STATE_TEST, context, null, parent);
    assertThat(stateView, notNullValue());

    lp = stateView.getLayoutParams();
    assertThat(lp, notNullValue());

    assertThat(lp.width, equalTo(LayoutParams.MATCH_PARENT));
    assertThat(lp.height, equalTo(LayoutParams.MATCH_PARENT));
  }

  /**
   * Test case 4.
   * State view provides fixed size or WRAP_CONTENT.
   * Expecting that state view layout params will not be changed.
   */
  @Test
  public void testFixedWithParentSize() {
    final Context context = getApplication();

    final int width = 480;
    final int height = 800;

    final int stateViewWidth = 240;
    final int stateViewHeight = 320;

    final FrameLayout parent = new FrameLayout(context);
    measureAndLayout(parent, width, height);

    // Configure state helper
    stateHelper.setStateViewCreator(STATE_TEST, new BaseTestStateViewCreator() {
      @Override
      protected LayoutParams createLayoutParams() {
        return new ViewGroup.LayoutParams(stateViewWidth, stateViewHeight);
      }
    });

    final View stateView = stateHelper.getCustomStateView(STATE_TEST, context, null, parent);
    assertThat(stateView, notNullValue());

    final ViewGroup.LayoutParams lp = stateView.getLayoutParams();
    assertThat(lp, notNullValue());

    assertThat(lp.width, equalTo(stateViewWidth));
    assertThat(lp.height, equalTo(stateViewHeight));
  }

  /**
   * Test state view creator.
   * @author Vladislav Lipskiy - Stanfy (http://www.stanfy.com)
   */
  private abstract class BaseTestStateViewCreator extends StateViewCreator {

    protected abstract ViewGroup.LayoutParams createLayoutParams();

    @Override
    protected View createView(final Context context, final ViewGroup parent) {
      final View stateView = new View(context);
      stateView.setLayoutParams(createLayoutParams());
      return stateView;
    }

    @Override
    protected void bindView(final Context context, final View view, final Object lastResponseData, final ViewGroup parent) {
      // Nothing
    }

  }

}
