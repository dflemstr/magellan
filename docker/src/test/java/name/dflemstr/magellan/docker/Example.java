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
        DockerUnits.etcdRegistrar(postgresDocker, "pgsql")
            .bindsTo("pgsql.service")
            .after("pgsql.service")
            .section("X-Fleet")
            .entry("MachineOf", "psql.service")
            .build();

    try (Fleet fleet = Fleet.usingFleetCtl();
         Unit.Singleton psql = fleet.service("pgsql", postgres).submitSingleton();
         Unit.Singleton psqlRegistrar = fleet.service("pgsql-registrar", postgresRegistrar)
             .submitSingleton()) {
      psql.start();
      psqlRegistrar.start();
      Thread.sleep(1000);
      psql.stop();
    }
  }
}
