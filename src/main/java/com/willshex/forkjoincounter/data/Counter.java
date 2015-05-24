//
//  Counter.java
//  appengine-forkjoincounter
//
//  Created by William Shakour (billy1380) on 24 May 2015.
//  Copyright © 2015 WillShex Limited. All rights reserved.
//
package com.willshex.forkjoincounter.data;

import java.util.Date;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.willshex.gson.json.shared.Jsonable;

/**
 * @author William Shakour (billy1380)
 *
 */
@Entity
public class Counter extends Jsonable {

  @Id
  public String name;

  @Index
  public Date created;

  public Long value;

  @Override
  public JsonObject toJson() {
    JsonObject object = super.toJson();
    JsonElement jsonName = name == null ? JsonNull.INSTANCE
        : new JsonPrimitive(name);
    object.add("name", jsonName);
    JsonElement jsonCreated = created == null ? JsonNull.INSTANCE
        : new JsonPrimitive(created.getTime());
    object.add("created", jsonCreated);
    JsonElement jsonValue = value == null ? JsonNull.INSTANCE
        : new JsonPrimitive(value);
    object.add("value", jsonValue);
    return object;
  }

  @Override
  public void fromJson(JsonObject jsonObject) {
    super.fromJson(jsonObject);
    if (jsonObject.has("name")) {
      JsonElement jsonName = jsonObject.get("name");
      if (jsonName != null) {
        name = jsonName.getAsString();
      }
    }
    if (jsonObject.has("created")) {
      JsonElement jsonCreated = jsonObject.get("created");
      if (jsonCreated != null) {
        created = new Date(jsonCreated.getAsLong());
      }
    }
    if (jsonObject.has("value")) {
      JsonElement jsonValue = jsonObject.get("value");
      if (jsonValue != null) {
        value = Long.valueOf(jsonValue.getAsLong());
      }
    }
  }

  public Counter name(String name) {
    this.name = name;
    return this;
  }

  public Counter created(Date created) {
    this.created = created;
    return this;
  }

  public Counter value(Long value) {
    this.value = value;
    return this;
  }
}