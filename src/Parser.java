import java.awt.Graphics2D;
import java.awt.FontMetrics;

// This class is used to parse the line given and tokenize it for the syntax highlights to be made

public class Parser {
    private static int i;
    private static int Start;
    private static int top_y;

    private static boolean comment = false;
    private static boolean string_or_char = false;
    private static boolean function = false;

    private static StringBuilder sb = new StringBuilder();
    private static String contents;

    private static Graphics2D g2D;
    private static FontMetrics fm;

    public static void parse(String cont, int y, Graphics2D g, FontMetrics f) {
        int len = cont.length();
        contents = cont;
        i = 0;
        Start = 0;
        top_y = y;
        g2D = g;
        fm = f;

        // Loop to parse through the lines chars and tokenize them
        for (; i < len; i++) {
            char c = contents.charAt(i);

            if (is_comment(c)) { // Checking if encounterting comments
                comment = true;
                sb.append(c);

                if (i == len - 1) { // This is to deal with '#'
                    tokenize(sb.toString());
                    Start = i + 1;
                    sb.setLength(0);
                }
            }
            else if (is_punctuation(c) && comment == false) {
                if (c == '(') { function = true; }

                tokenize(sb.toString());
                Start = i;
                sb.setLength(0);

                sb.append(c);
                tokenize(sb.toString());
                Start = i + 1;
                sb.setLength(0);
            }
            else if (i == len - 1) {
                if (is_punctuation(c)) { // This is mainly to deal with statements ending in ';'
                    if (c == '(') { function = true; }
    
                    tokenize(sb.toString());
                    Start = i;
                    sb.setLength(0);

                    sb.append(c);
                    tokenize(sb.toString());
                    Start = i + 1;
                    sb.setLength(0);
                }
                else {
                    sb.append(c);
                    tokenize(sb.toString());
                    Start = i;
                    sb.setLength(0);
                }
            }
            else {
                sb.append(c);
            }
        }

        comment = false;
        string_or_char = false;
    }

    // Checks what the next char is
    private static char peek(String contents) {
        if ((i + 1) >= contents.length()) {
            return '\u0000';
        }

        return contents.charAt(i + 1);
    }

    private static boolean is_punctuation(char c) {
        return c == ' ' || c == ',' || c == '(' || c == ')' || c == '[' || c == ']' || c == ';' || c == '.' || c == '{' || c == '}' || c == '*' || c == '+' || c == '-' || c == '/';
    }

    private static boolean is_comment(char c) {
        return (c == '/' && peek(contents) == '/') || c == '#';
    }

    private static void tokenize(String word) {
        if (comment) {
            Token t = new Token(TokenType.COMMENT, Start, contents, fm, word, top_y);
            t.draw_tokens(g2D);
            return;
        }

        if (string_or_char) {
            getString(word);
            return;
        }

        switch (word) {
            // KEYWORDS
            case "if":
            case "else":
            case "for":
            case "while":
            case "fun":
            case "fn":
            case "pub":
            case "public":
            case "private":
            case "static":
            case "struct":
            case "typedef":
            case "mod":
            case "import":
            case "const":
            case "loop":
            case "do":
            case "let":
            case "match":
            case "in":
            case "break":
            case "return":
            case "continue":
            case "impl":
            case "mut":
            case "free":
            case "use":
            case "switch":
            case "case":
                Token t1 = new Token(TokenType.KEYWORD, Start, contents, fm, word, top_y);
                t1.draw_tokens(g2D);
                break;

            // CLASSES
            case "class":
            case "implements":
            case "interface":
            case "inherits":
            case "new":
            case "object":
            case "enum":
            case "final":
            case "super":
            case "Self":
            case "self":
            case "type":
            case "this":
            case "extends":
                Token t2 = new Token(TokenType.CLASS, Start, contents, fm, word, top_y);
                t2.draw_tokens(g2D);
                break;

            // TYPES
            case "int":
            case "float":
            case "String":
            case "char":
            case "bool":
            case "boolean":
            case "Bool":
            case "double":
            case "void":
            case "i8":
            case "u8":
            case "i16":
            case "u16":
            case "i32":
            case "u32":
            case "i64":
            case "u64":
            case "usize":
            case "f32":
            case "f64":
            case "short":
            case "long":
                Token t3 = new Token(TokenType.TYPE, Start, contents, fm, word, top_y);
                t3.draw_tokens(g2D);
                break;
            
            // ARROWS
            case "->":
            case "<-":
                Token t4 = new Token(TokenType.ARROW, Start, contents, fm, word, top_y);
                t4.draw_tokens(g2D);
                break;

            // LITERALS
            case "null":
            case "NULL":
            case "nil":
            case "true":
            case "false":
            case "None":
            case "Some":
                Token t6 = new Token(TokenType.LITERAL, Start, contents, fm, word, top_y);
                t6.draw_tokens(g2D);
                break;
            
            // OTHER
            default:
                if (word.length() == 0) {
                    break;
                }

                if (word.charAt(0) == '"') {
                    string_or_char = true;
                    getString(word);
                }
                else if (word.charAt(0) == '\'') {
                    string_or_char = true;
                    getChar(word);
                }
                else if (isDigit(word.charAt(0))) {
                    getNumber(word);
                }
                else if (function) {
                    Token t5 = new Token(TokenType.FUNCTION, Start, contents, fm, word, top_y);
                    t5.draw_tokens(g2D);
                }
                else {
                    Token t5 = new Token(TokenType.WORD, Start, contents, fm, word, top_y);
                    t5.draw_tokens(g2D);
                }

                break;
        }

        function = false;
    }

    private static void getString(String s) {
        // This loop gets the string included from the quotations
        for (int j = 0; j < s.length(); j++) {
            char c = s.charAt(j);

            if (c == '"' && j != 0) {
                Token t = new Token(TokenType.STRING, Start, contents, fm, s.substring(0, j + 1), top_y);
                t.draw_tokens(g2D);

                String rest_of_word = s.substring(j + 1);

                if (rest_of_word.length() != 0) { // If there is sill content after the string
                    Start += j + 1;
                    Token t1 = new Token(TokenType.WORD, Start, contents, fm, rest_of_word, top_y);
                    t1.draw_tokens(g2D);
                }

                string_or_char = false;
                return;
            }
        }

        // If string is never closed, just draws the whole line as green
        Token t = new Token(TokenType.STRING, Start, contents, fm, s, top_y);
        t.draw_tokens(g2D);
    }

    private static void getChar(String s) {
        // This loop gets the char included from the quotations
        for (int j = 0; j < s.length(); j++) {
            char c = s.charAt(j);

            if (c == '\'' && j != 0 && j != 1) {
                Token t = new Token(TokenType.STRING, Start, contents, fm, s.substring(0, j + 1), top_y);
                t.draw_tokens(g2D);

                String rest_of_word = s.substring(j + 1);

                if (rest_of_word.length() != 0) { // If there is sill content after the string
                    Start += j + 1;
                    Token t1 = new Token(TokenType.WORD, Start, contents, fm, rest_of_word, top_y);
                    t1.draw_tokens(g2D);
                }

                string_or_char = false;
                return;
            }
        }

        // If char is never closed, just draws the whole line as green
        Token t = new Token(TokenType.STRING, Start, contents, fm, s, top_y);
        t.draw_tokens(g2D);
    }

    private static void getNumber(String s) {
        boolean first_decimal_point = false; // Flag to consume the first dot for a decimal number, sub-sequent dots are not consumed and end the highlight

        // This loop gets the number in the word
        for (int j = 0; j < s.length(); j++) {
            char c = s.charAt(j);

            if (!isDigit(c)) {
                if (c == '.' && !first_decimal_point) { 
                    first_decimal_point = true;
                    continue;
                 }

                Token t = new Token(TokenType.NUMBER, Start, contents, fm, s.substring(0, j), top_y);
                t.draw_tokens(g2D);

                String rest_of_word = s.substring(j);

                if (rest_of_word.length() != 0) { // If there is still content after the number
                    Start += j;
                    Token t1 = new Token(TokenType.WORD, Start, contents, fm, rest_of_word, top_y);
                    t1.draw_tokens(g2D);
                }

                return;
            }
        }

        // If its is only numbers
        Token t = new Token(TokenType.NUMBER, Start, contents, fm, s, top_y);
        t.draw_tokens(g2D);
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
}
