package ru.nsu.fit.boltava.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class ConsoleReader implements Reader {

    @Override
    public String read(String message) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(message + ": ");
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
