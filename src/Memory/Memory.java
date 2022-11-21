/**
 * CSCI 6461 - Fall 2022
 * 
 * Memory class handles the total memory and interaction with memory
 */

package Memory;

import Common.Common;

import java.util.Arrays;

public class Memory {

	private final int memSize = 2048;
	private final int[] memory;
	
    private int memoryFault = -1;
	private boolean runningUserProgram;

	public Memory() {
		memory = new int[memSize];
		// initialize the memory table
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
		store(0, 0);
		store(6, 1);
		store(0, 2);
		store(0, 3);
		store(0, 4);
		store(0, 5);
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
	public void store(int value, int location) {
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
	}

	public int load(int location) {
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

	public boolean getRunningUserProgram() {
		return runningUserProgram;
	}

	public void setRunningUserProgram(boolean running) {
		runningUserProgram = running;
	}

	public void reset() {
		Arrays.fill(memory, 0);
		setReservedLocations();
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
            if (i == Common.BOOT_PROGRAM_ADDRESS-1) { s += "\n"; }
            if (i == Common.USER_PROGRAM_OFFSET-1) {  s += "\n"; }
        }
        return s;
	}
}