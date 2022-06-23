import java.awt.*;
import javax.swing.*;

class Game{
    private int width = 1920;
    private int height = 1080;

    public Game(){
        System.setProperty("sun.java2d.opengl", "true");
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(Color.black);        
        frame.setResizable(false);
        frame.setSize(width, height);
        Window window = new Window(width, height);
        window.setFocusable(true);
        frame.add(window);
        frame.setVisible(true);
    }

    public static void main(String args[]){
        new Game();
    }
}
