package main;

import main.com.twitchcm.InputArgsProcessor;

public class Main {
    public static void main(String[] args) {

        InputArgsProcessor argsProcessor = new InputArgsProcessor(args);

        argsProcessor.run();
    }
}
