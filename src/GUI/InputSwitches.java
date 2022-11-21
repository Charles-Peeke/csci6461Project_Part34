/**
 * CSCI 6461 - Fall 2022
 * 
 * Input Switches Class handles the GUI and Functionality of the switches across the top of the simulator
 */

package GUI;

import javax.swing.*;

import Common.Utilities;

import java.awt.*;
import java.awt.event.*;

public class InputSwitches {

    // GUI Panel, Buttons, and Values
	private final JPanel switchPanel;
	private JToggleButton[] switches;
	private String switchValue;

    public InputSwitches() {
		switchPanel = new JPanel(new GridBagLayout());
		switchPanel.setPreferredSize(new Dimension(400, 100));
		addSwitches();
    }

	/**
	 * Returns the panel used to store the switches
	 * @return --> A JPanel that holds the switches
	 */
    public JPanel getPanel() {
        return switchPanel;
    }

	/**
	 * Resets the switch values to 0
	 */
    public void reset() {
		switchValue = "0".repeat(16);
		for (JToggleButton button: switches) {
			button.setSelected(false);
		}
    }

	/**
	 * Adds the switches used to load values into registers and memory to the panel.
	 */
	private void addSwitches() {
		switchValue = "0".repeat(16);
		switches = new JToggleButton[16];
		for (int i = 0; i < switches.length; i++) {
			String text = "" + i;
			JToggleButton button = new JToggleButton(text);
			button.addItemListener (e -> {
				int state = e.getStateChange();
				int index = switches.length - Integer.parseInt(button.getText()) - 1;
				// Update the switch value string depending on if the switch was toggled on/off
				if (state == ItemEvent.SELECTED) {
					switchValue = switchValue.substring(0, index) + 1 + switchValue.substring(index + 1);
				} else {
					switchValue = switchValue.substring(0, index) + 0 + switchValue.substring(index + 1);
				}
			});
			switches[i] = button;
			Utilities.addComponent(button, switchPanel, i, 0, 1);
		}
		// Add Labels of the total switch Instruction
		Utilities.addComponent(new JLabel("Operation"), switchPanel, 0, 2, 6); 
		Utilities.addComponent(new JLabel("GPR"), switchPanel, 6, 2, 2);
		Utilities.addComponent(new JLabel("IXR"), switchPanel, 8, 2, 2);
		Utilities.addComponent(new JLabel("I"), switchPanel, 10, 2, 1);
		Utilities.addComponent(new JLabel("Address"), switchPanel, 11, 2, 5);
	}

	/**
	 * Gets the string value of the switches
	 * @return -> a String representation of the switches
	 */
	public String getValue() {
		return switchValue;
	}

	/**
	 * Sets the value of the switches to replicate the string passed in.
	 * Not really used for anything other than displaying the current instruction being executed.
	 *
	 * @param value -> the binary string value to set the switches to
	 */
	public void setSwitchValue(String value) {
		if (value.length() != switches.length) { return; }
		for (int i = 0; i < value.length(); i++) {
			switches[i].setSelected(value.charAt(i) == '1');
		}
		switchValue = value;
	}

}