package view;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JLabel;

import model.Game;


public class ViewSimpleGame implements Observer {

	
	JFrame jFrame;
	JLabel turnText = new JLabel("Turn : ",JLabel.CENTER);
	
	public ViewSimpleGame( Observable obs) {
		
		
		obs.addObserver(this);
		
		
		jFrame= new JFrame();
		jFrame.setTitle("Game");
		jFrame.setSize(new Dimension(700, 700));
		Dimension windowSize=jFrame.getSize();
		GraphicsEnvironment ge=GraphicsEnvironment.getLocalGraphicsEnvironment();
		Point centerPoint=ge.getCenterPoint();
		int dx=centerPoint.x - windowSize.width/ 2 ;
		int dy=centerPoint.y - windowSize.height/ 2 - 350;
		jFrame.setLocation(dx,dy);
		
		jFrame.setVisible(true);

		Font font = new Font("Arial",Font.BOLD,26);
		turnText.setFont(font);
		
		jFrame.add(this.turnText);
		
	}
	
	


	@Override
	public void update(Observable o, Object arg) {
		
		Game game = (Game)o;
		
		turnText.setText("Turn : "+ game.getTurn());
		
	}
	
}
