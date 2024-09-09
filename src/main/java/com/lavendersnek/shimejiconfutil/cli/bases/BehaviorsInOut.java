package com.lavendersnek.shimejiconfutil.cli.bases;

import picocli.CommandLine;

import java.nio.file.Path;

public class BehaviorsInOut {

    @CommandLine.Option(names = {"-b", "--behavior", "--behaviour"}, description = "Input path for the behaviour file.", required = true)
    private Path inPath;

    @CommandLine.Option(names = {"-oB", "--output-behavior", "--output-behaviour"}, description = "Output path for the modified behavior file.", required = true)
    private Path outPath;

    public Path getInPath() {
        return inPath;
    }

    public Path getOutPath() {
        return outPath;
    }

}
