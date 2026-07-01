import java.awt.*;

// This class is used for creating tokens, and using those tokens to print syntax highlighted text to the screen:
public class Token {
    private int Start;
    private TokenType token;
    private String line;
    private String word;

    private final int OFFSET_X = 15;

    private int x_position;
    private int top_y;
    private FontMetrics fm;

    private Color WORD     = new Color(236, 224, 202); // Cream
    private Color KEYWORD  = new Color(230, 126, 34);  // Burnt orange
    private Color TYPE     = new Color(215, 170, 80);  // Golden
    private Color CLASS    = new Color(217, 120, 75);  // Terracotta
    private Color STRING   = new Color(155, 194, 110); // Soft olive
    private Color NUMBER   = new Color(219, 143, 75);  // Amber
    private Color COMMENT  = new Color(128, 118, 95);  // Warm gray
    private Color LITERAL  = new Color(206, 88, 88);   // Soft red
    private Color ARROW    = new Color(205, 190, 160); // Beige
    private Color FUNCTION = new Color(240, 170, 130); // Soft-Cream

    Token(TokenType token, int Start, String line, FontMetrics fm, String word, int top_y) {
        this.token = token;
        this.Start = Start;
        this.line = line;
        this.fm = fm;
        this.word = word;
        this.top_y = top_y;

        this.build_x_position();
    }

    private void build_x_position() {
        x_position = fm.stringWidth(line.substring(0, Start)) + OFFSET_X;
    }

    public void draw_tokens(Graphics2D g2D) {
        g2D.setFont(new Font("Consolas", Font.PLAIN, 16));

        switch (token) {
            case TokenType.CLASS:
                g2D.setColor(CLASS);
                break;
            case TokenType.TYPE:
                g2D.setColor(TYPE);
                break;
            case TokenType.KEYWORD:
                g2D.setColor(KEYWORD);
                break;
            case TokenType.WORD:
                g2D.setColor(WORD);
                break;
            case TokenType.NUMBER:
                g2D.setColor(NUMBER);
                break;
            case TokenType.STRING:
                g2D.setColor(STRING);
                break;
            case TokenType.ARROW:
                g2D.setColor(ARROW);
                break;
            case TokenType.COMMENT:
                g2D.setColor(COMMENT);
                break;
            case TokenType.LITERAL:
                g2D.setColor(LITERAL);
                break;
            case TokenType.FUNCTION:
                g2D.setColor(FUNCTION);
                break; 
        }

        g2D.drawString(word, x_position, top_y);
    }
}
