package ru.gameTheory;

import org.apache.log4j.BasicConfigurator;
import ru.gameTheory.server.Server;

import java.util.concurrent.*;
import java.util.stream.Stream;

public class Main {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        BasicConfigurator.configure();
        Server.start();
    }
}
