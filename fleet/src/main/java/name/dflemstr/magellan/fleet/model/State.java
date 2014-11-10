package name.dflemstr.magellan.fleet.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum State {
  INACTIVE("inactive"),
  LOADED("loaded"),
  LAUNCHED("launched");

  private final String value;

  State(String value) {
    this.value = value;
  }

  @JsonValue
  public String value() {
    return value;
  }

  @JsonCreator
  public static State forValue(String value) {
    for (State state : values()) {
      if (state.value().equals(value)) {
        return state;
      }
    }
    throw new IllegalArgumentException("No such state: " + value);
  }
}
