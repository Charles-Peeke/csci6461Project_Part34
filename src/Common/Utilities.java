package Common;

import javax.swing.*;
import java.awt.*;

public class Utilities {

	public Utilities() {}

	/**
	 * Converts an int to a signed binary based on the number of bits
	 *
	 * @param value -> the int to convert
	 * @param bits -> the amount of bits used to represent the value
	 * @return -> a signed binary string of length bits representing the value
	 */
	public static String intToSignedBinary(int value, int bits) {
		String binary = Integer.toBinaryString(value);

		// just use the last x bits
		if (binary.length() > bits) {
			binary = binary.substring(binary.length() - bits);
		} else {
			int missingBits = bits - binary.length();
			String extendedSignBits;
			if (value < 0) {
				extendedSignBits = "1".repeat(missingBits);
			} else {
				extendedSignBits = "0".repeat(missingBits);
			}
			binary = extendedSignBits + binary;
		}
		return binary;
	}

	/**
	 * Convert string to int, assuming it is signed (2's complement notation)
	 * @param s -> the binary string to be converted
	 * @return -> the value of the binary string
	 */
	public static int signedBinaryToInt(String s) {
		if (s.length() == 1) {
			return Integer.parseInt(s);
		}
		String withoutSignedBit = s.substring(1);
		int val = Integer.parseInt(withoutSignedBit, 2);
		// positive number, so done
		if (s.charAt(0) == '0') {
			return val;
		}

		// flip the bits
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < withoutSignedBit.length(); i++) {
			if (withoutSignedBit.charAt(i) == '0') {
				sb.append('1');
			} else {
				sb.append('0');
			}
		}
		int unsignedVal = Integer.parseInt(sb.toString(), 2);
		return -(unsignedVal + 1);
	}

	public static String intToUnsignedBinary(int value, int length) {
		String binary = Integer.toBinaryString(value);
		String result;
		if (binary.length() > length) {
			result = binary.substring(0, length);
		} else if (binary.length() < 8) {
			result = "0".repeat(length-binary.length()) + binary;
		} else {
			result = binary;
		}
		return result;
	}

	public static int floatToFixed(int signAndExponent, int mantissa) {
		String s = Utilities.intToSignedBinary(signAndExponent, 8);
		int sign = Integer.parseInt(s.substring(0, 1));
		int exponent = signedBinaryToInt(s.substring(1));

		if (sign == 1) {
			mantissa *= -1;
		}
		return (int) (mantissa * Math.pow(2, exponent));
	}

	public static int fixedToFloat(int value) {
		String sign;
		if (value < 0) {
			sign = "1";
		} else {
			sign = "0";
		}
		String signAndExponent = sign + "0000000";
		String combined = signAndExponent + Utilities.intToUnsignedBinary(Math.abs(value), 8);
		return Utilities.signedBinaryToInt(combined);
	}

	/**
	 * Generalized method to add a component to the specified panel using the GridBagLayout.
	 * 
	 * @param comp - the java swing component to be added.
	 * @param panel - the panel to add the component to.
	 * @param x - the x value of the grid in the GridBagLayout.
	 * @param y - the y value of the grid in the GridBagLayout.
	 * @param width - the width of the component (i.e. how many grid spaces it spans)
	 * @param anchor - the GridBagConstraints anchor that specifies where to put the component in the grid.
	 */
	public static void addComponent(Component comp, JPanel panel, int x, int y, int width, int anchor) {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = x;
		c.gridy = y;
		c.gridwidth = width;
		c.anchor = anchor;
		c.insets = new Insets(10, 0, 0, 10);
		panel.add(comp, c);
	}

	/**
	 * Generalized method to add a component to the specified panel using the GridBagLayout.
	 * 
	 * @param comp - the java swing component to be added.
	 * @param panel - the panel to add the component to.
	 * @param x - the x value of the grid in the GridBagLayout.
	 * @param y - the y value of the grid in the GridBagLayout.
	 * @param width - the width of the component (i.e. how many grid spaces it spans)
	 * @param insets - the insets used to add padding to the component.
	 */
	public static void addComponent(Component comp, JPanel panel, int x, int y, int width, Insets insets) {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = x;
		c.gridy = y;
		c.gridwidth = width;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = insets;
		panel.add(comp, c);
	}

	/**
	 * Generalized method to add a component to the specified panel using the GridBagLayout.
	 * 
	 * @param comp - the java swing component to be added.
	 * @param panel - the panel to add the component to.
	 * @param x - the x value of the grid in the GridBagLayout.
	 * @param y - the y value of the grid in the GridBagLayout.
	 * @param width - the width of the component (i.e. how many grid spaces it spans)
	 */
	public static void addComponent(Component comp, JPanel panel, int x, int y, int width) {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = x;
		c.gridy = y;
		c.gridwidth = width;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(10, 0, 0, 10);
		panel.add(comp, c);
	}

	/**
	 * Generalized method to add a component to the specified panel using the GridBagLayout.
	 *
	 * @param comp - the java swing component to be added.
	 * @param panel - the panel to add the component to.
	 * @param x - the x value of the grid in the GridBagLayout.
	 * @param y - the y value of the grid in the GridBagLayout.
	 * @param width - the width of the component (i.e. how many grid spaces it spans)
	 * @param height - the height of the component (i.e. how many grid spaces it spans)
	 * @param anchor - the GridBagConstraints anchor that specifies where to put the component in the grid.
	 */
	public static void addComponent(Component comp, JPanel panel, int x, int y, int width, int height, int anchor) {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = x;
		c.gridy = y;
		c.gridwidth = width;
		c.gridheight = height;
		c.anchor = anchor;
		c.insets = new Insets(10, 0, 0, 10);
		panel.add(comp, c);
	}

}