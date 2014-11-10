package name.dflemstr.magellan.fleet;

import java.io.Closeable;

/**
 * A fleet controlled by a {@code fleetd} cluster.
 */
public interface Fleet extends Closeable {

  /**
   * Creates a new unit in this fleet.
   *
   * @param baseName       The name of the unit.  Should be of the form  {@code
   *                       "foobar.(socket|device|mount|...)"} to create any type of systemd unit.
   * @param unitDefinition The definition for the unit.
   * @return A builder that can be used to specify further unit creation parameters.
   */
  Unit.Builder unit(String baseName, String kind, UnitDefinition unitDefinition);

  /**
   * Creates a new service in this fleet.
   *
   * @param name           The name of the service.  Must not include any {@code "."} characters.
   * @param unitDefinition The definition for the service.
   * @return A builder that can be used to specify further service creation parameters.
   */
  default Unit.Builder service(String name, UnitDefinition unitDefinition) {
    return unit(name, "service", unitDefinition);
  }

  // TODO: add utility methods for other unit types

  /**
   * Returns the fleet that is controlled by the {@code fleetctl} command on the current machine.
   *
   * @return the fleet that is controlled by the {@code fleetctl} command on the current machine.
   */
  static Fleet usingFleetCtl() {
    return CmdFleet.create();
  }

}
