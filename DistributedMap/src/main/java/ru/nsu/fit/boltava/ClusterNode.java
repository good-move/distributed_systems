package ru.nsu.fit.boltava;

import java.util.Map;
import java.util.Optional;

import org.jgroups.JChannel;
import ru.nsu.fit.boltava.io.Printer;
import ru.nsu.fit.boltava.io.Reader;

public class ClusterNode {

    private static final String CLUSTER_NAME = "distributed-map";
    private static final String USAGE =
            "\nAvailable commands:" +
            "list -- Get all entries\n" +
            "get -- Get value\n" +
            "set -- Set value\n" +
            "swap -- Compare and swap\n" +
            "remove -- Remove value\n" +
            "quit -- Quit\n";

    private final Reader reader;
    private final Printer printer;
    private final DistributedMap map;
    private JChannel channel;

    private final Map<String, Runnable> handlers = Map.of(
            "list", this::list,
            "get", this::get,
            "set", this::set,
            "swap", this::swap,
            "remove", this::remove,
            "quit", this::quit
    );

    public ClusterNode(String pathToXmlConfigFile, Reader reader, Printer printer) throws Exception {
        channel = new JChannel(pathToXmlConfigFile);
        channel.connect(CLUSTER_NAME);
        map = new DistributedMap(channel);
        this.printer = printer;
        this.reader = reader;
    }

    public void run() {
        while (true) {
            Optional.ofNullable(handlers.get(reader.read(USAGE).trim()))
                    .ifPresentOrElse(Runnable::run, () -> printer.print("-- Action is not supported --"));
        }
    }

    private void get() {
        String key = reader.read("Entry key");
        map.get(key)
            .ifPresentOrElse(
                value -> printer.print(key + " is " + value),
                () -> printer.print("no value for the key=" + key)
            );
    }

    private void set() {
        String key = reader.read("Entry key");
        double value = Double.parseDouble(reader.read("Value"));
        boolean result = map.put(key, value);
        var message = String.format("%s\n", result ? "Entry set" : "Entry was not set");
        printer.print(message);
    }

    private void swap() {
        String key = reader.read("Entry key");
        double oldValue = Double.parseDouble(reader.read("Old value"));
        double newValue = Double.parseDouble(reader.read("New value"));
        boolean result = map.compareAndSwap(key, oldValue, newValue);
        var message = String.format("%s\n", result
                ? "Entry updated"
                : "Entry was not updated, because old value does not equal to current value");
        printer.print(message);
    }

    private void remove() {
        var key = reader.read("Entry key");
        var result = map.remove(key);
        var message = String.format("%s\n", result ? "Entry removed" : "No entry with such key found");
        printer.print(message);
    }

    private void list() {
        printer.print("map:");
        synchronized (map) {
            for (Map.Entry<String, Double> entry : map.entrySet()) {
                printer.print(entry.getKey() + ": " + entry.getValue());
            }
        }
    }

    private void quit() {
        channel.close();
        System.exit(0);
    }


}
