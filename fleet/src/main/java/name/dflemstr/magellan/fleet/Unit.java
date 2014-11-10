package name.dflemstr.magellan.fleet;

import com.google.common.collect.ImmutableList;

import java.io.Closeable;
import java.io.IOException;
import java.util.stream.Stream;

import name.dflemstr.magellan.fleet.model.UnitInfo;

/**
 * An unit that exists within a fleet.
 */
public interface Unit extends Closeable {

  /**
   * Returns the name of the unit.  In case of a template, this is the full template name, like
   * {@code "foobar@.service"}; otherwise it's the normal unit name, like {@code "foobar.service"}.
   *
   * @return the name of the unit.
   */
  String name();

  /**
   * Returns the unit definition that the unit was created from.
   *
   * @return the unit definition that the unit was created from.
   */
  UnitDefinition definition();

  public interface Singleton extends Unit {


    /**
     * Retrieves some information about the unit's current state within the fleet.
     *
     * @return run-time information about the unit.
     * @throws IOException          if the information could not be fetched.
     * @throws InterruptedException if interrupted while fetching the information.
     */
    UnitInfo fetchInfo() throws IOException, InterruptedException;

    /**
     * Retrieves human-readable status information about the unit, that can be rendered on a
     * terminal.
     *
     * @return human-readable status.
     * @throws IOException          if the status could not be fetched.
     * @throws InterruptedException if interrupted while fetching the status.
     */
    String fetchStatus() throws IOException, InterruptedException;

    /**
     * Retrieves log lines from the journal of this unit.
     *
     * @param numBack The number of lines from the past to return. This is a maximum; the call will
     *                not block until the number of lines are available.
     * @return The requested journal lines.
     * @throws IOException          if the journal could not be fetched.
     * @throws InterruptedException if interrupted while fetching the journal.
     */
    ImmutableList<String> fetchJournal(int numBack) throws IOException, InterruptedException;

    Stream<String> streamJournal() throws IOException, InterruptedException;

    Stream<String> streamJournal(int numBack) throws IOException, InterruptedException;

    void start() throws IOException, InterruptedException;

    void stop() throws IOException, InterruptedException;

    void load() throws IOException, InterruptedException;

    void unload() throws IOException, InterruptedException;

  }

  public interface Template extends Unit {

    Singleton instance(String instance);
  }

  public interface Builder {

    Singleton submitSingleton() throws IOException, InterruptedException;

    Template submitTemplate() throws IOException, InterruptedException;

  }
}
