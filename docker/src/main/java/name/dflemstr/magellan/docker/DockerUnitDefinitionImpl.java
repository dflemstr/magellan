package name.dflemstr.magellan.docker;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

@AutoValue
abstract class DockerUnitDefinitionImpl implements DockerUnitDefinition {

  DockerUnitDefinitionImpl() {
  }

  private static DockerUnitDefinitionImpl create(String image, ImmutableList<String> args) {
    return new AutoValue_DockerUnitDefinitionImpl(image, args);
  }

  @AutoValue
  static abstract class BuilderImpl implements Builder {

    private ImmutableList<String> args = ImmutableList.of();

    abstract String image();

    static BuilderImpl create(String image) {
      return new AutoValue_DockerUnitDefinitionImpl_BuilderImpl(image);
    }

    @Override
    public BuilderImpl args(String... args) {
      this.args = ImmutableList.copyOf(args);
      return this;
    }

    @Override
    public BuilderImpl args(ImmutableList<String> args) {
      this.args = args;
      return this;
    }

    @Override
    public DockerUnitDefinitionImpl build() {
      return DockerUnitDefinitionImpl.create(image(), args);
    }
  }
}
