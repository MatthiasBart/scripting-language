package lexer;

import java.util.ArrayList;
import java.util.List;

import lexer.tokens.Token;
import lexer.tokens.TokenType;

public class Lexer {
    int p = 0; // position
    private String input;
    String fileName;

    public Lexer(String input, String fileName) {
        this.input = input;
        this.fileName = fileName;
    }

    public List<Token> tokenize() {
       List<Token> tokens = new ArrayList<>();

       while(p < input.length()) {
           char c = input.charAt(p);

           if (Character.isWhitespace(c)) {
               p++;
               continue;
           }

           TokenType type = TokenType.fromChar(c);
           if (type != null) {
               tokens.add(new Token(type, null, null));
               p++;
               continue;
           }

           if(c == '"') {
               p++;
               tokens.add(stringLiteral());
               p++;
           } else if (isDigit()) {
               tokens.add(integer());
           } else if (isLetter()) {
               tokens.add(identifier());
           } else {
               throw new LexerException("Unexpected character", 0, 0);
           }
       }

        return tokens;
   }

   private boolean isDigit() {
        return Character.isDigit(input.charAt(p));
    }

   private boolean isLetter() {
        return Character.isLetter(input.charAt(p));
   }

    private Token stringLiteral() {
        String value = "";
        while (p < input.length() && input.charAt(p) != '"') {
            value += input.charAt(p);
            p++;
        }
        if (p >= input.length()) throw new LexerException("Unterminated string", 0, 0);
        return new Token(TokenType.STRING, value, null);
    }

    private Token identifier() {
        String value = "";
        while (p < input.length() && (Character.isLetterOrDigit(input.charAt(p)) || input.charAt(p) == '_')) {
            value += input.charAt(p);
            p++;
        }
        return new Token(TokenType.IDENTIFIER, value, null);
    }

    private Token integer() {
        String value = "";
        while (p < input.length() && Character.isDigit(input.charAt(p))) {
            value += input.charAt(p);
            p++;
        }
        return new Token(TokenType.INT, value, null);
    }

   public static class LexerException extends RuntimeException {
       private final int line;
       private final int column;

       public LexerException(String message, int line, int column) {
           super(message + " at " + line + ":" + column);
           this.line = line;
           this.column = column;
       }
   }
}