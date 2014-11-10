package name.dflemstr.magellan.fleet.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@AutoValue
@JsonInclude(NON_NULL)
public abstract class UnitInfo {

  UnitInfo() {
  }

  @JsonProperty("name")
  @Nullable
  public abstract String name();

  @JsonProperty("desiredState")
  public abstract State desiredState();

  @JsonProperty("currentState")
  @Nullable
  public abstract State currentState();

  @JsonProperty("machineId")
  @Nullable
  public abstract String machineId();

  @JsonProperty("options")
  public abstract ImmutableList<Option> options();

  public static UnitInfo define(State desiredState, ImmutableList<Option> options) {
    return new AutoValue_UnitInfo(null, desiredState, null, null, options);
  }

  @JsonCreator
  public static UnitInfo details(
      @JsonProperty("name")
      String name,
      @JsonProperty("desiredState")
      State desiredState,
      @JsonProperty("currentState")
      State currentState,
      @JsonProperty("machineId")
      String machineId,
      @JsonProperty("options")
      ImmutableList<Option> options) {
    return new AutoValue_UnitInfo(name, desiredState, currentState, machineId, options);
  }
}
