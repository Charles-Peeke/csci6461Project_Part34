import GUI.GUI;

public class Simulator {
	public static void main(String[] args) {
		// From oracle docs: Schedule a job for the event-dispatching thread: creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(() -> {
			GUI simulatorGUI = new GUI();
			simulatorGUI.startMachine();
		});
	}
}
