package view;

public class StateRunning implements StateViewCommand{

	ViewCommand view;
	
	
	public StateRunning(ViewCommand view) {
		
		this.view = view;
	
		this.view.initChoice.setEnabled(false);
		this.view.stepChoice.setEnabled(false);
		this.view.playChoice.setEnabled(false);
		this.view.pauseChoice.setEnabled(true);
		
	}


	@Override
	public void clickRestart() {
		

	}


	@Override
	public void clickStep() {
		
		
	}


	@Override
	public void clickPlay() {
		
		
	}


	@Override
	public void clickPause() {
		this.view.setState(new StateWaiting(this.view));
		
	}
	

	
	

}
