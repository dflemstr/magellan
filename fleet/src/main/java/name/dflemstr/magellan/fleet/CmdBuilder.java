package name.dflemstr.magellan.fleet;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;

class CmdBuilder {

  private final ImmutableList.Builder<String> argsBuilder;

  CmdBuilder(ImmutableList.Builder<String> argsBuilder) {
    this.argsBuilder = argsBuilder;
  }

  static CmdBuilder prog(String prog) {
    CmdBuilder result = new CmdBuilder(ImmutableList.builder());
    return result.arg(prog);
  }

  CmdBuilder arg(String arg) {
    argsBuilder.add(quoteArg(arg));
    return this;
  }

  private String quoteArg(String arg) {
    boolean hasSpaces = CharMatcher.WHITESPACE.matchesAnyOf(arg);
    boolean hasDoubleQuote = CharMatcher.is('"').matchesAnyOf(arg);
    boolean hasSingleQuote = CharMatcher.is('\'').matchesAnyOf(arg);

    if (!hasSpaces && !hasSingleQuote && !hasDoubleQuote) {
      return arg;
    } else if (!hasSingleQuote) {
      return "'" + arg + "'";
    } else if (!hasDoubleQuote) {
      return "\"" + arg.replace("\\", "\\\\") + "\"";
    } else {

    }
  }
}
