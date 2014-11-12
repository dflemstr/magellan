package name.dflemstr.magellan.docker;

import java.io.IOException;

import name.dflemstr.magellan.fleet.Fleet;
import name.dflemstr.magellan.fleet.Unit;
import name.dflemstr.magellan.fleet.UnitDefinition;

public class Example {

  public static void main(String[] args) throws IOException, InterruptedException {
    DockerUnitDefinition postgresDocker = DockerUnitDefinition.usingImage("postgres").build();
    UnitDefinition postgres = DockerUnits.basic(postgresDocker).build();
    UnitDefinition postgresRegistrar =
        DockerUnits.etcdRegistrar("pgsql@%i.service",
                                  "_spotify-${service}._${protocol}.services.${domain}")
            .bindsTo("pgsql@%i.service")
            .after("pgsql@%i.service")
            .section("X-Fleet")
            .entry("MachineOf", "pgsql@%i.service")
            .build();

    try (Fleet fleet = Fleet.usingFleetCtl();
         Unit.Template psql = fleet.service("pgsql", postgres).submitTemplate();
         Unit.Template psqlRegistrar = fleet.service("pgsql-registrar", postgresRegistrar)
             .submitTemplate()) {
      psql.instance(1).startAsync();
      psql.instance(2).startAsync();
      psqlRegistrar.instance(1).startAsync();
      psqlRegistrar.instance(2).startAsync();
      Thread.sleep(1000);
      psql.instance(1).stopAsync();
      psql.instance(2).stopAsync();
    }
  }
}
