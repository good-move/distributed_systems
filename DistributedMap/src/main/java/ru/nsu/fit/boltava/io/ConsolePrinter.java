package ru.nsu.fit.boltava.io;

public class ConsolePrinter implements Printer {

    @Override
    public void print(String message) {
        System.out.println(message);
    }

}
