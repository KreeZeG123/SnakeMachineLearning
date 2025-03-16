package view;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JFrame;
import agent.Snake;
import controller.ControllerSnakeGame;
import item.Item;
import model.SnakeGame;
import utils.FeaturesItem;
import utils.FeaturesSnake;



@SuppressWarnings({"deprecation" })
public class ViewSnakeGame implements Observer{

	JFrame jFrame;

	PanelSnakeGame panelSnakeGame;

	public ViewSnakeGame(ControllerSnakeGame controller,  PanelSnakeGame panelSnakeGame) {


		jFrame= new MainFrame(controller);
			
		jFrame.setTitle("Game");
		
		jFrame.setSize(new Dimension(panelSnakeGame.getSizeX()*45, panelSnakeGame.getSizeY()*45));
		Dimension windowSize=jFrame.getSize();
		GraphicsEnvironment ge=GraphicsEnvironment.getLocalGraphicsEnvironment();
		Point centerPoint=ge.getCenterPoint();
		int dx=centerPoint.x - windowSize.width/ 2 ;
		int dy=centerPoint.y - windowSize.height/ 2 - 350;
		jFrame.setLocation(dx,dy);
		
		this.panelSnakeGame = panelSnakeGame;
		
		jFrame.add("Center",panelSnakeGame);

		jFrame.setVisible(true);
	

	}

	public void setPanelSnakeGame(PanelSnakeGame panelSnakeGame) {
		
		this.panelSnakeGame = panelSnakeGame;
	}
		
	
	
	public void addSnakeGame(SnakeGame game) {
		
		game.addObserver(this);
	}
	
	
	@Override
	public void update(Observable o, Object arg) {

		SnakeGame snakeGame = (SnakeGame)o;


		
		ArrayList<FeaturesSnake>  featuresSnakes = new ArrayList<FeaturesSnake>();
	
		for(Snake snake : snakeGame.getSnakes()) {	
			
			boolean isInvincible;
			if(snake.getInvincibleTimer() > 0) {
				isInvincible = true;
			} else {
				isInvincible = false;
			}
			
			boolean isSick;
			if(snake.getSickTimer() > 0) {
				isSick = true;
			} else {
				isSick = false;
			}
			
			featuresSnakes.add(new FeaturesSnake(snake.getPositions(), snake.getLastMove(), snake.getColorSnake(), isInvincible, isSick, snake.isDead()));
		}
		
		ArrayList<FeaturesItem> featuresItem = new ArrayList<FeaturesItem>();
		
		for(Item item : snakeGame.getItems()) {		
			featuresItem.add(new FeaturesItem(item.getX(), item.getY(), item.getItemType()));
		}

		
		panelSnakeGame.updateInfoGame(featuresSnakes , featuresItem);


		panelSnakeGame.repaint();

		

	}


}
