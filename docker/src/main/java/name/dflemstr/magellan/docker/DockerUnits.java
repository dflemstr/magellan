package name.dflemstr.magellan.docker;

import name.dflemstr.magellan.fleet.UnitDefinition;

public final class DockerUnits {

  private DockerUnits() {
    throw new IllegalAccessError("This class may not be instantiated.");
  }

  public static UnitDefinition.Builder basic(DockerUnitDefinition definition) {
    // Return .Builder instead of .Builder.SectionFocused so that consumers have to call .section()
    // again, and we don't leak which section we have currently selected.
    return UnitDefinition.builder()
        .after("docker.service")
        .requires("docker.service")
        .after("etcd.service")
        .requires("etcd.service")
        .section("Service")
        .entry("TimeoutStartSec", "0")
        .entry("ExecStartPre", "-/usr/bin/docker kill %n")
        .entry("ExecStartPre", "-/usr/bin/docker rm %n")
        .entry("ExecStartPre", "-/usr/bin/docker pull {0}", definition.image())
        .entry("ExecStart", "/usr/bin/docker run --rm --name %n -P {0}", definition.image())
        .entry("ExecStop", "/usr/bin/docker stop %n");
  }

  public static UnitDefinition.Builder etcdRegistrar(DockerUnitDefinition definition,
                                                     String unitNameToRegister) {
    // Return .Builder instead of .Builder.SectionFocused so that consumers have to call .section()
    // again, and we don't leak which section we have currently selected.

    if (unitNameToRegister.endsWith(".service")) {
      unitNameToRegister =
          unitNameToRegister.substring(0, unitNameToRegister.length() - ".service".length());
    }

    if (unitNameToRegister.endsWith("@")) {
      unitNameToRegister += "%i";
    }

    unitNameToRegister += ".service";

    return UnitDefinition.builder()
        .section("Service")
        .entry("ExecStart",
               "/bin/sh -c \"while true; do /usr/bin/docker port {0} | while read spec x port; do /usr/bin/etcdctl set /unit/{0}/port/$(echo $spec | tr '/' ':') %H:$(echo $port | cut -d: -f2) --ttl 10; done; sleep 5; done\"",
               unitNameToRegister)
        .entry("ExecStop",
               "/usr/bin/etcdctl rm --recursive /unit/{0}", unitNameToRegister);
  }
}
