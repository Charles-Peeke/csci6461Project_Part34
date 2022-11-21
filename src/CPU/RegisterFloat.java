/**
 * CSCI 6461 - Fall 2022
 * 
 * Register Float extends a register to also support floating point values and operations
 */

package CPU;

import Common.Utilities;
import GUI.InputSwitches;

public class RegisterFloat extends Register {

    // Nothing extra during register creation compared to all other registers
    public RegisterFloat(String name, int length, InputSwitches switches, boolean supportsNegatives) {
        super(name, length, switches, supportsNegatives);
    }

    // Returns the exponent portion of the register (only should be used for floating point registers)
    public int getExponent() {
        if (length != 16) { return 0; }
        // bits 1-7 represent the (signed) exponent
        return Utilities.signedBinaryToInt(getBinaryStringValue().substring(1, 8));
    }

    /**
     * Sets the exponent portion of a floating point register to the specified value
     * @param value - the value to set the exponent to
     */
    public void setExponent(int value) {
        String binaryValue = Integer.toBinaryString(value);
        String oldVal = getBinaryStringValue();
        String newVal = oldVal.charAt(0) + binaryValue + oldVal.substring(8);
        setValue(Utilities.signedBinaryToInt(newVal));
    }

    // Get the beginning part of the register
    public int getSignAndExponent() {
        // bit 0 is signed; bits 1-7 represent the (signed) exponent
        return Utilities.signedBinaryToInt(getBinaryStringValue().substring(0, 8));
    }

    // The bulk of the value of the register. This is used with exponents and sign
    public int getMantissa() {
        if (length != 16) { return 0; }
        return Integer.parseInt(getBinaryStringValue().substring(8), 2);
    }

    /**
     * Sets the mantissa portion of a floating point register to the specified value
     * @param value - the value to set the mantissa to
     */
    public void setMantissa(int value) {
        String binaryValue = Utilities.intToSignedBinary(value, 8);
        String oldVal = getBinaryStringValue();
        String newVal = oldVal.substring(0, 8) + binaryValue;
        setValue(Utilities.signedBinaryToInt(newVal));
    }

    public int getSign() { return getBinaryStringValue().charAt(0); }

    // Calculate the Register Value from the mantissa, exponent, and sign
    public double getFloatingPointValue() {
        int mantissa = getMantissa();
        int exponent = getExponent();
        double result = mantissa * Math.pow(2, exponent);
        if (getSign() == 1) { result *= -1; }
        return result;
    }
}
