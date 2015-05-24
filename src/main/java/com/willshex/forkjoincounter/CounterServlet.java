//
//  CounterServlet.java
//  appengine-forkjoincounter
//
//  Created by William Shakour (billy1380) on 24 May 2015.
//  Copyright © 2015 WillShex Limited. All rights reserved.
//
package com.willshex.forkjoincounter;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.google.appengine.api.taskqueue.TransientFailureException;
import com.willshex.service.ContextAwareServlet;

/**
 * @author William Shakour (billy1380)
 *
 */
public class CounterServlet extends ContextAwareServlet {

  private static final long serialVersionUID = 5963079326497177021L;
  private static final Logger LOG = Logger.getLogger(CounterServlet.class
      .getName());
  private static final CounterService counterService = new CounterService();

  /*
   * (non-Javadoc)
   * 
   * @see com.willshex.service.ContextAwareServlet#doPost()
   */
  @Override
  protected void doPost() throws ServletException, IOException {
    super.doPost();

    doGet();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.willshex.service.ContextAwareServlet#doGet()
   */
  @Override
  protected void doGet() throws ServletException, IOException {
    super.doGet();

    String appEngineQueue = REQUEST.get().getHeader("X-AppEngine-QueueName");

    if (LOG.isLoggable(Level.FINE)) {
      LOG.log(Level.FINE,
          String.format("App Engine Queue is [%s]", appEngineQueue));
    }

    boolean isNotQueue = false;

    // bail out if we have not been called by app engine queue
    if (isNotQueue = (appEngineQueue == null || !"counter".toLowerCase()
        .equals(appEngineQueue.toLowerCase()))) {
      RESPONSE.get().setStatus(401);
      RESPONSE.get().getOutputStream().print("failure");
      LOG.log(Level.WARNING,
          "Attempt to run script directly, this is not permitted");
      return;
    }

    if (LOG.isLoggable(Level.FINE)) {
      if (!isNotQueue) {
        LOG.log(Level.FINE, String.format(
            "Servelet is being called from [%s] queue", appEngineQueue));
      }
    }

    String name = REQUEST.get().getParameter("name");
    Long index = Long.valueOf(REQUEST.get().getParameter("index"));

    counterService.join(name, index);
  }

  /**
   * @param name
   * @param index
   */
  public static void enqueue(String name, Long index) {
    long now = (new Date()).getTime();

    Queue queue = QueueFactory.getQueue("counter");

    int seconds = 1;
    TaskOptions options = TaskOptions.Builder.withUrl("/counter")
        .taskName(String.format("%s-%d-%d", name, now / 30L, index.intValue()))
        .etaMillis(now + (seconds * 1000)).param("counterName", name)
        .param("index", index.toString());
    options.method(Method.GET);

    try {
      queue.add(options);
    } catch (TransientFailureException ex) {
      if (LOG.isLoggable(Level.WARNING)) {
        LOG.warning(String.format(
            "Could not queue a message because of [%s] - will retry it once",
            ex.toString()));
      }

      // retry once
      try {
        queue.add(options);
      } catch (TransientFailureException reEx) {
        if (LOG.isLoggable(Level.SEVERE)) {
          LOG.log(
              Level.SEVERE,
              String.format("Retry to insert into [%s] failed twice",
                  queue.getQueueName()), reEx);
        }
      }
    }
  }

}
