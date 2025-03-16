package view;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import controller.AbstractController;
import model.Game;
import model.SnakeGame;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observer;
import java.util.Observable;

public class ViewCommand implements Observer {

	JFrame jFrame;
	JLabel jtext;

	AbstractController controller;

	StateViewCommand state;



	JButton initChoice;
	JButton pauseChoice;
	JButton playChoice;
	JButton stepChoice;
	
	public ViewCommand (AbstractController controller, Game game)  {


		this.controller = controller;
		
		
		game.addObserver(this);
		



		jFrame= new JFrame();
		jFrame.setTitle("Bouton");
		jFrame.setSize(new Dimension(700, 300));
		Dimension windowSize=jFrame.getSize();
		GraphicsEnvironment ge=GraphicsEnvironment.getLocalGraphicsEnvironment();
		Point centerPoint=ge.getCenterPoint();

		int dx= centerPoint.x - windowSize.width/ 2 + 1000;
		int dy= centerPoint.y - windowSize.height/ 2 - 350;
		jFrame.setLocation(dx,dy);

		Icon icon_restart= new ImageIcon("icons/icon_restart.png");
		initChoice = new JButton(icon_restart);

		Icon icon_play= new ImageIcon("icons/icon_play.png");
		playChoice = new JButton(icon_play);

		Icon icon_step= new ImageIcon("icons/icon_step.png");
		stepChoice = new JButton(icon_step);

		Icon icon_pause= new ImageIcon("icons/icon_pause.png");
		pauseChoice = new JButton(icon_pause);

		
		initChoice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evenement){
				controller.restart();
				state.clickRestart();
			}
		});

		//
		pauseChoice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evenement){
				controller.pause();
				state.clickPause();
			}
		});


		playChoice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evenement){
				
				System.out.println("Play");
				controller.play();
				state.clickPlay();
			}
		});


		stepChoice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evenement){
				controller.step();
				state.clickStep();
			}
		});


		JSlider j = new JSlider(1,10);

		j.setValue(2);
		j.setMajorTickSpacing(1);
		j.setPaintTicks(true);
		j.setPaintLabels(true);



		j.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evenement) {
				JSlider source = (JSlider)evenement.getSource();
				if(!source.getValueIsAdjusting()) {
					double speed = source.getValue();
					controller.setSpeed(speed);
					System.out.println("Vitesse changée à : " + speed);
				}
			}
		});


		jFrame.setLayout(new GridLayout(2,1));

		JPanel haut = new JPanel();
		haut.setLayout(new GridLayout(1,4));
		haut.add(initChoice);
		haut.add(pauseChoice);
		haut.add(playChoice);
		haut.add(stepChoice);

		jFrame.add(haut);

		JPanel bas = new JPanel();
		bas.setLayout(new GridLayout(1,2));

		bas.add(j);

		jtext = new JLabel("Tour : ",JLabel.CENTER);

		bas.add(jtext);
		jFrame.add(bas);

		jFrame.setVisible(true);
		
		
		state = new StateStarting(this);

	}

	public void addObservable(Observable o) {
	
		o.addObserver(this);
		
	}

	public void setState(StateViewCommand state) {
		this.state = state;
	}


	@Override
	public void update(Observable o, Object arg) {

		Game game = (Game)o;

		jtext.setText("Tour :"+ game.getTurn());

	}

	public StateViewCommand getState() {
		return state;
	}

}