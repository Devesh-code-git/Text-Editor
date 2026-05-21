import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

// Frame class to intialize JPanel

public class Frame extends JFrame{
    public Frame() {
        Panel screen = new Panel(); // New JPanel

        this.setTitle("Devesh's - TextEditor");
        this.add(screen);
        this.pack();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH); // Extends the panel to fit devices screen
        getContentPane().setBackground(new Color(11, 28, 44));

        // Request focus so key events work
        SwingUtilities.invokeLater(() -> screen.requestFocusInWindow());
        this.setVisible(true);
    }
}
