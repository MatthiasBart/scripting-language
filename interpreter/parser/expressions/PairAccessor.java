package parser.expressions;

import lexer.tokens.Token;
import lexer.tokens.TokenType;
import parser.ParsingException;

public record PairAccessor(Identifier identifier, Field field) implements Expression {
  public PairAccessor(Identifier identifier, Token fieldToken) {
    if (fieldToken.type() != TokenType.INT) {
      throw new ParsingException("Expected integer for pair accessor", fieldToken);
    }

    int value = Integer.parseInt(fieldToken.value());
    this(identifier, value == 0 ? Field.LEFT : Field.RIGHT);
  }

  public enum Field {
    LEFT,
    RIGHT
  }
}
