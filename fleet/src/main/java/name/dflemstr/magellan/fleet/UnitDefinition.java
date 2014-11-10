package name.dflemstr.magellan.fleet;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import java.net.URI;
import java.text.MessageFormat;

import name.dflemstr.magellan.fleet.model.Option;

public interface UnitDefinition {

  ImmutableList<Option> getOptions();

  String toSystemdFormat();

  static Builder builder() {
    return UnitDefinitionImpl.begin();
  }

  public interface Builder {

    SectionFocused section(String section);

    default Builder description(String description) {
      return section("Unit").entry("Description", description);
    }

    default Builder documentation(URI... uris) {
      return section("Unit").entry("Documentation", Joiner.on(' ').join(uris));
    }

    default Builder requires(String... units) {
      return section("Unit").entry("Requires", Joiner.on(' ').join(units));
    }

    default Builder requiresOverridable(String... units) {
      return section("Unit").entry("RequiresOverridable", Joiner.on(' ').join(units));
    }

    default Builder requisite(String... units) {
      return section("Unit").entry("Requisite", Joiner.on(' ').join(units));
    }

    default Builder requisiteOverridable(String... units) {
      return section("Unit").entry("RequisiteOverridable", Joiner.on(' ').join(units));
    }

    default Builder wants(String... units) {
      return section("Unit").entry("Wants", Joiner.on(' ').join(units));
    }

    default Builder bindsTo(String... units) {
      return section("Unit").entry("BindsTo", Joiner.on(' ').join(units));
    }

    default Builder partOf(String... units) {
      return section("Unit").entry("PartOf", Joiner.on(' ').join(units));
    }

    default Builder conflicts(String... units) {
      return section("Unit").entry("Conflicts", Joiner.on(' ').join(units));
    }

    default Builder before(String... units) {
      return section("Unit").entry("Before", Joiner.on(' ').join(units));
    }

    default Builder after(String... units) {
      return section("Unit").entry("After", Joiner.on(' ').join(units));
    }

    // TODO: add more stuff from "man systemd.unit"

    UnitDefinition build();

    public interface SectionFocused extends Builder {

      SectionFocused entry(String name, String value);

      default SectionFocused entry(String name, String format, Object... args) {
        return entry(name, MessageFormat.format(format, args));
      }
    }
  }
}
