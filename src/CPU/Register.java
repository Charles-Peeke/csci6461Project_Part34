package CPU;

import javax.swing.*;

import Common.Utilities;
import GUI.InputSwitches;

public class Register {

    // name of the register
    protected final String name;

    // amount of bits that the register holds
    protected final int length;

    // decimal value that the register holds
    protected int value;

    protected final boolean supportsNegatives;

    // used to tell whether the value represents a character or number --> for console printing
    protected boolean isChar;

    // Front-panel display components
    protected final JLabel label;
    protected final JTextField textField;
    protected final JButton load;

    protected final InputSwitches switches;


    public Register(String name, int length, InputSwitches switches, boolean supportsNegatives) {
        this.name = name;
        this.length = length;
        this.switches = switches;
        this.supportsNegatives = supportsNegatives;

        value = 0;
        isChar = false;
        label = new JLabel(name);
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
            // check to see if switch value is > # bits in register
            if (switches.getValue().length() > length) {
                System.out.println("Warning: switch length is greater than register length. Only setting first "
                        + length + " bits.");
            }
            // set the register value equal to the switch value
            String valueToSet = switches.getValue().substring(switches.getValue().length() - length);
            textField.setText(valueToSet);
            if (supportsNegatives) {
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
     * Sets the value of the register based on the inputted integer. Generally we want to use a signed binary string
     * for the number, but for some registers we don't want to do this(e.g. rs1, which has 2 bits and represents
     * decimal values 0-3). Because of this complexity, we first convert the passed input to a signed binary string.
     * Then, depending on if the register supports negative values, we either parse the string directly or use a 2s
     * complement notation to get the signed binary version of the original passed integer.
     *
     * Note that this process would be much easier if we used binary strings as values for everything instead of decimal,
     * (which an actual computer would do), but this would complicate the process in most other aspects of the machine
     * (memory, arithmetic operations, instructions, etc).
     * @param value -> the integer value to set the register to
     */
    public void setValue(int value) {
        isChar = false;
        String binary = Utilities.intToSignedBinary(value, length);

        if (supportsNegatives) {
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

    public String getBinaryStringValue() {
        return textField.getText();
    }

    public boolean isChar() {
        return isChar;
    }

    public JLabel getLabel() {
        return label;
    }

    public JTextField getTextField() {
        return textField;
    }

    public JButton getLoad() {
        return load;
    }

    public int getLength() {
        return length;
    }

    public String toString() {
        return name + ": " + length + " bits. Value: " + value;
    }
}