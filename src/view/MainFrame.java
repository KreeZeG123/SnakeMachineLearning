package view;

import javax.swing.JFrame;

import controller.AbstractController;
import controller.ControllerSnakeGame;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;



public class MainFrame extends JFrame implements KeyListener{

    ControllerSnakeGame controller;
    
    public MainFrame(ControllerSnakeGame controller){

        this.controller = controller;
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
    }



    public void keyPressed(KeyEvent e) {

        System.out.println("Key pressed");
        System.out.println("this.controller");
        System.out.println(this.controller);

        if(e.getKeyCode()== KeyEvent.VK_RIGHT)
            this.controller.goRight();
        else if(e.getKeyCode()== KeyEvent.VK_LEFT)
            this.controller.goLeft();
        else if(e.getKeyCode()== KeyEvent.VK_DOWN)
            this.controller.goDown();
        else if(e.getKeyCode()== KeyEvent.VK_UP)
            this.controller.goUp();

    }

    @Override
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub
        
    }


}