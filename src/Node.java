// This class is a Node that holds necessary information for performing undos at each undo step

public class Node {
    Node prev;

    private final char type;
    private final String s;

    private final int height;
    private final int start;

    Node(char type, int height, int start, String s) {
        this.type = type;
        this.height = height;
        this.start = start;
        this.s = s;
    }

    public char getType() {
        return type;
    }

    public int get_start() {
        return start;
    }
    
    public int get_height() {
        return height;
    }

    public String getString() {
        return s;
    }
}
