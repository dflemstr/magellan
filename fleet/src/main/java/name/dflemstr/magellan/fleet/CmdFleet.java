package name.dflemstr.magellan.fleet;

import com.google.common.base.Preconditions;

class CmdFleet implements Fleet {

  static CmdFleet create() {
    return new CmdFleet();
  }

  @Override
  public Unit.Builder unit(String baseName, String kind, UnitDefinition unitDefinition) {
    Preconditions
        .checkArgument(!baseName.contains("."), "The unit base name may not contain any '.'");
    Preconditions
        .checkArgument(!baseName.contains("@"), "The unit base name may not contain any '@'");
    return CmdUnit.BuilderImpl.create(unitDefinition, baseName, kind);
  }

  @Override
  public void close() {
    // Do nothing
  }
}
