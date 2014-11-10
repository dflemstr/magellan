package name.dflemstr.magellan.fleet;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

public class Example {

  public static void main(String[] args) throws IOException, InterruptedException {

    try (Fleet fleet = Fleet.usingFleetCtl()) {
      UnitDefinition testDefinition = UnitDefinition.builder()
          .section("Unit")
          .entry("Description", "Test")
          .section("Service")
          .entry("ExecStart", "/usr/bin/sh -c 'while true; do echo hi; sleep 1; done'")
          .build();

      ExecutorService service = Executors.newSingleThreadExecutor();

      try (Unit.Singleton test = fleet.service("test", testDefinition).submitSingleton()) {
        System.out.println(test.fetchInfo());
        test.load();

        Future<Void> logJob = service.submit(() -> {
          try (Stream<String> journal = test.streamJournal()) {
            journal.forEach(System.out::println);
          }
          return null;
        });

        System.out.println(test.fetchInfo());
        System.out.print(test.fetchStatus());
        test.start();
        for (int i = 0; i < 3; i++) {
          System.out.println(test.fetchInfo());
          System.out.print(test.fetchStatus());
          Thread.sleep(1000);
        }
        test.stop();
        System.out.println(test.fetchInfo());
        System.out.print(test.fetchStatus());
        logJob.cancel(true);
        test.unload();
        System.out.println(test.fetchInfo());
      } finally {
        service.shutdownNow();
      }
    }
  }
}
