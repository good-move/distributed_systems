package ru.nsu.fit.boltava;

import ru.nsu.fit.boltava.io.ConsolePrinter;
import ru.nsu.fit.boltava.io.ConsoleReader;

public class Main {

    private static final String CLUSTER_CONFIG_PATH = "src/main/resources/jgroups-config.xml";


    public static void main(String[] args) throws Exception {
        var reader = new ConsoleReader();
        var printer = new ConsolePrinter();
        new ClusterNode(CLUSTER_CONFIG_PATH, reader, printer).run();
    }

}
