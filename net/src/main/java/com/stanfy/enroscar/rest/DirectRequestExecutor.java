package com.stanfy.enroscar.rest;

import android.content.Context;
import android.util.Log;

import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.rest.RequestMethod.RequestMethodException;
import com.stanfy.enroscar.rest.RequestMethod.RequestResult;
import com.stanfy.enroscar.rest.request.RequestDescription;
import com.stanfy.enroscar.rest.response.ContentAnalyzer;
import com.stanfy.enroscar.rest.response.ResponseData;
import com.stanfy.enroscar.rest.response.ResponseModelConverter;

/**
 * Performs request synchronously.
 */
public class DirectRequestExecutor implements RequestExecutor {

  /** Logging tag. */
  private static final String TAG = "Request";
  
  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_REST;

  /** Application context. */
  private final Context context;
  /** Configuration. */
  private final RemoteServerApiConfiguration config;
  /** Hooks. */
  private final DirectRequestExecutorHooks hooks;
  
  public DirectRequestExecutor(final Context context, final DirectRequestExecutorHooks hooks) {
    this.context = context.getApplicationContext();
    this.config = BeansManager.get(context)
          .getContainer().getBean(RemoteServerApiConfiguration.BEAN_NAME, RemoteServerApiConfiguration.class);
    this.hooks = hooks;
  }

  /** @return configuration object */
  public RemoteServerApiConfiguration getConfig() { return config; }

  /** @return hooks object */
  public DirectRequestExecutorHooks getHooks() { return hooks; }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static ResponseData<?> analyze(final Context context, final ContentAnalyzer analyzer, final ResponseData<?> responseData,
      final RequestDescription description) throws RequestMethodException {
    final ResponseData data = responseData;
    return analyzer.analyze(context, description, data);
  }

  @Override
  public int performRequest(final RequestDescription description) {
    final RequestMethod requestMethod = config.getRequestMethod(description);
    final ResponseModelConverter converter = config.getResponseModelConverter(description);

    ContentAnalyzer<?, ?> analyzer = null;

    final String analyzerBeanName = description.getContentAnalyzer();
    if (analyzerBeanName != null) {
      analyzer = BeansManager.get(context).getContainer().getBean(analyzerBeanName, ContentAnalyzer.class);
      if (analyzer == null) {
        throw new RuntimeException("ContentAnalyzer bean with name " + analyzerBeanName + " is not declared.");
      }
    }

    if (DEBUG) { Log.d(TAG, "Process request id " + description.getId()); }

    hooks.beforeRequestProcessingStarted(description, requestMethod);

    boolean passedToAnalyzer = false;

    try {
      // execute request method
      final RequestResult res = requestMethod.perform(context, description);

      // check for cancel
      if (description.isCanceled()) {
        hooks.onRequestCancel(description, null);
        return description.getId();
      }

      // process results
      ResponseData<?> response
          = converter.toResponseData(description, res.getConnection(), res.getModel());

      // check for cancel
      if (description.isCanceled()) {
        hooks.onRequestCancel(description, response);
        return description.getId();
      }

      // analyze
      passedToAnalyzer = true;
      if (analyzer != null) {
        response = analyze(context, analyzer, response, description);
      }

      // report results
      if (response.isSuccessful()) {
        hooks.onRequestSuccess(description, response);
      } else {
        Log.e(TAG, "Server error: " + response.getErrorCode() + ", " + response.getMessage());
        hooks.onRequestError(description, response);
      }

    } catch (final RequestMethodException e) {

      Log.e(TAG, "Request method error while processing " + description, e);
      ResponseData<?> data = converter.toResponseData(description, e);

      if (analyzer != null && !passedToAnalyzer) {
        try {
          data = analyze(context, analyzer, data, description);
        } catch (RequestMethodException analyzerException) {
          Log.e(TAG, "Analyzer exception analyzerName=" + analyzerBeanName + " for " + description, analyzerException);
          // repack data to use the current exception
          data = converter.toResponseData(description, analyzerException);
        }
      }

      hooks.onRequestError(description, data);

    } finally {
      hooks.afterRequestProcessingFinished(description, requestMethod);
    }
    
    return description.getId();
  }

}
