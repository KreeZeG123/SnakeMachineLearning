package view;

public class StateWaiting implements StateViewCommand{

	ViewCommand view;
	
	
	public StateWaiting(ViewCommand view) {
		
		this.view = view;
		
		this.view.initChoice.setEnabled(true);
		this.view.stepChoice.setEnabled(true);
		this.view.playChoice.setEnabled(true);
		this.view.pauseChoice.setEnabled(false);
		
	}

	
	@Override
	public void clickRestart() {
		
		this.view.setState(new StateStarting(this.view));
		
	}

	@Override
	public void clickStep() {
		
		
	}

	@Override
	public void clickPlay() {
		
		
		this.view.setState(new StateRunning(this.view));
		
	}

	@Override
	public void clickPause() {
		
		
	}

}
