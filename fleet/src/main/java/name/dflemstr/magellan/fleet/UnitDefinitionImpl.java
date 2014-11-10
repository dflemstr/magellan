package name.dflemstr.magellan.fleet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;

import java.util.List;
import java.util.Map;

import name.dflemstr.magellan.fleet.model.Option;

class UnitDefinitionImpl implements UnitDefinition {

  // Each entry: <section, name, values>
  private final ImmutableTable<String, String, ImmutableList<String>> entries;

  UnitDefinitionImpl(final ImmutableTable<String, String, ImmutableList<String>> entries) {
    this.entries = entries;
  }

  static BuilderImpl begin() {
    // Insertion-ordered
    return new BuilderImpl(
        Tables.<String, String, List<String>>newCustomTable(
            Maps.newLinkedHashMap(),
            Maps::newLinkedHashMap));
  }

  @Override
  public ImmutableList<Option> getOptions() {
    ImmutableList.Builder<Option> resultBuilder = ImmutableList.builder();

    for (Table.Cell<String, String, ImmutableList<String>> cell : entries.cellSet()) {
      for (String value : cell.getValue()) {
        resultBuilder.add(Option.create(cell.getRowKey(), cell.getColumnKey(), value));
      }
    }

    return resultBuilder.build();
  }

  @Override
  public String toSystemdFormat() {
    final StringBuilder resultBuilder = new StringBuilder();

    for (final Map.Entry<String, Map<String, ImmutableList<String>>> section : entries.rowMap()
        .entrySet()) {
      resultBuilder.append('[').append(section.getKey()).append("]\n");

      for (final Map.Entry<String, ImmutableList<String>> entry :
          section.getValue().entrySet()) {
        for (String value : entry.getValue()) {
          resultBuilder.append(entry.getKey()).append('=').append(value).append('\n');
        }
      }
    }

    return resultBuilder.toString();
  }

  static class BuilderImpl implements Builder {

    private final Table<String, String, List<String>> entriesBuilder;

    BuilderImpl(final Table<String, String, List<String>> entriesBuilder) {
      this.entriesBuilder = entriesBuilder;
    }

    @Override
    public SectionFocusedImpl section(final String section) {
      return new SectionFocusedImpl(section);
    }

    @Override
    public UnitDefinitionImpl build() {
      return new UnitDefinitionImpl(ImmutableTable.copyOf(
          Tables.transformValues(entriesBuilder, ImmutableList::copyOf)));
    }

    class SectionFocusedImpl implements SectionFocused {

      private final String section;

      SectionFocusedImpl(String section) {
        this.section = section;
      }

      @Override
      public SectionFocusedImpl section(String section) {
        return BuilderImpl.this.section(section);
      }

      @Override
      public UnitDefinitionImpl build() {
        return BuilderImpl.this.build();
      }

      @Override
      public SectionFocusedImpl entry(String name, String value) {
        List<String> values = entriesBuilder.get(section, name);
        if (values == null) {
          values = Lists.newArrayList();
          entriesBuilder.put(section, name, values);
        }
        values.add(value);
        return this;
      }
    }
  }
}
