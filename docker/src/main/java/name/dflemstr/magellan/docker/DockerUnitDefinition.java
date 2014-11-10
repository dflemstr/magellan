package name.dflemstr.magellan.docker;

import com.google.common.collect.ImmutableList;

public interface DockerUnitDefinition {

  String image();

  ImmutableList<String> args();

  static Builder usingImage(String image) {
    return DockerUnitDefinitionImpl.BuilderImpl.create(image);
  }

  public interface Builder {

    // TODO: add things like extra port config or custom entry point etc.
    Builder args(String... args);

    Builder args(ImmutableList<String> args);

    DockerUnitDefinition build();
  }
}
