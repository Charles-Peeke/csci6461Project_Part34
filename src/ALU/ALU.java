/**
 * CSCI 6461 - Fall 2022
 * 
 * ALU Class controls the coded logic behind an
 * Arithmetic Logic Unit found in a CPU
 */

package ALU;

import javax.swing.*;
import CPU.Register;
import Common.Utilities;
import java.awt.*;
import java.util.Arrays;

public class ALU {
    private final int[] condCodes;
    private final JLabel[] condCodeLabels;
    private final JLabel[] condCodeValues;

    public ALU() {
        condCodes = new int[4];
        condCodeLabels = new JLabel[4];
        condCodeValues = new JLabel[4];
    }

    /**
     * Add register with value
     * 
     * @param result    the register to store the result into
     * @param regAdd    the register to add with
     * @param value     the value to add
     */
    public void add(Register result, Register regAdd, int value) {
        // add the values, store in register
        result.setValue(regAdd.getValue() + value);
        System.out.println("ADDITION: " + regAdd.getValue() + "\t" + value);


        if (result.getValue() < 0) {
            // pos + pos = neg, so must have overflown
            if (regAdd.getValue() > 0 && value > 0) {
                condCodes[0] = 1;
            }
        } else {
            // neg + neg = pos, so underflow
            if (regAdd.getValue() < 0 && value < 0) {
                condCodes[1] = 1;
            }
        }
        System.out.println("Result of add: " + result.getValue());
    }

    /**
     * Subtract value from register
     * 
     * @param result    the register to store the result into
     * @param in     the register to use
     * @param value  the value to subtract
     */
    public void subtract(Register result, Register in, int value) {
        // add the values, store in register
        result.setValue(in.getValue() - value);
        System.out.println("SUBTRACT: " + in.getValue() + "\t" + value);


        if (result.getValue() < 0) {
            // pos - neg = neg, so must have overflown
            if (in.getValue() > 0 && value < 0) {
                condCodes[0] = 1;
            }
        } else {
            // neg - pos = pos, so must have underflown
            if (in.getValue() < 0 && value > 0) {
                condCodes[1] = 1;
            }
        }
        System.out.println("Result of subtract: " + result.getValue());
    }

    /**
     * Multiply Register rx by Register ry. The high order bits of the result are
     * stored in rx, while the
     * low order bits of the result are stored in rxPlusOne. Sets overflow/underflow
     * accordingly.
     *
     * @param rx        - first input for the multiplication. stores the high
     *                  order bits
     * @param ry        - second input for the multiplication
     * @param carryRegister - used to store the low order bits.
     */
    public void multiply(Register rx, Register ry, Register carryRegister) {
        int result = rx.getValue() * ry.getValue();
        // use register to handle the int to signed binary conversion
        Register r = new Register("r", 16, null, true);
        r.setValue(result);

        if (r.getValue() < 0) {
            // pos*pos=neg, so must have overflown
            if (rx.getValue() > 0 && ry.getValue() > 0) {
                condCodes[0] = 1;
            } else if (rx.getValue() < 0 && ry.getValue() < 0) { // neg*neg=neg, so must have overflown
                condCodes[0] = 1;
            }
        } else {
            // neg*pos=pos, must have underflown
            if (rx.getValue() < 0 && ry.getValue() > 0) {
                condCodes[1] = 1;
            } else if (rx.getValue() > 0 && ry.getValue() < 0) {
                condCodes[1] = 1;
            }
        }
        String binary = r.getBinaryStringValue();
        System.out.println("Result of mult: " + r.getValue() + ". Binary string: " + binary);
        String highOrderBits = binary.substring(0, 8);
        String lowOrderBits = binary.substring(8);
        int rxVal = Integer.parseInt(highOrderBits, 2);
        System.out.println("setting " + rx.getName() + " to a value of " + rxVal);
        rx.setValue(rxVal);

        int rx1Val = Integer.parseInt(lowOrderBits, 2);
        System.out.println("setting " + carryRegister.getName() + " to a value of " + rx1Val);
        carryRegister.setValue(rx1Val);
    }

    /**
     * Divide Register rx by Register ry. rx stores the quotient of the division,
     * rxPlusOne stores the remainder of
     * the division. If ry is 0, the divide by zero cc bit is set.
     *
     * @param rx        - numerator of the division. Also stores the quotient of
     *                  the division
     * @param ry        - denominator of the division
     * @param rxPlusOne - stores the remainder of the division.
     */
    public void divide(Register rx, Register ry, Register rxPlusOne) {
        // set DIVZERO cc bit
        if (ry.getValue() == 0) {
            condCodes[2] = 1;
            System.out.println("Divide by 0");
            return;
        }

        int remainder = rx.getValue() % ry.getValue();
        int quotient = rx.getValue() / ry.getValue();

        System.out.println("Quotient of divide: " + quotient);
        System.out.println("Remainder of divide: " + remainder);

        // set rx and rx+1 here
        rx.setValue(quotient);
        rxPlusOne.setValue(remainder);
    }

    /**
     * Checks if the two registers have the same value. If they do, cc[3] is set to
     * 1.
     * Otherwise, cc[3] is set to 0
     * 
     * @param rx  the first register to check
     * @param ry  the second register to check
     */
    public void testEquality(Register rx, Register ry) {
        if (rx.getValue() == ry.getValue()) {
            condCodes[3] = 1;
        } else {
            condCodes[3] = 0;
        }
        System.out.println("EqualOrNot[3] " + condCodes[3]);
    }

    /**
     * Performs a logical and of the two registers
     * 
     * @param rx  the first register of the operation
     * @param ry  the second register of the operation
     * @return  the integer value of the operation
     */
    public int logicalAnd(Register rx, Register ry) {
        String rxBinary = rx.getBinaryStringValue();
        String ryBinary = ry.getBinaryStringValue();

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < rxBinary.length(); i++) {
            char rxChar = rxBinary.charAt(i);
            char ryChar = ryBinary.charAt(i);
            if (rxChar == '1' && ryChar == '0') {
                sb.append('0');
            } else if (rxChar == '0' && ryChar == '1') {
                sb.append('0');
            } else if (rxChar == '0' && ryChar == '0') {
                sb.append('0');
            } else {
                sb.append('1');
            }
        }
        int result = Utilities.signedBinaryToInt(sb.toString());
        System.out.println("Logical and result: " + result);
        return result;
    }

    /**
     * Performs a logical or of the two registers
     * 
     * @param rx  the first register of the operation
     * @param ry  the second register of the operation
     * @return  the integer value of the operation
     */
    public int logicalOr(Register rx, Register ry) {
        String rxBinary = rx.getBinaryStringValue();
        String ryBinary = ry.getBinaryStringValue();

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < rxBinary.length(); i++) {
            char rxChar = rxBinary.charAt(i);
            char ryChar = ryBinary.charAt(i);
            if (rxChar == '1' && ryChar == '0') {
                sb.append('1');
            } else if (rxChar == '0' && ryChar == '1') {
                sb.append('1');
            } else if (rxChar == '0' && ryChar == '0') {
                sb.append('0');
            } else {
                sb.append('0');
            }
        }

        int result = Utilities.signedBinaryToInt(sb.toString());
        System.out.println("Logical Or result: " + result);
        return result;
    }

    /**
     * Performs a logical not of the inputted register
     * 
     * @param rx  the first register of the operation
     * @return  the integer value of the operation
     */
    public int logicalNot(Register rx) {
        int count = 0;
        String rBinary = rx.getBinaryStringValue();
        StringBuilder sb = new StringBuilder();

        while (count != rBinary.length()) {
            char x = rBinary.charAt(count);
            if (x == '1') {
                x = '0';
                sb.append(x);
                count++;
            } else {
                x = '1';
                sb.append(x);
                count++;
            }
        }
        int result = Utilities.signedBinaryToInt(sb.toString());
        System.out.println("Logical Not result: " + result);
        return result;
    }

    /**
     * Shifts a register left or right arithmatically or logically.
     *
     * @param r     - the register to shift
     * @param count - the amount to shift the count by [0-15]
     * @param LR    - if LR = 1, shift left; else shift right
     * @param AL    - if AL = 1, do a logical shift. Else do arithmetic shift
     * @return - the result of the shift
     */
    public int shift(Register r, int count, int LR, int AL) {
        if (count == 0) {
            return r.getValue();
        }

        String registerBinary = r.getBinaryStringValue();
        String result;

        // logical shift
        if (AL == 1) {
            String extraZeroes = "0".repeat(count);
            // left shift; add 0s to end
            if (LR == 1) {
                String shifted = registerBinary.substring(count);
                result = shifted + extraZeroes;
            } else {
                // right shift; add 0s to start
                String shifted = registerBinary.substring(0, r.getLength() - count);
                result = extraZeroes + shifted;
            }
        } else {
            // arithmetic shift

            // left shift
            if (LR == 1) {
                String extraZeroes = "0".repeat(count);
                String shifted = registerBinary.substring(count);
                result = shifted + extraZeroes;

                // check if bits overflowed
                if (Integer.parseInt(registerBinary.substring(0, count), 2) > 0) {
                    condCodes[0] = 1;
                }
            } else {
                // right shift
                // extend the sign bit
                String extendedSignBits = Character.toString(registerBinary.charAt(0)).repeat(count);
                // drop the right-most bits that will be lost by the shift
                String shifted = registerBinary.substring(0, r.getLength() - count);
                result = extendedSignBits + shifted;

                // check if bits underflowed
                if (Integer.parseInt(registerBinary.substring(r.getLength() - count), 2) > 0) {
                    condCodes[1] = 1;
                }
            }
        }
        System.out.println("Shift result: " + result);
        return Utilities.signedBinaryToInt(result);
    }

    /**
     * Returns the condition code at the specified bit
     * 
     * @param index - which bit in the condition code to return
     * @return  the value of the cc bit (either 0 or 1)
     */
    public int getCc(int index) {
        if (index < 0 || index > 3) {
            return 0;
        }
        return condCodes[index];
    }

    /**
     * Sets the cc at the specified index to the specified value
     * 
     * @param index - the index of the cc bits to set (range [0, 3])
     * @param val   - the value to set the cc bit to (range [0, 1])
     */
    public void setCc(int index, int val) {
        if (index < 0 || index > 3 && val != 0 && val != 1) {
            return;
        }
        condCodes[index] = val;
    }

    /**
     * Adds the condition code bits to the front panel
     *
     * @param mainPanel - the panel to add the condition code bits to
     */
    public void addConditionCodeBits(JPanel mainPanel) {
        condCodeLabels[0] = new JLabel("0 - OverFlow");
        condCodeLabels[1] = new JLabel("1 - UnderFlow");
        condCodeLabels[2] = new JLabel("2 - Divide by Zero");
        condCodeLabels[3] = new JLabel("3 - EqualOrNot");

        for (int i = 0; i < condCodeValues.length; i++) {
            condCodeValues[i] = new JLabel("" + condCodes[i]);
            Utilities.addComponent(condCodeLabels[i], mainPanel, 10, 4 + i, 1, GridBagConstraints.LINE_START);
            Utilities.addComponent(condCodeValues[i], mainPanel, 11, 4 + i, 1);
        }
    }

    /**
     * Updates the front panel displays for the cc bits
     */
    public void updateCcDisplays() {
        for (int i = 0; i < condCodeValues.length; i++) {
            condCodeValues[i].setText("" + condCodes[i]);
        }
    }

    /**
     * Resets all the condition codes to 0
     */
    public void reset() {
        Arrays.fill(condCodes, 0);
    }
}
