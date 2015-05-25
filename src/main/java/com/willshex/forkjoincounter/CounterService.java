//
//  CounterService.java
//  appengine-forkjoincounter
//
//  Created by William Shakour (billy1380) on 24 May 2015.
//  Copyright © 2015 WillShex Limited. All rights reserved.
//
package com.willshex.forkjoincounter;

import java.util.Date;
import java.util.List;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.taskqueue.TaskAlreadyExistsException;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Work;
import com.willshex.forkjoincounter.data.Counter;
import com.willshex.forkjoincounter.data.Increment;

/**
 * @author William Shakour (billy1380)
 *
 */
public class CounterService implements ICounterService {

  static {
    factory().register(Counter.class);
    factory().register(Increment.class);
  }

  private static Objectify ofy() {
    return ObjectifyService.ofy();
  }

  private static ObjectifyFactory factory() {
    return ObjectifyService.factory();
  }

  private MemcacheService memcache = MemcacheServiceFactory
      .getMemcacheService();

  private static final long POW_2_16 = 0x1000000000000000L;
  private static final long POW_2_15 = 0x0100000000000000L;

  private boolean insert(String name, int delta) {
    Long index = (Long) memcache.get("index-" + name);
    if (index == null) {
      memcache.put("index-" + name, Long.valueOf(1));
      index = (Long) memcache.get("index-" + name);
    }

    String lock = String.format("%s-lock-%d", name, index.longValue());
    if (memcache.get(lock) == null) {
      memcache.put(lock, Long.valueOf(0));
    }

    Long writers = memcache.increment(lock, POW_2_16);
    if (writers.longValue() < POW_2_16) {
      memcache.increment(lock, -1L);
      return false;
    }

    ofy()
        .save()
        .entity(
            new Increment().size(delta).index(
                String.format("%s-%d", name, Long.toString(index).hashCode())))
        .now();

    try {
      CounterServlet.enqueue(name, index);
    } catch (TaskAlreadyExistsException ex) {
      // Fan-in magic
    } finally {
      memcache.increment(lock, -1L);
    }

    // increment the running counter in memcache
    if (memcache.get(name) == null) {
      value(name); // calling value puts the value in memcache
    }

    memcache.increment(name, delta);

    return true;
  }

  public void join(final String name, Long index) {
    // force new writers to use the next index
    memcache.increment("index-" + name, 1L);

    String lock = String.format("%s-lock-%d", name, index.intValue());
    memcache.increment(lock, -POW_2_15); // You missed the boat

    // busy wait for writers
    for (int i = 0; i < 20; i++) { // timeout after 5s
      if (memcache.get(lock) == null
          || ((Long) memcache.get(lock)).longValue() <= POW_2_15) {
        break;
      }

      try {
        Thread.sleep(250);
      } catch (InterruptedException e) {
      }
    }

    List<Increment> results = ofy()
        .load()
        .type(Increment.class)
        .filter("index",
            String.format("%s-%d", name, index.toString().hashCode()))
        .orderKey(false).list();

    final long delta = sumIncrements(results);
    Counter updated = ofy().transact(new Work<Counter>() {
      public Counter run() {
        Counter counter = ofy().load().type(Counter.class).id(name).now();
        if (counter == null) {
          counter = new Counter().name(name).value(Long.valueOf(0))
              .created(new Date());
        }

        counter.value(Long.valueOf(counter.value.longValue() + delta));
        ofy().save().entity(counter).now();

        return counter;
      }

    });

    memcache.put(updated.name, updated.value);
    ofy().delete().entities(results).now();
  }

  /**
   * @param results
   * @return
   */
  private long sumIncrements(List<Increment> results) {
    long sum = 0;
    for (Increment increment : results) {
      sum += increment.size.longValue();
    }
    return sum;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.willshex.forkjoincounter.ICounterService#increment(java.lang.String)
   */
  public void increment(String name) {
    increment(name, 1);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.willshex.forkjoincounter.ICounterService#increment(java.lang.String,
   * int)
   */
  public void increment(String name, int delta) {
    insert(name, delta);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.willshex.forkjoincounter.ICounterService#decrement(java.lang.String)
   */
  public void decrement(String name) {
    decrement(name, 1);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.willshex.forkjoincounter.ICounterService#decrement(java.lang.String,
   * int)
   */
  public void decrement(String name, int delta) {
    increment(name, -delta);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.willshex.forkjoincounter.ICounterService#value(java.lang.String)
   */
  public long value(String name) {
    Counter counter = ofy().load().type(Counter.class).id(name).now();
    Long value = counter == null ? Long.valueOf(0) : counter.value;
    memcache.put(name, value);
    return value;
  }

}
