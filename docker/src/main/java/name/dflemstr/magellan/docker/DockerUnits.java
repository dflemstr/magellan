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
        .entry("ExecStartPre", "-/usr/bin/docker kill %p.%i")
        .entry("ExecStartPre", "-/usr/bin/docker rm %p.%i")
        .entry("ExecStartPre", "-/usr/bin/docker pull {0}", definition.image())
        .entry("ExecStart", "/usr/bin/docker run --rm --name %p.%i -P {0}", definition.image())
        .entry("ExecStop", "/usr/bin/docker stop %p.%i");
  }

  public static UnitDefinition.Builder etcdRegistrar(String unitNameToRegister) {
    // Return .Builder instead of .Builder.SectionFocused so that consumers have to call .section()
    // again, and we don't leak which section we have currently selected.
    return UnitDefinition.builder()
        .section("Service")
        .entry("ExecStart",
               "/bin/sh -c \"while true; do /usr/bin/docker port {1} | while read spec x port; do /usr/bin/etcdctl set /unit/{0}/port/$(echo $spec | tr '/' ':') %H:$(echo $port | cut -d: -f2) --ttl 10; done; sleep 5; done\"",
               unitNameToRegister, unitNameToRegister.replace('@', '.'))
        .entry("ExecStopPost",
               "-/usr/bin/etcdctl rm --recursive /unit/{0}", unitNameToRegister);
  }
}
