//
//  CounterServiceTest.java
//  appengine-forkjoincounter
//
//  Created by William Shakour (billy1380) on 24 May 2015.
//  Copyright © 2015 SPACEHOPPER STUDIOS Ltd. All rights reserved.
//
package com.willshex.forkjoincounter;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.appengine.api.taskqueue.dev.QueueStateInfo.TaskStateInfo;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

/**
 * @author William Shakour (billy1380)
 *
 */
public class CounterServiceTest {

  // private final LocalTaskQueueTestConfig.TaskCountDownLatch latch = new
  // LocalTaskQueueTestConfig.TaskCountDownLatch(
  // 1);

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
      new LocalDatastoreServiceTestConfig().setApplyAllHighRepJobPolicy(),
      new LocalTaskQueueTestConfig()
          .setQueueXmlPath("src/test/resources/queue.xml")
      // .setDisableAutoTaskExecution(false)
      // .setCallbackClass(LocalTaskQueueTestConfig.DeferredTaskCallback.class)
      // .setTaskExecutionLatch(latch)
      , new LocalMemcacheServiceTestConfig());

  private Closeable objectify;

  @Before
  public void setUp() {
    helper.setUp();
    objectify = ObjectifyService.begin();
  }

  @After
  public void tearDown() {
    helper.tearDown();

    objectify.close();
  }

  ICounterService c = null;

  private ICounterService counterService() {
    if (c == null) {
      c = new CounterService();
    }

    return c;
  }

  @Test
  public void countTest() throws InterruptedException, IOException,
      SAXException {
    counterService().increment("testcounter");
    counterService().increment("testcounter");
    counterService().increment("testcounter");
    counterService().increment("testcounter");
    counterService().increment("testcounter");
    // assertTrue(latch.await(5, TimeUnit.SECONDS));

    TaskStateInfo info = LocalTaskQueueTestConfig.getLocalTaskQueue()
        .getQueueStateInfo().get("counter").getTaskInfo().get(0);

    ServletRunner runner = new ServletRunner();
    runner.registerServlet("/counter", CounterServlet.class.getName());
    ServletUnitClient client = runner.newClient();
    GetMethodWebRequest request = new GetMethodWebRequest("http:"
        + info.getUrl());
    request.setHeaderField("X-AppEngine-QueueName", "counter");
    client.sendRequest(request);

    assertEquals(5L, counterService().value("testcounter"));
  }
}
