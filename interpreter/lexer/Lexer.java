package lexer;

import java.util.ArrayList;
import java.util.List;

import lexer.tokens.*;

public class Lexer {
   static public List<Token> tokenize(String input) {
       List<Token> tokens = new ArrayList<>();

       int start = -1;
       int end = -1;
       TokenType ongoing = null;

       for (int i = 0; i < input.length(); i++) {
           char c = input.charAt(i);
            // " start/end string
           // 0-9 start/end int
           // a-z start/end identifier
           if (start != -1) {
               switch (ongoing) {
                   case TokenType.STRING:
                       break;
                   case TokenType.INT:
                       break;
                   case TokenType.IDENTIFIER:
                       break;
                   default:
                       throw new Error();
               }
           }
            if(c == '"') {

            } else if ("0123456789".indexOf(c) >= 0) {

            } else if ("abc".indexOf(c) >= 0) {

            }
           if (String.valueOf(c).equals("\"")) {
               if (start == -1) {
                   start = i;
               } else {
                   end = i;

                   String value = input.substring(start, end);

                   tokens.add(new Token(TokenType.STRING, value, null));

                   start = -1;
                   end = -1;
               }
           } else if (start != -1) {
               continue;
           }

           TokenType type = TokenType.fromChar(c);
           if (type != null) {
               tokens.add(new Token(type));
               continue;
           }
       }

        return tokens;
   }

   public class LexerException extends RuntimeException {
       
   }
}