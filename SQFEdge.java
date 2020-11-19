package org.myrskynkantaja.harakka.sqfcallgraph;

import java.nio.file.Path;

/**
 * Created by: harakka
 * Date and time: 9.11.2012, 12:12
 */
public class SQFEdge {
    public final Path start, end;
    public final EdgeType type;

    public SQFEdge (Path start, Path end, EdgeType type) {
        this.start = start;
        this.end = end;
        this.type = type;
    }

    public SQFEdge (Path relativeTo, Path start, Path end, EdgeType type) {
        this.start = relativeTo.relativize(start);
        this.end = relativeTo.relativize(end);
        this.type = type;
    }


    public String csv() {
        return new String(start + "," + end + "," + type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SQFEdge sqfEdge = (SQFEdge) o;

        if (!end.equals(sqfEdge.end)) return false;
        if (!start.equals(sqfEdge.start)) return false;
        if (type != sqfEdge.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = start.hashCode();
        result = 31 * result + end.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
