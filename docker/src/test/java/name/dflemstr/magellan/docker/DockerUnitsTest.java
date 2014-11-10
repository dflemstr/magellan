package name.dflemstr.magellan.docker;

import org.junit.Test;

import name.dflemstr.magellan.fleet.UnitDefinition;

public class DockerUnitsTest {

  @Test
  public void testBasic() throws Exception {
    UnitDefinition unitDefinition =
        DockerUnits.basic(
            DockerUnitDefinition.usingImage("postgres").build()).build();

    String unitFile = unitDefinition.toSystemdFormat();
  }
}
