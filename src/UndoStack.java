// This class is a simple stack for storing and entering undo Nodes

public class UndoStack {
    private Node end = null;
    private int count = 0;

    UndoStack() {}

    public void Push(Node n) {
        if (count == 0) {
            end = n;
            n.prev = null;
            count++;
            return;
        }

        // New Node
        n.prev = end;

        // Previous node
        end = n;

        count++;
    }

    public Node Pop() {
        if (count == 0) {
            return null;
        }

        if (count == 1) {
            Node n = end;
            end = null;
            count--;
            return n;
        }

        Node temp = end.prev;
        Node n = end;
        end.prev = null;
        end = temp;
        count--;
        return n;
    }
}
