/**
 * CSCI 6461 - Fall 2022
 * 
 * Register Class controls the attrivutes needed for all registers (outside of floating point ones)
 */

package CPU;

import javax.swing.*;

import Common.Utilities;
import GUI.InputSwitches;

public class Register {

    // Name, Length (in bits), Value, and Flags
    protected final String name;
    protected final int length;
    protected int value;

    protected final boolean isSigned;
    protected boolean isChar;

    // GUI Components
    protected final JLabel label;
    protected final JTextField textField;
    protected final JButton load;
    protected final InputSwitches switches;

    public Register(String name, int length, InputSwitches switches, boolean supportsNegatives) {
        this.name = name;
        this.length = length;
        this.switches = switches;
        this.isSigned = supportsNegatives;

        value = 0;   isChar = false;   label = new JLabel(name);
        textField = new JTextField(length);
        textField.setName(name);
        textField.setText("0".repeat(length));
        textField.setEditable(false);
        textField.setHorizontalAlignment(JTextField.RIGHT);
        load = new JButton("Load");

        addLoadListener();
    }

    /**
     * Adds a listener to the load button that stores the value in the switches into the register
     */
    protected void addLoadListener() {
        load.addActionListener(ae -> {
            // Some registers can't hold all of the bits provided in the input switches
            if (switches.getValue().length() > length) {
                System.out.println("Warning: switch length is greater than register length. Only setting first "
                        + length + " bits.");
            }
            // Set the register value to whatever the switches represent
            String valueToSet = switches.getValue().substring(switches.getValue().length() - length);
            textField.setText(valueToSet);
            if (isSigned) {
                value = Utilities.signedBinaryToInt(valueToSet);
            } else {
                value = Integer.parseInt(valueToSet, 2);
            }
            System.out.println("setting " + name + " to value " + value);
        });
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    public char getChar() {
        return (char) value;
    }

    /**
     * Sets the value of the register based on the inputted integer. Must convert to binary for display/usage purposes
     * @param value -> the integer value to set the register to
     */
    public void setValue(int value) {
        isChar = false;
        String binary = Utilities.intToSignedBinary(value, length);

        if (isSigned) {
            this.value = Utilities.signedBinaryToInt(binary);
        } else {
            this.value = Integer.parseInt(binary, 2);
        }
        textField.setText(binary);
    }

    /**
     * Sets the register to a character
     * @param value -> the character to set the register value to (ascii values)
     */
    public void setValue(char value) {
        isChar = true;
        this.value = value;
    }

    public String getBinaryStringValue() { return textField.getText(); }

    public boolean isChar() { return isChar; }

    public JLabel getLabel() { return label; }

    public JTextField getTextField() { return textField; }

    public JButton getLoad() { return load; }

    public int getLength() { return length; }

    public String toString() { return name + ": " + length + " bits. Value: " + value; }
}