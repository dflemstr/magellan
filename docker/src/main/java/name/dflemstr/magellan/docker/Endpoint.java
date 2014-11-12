package name.dflemstr.magellan.docker;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Endpoint {

  Endpoint() {
  }

  public abstract String protocol();

  public abstract int internalPort();

  public Endpoint create(String protocol, int internalPort) {
    return new AutoValue_Endpoint(protocol, internalPort);
  }
}
