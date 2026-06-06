package parser;

import java.util.List;

public class Procedure implements Node {
    private Identifier identifier;

    private List<Identifier> valueParameters;

    private List<Identifier> refParameters;

    private Body body;

    public Procedure(Identifier identifier, List<Identifier> valueParameters, List<Identifier> refParameters, Body body) {
        this.identifier = identifier;
        this.valueParameters = valueParameters;
        this.refParameters = refParameters;
        this.body = body;
    }
}
