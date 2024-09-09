package com.lavendersnek.shimejiconfutil.cli.bases;

import picocli.CommandLine.Option;

import java.nio.file.Path;

public class ActionsInOut {

    @Option(names = {"-a", "--actions"}, description = "Input path for the actions file.", required = true)
    private Path inPath;

    @Option(names = {"-oA", "--output-actions"}, description = "Output path for the modified action file.", required = true)
    private Path outPath;

    public ActionsInOut() {}

    public ActionsInOut(Path inPath, Path outPath) {
        this.inPath = inPath;
        this.outPath = outPath;
    }

    public Path getInPath() {
        return inPath;
    }

    public Path getOutPath() {
        return outPath;
    }

}
