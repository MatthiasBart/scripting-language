package lexer;

import java.util.ArrayList;
import java.util.List;

import lexer.tokens.Token;
import lexer.tokens.TokenType;
import lexer.tokens.Position;

public class Lexer {
    int p = 0; // position
    private String input;
    String fileName;

    int line = 1;
    int charsInLinesAbove = 0;

    public Lexer(String input, String fileName) {
        this.input = input;
        this.fileName = fileName;
    }

    public List<Token> tokenize() {
       List<Token> tokens = new ArrayList<>();

       while(p < input.length()) {
           char c = input.charAt(p);

           if (Character.isWhitespace(c)) {
               if (c == '\n') {
                   line++;
                   charsInLinesAbove = p + 1;
               }
               p++;
               continue;
           }

           TokenType type = TokenType.fromChar(c);
           if (type != null) {
               tokens.add(new Token(type, null, position()));
               p++;
               continue;
           }

           if(c == '"') {
               tokens.add(stringLiteral());
               p++;
           } else if (c == '-') {
               if (input.charAt(p + 1) == '>') {
                   tokens.add(out());
               } else {
                   tokens.add(integer());
               }
           } else if (isDigit()) {
               tokens.add(integer());
           } else if (isLetter()) {
               tokens.add(identifier());
           } else {
               throw new LexerException("Unexpected character", position());
           }
       }

        return tokens;
   }

   private Token out() {
        p++;
        p++;
        return new Token(TokenType.OUT, null, position());
   }

   private boolean isDigit() {
        return Character.isDigit(input.charAt(p));
    }

   private boolean isLetter() {
        return Character.isLetter(input.charAt(p));
   }

    private Token stringLiteral() {
        Position pos = position();
        p++; // skip opening "
        String value = "";
        while (p < input.length() && input.charAt(p) != '"') {
            value += input.charAt(p);
            p++;
        }
        if (p >= input.length()) throw new LexerException("Unterminated string", position());
        return new Token(TokenType.STRING, value, pos);
    }

    private Token identifier() {
        Position pos = position();
        String value = "";
        while (p < input.length() && (Character.isLetterOrDigit(input.charAt(p)) || input.charAt(p) == '_')) {
            value += input.charAt(p);
            p++;
        }
        return new Token(TokenType.IDENTIFIER, value, pos);
    }

    private Token integer() {
        Position pos = position();
        String value = String.valueOf(input.charAt(p));
        p++;
        while (p < input.length() && Character.isDigit(input.charAt(p))) {
            value += input.charAt(p);
            p++;
        }
        return new Token(TokenType.INT, value, pos);
    }

    private Position position() {
        return new Position(fileName, line, p - charsInLinesAbove + 1);
    }

   public static class LexerException extends RuntimeException {
       public LexerException(String message, Position position) {
           super(
                   message
                   + " in " + position.fileName()
                   +  " at " + position.line()
                   + ":" + position.column()
           );
       }
   }
}