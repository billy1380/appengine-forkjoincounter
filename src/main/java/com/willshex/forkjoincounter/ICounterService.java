//
//  ICounterService.java
//  appengine-forkjoincounter
//
//  Created by William Shakour (billy1380) on 24 May 2015.
//  Copyright © 2015 SPACEHOPPER STUDIOS Ltd. All rights reserved.
//
package com.willshex.forkjoincounter;

/**
 * @author William Shakour (billy1380)
 *
 */
public interface ICounterService {

  public void increment(String name);

  public void increment(String name, int delta);

  public void decrement(String name);

  public void decrement(String name, int delta);

  public long value(String name);

}
