package lexer;

import java.util.ArrayList;
import java.util.List;

import lexer.tokens.*;

public class Lexer {
    int p = 0; // position
    private String input;
    String fileName;

    public Lexer(String input) {
        this.input = input;
    }

    public List<Token> tokenize() {
       char c = input.charAt(p);
       List<Token> tokens = new ArrayList<>();

       while(p != input.length()) {
           TokenType type = TokenType.fromChar(c);
           if (type != null) {
               tokens.add(new Token(type, null, null));
               continue;
           }

           if(c == '"') {
               tokens.add(stringLiteral());
           } else if (isNumber()) {
               tokens.add(integer());
           } else if (isLetter()) {
               tokens.add(identifier());
           }
       }

        return tokens;
   }

   private boolean isNumber() {
        return "0123456789".indexOf(input.charAt(p)) >= 0;
    }

   private boolean isLetter() {
        return "abc".indexOf(input.charAt(p)) >= 0;
   }

   private Token stringLiteral() {
        char c = input.charAt(p);
        while(c != )
        return null;
   }

   private Token identifier() {
     return null;
   }

   private Token integer() {
        return null;
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