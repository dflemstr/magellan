package name.dflemstr.magellan.fleet;

import com.google.auto.value.AutoValue;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;

import java.io.BufferedReader;
import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import name.dflemstr.magellan.fleet.model.State;
import name.dflemstr.magellan.fleet.model.UnitInfo;

import static java.lang.ProcessBuilder.Redirect.INHERIT;

abstract class CmdUnit implements Unit {

  static final Splitter STATUS_SPLITTER = Splitter.on('\t').omitEmptyStrings();

  CmdUnit() {
  }

  abstract String baseName();

  abstract String kind();

  abstract boolean isTemplate();

  @Override
  public void close() throws IOException {
    try {
      // TODO: remove inheritIO
      if (new ProcessBuilder().command("fleetctl", "destroy", name()).inheritIO().start()
              .waitFor()
          != 0) {
        throw new RuntimeException("Could not destroy unit");
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted while destroying unit", e);
    }
  }

  @AutoValue
  static abstract class SingletonImpl extends CmdUnit implements Singleton {

    SingletonImpl() {
    }

    @Nullable
    abstract String instance();

    @Override
    public String name() {
      return baseName() + (instance() == null ? "" : "@" + instance()) + "." + kind();
    }

    @Override
    boolean isTemplate() {
      return false;
    }

    @Override
    public UnitInfo fetchInfo() throws IOException, InterruptedException {
      String name = name();
      // TODO: remove inheritIO
      Process process = new ProcessBuilder()
          .command("fleetctl", "list-unit-files", "-full", "-no-legend")
          .redirectError(INHERIT)
          .start();
      int exitCode = process.waitFor();
      if (exitCode == 0) {
        try (BufferedReader reader =
                 new BufferedReader(
                     new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
          Optional<List<String>> maybeStatus = reader.lines()
              .map(STATUS_SPLITTER::splitToList)
              .filter(fs -> name.equals(fs.get(0)))
              .findAny();

          if (maybeStatus.isPresent()) {
            List<String> status = maybeStatus.get();
            return UnitInfo.details(
                name,
                State.forValue(status.get(2)),
                State.forValue(status.get(3)),
                "-".equals(status.get(4)) ? null : status.get(4),
                definition().getOptions());
          } else {
            return UnitInfo
                .details(name, State.INACTIVE, State.INACTIVE, null, definition().getOptions());
          }
        }
      } else {
        throw new RuntimeException("Could not list unit files, exit code " + exitCode);
      }
    }

    @Override
    public String fetchStatus() throws IOException, InterruptedException {
      // TODO: remove inheritIO
      Process process = new ProcessBuilder()
          .command("fleetctl", "status", name())
          .redirectError(INHERIT)
          .start();
      if (process.waitFor() != 0) {
        throw new RuntimeException("Could not fetch status for " + name());
      }
      return CharStreams.toString(new InputStreamReader(process.getInputStream()));
    }

    @Override
    public ImmutableList<String> fetchJournal(int numBack)
        throws IOException, InterruptedException {
      // TODO: remove inheritIO
      Process process = new ProcessBuilder()
          .command("fleetctl", "journal", "-lines", Integer.toString(numBack), name())
          .redirectError(INHERIT)
          .start();
      if (process.waitFor() != 0) {
        throw new RuntimeException("Could not fetch status for " + name());
      }

      Iterable<String> lines =
          Splitter.on('\n')
              .split(CharStreams.toString(new InputStreamReader(process.getInputStream())));

      if (Iterables.isEmpty(lines)) {
        return ImmutableList.of();
      } else {
        return ImmutableList.copyOf(Iterables.skip(lines, 1));
      }
    }

    @Override
    public Stream<String> streamJournal() throws IOException, InterruptedException {
      // TODO: remove inheritIO
      Process process = new ProcessBuilder()
          .command("fleetctl", "journal", "-lines", "0", "-f", name())
          .redirectError(INHERIT)
          .start();

      Reader reader =
          new ProcessStoppingReader(new InputStreamReader(process.getInputStream()), process);

      return new BufferedReader(reader).lines().skip(1);
    }

    @Override
    public Stream<String> streamJournal(int numBack)
        throws IOException, InterruptedException {
      // TODO: remove inheritIO
      Process process = new ProcessBuilder()
          .command("fleetctl", "journal", "-lines", Integer.toString(numBack), "-f", name())
          .redirectError(INHERIT)
          .start();

      Reader reader =
          new ProcessStoppingReader(new InputStreamReader(process.getInputStream()), process);

      return new BufferedReader(reader).lines().skip(1);
    }

    @Override
    public void start() throws IOException, InterruptedException {
      // TODO: remove inheritIO
      if (new ProcessBuilder().command("fleetctl", "start", name()).inheritIO().start().waitFor()
          != 0) {
        throw new RuntimeException("Could not start unit");
      }
    }

    @Override
    public void stop() throws IOException, InterruptedException {
      // TODO: remove inheritIO
      if (new ProcessBuilder().command("fleetctl", "stop", name()).inheritIO().start()
              .waitFor() != 0) {
        throw new RuntimeException("Could not stop unit");
      }
    }

    @Override
    public void load() throws IOException, InterruptedException {
      // TODO: remove inheritIO
      if (new ProcessBuilder().command("fleetctl", "load", name()).inheritIO().start()
              .waitFor() != 0) {
        throw new RuntimeException("Could not load unit");
      }
    }

    @Override
    public void unload() throws IOException, InterruptedException {
      // TODO: remove inheritIO
      if (new ProcessBuilder().command("fleetctl", "unload", name()).inheritIO().start()
              .waitFor()
          != 0) {
        throw new RuntimeException("Could not unload unit");
      }
    }

    @Override
    public void startAsync() throws IOException, InterruptedException {
      // TODO: remove inheritIO
      if (new ProcessBuilder().command("fleetctl", "start", "-no-block", name()).inheritIO().start()
              .waitFor()
          != 0) {
        throw new RuntimeException("Could not start unit");
      }
    }

    @Override
    public void stopAsync() throws IOException, InterruptedException {
      // TODO: remove inheritIO
      if (new ProcessBuilder().command("fleetctl", "stop", "-no-block", name()).inheritIO().start()
              .waitFor() != 0) {
        throw new RuntimeException("Could not stop unit");
      }
    }

    @Override
    public void loadAsync() throws IOException, InterruptedException {
      // TODO: remove inheritIO
      if (new ProcessBuilder().command("fleetctl", "load", "-no-block", name()).inheritIO().start()
              .waitFor() != 0) {
        throw new RuntimeException("Could not load unit");
      }
    }

    @Override
    public void unloadAsync() throws IOException, InterruptedException {
      // TODO: remove inheritIO
      if (new ProcessBuilder().command("fleetctl", "unload", "-no-block", name()).inheritIO()
              .start()
              .waitFor()
          != 0) {
        throw new RuntimeException("Could not unload unit");
      }
    }
  }

  @AutoValue
  static abstract class TemplateImpl extends CmdUnit implements Template {

    private Set<String> instances = Sets.newHashSet();

    @Override
    public synchronized Singleton instance(String instance) {
      instances.add(instance);
      return new AutoValue_CmdUnit_SingletonImpl(definition(), baseName(), kind(), instance);
    }

    @Override
    public String name() {
      return baseName() + "@." + kind();
    }

    @Override
    boolean isTemplate() {
      return true;
    }

    @Override
    public void close() throws IOException {
      for (String i : instances) {
        instance(i).close();
      }

      super.close();
    }
  }

  @AutoValue
  static abstract class BuilderImpl implements Builder {

    abstract UnitDefinition unitDefinition();

    abstract String baseName();

    abstract String kind();

    static BuilderImpl create(UnitDefinition unitDefinition, String baseName, String kind) {
      return new AutoValue_CmdUnit_BuilderImpl(unitDefinition, baseName, kind);
    }

    @Override
    public SingletonImpl submitSingleton() throws IOException, InterruptedException {
      submitUnit(false);
      return new AutoValue_CmdUnit_SingletonImpl(unitDefinition(), baseName(), kind(), null);
    }

    @Override
    public Template submitTemplate() throws IOException, InterruptedException {
      submitUnit(true);
      return new AutoValue_CmdUnit_TemplateImpl(unitDefinition(), baseName(), kind());
    }

    private void submitUnit(boolean isTemplate)
        throws IOException, InterruptedException {
      String name = baseName() + (isTemplate ? "@." : ".") + kind();
      Path dir = Files.createTempDirectory("magellan-unit-");
      try {
        Path file = dir.resolve(name);
        try {
          Files.write(file, ImmutableSet.of(unitDefinition().toSystemdFormat()),
                      StandardCharsets.UTF_8);
          Process process = new ProcessBuilder()
              .command("fleetctl", "submit", name)
              .inheritIO() // TODO: remove
              .directory(dir.toFile())
              .start();
          int exitCode = process.waitFor();

          if (exitCode != 0) {
            throw new RuntimeException("Could not submit unit files, exit code " + exitCode);
          }
        } finally {
          Files.deleteIfExists(file);
        }
      } finally {
        Files.delete(dir);
      }
    }
  }

  class ProcessStoppingReader extends FilterReader {

    private final Process process;

    public ProcessStoppingReader(Reader in, Process process) {
      super(in);
      this.process = process;
    }

    @Override
    public void close() throws IOException {
      super.close();
      process.destroyForcibly();
    }
  }
}
