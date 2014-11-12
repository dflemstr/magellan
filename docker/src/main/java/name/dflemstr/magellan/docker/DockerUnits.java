package name.dflemstr.magellan.docker;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang3.StringEscapeUtils;

import java.text.MessageFormat;

import name.dflemstr.magellan.fleet.UnitDefinition;

public final class DockerUnits {

  private DockerUnits() {
    throw new IllegalAccessError("This class may not be instantiated.");
  }

  public static UnitDefinition.Builder basic(DockerUnitDefinition definition, boolean isTemplate) {
    // Return .Builder instead of .Builder.SectionFocused so that consumers have to call .section()
    // again, and we don't leak which section we have currently selected.

    final String containerPattern = getContainerPattern("%p", isTemplate);

    String image = definition.image();
    return UnitDefinition.builder()
        .after("docker.service")
        .requires("docker.service")

        .section("Service")
        .entry("TimeoutStartSec", "0")
        .entry("ExecStartPre", "-/usr/bin/docker kill {0}", containerPattern)
        .entry("ExecStartPre", "-/usr/bin/docker rm {0}", containerPattern)
        .entry("ExecStartPre", "-/usr/bin/docker pull {0}", image)
        .entry("ExecStart", "/usr/bin/docker run --rm --name {0} -P {1}", containerPattern, image)
        .entry("ExecStop", "/usr/bin/docker stop {0}", containerPattern);
  }

  public static UnitDefinition.Builder etcdRegistrar(
      String service, String domain, ImmutableList<Endpoint> endpoints, boolean isTemplate) {
    // Return .Builder instead of .Builder.SectionFocused so that consumers have to call .section()
    // again, and we don't leak which section we have currently selected.

    final String containerPattern = getContainerPattern(service, isTemplate);

    final String registerCommand =
        createRegisterCommand(domain, service, containerPattern, endpoints, isTemplate);

    final String unregisterCommand =
        createUnregisterCommand(domain, service, containerPattern, endpoints, isTemplate);

    return UnitDefinition.builder()
        .requires("etcd.service")
        .after("etcd.service")

        .section("Service")
        .entry("ExecStart", registerCommand)
        .entry("ExecStopPost", unregisterCommand);
  }

  static String createUnregisterCommand(
      String domain, String service, String containerPattern, ImmutableList<Endpoint> endpoints,
      boolean isTemplate) {
    // This is to get the escaping absolutely correct; it can be tricky
    CommandLine sh = new CommandLine("/bin/sh");
    sh.addArgument("-c");

    StringBuilder scriptBuilder = new StringBuilder();
    for (Endpoint endpoint : endpoints) {
      CommandLine etcdctl = new CommandLine("/usr/bin/etcdctl");
      etcdctl.addArgument("rm");
      etcdctl.addArgument("--recursive");
      etcdctl.addArgument(getKey(domain, service, endpoint.protocol(), isTemplate));

      scriptBuilder.append(Joiner.on(' ').join(etcdctl.toStrings()));
    }
    sh.addArgument(scriptBuilder.toString());

    return Joiner.on(' ').join(sh.toStrings());
  }

  static String getContainerPattern(String service, boolean isTemplate) {
    String containerPattern;
    if (isTemplate) {
      containerPattern = service + ".%i";
    } else {
      containerPattern = service;
    }
    return containerPattern;
  }

  static String createRegisterCommand(
      String domain, String service, String containerPattern, ImmutableList<Endpoint> endpoints,
      boolean isTemplate) {
    StringBuilder registerCommandBuilder = new StringBuilder();

    registerCommandBuilder.append("while true; do ");
    for (Endpoint endpoint : endpoints) {
      String skydnsJson =
          MessageFormat.format(
              "{\"host\":\"%H\",\"port\":$(/usr/bin/docker port {0} {1} | cut -d: -f2)}",
              containerPattern, endpoint.internalPort());

      String registerKeyPattern =
          getKey(domain, service, endpoint.protocol(), isTemplate);

      // Do this to make 'etcdctl ... "..."' behave; it's not for JSON, but rather just to escape '"'
      String escapedSkydnsJson = StringEscapeUtils.escapeJson(skydnsJson);

      registerCommandBuilder.append(
          MessageFormat.format(
              "etcdctl set --ttl 10 {0} \"{1}\"; ",
              registerKeyPattern,
              escapedSkydnsJson));
    }
    registerCommandBuilder.append("sleep 5; done");

    return registerCommandBuilder.toString();
  }

  static String getKey(String domain, String service, String protocol,
                       boolean isTemplate) {
    String domainPath = Joiner.on('/').join(Lists.reverse(Splitter.on('.').splitToList(domain)));
    return MessageFormat.format(
        "/skydns/{0}/services/_{2}/_spotify-{1}/{3}",
        domainPath, service, protocol, isTemplate ? "%i" : "s");
  }
}
