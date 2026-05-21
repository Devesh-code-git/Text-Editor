// This class implements the main data-structure for the text editor which is the GapBuffer

public class GapBuffer {
    private char[] line = new char[128];
    private int cursor = 0, Max_cursor = 0;
    private int gap_end = line.length - 1;
    private int gap_size;

    GapBuffer() {
        gap_size = gap_end - this.cursor;
    }
    
    public void insert(char c) {
        gap_size = gap_end - this.cursor;

        if (gap_size <= 0) {
            this.resize(c);
        } 
        else {
            line[this.cursor] = c;
            this.cursor++;
            this.Max_cursor++;
            gap_size = gap_end - this.cursor;
        }
    }

    public void insert(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c == '\t') {
                this.insert(' ');
                this.insert(' ');
                this.insert(' ');
            }
            else {
                this.insert(c);
            }
        }
    }

    public void remove() {
        if (!(this.cursor <= 0)) {
            this.cursor--;
            this.Max_cursor--;
            line[this.cursor] = '\u0000';
            gap_size = gap_end - this.cursor;
        }
    }

    public void left() {
        char c;

        if (this.cursor != 0) {
            c = line[this.cursor - 1]; // Save the character before the start of the gap

            // Move start of and end of gap leftwards
            this.cursor--;
            gap_end--;
            gap_size = gap_end - this.cursor;

            line[this.cursor] = '\u0000'; // Put the empty chracter at the new start of the gap
            line[gap_end + 1] = c; // Put saved character at end of the gap
        }
    }

    public void right() {
        char c;

        if (gap_end != line.length - 1) {
            c = line[gap_end + 1]; // Save the character thats after the end of the gap

            // Move start of and end of gap rightwards
            this.cursor++;
            gap_end++;
            gap_size = gap_end - this.cursor;

            line[gap_end] = '\u0000';
            line[this.cursor - 1] = c;
        }
    }

    public char getChar(int idx) {
        return line[idx];
    }

    public int getSize() {
        return line.length;
    }

    public int getCursor() {
        return cursor;
    }

    public int getMaxCursor() {
        return Max_cursor;
    }

    public int getGapEnd() {
        return gap_end;
    }

    public int get_number_of_chars() {
        return line.length - gap_size - 1;
    }

    // Finds where the first char is in array
    public int getFirstCharIdx() {
        if (cursor == 0) {
            return gap_end + 1;
        }

        return 0;
    }

    // Brings cursor to position of last char
    public void setCursorToMax() {
        int diff = Max_cursor - cursor;
        for (int i = 0; i <= diff; i++) {
            this.right();
        }
    }

    // Brings cursor to the start/0th index
    public void setToStart() {
        for (int i = cursor; i != 0; i--) {
            this.left();
        }
    }

    // Brings cursor to nth index
    public void setCursorTo(int n) {
        if (n >= line.length || n < 0) {
            return;
        }

        if (n < this.cursor) {
            int diff = this.cursor - n;

            for (int i = 0; i <= diff; i++) {
                this.left();
            }
        } else {
            int diff = n - this.cursor;
            
            for (int i = 0; i <= diff; i++) {
                this.right();
            }
        }
    }

    private void resize(char c) {
        int size = line.length;
        char[] new_arr = new char[size*2];

        for (int i = 0; i < line.length; i++) {
            new_arr[i] = line[i];
        }

        /* Incase cursor is not at end of the array, puts character where cursor currently is before resizing at new array
           and updates cursor position to one after last element of the original array*/
        new_arr[this.cursor] = c;
        this.Max_cursor++;
        int l = line.length - 1 - this.cursor; // Calculates length from cursors original position to end of original array
        this.cursor = line.length;

        line = new_arr;

        gap_end = new_arr.length - 1;
        gap_size = gap_end - this.cursor;

        for (int i = 0; i < l; i++) { // Incase cursor is in the middle of the array, after resizing move cursor left until back at cursors original position + 1
            this.left();
        }
    }
}