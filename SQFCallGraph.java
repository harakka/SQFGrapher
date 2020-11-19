package org.myrskynkantaja.harakka.sqfcallgraph;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;

/**
 * Main class for SQFCallGraph. Takes two command line parameters, a directory containing scripts to analyze, and a
 * file path for writing the graph's edge list to.
 * Created by: harakka
 * Date and time: 9.11.2012, 1:00
 */
public class SQFCallGraph {
    public static void main(String[] args) {
        // Check if parameter count is correct
        if (args.length != 2) {
            System.out.println("USAGE: SQFCallGraph \"targetdir\" \"csvdestfile\"\n" +
                    "Where \"targetdir\" is the directory containing the script files you want to" +
                    "generate the call graph for, and \"csvdestfile\" is a path to a file that you" +
                    "want to write the .csv data to. The file will be erased if it already exists.");
            System.exit(1);
        }

        // Check if the first parameter is a valid directory
        Path sqfDirectory = Paths.get(args[0]);
        if (!Files.isDirectory(sqfDirectory)) {
            System.out.println("ERROR: first parameter must be an existing directory.");
            System.exit(1);
        }

        // Check that the second parameter isn't a directory.
        // TODO: add actual file name validation. The proper way to do this would require an atomic operation for old
        // file removal, name validity check and new file creation.
        Path csvFile = Paths.get(args[1]);
        if (Files.isDirectory(csvFile)) {
            System.out.println("ERROR: second parameter must be a valid file path, not a directory.");
            System.exit(1);
        }

        System.out.println("Generating execution graph for files in " + sqfDirectory);
        SQFFileVisitor visitor = new SQFFileVisitor(sqfDirectory);
        Charset charset = Charset.forName("UTF-8");

        try {
            BufferedWriter writer = Files.newBufferedWriter(csvFile, charset);
            Files.walkFileTree(sqfDirectory, visitor);
            for (SQFEdge e: visitor.getResults()) {
                writer.write(e.csv(), 0, e.csv().length());
                writer.newLine();
            }
            writer.close();
            System.out.println("Execution graph written to " + csvFile);
        } catch (IOException e) {
            System.err.println("(E) IO exception: " + e);
        }
    }
}