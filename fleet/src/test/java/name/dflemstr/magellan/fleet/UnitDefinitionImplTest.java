package name.dflemstr.magellan.fleet;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class UnitDefinitionImplTest {

  @Test
  public void testBuild() throws Exception {
    UnitDefinition unitDefinition = UnitDefinition.builder()
        .section("Foo")
        .entry("Bar", "baz")
        .entry("Bir", "biz")
        .build();

    assertThat(unitDefinition.toSystemdFormat(), is("[Foo]\nBar=baz\nBir=biz\n"));
  }

  @Test
  public void testBuildInterleaved() throws Exception {
    UnitDefinition unitDefinition = UnitDefinition.builder()
        .section("Foo")
        .entry("Bar", "baz")
        .section("Inc")
        .entry("Bar", "baz")
        .section("Foo")
        .entry("Bir", "biz")
        .build();

    assertThat(unitDefinition.toSystemdFormat(),
               is("[Foo]\nBar=baz\nBir=biz\n[Inc]\nBar=baz\n"));
  }

  @Test
  public void testBuildMultiValue() throws Exception {
    UnitDefinition unitDefinition = UnitDefinition.builder()
        .section("Foo")
        .entry("Bar", "baz") // Note: values not alphabetical!
        .entry("Bar", "bain")
        .entry("Bir", "biz")
        .build();

    assertThat(unitDefinition.toSystemdFormat(),
               is("[Foo]\nBar=baz\nBar=bain\nBir=biz\n"));
  }

  @Test
  public void testBuildFormat() throws Exception {
    UnitDefinition unitDefinition = UnitDefinition.builder()
        .section("Foo")
        .entry("Bar", "{0} + {1}", 2, 3)
        .build();

    assertThat(unitDefinition.toSystemdFormat(), is("[Foo]\nBar=2 + 3\n"));
  }

  @Test
  public void testBuildNotOrdered() throws Exception {
    UnitDefinition unitDefinition = UnitDefinition.builder()
        .section("B")
        .entry("B", "b")
        .entry("A", "a")
        .entry("C", "c")
        .section("A")
        .entry("Y", "y")
        .entry("X", "x")
        .entry("Z", "z")
        .section("C")
        .entry("N", "n")
        .entry("M", "m")
        .entry("O", "o")
        .build();

    assertThat(unitDefinition.toSystemdFormat(),
               is("[B]\nB=b\nA=a\nC=c\n[A]\nY=y\nX=x\nZ=z\n[C]\nN=n\nM=m\nO=o\n"));
  }
}
