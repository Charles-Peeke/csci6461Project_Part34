package Memory;

import javax.swing.*;

import Common.Common;
import Common.Utilities;

import java.util.Arrays;

public class Memory {

	private final int memSize = 2048;
	private final int[] memory;

	// indicates if there was a memory fault if value != -1
	private int memoryFault = -1;

	// whether a user program is running or not; affects memory inserts/retrieval
	private boolean runningUserProgram;

	// Table to display memory indexes and values
	private JTable memoryTable;
	// Data for the memory table
	private final Object[][] memoryData;

	public Memory() {
		memory = new int[memSize];
		memoryData = new Object[memSize][4];
		// initialize the memory table
		createMemoryTable();
		setReservedLocations();
		runningUserProgram = false;
	}

	public void setReservedLocations() {
		// Reserved memory contents:
		// 0:	Reserved for TRAP instruction
		// 1:	Reserved for PC fault
		// 2:	Store PC for TRAP
		// 3: 	Not used
		// 4:	Store PC for machine fault
		// 5:	Not used

		// On a fault, PC will be loaded with address at 1 (6), which executes a halt to view the fault
		insertDirect(0, 0);
		insertDirect(6, 1);
		insertDirect(0, 2);
		insertDirect(0, 3);
		insertDirect(0, 4);
		insertDirect(0, 5);
	}
	/**
	 * Converts a hex string to an integer
	 *
	 * @param input - the hex string to be converted
	 * @return - the integer value of the hex string
	 */
	public int hexToDec(String input) {
		try {
			return Integer.parseInt(input, 16);
		}
		catch(NumberFormatException ne){
			// todo: fault
			System.out.println("hexToDec: invalid input string");
		}
		return -1;
	}

	/**
	 * Inserts a word into memory at the specified location
	 *
	 * @param value - the value to be inserted
	 * @param location - the location in memory to insert the value into
	 */
	public void insertDirect(int value, int location) {
		if (location < 0 || location > memSize) {
			if (location < 0) {
				memoryFault = Common.ILLEGAL_MEMORY_ADDRESS_RESERVED_LOCATION;
			} else {
				memoryFault = Common.ILLEGAL_MEMORY_ADDRESS_OUT_OF_BOUNDS;
			}
			System.out.println("[Error] Insert value into memory " + location + ": illegal location");
			return;
		}
		memory[location] = value;

		// update the memory data for display purposes
		memoryData[location][2] = Utilities.intToSignedBinary(value, 16);
		memoryData[location][3] = value;
		memoryTable.repaint();
	}

	/**
	 * Inserts a word into memory at the specified location
	 *
	 * If a user program is running, we want to offset the location because we don't want users inserting at
	 * restricted memory locations. So when a user program inserts at location 0, they are actually inserting
	 * at location 0 + user program offset.
	 * @param value - the value to be inserted
	 * @param location - the location in memory to insert the value into
	 */
	public void insert(int value, int location) {
		if (runningUserProgram) {
			location += Common.USER_PROGRAM_OFFSET;
		}
		if (location < 0 || location > memSize) {
			if (location < 0) {
				memoryFault = Common.ILLEGAL_MEMORY_ADDRESS_RESERVED_LOCATION;
			} else {
				memoryFault = Common.ILLEGAL_MEMORY_ADDRESS_OUT_OF_BOUNDS;
			}
			System.out.println("[Error] Insert value into memory " + location + ": illegal location");
			return;
		}
		memory[location] = value;

		// update the memory data for display purposes
		memoryData[location][2] = Utilities.intToSignedBinary(value, 16);
		memoryData[location][3] = value;
		memoryTable.repaint();
	}

	/**
	 * Gets a value in memory at the specified location.
	 *
	 * @param location - an integer specifying the memory location
	 * @return - the value in memory at the specified location + user offset
	 */
	public int getDirect(int location) {
		if (location < 0 || location > memSize) {
			if (location < 0) {
				memoryFault = Common.ILLEGAL_MEMORY_ADDRESS_RESERVED_LOCATION;
			} else {
				memoryFault = Common.ILLEGAL_MEMORY_ADDRESS_OUT_OF_BOUNDS;
			}
			System.out.println("[Error] Get value into memory " + location + ": illegal location");
			return 0;
		}
		return memory[location];
	}

	/**
	 * Gets a value in memory at the specified location.
	 *
	 * If a user program is running, we want to offset the location because we don't want users getting
	 * values at restricted memory locations. So when a user program retrieves the value at location 0,
	 * they are actually getting value at location 0 + user program offset.
	 * @param location - an integer specifying the memory location
	 * @return - the value in memory at the specified location + user offset
	 */
	public int get(int location) {
		if (runningUserProgram) {
			location += Common.USER_PROGRAM_OFFSET;
		}

		if (location < 0 || location > memSize) {
			if (location < 0) {
				memoryFault = Common.ILLEGAL_MEMORY_ADDRESS_RESERVED_LOCATION;
			} else {
				memoryFault = Common.ILLEGAL_MEMORY_ADDRESS_OUT_OF_BOUNDS;
			}
			System.out.println("[Error] Get value into memory " + location + ": illegal location");
			return 0;
		}
		return memory[location];
	}

	public JTable getMemoryTable() {
		return memoryTable;
	}

	/**
	 * Populates the data in the memory table --> only to be used for display purposes
	 * when viewing memory contents.
	 */
	private void createMemoryTable() {
		String[] columnNames = {
				"Actual Location", "\"User\" Location", "Binary Value", "Decimal value"
		};
		setMemoryData();

		memoryTable = new JTable(memoryData, columnNames);
		memoryTable.setDefaultEditor(Object.class, null);
		memoryTable.setFillsViewportHeight(true);
	}

	/**
	 * Set the memory display data based on the memory values
	 */
	private void setMemoryData() {
		for (int i = 0; i < memory.length; i++) {
			memoryData[i][0] = i;
			if (i >= Common.USER_PROGRAM_OFFSET) {
				memoryData[i][1] = i - Common.USER_PROGRAM_OFFSET;
			} else {
				memoryData[i][1] = "";
			}
			memoryData[i][2] = Utilities.intToSignedBinary(memory[i], 16);
			memoryData[i][3] = memory[i];
		}
	}

	public boolean getRunningUserProgram() {
		return runningUserProgram;
	}

	public void setRunningUserProgram(boolean running) {
		runningUserProgram = running;
		memoryTable.repaint();
	}

	public void resetUserMemory() {
		for (int i = Common.USER_PROGRAM_OFFSET; i < memory.length; i++) {
			memoryData[i][2] = Utilities.intToSignedBinary(0, 16);
			memoryData[i][3] = 0;
		}
		memoryTable.repaint();
	}

	/**
	 * Reset the memory: actual memory and display values
	 */
	public void reset() {
		Arrays.fill(memory, 0);
		setReservedLocations();
		setMemoryData();
		memoryFault = -1;
	}

	public int getMemoryFault() {
		return memoryFault;
	}

	public String getMemoryString() {
        String s = "Memory:\n";
        for (int i = 0; i < memory.length; i++) {
            if (memory[i] != 0) {
                s += "" + i + ":" + memory[i] + "\t";
            }
            if (i == Common.BOOT_PROGRAM_ADDRESS-1) {
                s += "\n";
            }
            if (i == Common.USER_PROGRAM_OFFSET-1) {
                s += "\n";
            }
        }
        return s;
		// return "Memory: " + Arrays.toString(memory);
	}
}