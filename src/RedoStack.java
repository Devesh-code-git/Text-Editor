// This class holds necessary information based on the type of redo needed, used for performing redos

public class RedoStack {
    private final Node n;

    private final String s;
    private final char type;

    private final int length;
    private final int height;
    private final int start;

    // Constructor used for regular char redos
    RedoStack(char type, String s, Node n, int h, int start) {
        this.type = type;
        this.s = s;
        this.n = n;
        this.start = start;
        height = h;

        this.length = 0;
    }

    // Constructor used for backspace redos
    RedoStack(char type, int length, Node n, int h, int start) {
        this.type = type;
        this.length = length;
        this.n = n;
        this.start = start;
        height = h;

        this.s = "";
    }

    // Constructor used for enter redos and deleted line nodes
    RedoStack(char type, String s, Node n, int h) {
        this.type = type;
        this.s = s;
        this.n = n;
        height = h;

        this.length = 0;
        this.start = 0;
    }

    public Node get_Node() {
        return n;
    }

    public int get_length() {
        return length;
    }

    public int get_height() {
        return height;
    }

    public int get_Start() {
        return start;
    }

    public String get_String() {
        return s;
    }

    public char get_type() {
        return type;
    }
}
