package name.dflemstr.magellan.fleet.model;

import com.google.auto.value.AutoValue;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@AutoValue
public abstract class Option {

  Option() {
  }

  @JsonProperty("section")
  public abstract String section();

  @JsonProperty("name")
  public abstract String name();

  @JsonProperty("value")
  public abstract String value();

  @JsonCreator
  public static Option create(
      @JsonProperty("section") String section,
      @JsonProperty("name") String name,
      @JsonProperty("value") String value) {
    return new AutoValue_Option(section, name, value);
  }
}
