//
//  Increment.java
//  appengine-forkjoincounter
//
//  Created by William Shakour (billy1380) on 24 May 2015.
//  Copyright © 2015 WillShex Limited. All rights reserved.
//
package com.willshex.forkjoincounter.data;

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
public class Increment extends Jsonable {

  @Id
  Long id;

  @Index
  public String name;

  public Integer size;

  @Override
  public JsonObject toJson() {
    JsonObject object = super.toJson();
    JsonElement jsonId = id == null ? JsonNull.INSTANCE : new JsonPrimitive(id);
    object.add("id", jsonId);
    JsonElement jsonName = name == null ? JsonNull.INSTANCE
        : new JsonPrimitive(name);
    object.add("name", jsonName);
    JsonElement jsonSize = size == null ? JsonNull.INSTANCE
        : new JsonPrimitive(size);
    object.add("size", jsonSize);
    return object;
  }

  @Override
  public void fromJson(JsonObject jsonObject) {
    super.fromJson(jsonObject);
    if (jsonObject.has("id")) {
      JsonElement jsonId = jsonObject.get("id");
      if (jsonId != null) {
        id = Long.valueOf(jsonId.getAsLong());
      }
    }
    if (jsonObject.has("name")) {
      JsonElement jsonName = jsonObject.get("name");
      if (jsonName != null) {
        name = jsonName.getAsString();
      }
    }
    if (jsonObject.has("size")) {
      JsonElement jsonSize = jsonObject.get("size");
      if (jsonSize != null) {
        size = Integer.valueOf(jsonSize.getAsInt());
      }
    }
  }

  public Increment id(Long id) {
    this.id = id;
    return this;
  }

  public Increment name(String name) {
    this.name = name;
    return this;
  }

  public Increment size(Integer size) {
    this.size = size;
    return this;
  }
}