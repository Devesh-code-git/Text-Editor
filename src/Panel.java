import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.filechooser.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

// This class uses javaSwing to handle all of the rendering onto the screen, it also handles basic mouse functionality

public class Panel extends JPanel implements KeyListener, MouseListener, MouseMotionListener {
    private int OFFSET_X = 15, OFFSET_Y = 15;
    private int cursorX = OFFSET_X, Old_X;
    private int cursorY = OFFSET_Y, Old_Y;
    private int charWidth, charHeight;
    private int line_start, line_end;
    private int Y_start, Y_end;
    private int width, height;
    private int new_width, open_width, save_width;

    private StringBuilder sb = new StringBuilder();
    
    private boolean showCursor = true, moving = false;
    private boolean ctrlPressed = false;
    private boolean paint_whole_screen = false;
    private boolean saved = false;
    private boolean New = false, Open = false, Save = false;

    private Font font = new Font("Consolas", Font.PLAIN, 16); // Using a monospaced font so each char has the same width
    private FontMetrics fm;

    private FileIO file = new FileIO(); // Object to handle all of the File I/O for the editor
    private Input input = new Input(); // Object to handle all keyboard input and Gapbuffer / Lines managment

    private Color BACKGROUND = new Color(10, 10, 10);
    private Color TEXT = new Color(245, 245, 245);
    private Color CURSOR = new Color(120, 200, 255);
    private Color SELECTION = new Color(20, 20, 20);
    
    public Panel() {
        this.setBackground(BACKGROUND);
        this.setFocusable(true);
        this.setFocusTraversalKeysEnabled(false);
        this.addKeyListener(this);
        this.addMouseMotionListener(this);
        this.addMouseListener(this);

        int delay = 600; // milliseconds (0.6 second)

        Timer timer = new Timer(delay, new ActionListener() { // This gives the cursor blinking effect
            @Override
            public void actionPerformed(ActionEvent e) {
                showCursor = !showCursor;
                repaint(cursorX, cursorY + 3, charWidth + 1, charHeight * 2);
            }
        });

        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;

        // if fm is null, than none of these variables have been set before, so intially sets these variables
        if (fm == null) {
            fm = getFontMetrics(font);
            charHeight = fm.getHeight();
            charWidth = fm.charWidth('A');

            width = getWidth();
            height = getHeight();

            line_end = ((width - OFFSET_X) / charWidth) + 1;
            line_start = 0;
            Y_end = ((height - OFFSET_Y) / (charHeight + 7)) - 2;
            Y_start = 0;

            new_width = fm.stringWidth("New");
            save_width = fm.stringWidth("Save");
            open_width = fm.stringWidth("Open");
        }

        g2D.setFont(font);

        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Makes the graphics look better
        
        g2D.setColor(SELECTION);
        g2D.fillRect(0, 0, 10, height);
        g2D.fillRect(0, cursorY + 3, width, charHeight + 2);
        g2D.fillRect(0, (height - (charHeight * 2)), width, 100); 

        // Drawing the cursor
        if (showCursor || moving) {
            g2D.setColor(CURSOR);
            g2D.fillRect(cursorX, cursorY + 3, charWidth, charHeight + 2);
        }

        g2D.setColor(TEXT);
        ArrayList<GapBuffer> lines = input.getLines();

        // Printing each char onto screen that fits the visable window
        for (int i = Y_start; i < Y_end + 1; i++) {
            if (i >= lines.size()) {
                break;
            }

            GapBuffer line = lines.get(i);

            for (int j = 0 + line_start; j < line.getCursor(); j++) {
                if (j >= line.getSize()) {
                    break;
                }

                char c = line.getChar(j);
                sb.append(c);
            }

            int start = 0;

            if (line.getSize() == 128) {
                start = line.getGapEnd() + 1 + line_start;
            } 
            else {
                start = line.getGapEnd() + 1;
            }

            for (int j = start; j < line.getSize(); j++) {
                if (j >= line.getSize()) {
                    break;
                }

                char c = line.getChar(j);
                sb.append(c);
            }

            int topY = charHeight + OFFSET_Y + (i - Y_start) * (charHeight + 7);
            g2D.drawString(sb.toString(), OFFSET_X, topY);
            sb.setLength(0);
        }

        int cursor_Index = input.getIndex();
        int cursor_Height = input.getHeight();

        if (New) {
            g2D.setColor(new Color(20, 40, 85));
            g2D.fillRect(width - 310, (height - (charHeight * 2) + 1), (new_width * 2), 40);
        } 
        else if (Open) {
            g2D.setColor(new Color(20, 40, 85));
            g2D.fillRect(width - 210, (height - (charHeight * 2) + 1), (open_width * 2) - 10, 40);
        } 
        else if (Save) {
            g2D.setColor(new Color(20, 40, 85));
            g2D.fillRect(width - 110, (height - (charHeight * 2) + 1), (save_width * 2) - 10, 40);
        }

        g2D.setColor(TEXT);
        g2D.drawString("Ln " + (cursor_Height + 1) + ", Col " + (cursor_Index + 1), OFFSET_X, (height - charHeight + 1));
        g2D.drawString("Save", width - 100, (height - charHeight + 1));
        g2D.drawString("Open", width - 200, (height - charHeight + 1));
        g2D.drawString("New", width - 300, (height - charHeight + 1));

        if (file.getFile() == null) {
            g2D.drawString("[*]", width / 3, (height - charHeight + 1));
        } 
        else {
            String name = file.getName();

            if (saved) {
                g2D.drawString(name + " [•]", width / 3, (height - charHeight + 1));
            } 
            else {
                g2D.drawString(name + " [*]", width / 3, (height - charHeight + 1));
            }
        }
    }

    // This funciton checks if the cursor is outside the visible range and updates the visible window so the proper chars are printed
    private void accumulator() {
        int line_width = line_end - line_start;
        int line_height = Y_end - Y_start;
        int cursor_Index = input.getIndex();
        int cursor_Height = input.getHeight();

        // For shifting the view window left or right
        if (cursor_Index > line_end) {
            line_start = cursor_Index - line_width;
            paint_whole_screen = true;
        } 
        else if (cursor_Index < line_start) {
            line_start = cursor_Index;
            paint_whole_screen = true;
        }

        // For shifting the view window up or down
        if (cursor_Height > Y_end) {
            Y_start = cursor_Height - line_height;
            paint_whole_screen = true;
        } 
        else if (cursor_Height < Y_start) {
            Y_start = cursor_Height;
            paint_whole_screen = true;
        }

        line_end = ((width - OFFSET_X) / charWidth) + line_start;
        Y_end = ((height - OFFSET_Y) / (charHeight + 7)) - 2 + Y_start;
    }

    // Function that updates the cursors coordinates
    private void updateCursor() {
        int cursor_Height = input.getHeight();

        GapBuffer line = input.getline(cursor_Height);
        sb.setLength(0);

        // Loop builds a string from the start of the first visible to char, to last char before cursor
        for (int i = 0 + line_start; i < line.getCursor(); i++) {
            char c = line.getChar(i);
            sb.append(c);
        }

        cursorX = fm.stringWidth(sb.toString()) + OFFSET_X; // Uses the strings width to calculate where the cursor should be
        cursorY = ((charHeight + 7) * (cursor_Height - Y_start)) + OFFSET_Y;
        sb.setLength(0);
    }

    // Paint functions to decide which area of the screen to repaint
    private void Paint(char c) {
        if (paint_whole_screen) {
            repaint();
            paint_whole_screen = false;
            return;
        }

        if (c == '\n') { // ENTER
            int y = ((charHeight + 7) * (input.getHeight() - 1 - Y_start)) + 15;
            repaint(0, y, width, height - y - OFFSET_Y);
            repaint(0, (height - (charHeight * 2)), 150 + OFFSET_X, 100);
            return;
        }

        if (c == '\b' || c == '\t') { // BACKSPACE and TAB
            repaint(0, cursorY, width, charHeight * 2);
            repaint(0, (height - (charHeight * 2)), 150 + OFFSET_X, 100);
        }

        if (c == 'U') { // ARROW-KEYS
            repaint(cursorX, cursorY, charWidth * 2, charHeight * 2);
            repaint(Old_X, Old_Y, charWidth * 2, charHeight * 2);
            repaint(0, (height - (charHeight * 2)), width, 100);
            return;
        }

        if (c == 'P') { // NEW - OPEN - SAVE, buttons
            repaint(0, (height - (charHeight * 2)), width, 100);
            return;
        }

        // Regular Char
        repaint(0, cursorY, width, charHeight * 2);
        repaint(0, (height - (charHeight * 2)), 150 + OFFSET_X, 100);
    }

    private void update(char c) {
        accumulator();
        updateCursor();
        Paint(c);
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();

        if (ctrlPressed) {
            return;
        }

        saved = false;
        moving = true;

        if (c == '\n') {
            input.Enter();
        } 
        else if (c == '\b') {
            input.Backspace();
        }
        else if (c == '\t') {
            input.Tab();
        }
        else {
            input.RegularChar(c);
        }

        update(c);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        Old_X = cursorX;
        Old_Y = cursorY;

        // CTRL-KEY
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            ctrlPressed = true;
            return;
        }

        // If CTRL + S are pressed
        if (ctrlPressed && e.getKeyCode() == KeyEvent.VK_S) {
            if (file.getFile() == null) {
                // Open file explorer and get path of where the file is being saved
                JFileChooser j = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                int r = j.showSaveDialog(null);

                if (r == JFileChooser.APPROVE_OPTION) {
                    file.changeFile(j.getSelectedFile().getAbsolutePath());
                    file.saveFile(input.getLines());
                }
            } 
            else {
                file.saveFile(input.getLines());
            }

            saved = true;
            repaint(0, (height - (charHeight * 2)), width, 100);
            return;
        }

        // If CTRL + Z are pressed
        if (ctrlPressed && e.getKeyCode() == KeyEvent.VK_Z) {
            input.UNDO();

            moving = true;

            accumulator();
            updateCursor();
            repaint();
            return;
        }

        if (ctrlPressed && e.getKeyCode() == KeyEvent.VK_Y) {
            input.REDO();

            moving = true;

            accumulator();
            updateCursor();
            repaint();
            return;
        }

        // ARROW-KEYS
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            input.UP();
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            input.DOWN();
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            input.RIGHT();
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            input.LEFT();
        }

        moving = true;
        update('U');
    }

    @Override
    public void keyReleased(KeyEvent e) {
        moving = false; // Cursor can blink again after any key is not being pressed/typed

        if (ctrlPressed && e.getKeyCode() == KeyEvent.VK_CONTROL) {
            ctrlPressed = false;
        }
    }

    private int getButton(int x, int y) {
        // If cursor is at New
        if (x >= width - 310 && x <= width - 310 + (new_width * 2)) {
            if (y >= (height - (charHeight * 2) + 1)) {
                return 0;
            }
        }

        // If cursor is at Open
        if (x >= width - 210 && x <= width - 220 + (open_width * 2)) {
            if (y >= (height - (charHeight * 2) + 1)) {
                return 1;
            }
        } 

        // If cursor is at Save
        if (x >= width - 110 && x <= width - 120 + (save_width * 2)) {
            if (y >= (height - (charHeight * 2) + 1)) {
                return 2;
            }
        }

        return -1;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        int button = getButton(x, y);

        if (button == 0) {
            New = true;
            Paint('P');
            return;
        }
        else if (button == 1) {
            Open = true;
            Paint('P');
            return;
        }
        else if (button == 2) {
            Save = true;
            Paint('P');
            return;
        }

        if (New) {
            New = false;
            Paint('P');
        } 
        else if (Open) {
            Open = false;
            Paint('P');
        }
        else if (Save) {
            Save = false;
            Paint('P');
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        int button = getButton(x, y);

        if (button == 0) {
            if (file.getFile() != null) {
                file.saveFile(input.getLines());
            }

            input = new Input();
            accumulator();
            updateCursor();
            repaint();
        }

        if (button == 1) {
            // Open file explorer and get path of where the file is being opened from
            JFileChooser j = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
            int r = j.showOpenDialog(null);

            if (r == JFileChooser.APPROVE_OPTION) {
                file.changeFile(j.getSelectedFile().getAbsolutePath());
                input = new Input();
                input.setLines(file.readFile());
                input.resetPosition();
                accumulator();
                updateCursor();
                repaint();
            }
        }

        if (button == 2) {
            if (file.getFile() == null) {
                // Open file explorer and get path of where the file is being saved
                JFileChooser j = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                int r = j.showSaveDialog(null);

                if (r == JFileChooser.APPROVE_OPTION) {
                    file.changeFile(j.getSelectedFile().getAbsolutePath());
                    file.saveFile(input.getLines());
                }

                repaint(0, (height - (charHeight * 2)), width, 100);
            } 
            else {
                file.saveFile(input.getLines());
            }

            saved = true;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mouseDragged(MouseEvent e) {}
}