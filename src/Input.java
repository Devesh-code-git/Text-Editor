import java.util.ArrayList;

// This class handles all user key inputs, as well as the undo logic

public class Input {
    private int cursor_Index;
    private int cursor_Height;

    private ArrayList<GapBuffer> lines = new ArrayList<GapBuffer>();

    private boolean save_cursor = false;

    private UndoStack undo = new UndoStack();
    private RedoStack redo = null;

    private StringBuilder enter_string = new StringBuilder(); // Holds chars moved to new line
    private StringBuilder backspace_string = new StringBuilder(); // Holds chars that were removed
    private StringBuilder deleted_line_string = new StringBuilder(); // Holds chars moved from delted line to prev line
    private StringBuilder typed_string = new StringBuilder(); // Holds chars that were entered

    Input() {
        lines.add(new GapBuffer());

        cursor_Index = lines.get(0).getCursor();
        cursor_Height = lines.size() - 1;
    }

    public int getIndex() {
        return this.cursor_Index;
    }

    public int getHeight() {
        return this.cursor_Height;
    }

    public void resetPosition() {
        cursor_Height = 0;
        cursor_Index = 0;
        lines.get(cursor_Height).setToStart();
    }

    public ArrayList<GapBuffer> getLines() {
        return this.lines;
    }

    public void setLines(ArrayList<GapBuffer> lines) {
        this.lines = lines;
    }

    public GapBuffer getline(int height) {
        return this.lines.get(height);
    }

    // Function handles logic of the ENTER-KEY
    public void Enter() {
        regular_undo_checker();
        backspace_undo_checker();

        if (cursor_Height == lines.size() - 1) {
            lines.add(new GapBuffer());
            cursor_Height = lines.size() - 1;
        } 
        else {
            lines.add(cursor_Height + 1, new GapBuffer());
            cursor_Height++;
        }

        GapBuffer prev = lines.get(cursor_Height - 1);
        GapBuffer current = lines.get(cursor_Height);

        int end = prev.getGapEnd();
        int i = end + 1;
        int max = prev.getMaxCursor() + end - prev.getCursor();
        current.setCursorToMax();

        while(i <= max) { // Will move any chars to the line below it
            char c = prev.getChar(i);
            current.insert(c);

            enter_string.append(c);

            prev.right();
            prev.remove(); // Deletes chars being moved in previous line
            i++;
        }

        Node n = new Node('\n', cursor_Height - 1, prev.getCursor(), enter_string.toString());
        undo.Push(n);
        enter_string.setLength(0);

        current.setToStart();
        cursor_Index = current.getCursor();
        redo = null;
    }

    // Function handles logic of the BACKSPACE-KEY
    public void Backspace() {
        regular_undo_checker();
        boolean start = false;

        // If backspace is not pressed at the beginning of the line
        if (cursor_Index != 0 || cursor_Height == 0) {
            GapBuffer line = lines.get(cursor_Height);
            int get_from = cursor_Index - 1;

            if (!(get_from < 0)) {
                char c = line.getChar(get_from);
                backspace_string.append(c);
            }

            line.remove();

            cursor_Index = line.getCursor();
            redo = null;
            return;
        }

        // If previous line is empty, set flag to move cursor to the start after
        if (lines.get(cursor_Height - 1).getChar(0) == '\u0000') {
            start = true;
        }

        backspace_undo_checker();
        
        GapBuffer prev = lines.get(cursor_Height);
        GapBuffer current = lines.get(cursor_Height - 1);

        int end = prev.getGapEnd();
        int i = end + 1;
        int max = prev.getMaxCursor() + end;
        current.setCursorToMax();

        int prev_position = i;

        while(i <= max) { // Will move any chars to the line above it
            char c = prev.getChar(i);
            current.insert(c);

            deleted_line_string.append(c);
            i++;
        }

        lines.remove(cursor_Height);
        cursor_Height--;

        Node n = new Node('d', cursor_Height, prev_position, deleted_line_string.toString());
        undo.Push(n);
        deleted_line_string.setLength(0);

        // Moving cursor to start
        if (start) {
            lines.get(cursor_Height).setToStart();
            start = false;
        }

        cursor_Index = lines.get(cursor_Height).getCursor();
        redo = null;
    }

    // Function checks if chars have been deleted and creates a undo node for them
    private void backspace_undo_checker() {
        if (backspace_string.length() == 0) {
            return;
        }

        Node n = new Node('\b', cursor_Height, cursor_Index, backspace_string.reverse().toString());
        undo.Push(n);

        backspace_string.setLength(0);
    }

    // Function handles logic of the TAB-KEY
    public void Tab() {
        backspace_undo_checker();

        typed_string.append(" ");
        typed_string.append(" ");
        typed_string.append(" ");

        GapBuffer line = lines.get(cursor_Height);
        line.insert(' ');
        line.insert(' ');
        line.insert(' ');

        cursor_Index = line.getCursor();
        redo = null;
    }

    // Function handles logic of any other char
    public void RegularChar(char c) {
        backspace_undo_checker();

        typed_string.append(c);
        GapBuffer line = lines.get(cursor_Height);
        line.insert(c);

        cursor_Index = line.getCursor();
        redo = null;
    }

    // Function checks if chars have been typed and creates a undo node for them
    private void regular_undo_checker() {
        if (typed_string.length() == 0) {
            return;
        }

        Node n = new Node('c', cursor_Height, cursor_Index, typed_string.toString());
        undo.Push(n);

        typed_string.setLength(0);
    }

    //UP-KEY
    public void UP() {
        regular_undo_checker();
        backspace_undo_checker();

        if (cursor_Height == 0) {
            return;
        }

        cursor_Height--;
        GapBuffer line = lines.get(cursor_Height);

        if (save_cursor) {
            cursor_Index = line.getCursor();
            save_cursor = false;
            return;
        }

        if (cursor_Index > line.getMaxCursor()) {
            cursor_Index = line.getMaxCursor();
            save_cursor = true;

            line.setCursorToMax();
        } 
        else {
            if (cursor_Index < line.getCursor()) {
                while(line.getCursor() != cursor_Index) {
                    line.left();
                }
            } 
            else {
                while(line.getCursor() != cursor_Index) {
                    line.right();
                }
            }
        }
    }

    //DOWN-KEY
    public void DOWN() {
        regular_undo_checker();
        backspace_undo_checker();

        if (cursor_Height == lines.size() - 1) {
            return;
        }

        cursor_Height++;
        GapBuffer line = lines.get(cursor_Height);

        if (save_cursor) {
            cursor_Index = line.getCursor();
            save_cursor = false;
            return;
        }

        if (cursor_Index > line.getMaxCursor()) {
            cursor_Index = line.getMaxCursor();
            save_cursor = true;

            line.setCursorToMax();
        } 
        else {
            if (cursor_Index < line.getCursor()) {
                while(line.getCursor() != cursor_Index) {
                    line.left();
                }
            } 
            else {
                while(line.getCursor() != cursor_Index) {
                    line.right();
                }
            }
        }
    }

    //LEFT-KEY
    public void LEFT() {
        regular_undo_checker();
        backspace_undo_checker();

        save_cursor = false;
        GapBuffer line = lines.get(cursor_Height);

        if (line.getCursor() == 0 && cursor_Height > 0) {
            cursor_Height--;
            lines.get(cursor_Height).setCursorToMax();
        } 
        else {
            lines.get(cursor_Height).left(); // In the data-structure move left
        }

        cursor_Index = lines.get(cursor_Height).getCursor(); // Update the cursors index for current line
    }

    //RIGHT-KEY
    public void RIGHT() {
        regular_undo_checker();
        backspace_undo_checker();

        save_cursor = false;
        GapBuffer line = lines.get(cursor_Height);

        if (line.getCursor() == line.getMaxCursor() && cursor_Height < lines.size() - 1) {
            cursor_Height++;
            lines.get(cursor_Height).setToStart();
        } 
        else {
            lines.get(cursor_Height).right(); // In the data-structure move right
        }

        cursor_Index = lines.get(cursor_Height).getCursor(); // Update cursors index for current line
    }

    private void helper_one() {
        GapBuffer line = lines.get(cursor_Height);

        for (int i = 0; i < typed_string.length(); i++) {
            line.remove();
        }

        Node n = new Node('c', cursor_Height, cursor_Index, typed_string.toString());

        cursor_Index = line.getCursor();
        redo = new RedoStack('c', typed_string.toString(), n, cursor_Height, cursor_Index);

        typed_string.setLength(0);
    }

    private void helper_two() {
        GapBuffer line = lines.get(cursor_Height);
        int length = backspace_string.length();

        Node n = new Node('\b', cursor_Height, cursor_Index, backspace_string.reverse().toString());

        line.insert(backspace_string.toString());
        backspace_string.setLength(0);

        cursor_Index = line.getCursor();
        redo = new RedoStack('\b', length, n, cursor_Height, cursor_Index);
    }

    public void UNDO() {
        // If user was typing word, and presses undo before presseing space
        if (typed_string.length() != 0) {
            this.helper_one();
            return;
        }

        // If user was deleting chars, and presses undo before pressing any other key
        if (backspace_string.length() != 0) {
            this.helper_two();
            return;
        }

        Node n = undo.Pop();

        if (n == null) {
            return;
        }

        char type = n.getType();

        if (type == 'c') {
            this.reg_undo(n);
        }
        else if (type == '\n') {
            this.enter_undo(n);
        }
        else if (type == '\b') {
            this.backspace_undo(n);
        }
        else if (type == 'd') {
            this.deleted_line_undo(n);
        }
    }

    private void reg_undo(Node n) {
        int h = n.get_height();
        GapBuffer line = lines.get(h);
        int length = n.getString().length();

        line.setCursorTo(n.get_start());

        for (int i = 0; i < length; i++) {
            line.remove();
        }

        cursor_Index = line.getCursor();
        cursor_Height = h;
        redo = new RedoStack('c', n.getString(), n, h, cursor_Index);
    }

    private void enter_undo(Node n) {
        int h = n.get_height();

        GapBuffer old_line = lines.get(h);
        String s = n.getString();

        old_line.setCursorTo(n.get_start());
        old_line.insert(s);
        old_line.setCursorTo(n.get_start() + 1);

        lines.remove(h + 1);

        cursor_Height = h;
        cursor_Index = old_line.getCursor();

        redo = new RedoStack('\n', s, n, h);
    }

    private void backspace_undo(Node n) {
        int h = n.get_height();
        String s = n.getString();
        GapBuffer line = lines.get(h);

        int length = s.length();

        line.setCursorTo(n.get_start());
        line.insert(s);

        cursor_Index = line.getCursor();
        cursor_Height = h;
        redo = new RedoStack('\b', length, n, h, cursor_Index);
    }

    private void deleted_line_undo(Node n) {
        int h = n.get_height();
        int go_to = n.get_start();
        String s = n.getString();
        GapBuffer line;

        if (h == lines.size() - 1) {
            lines.add(new GapBuffer());
            line = lines.get(lines.size() - 1);
        }
        else {
            lines.add(h + 1, new GapBuffer());
            line = lines.get(h);
        }

        GapBuffer prev = lines.get(h);
        prev.setCursorTo(go_to);
        for (int i = 0; i < s.length(); i++) {
            prev.right();
            prev.remove();
        }

        line.insert(s);
        line.setToStart();
        cursor_Height = h + 1;
        cursor_Index = line.getCursor();

        redo = new RedoStack('d', s, n, h + 1);
    }

    public void REDO() {
        if (redo == null) {
            return;
        }

        char type = redo.get_type();

        if (type == 'c') {
            this.reg_redo();
        }
        else if (type == '\b') {
            this.backspace_redo();
        }
        else if (type == '\n') {
            this.enter_redo();
        }
        else if (type == 'd') {
            this.deleted_line_redo();
        }
    }

    private void reg_redo() {
        GapBuffer line = lines.get(redo.get_height());
        line.setCursorTo(redo.get_Start());
        line.insert(redo.get_String());
        
        undo.Push(redo.get_Node());
        cursor_Index = line.getCursor();
        cursor_Height = redo.get_height();
        redo = null;
    }

    private void backspace_redo() {
        GapBuffer line = lines.get(redo.get_height());
        line.setCursorTo(redo.get_Start());

        for (int i = 0; i < redo.get_length(); i++) {
            line.remove();
        }

        cursor_Index = line.getCursor();
        cursor_Height = redo.get_height();

        undo.Push(redo.get_Node());
        redo = null;
    }

    private void enter_redo()  {
        int h = redo.get_height();
        GapBuffer old_line = lines.get(h);
        old_line.setCursorToMax();

        String s = redo.get_String();
        int length = s.length();
        for (int i = 0; i < length; i++) {
            old_line.remove();
        }

        if (h == lines.size() - 1) {
            lines.add(new GapBuffer());
        }
        else {
            lines.add(h + 1, new GapBuffer());
        }

        GapBuffer new_line = lines.get(h + 1);
        new_line.insert(s);
        new_line.setToStart();

        cursor_Height = h + 1;
        cursor_Index = new_line.getCursor();
        undo.Push(redo.get_Node());
        redo = null;
    }

    private void deleted_line_redo() {
        int h = redo.get_height();
        String s = redo.get_String();

        GapBuffer prev_line = lines.get(h - 1);
        prev_line.setCursorToMax();
        prev_line.insert(s);

        lines.remove(h);

        cursor_Height = h - 1;
        cursor_Index = prev_line.getCursor();

        undo.Push(redo.get_Node());
        redo = null;
    }
}
