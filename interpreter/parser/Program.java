package parser;

import java.util.List;

public record Program(List<Procedure> procedures, Body body) implements Node {
}