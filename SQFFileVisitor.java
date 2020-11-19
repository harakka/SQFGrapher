package org.myrskynkantaja.harakka.sqfcallgraph;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

// TODO: create separate visitors for mission.sqm, description.ext and .h files

/**
 * FileVisitor for traversing a directory containing sqf script files and creating a Set of SQFEdges depicting the
 * call graph edges from the traversed data.
 * Created by: harakka
 * Date: 9.11.2012, 1:36
 */
public class SQFFileVisitor extends SimpleFileVisitor<Path> {
    private Set<SQFEdge> edgeList = new HashSet<>();    // Collection for storing the generated edge list
    private final PathMatcher sqfMatcher = FileSystems.getDefault().getPathMatcher("glob:*.{sqf,sqs,sqm,ext,hpp,h}");   // Glob pattern for sqf-relevant files
    private final Path workingDir;  // The directory we are reading the scripts from

    public SQFFileVisitor(Path workingDir) {
        this.workingDir = workingDir;
        System.out.println("SQFVisitor initialized with path " + workingDir);
    }

    /**
     * Returns the generated edge list as a Collection of SQFEdges.
     * Usage note: you need to call Files.walkFileTree on the SQFFileVisitor object and wait for the call to finish
     * before calling this method.
     * @return
     */
    public Collection<SQFEdge> getResults() {
        return edgeList;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (sqfMatcher.matches(file.getFileName())) {
            for (SQFEdge edge: fileConnections(file)) {
                edgeList.add(edge);
            }
        }
        return super.visitFile(file, attrs);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Finds connections from this file to other files by searching for SQF script commands like execVM and #include.
     * @param file search target
     * @return SQFEdges representing the directed edges from target file to other files.
     * @throws IOException
     */
    private Set<SQFEdge> fileConnections(Path file) throws IOException {
        //TODO: AddAction. Aargh, an entirely new format to parse

        Set<SQFEdge> connections = new HashSet<>();
        Charset charset = Charset.forName("UTF-8");
        try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                for (String s: line.split(";")) {
                    String result;
                    if ((result = parseFilenameFromCommand("execvm", s)) != null) {
                        connections.add(new SQFEdge(workingDir, file, Paths.get(workingDir.toString(), result).toRealPath(), EdgeType.execVM));
                    } else if ((result = parseFilenameFromCommand("#include", s)) != null) {
                        connections.add(new SQFEdge(workingDir, file, file.resolveSibling(result).toRealPath(), EdgeType.include));
                    } else if ((result = parseFilenameFromCommand("addaction", s)) != null) {
                        connections.add(new SQFEdge(workingDir, file, Paths.get(workingDir.toString(), result).toRealPath(), EdgeType.addAction));
                    } else if ((result = parseFilenameFromCommand("compile preprocessfile", s)) != null) {
                        connections.add(new SQFEdge(workingDir, file, Paths.get(workingDir.toString(), result).toRealPath(), EdgeType.callCompilePreprocessFile));
                    } else if ((result = parseFilenameFromCommand("compile preprocessfilelinenumbers", s)) != null) {
                        connections.add(new SQFEdge(workingDir, file, Paths.get(workingDir.toString(), result).toRealPath(), EdgeType.callCompilePreprocessFile));
                    }
                }
            }
        } catch (IOException x) {
            System.err.format("IOException in " + workingDir.relativize(file) + " while parsing: %s%n", x);
        }
        return connections;
    }

    /**
     * Return the file targeted by the command given as parameter.
     * Accepted format: ..... COMMAND "targetfile" .....
     * Acceptable quotes are ", "" or '.
     * @param command Command to search for in the line
     * @param line target string
     * @return file targeted by command
     */
    private String parseFilenameFromCommand(String command, String line) {
        // TODO: we already have the EdgeType enum, could be turned to CallType and have that passed as param here
        // We have to handle AddAction specifically because it doesn't parse with the default mechanism
        if (!line.trim().startsWith("//") && command.toLowerCase().equals("addaction") && line.toLowerCase().contains(command)) {
            String[] substrings = line.split("(?i)(" + command + ")");
            return parseQuotedString(substrings[1].split(",")[1]);
        }
        else if (!line.trim().startsWith("//") && line.toLowerCase().contains(command)) {
            String[] substrings = line.split("(?i)(" + command + ")");
            return parseQuotedString(substrings[1]);
        }
        return null;
    }

    /**
     * Parses first quoted string from target string. Acceptable quotes are ", "" and '.
     * @param line string to remove quotes from
     * @return first quoted string without the quotes
     */
    private String parseQuotedString(String line) {
        int start, end;
        if (line.trim().startsWith("\"\"")) {       //Double double quoted
            start = line.indexOf("\"\"") + 2;
            end = line.indexOf("\"", start);
        } else if (line.trim().startsWith("\"")) {  // Double quoted
            start = line.indexOf("\"") + 1;
            end = line.indexOf("\"", start);
        } else if (line.trim().startsWith("\'")) {  // Single quoted
            start = line.indexOf("\'") + 1;
            end = line.indexOf("\'", start);
        } else {
            // We end up here if there are no quotes to parse
            return null;
        }
        return line.substring(start, end);
    }
}
