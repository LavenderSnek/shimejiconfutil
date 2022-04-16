package com.snek.shimejiconfutil.cli;

import picocli.CommandLine;

import picocli.CommandLine.Command;

@Command(name = "shimejiconf", mixinStandardHelpOptions = true, description = "Various utilities for editing shimeji config files")
public class ConfMain {

    public static void main(String... args) {
        //noinspection InstantiationOfUtilityClass
        CommandLine commandLine = new CommandLine(new ConfMain())
                .addSubcommand("tr", new ConfTr())
                .addSubcommand("res", new ConfResources());

        commandLine.execute(args);
    }

}
