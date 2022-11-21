/**
 * CSCI 6461 - Fall 2022
 * 
 * CPU Class controls the coded logic between
 * the ALU, the Registers, the Instructions, and Memory
 * 
 * Consider this the 'brain' of the overall simulator
 */

package CPU;

import javax.swing.*;

import ALU.ALU;
import Common.Common;
import Common.Utilities;
import GUI.*;
import Memory.*;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CPU {

	// Simulator GUI Panel
	private final JPanel mainPanel;
	private Thread runThread;

	// Memory Variable
	private final Memory Memory;	private final Cache Cache;

	// ALU 
	private final ALU ALU;

	// User Input 
	private final InputSwitches InputSwitches;
	private JTextArea DevConsole;	private JTextField InputText;

	// General Purpose Registers
	private Register GPR0;	private Register GPR1;
	private Register GPR2;	private Register GPR3;

	// Index Registers
	private Register IX1;	private Register IX2;	private Register IX3;
    
    // Floating Registers 
	private RegisterFloat R0;	private RegisterFloat R1;

	// Other Registers
	private Register RX;	private Register RY;
	
	// Program Counter
	private Register PC;

	// Memory Registers
	private Register MAR;	private Register MARMem;	private Register MBR;

	// Instruction Register
	private Register IR;

	// Memory Fault Register
	private Register MFR;

	// Internal address register
	private Register IAR;	private JLabel IARLabel;	
	// Internal result register
	private Register IRR;	private JLabel IRRLabel;
	// Register select 1
	private Register RS1;	private JLabel RS1Label;

	//Run and Halt Buttons
	private JButton run;
	private JButton singleStep;
	private JToggleButton halt;
	private boolean paused = false;

	// Internal flags
	private String targetLocation = "";
	private String registerType = "";

	// Next PC location
	private int nextPc;
	
	// Instruction Variables
	private Instruction currentInstruction;	private Instruction lastInstruction;	
    private JLabel currentInstructionDisplay;

	private int OPCode;	private int ix;	private int GPRSelect;
	private int IndirectFlagg;	private int memoryLocation;
	private int ShiftLeft;	private int ShiftRight;	private int Count;
	private boolean useIxi = true;

    // Flags for Offsets / Memory allocations
	private boolean program1 = false;
	private boolean runningBoot = false;

	
	public CPU(JPanel mainPanel, Memory memory, InputSwitches switches) {
        // Variable Creations
		this.mainPanel = mainPanel;
		this.Memory = memory;
		this.InputSwitches = switches;
		ALU = new ALU();
		Cache = new Cache();

        // Initializing Functions
		addGeneralPurposeRegisters();
		addIndexRegisters();		addFloatRegisters();
		addPC();		addMAR();		addMBR();
		addIR();		addMFR();		addRunHalt();
		addInternalRegisters();		addRxRy();
		addIODevices();
		addCurrentInstructionDisplay();
		ALU.addConditionCodeBits(mainPanel); // Display Purposes

		addListeners();

		// Create Thread to prevent Infinite Looping
		createRunThread();
	}

	public void boot() {
		// Boot Program Offset
		PC.setValue(Common.BOOT_PROGRAM_ADDRESS);
		loadBootProgram();
		runningBoot = true;
		// Run the Program
		while(runningBoot) {
			singleInstructionCycle();
		}
		Memory.setReservedLocations();
	}

	public void handleProgramTermination() {
		Memory.setRunningUserProgram(false);
		DevConsole.append("\nProgram finished.\nUse the IPL button.\n");
		setHalted(false);		paused = true;
	}

	/**
	 * Executes one full instruction cycle: 
     * * Fetch
     * * Decode
     * * Execute
	 */
	public void singleInstructionCycle() {
		fetchInstruction();
		decodeInstruction();
		fetchOperand();
		execute();
        
        // Special Case for Halt
		if (!halt.isSelected()) {
			depositResults();
			nextInstruction();
			resetFlags();
			updateInternalDisplays();
		}
	}

	/**
	 * Instruction Cycle - Step 1 - Fetch Instruction
	 * 
	 * This transfers the address of the next instruction (indicated by the PC) to the MAR, 
	 * and loads the MAR into the MBR.
	 */
	private void fetchInstruction() {
		// Store PC in MAR
		MAR.setValue(PC.getValue());
		// Fetch the word in memory if not in cache at the MAR and store it in the MBR
		
		if(Cache.inCache(PC.getValue())) {
			System.out.println("Address is in cache");
		
			MBR.setValue(Cache.getData(PC.getValue())); // assume key+value is unified
			
		}else {
			System.out.println("Address is NOT in cache, so retrieve from Memory and add to Cache");
			int data = Memory.load(PC.getValue());

			MBR.setValue(data);
		}

		nextPc = PC.getValue() + 1;
	}

	/**
	 * Instruction Cycle - Step 2 - Decode Instruction
	 * 
	 * This transfers the address of the next instruction (indicated by the PC) to the MAR, 
	 * and loads the MAR into the MBR.
	 */
	private void decodeInstruction() {
		// store MBR into IR
		IR.setValue(MBR.getValue());

		// process the instruction and use it to set several flags
		parseInstruction();

		RS1.setValue(GPRSelect);

		lastInstruction = currentInstruction;
		currentInstruction = Instruction.getInstruction(OPCode);
		// update the front panel display
		updateCurrentInstructionDisplay();

		if (currentInstruction != null) {
			currentInstruction.decode(this);
			//System.out.println("Instruction is " + currentInstruction);
		} else {
			System.out.println("Error decoding instruction: unknown opcode: " + OPCode + " (decimal).");
			handleMachineFault(Common.ILLEGAL_OPERATION_CODE);
		}
	}

	/**
	 * Instruction Cycle - Step 2a - Operation
	 * 
	 */
	private void fetchOperand() {
		IAR.setValue(memoryLocation);

		if(useIxi) {
			if (!registerType.equals(Common.IXR)) {
				if (ix != 0) {
					IAR.setValue(IAR.getValue() + selectIxr(ix).getValue());
					System.out.println("Iar after indexing: " + IAR.getValue());
				}
			}
			if (IndirectFlagg == 1) {
				System.out.println("Indirect addressing");
				IAR.setValue(Memory.get(IAR.getValue()));
			}
		}
		MAR.setValue(IAR.getValue());
		MBR.setValue(Memory.get(MAR.getValue()));

        // Fault Checking
		int fault = Memory.getMemoryFault();
		if (fault != -1) {
			handleMachineFault(fault);
		}
	}

	/**
	 * Instruction Cycle - Step 3 - Execute
	 * 
	 */
	private void execute() {
		if (currentInstruction != null) {
			currentInstruction.execute(this);
		} else {
			// Fault Checking
			System.out.println("Error executing instruction: unknown opcode: " + OPCode + " (decimal).");
			handleMachineFault(Common.ILLEGAL_OPERATION_CODE);
		}
	}

	/**
	 * Instruction Cycle - Post Operation
	 * Save Results to Memory or Registers
	 */
	private void depositResults() {
		//logger info for debugging
		// System.out.println("irr: " + irr.getValue());
		// System.out.println("iar: " + iar.getValue());

		if (targetLocation.equals(Common.REGISTER)) {
			System.out.println("Loading value " + IRR.getValue() + " into " + registerType + RS1.getValue());
			// use register select 1 to store irr contents into the specified register
			if (registerType.equals(Common.GPR)) {
				Register r = selectGpr(RS1.getValue());
				r.setValue(IRR.getValue());
				System.out.println("Storing value " + IRR.getValue() + " into register " + r.getName());
			} else if (registerType.equals(Common.IXR)){
				Register r = selectIxr(RS1.getValue());
				r.setValue(IRR.getValue());
				System.out.println("Storing value " + IRR.getValue() + " into register " + r.getName());
			} else if (registerType.equals(Common.FPR)) {
				// Set the floating point register values
				RegisterFloat r = selectFpr(RS1.getValue());
				r.setExponent(Memory.get(IAR.getValue()));
				r.setMantissa(Memory.get(IAR.getValue()+1));
			}
		} else if (targetLocation.equals(Common.MEMORY)) {
			// move contents of irr to mbr
			MBR.setValue(IRR.getValue());

			// floating point register; handle unique store
			if (registerType.equals(Common.FPR)) {
				RegisterFloat r = selectFpr(RS1.getValue());
				// memory.insert(r.getExponent(), mar.getValue());
				Memory.insert(r.getSignAndExponent(), MAR.getValue());
				Memory.insert(r.getMantissa(), MAR.getValue()+1);
			} else {
				System.out.println("Storing value " + MBR.getValue() + " from register into memory location " + MAR.getValue());
				// move contents of mbr to memory using the address in the mar
				if (OPCode != 2 && OPCode != 34) {
					Cache.cacheInsert(MBR.getValue(), MAR.getValue(), Memory); //writing buffer is handled in here
				} else {
					Memory.insert(MBR.getValue(), MAR.getValue());
				}
			}
			// Fault Checking
			int fault = Memory.getMemoryFault();
			if (fault != -1) {
				handleMachineFault(fault);
			}
		}
	}

	/**
	 * Compute the address of the next instruction. Normally, this is just incrementing the PC.
	 */
	private void nextInstruction() {
		// Jump instructions will set nextPc manually; otherwise it is just equal to the next location in memory
		if(lastInstruction == Instruction.TRAP){
			PC.setValue(Memory.get(2));
		}else {
			PC.setValue(nextPc);

			System.out.println("pc: " + PC.getValue());
		}
	}

	/**
	 * Handles a machine fault
	 *
	 * @param id -> the id of the fault
	 */
	public void handleMachineFault(int id) {
		switch (id) {
			case 0:
				MFR.setValue(1);
				break;
			case 1:
				MFR.setValue(2);
				break;
			case 2:
				MFR.setValue(4);
				break;
			case 3:
				MFR.setValue(8);
				break;
			default:
				System.out.println("Unknown machine fault; should not be here");
				break;
		}
		// store pc at memory 5
		Memory.store(PC.getValue(), 5);
		// Load PC with value at memory 1
		setNextPc(Memory.load(1));
	}

	/**
	 * Updates the marMemory display in case the MAR hasn't changed, but the memory at the MAR has changed
	 */
	private void updateInternalDisplays() {
		// update memory at MAR
		MARMem.setValue(Memory.get(MAR.getValue()));

		// mem

		IARLabel.setText("" + IAR.getValue());
		IRRLabel.setText("" + IRR.getValue());
		RS1Label.setText("" + RS1.getValue());

		ALU.updateCcDisplays();
	}

	public void fullReset() {
		// reset the cpu components and all memory values
		reset();

		// todo: reset cache
		Memory.reset();

		displayRaw(false);
	}

	/**
	 * Resets all the internal components of the CPU
	 */
	public void reset() {
		// Reset Register Values
		GPR0.setValue(0);	GPR1.setValue(0);
		GPR2.setValue(0);	GPR3.setValue(0);
		IX1.setValue(0);		IX2.setValue(0);
		IX3.setValue(0);
		RX.setValue(0);		RY.setValue(0);
		R0.setValue(0);		R1.setValue(0);

		if (Memory.getRunningUserProgram()) {
			PC.setValue(Common.USER_PROGRAM_OFFSET);
			nextPc = Common.USER_PROGRAM_OFFSET;
		} else {
			PC.setValue(0);
			nextPc = 0;
		}
        // Reset Memory Register Values
		MAR.setValue(0);		MARMem.setValue(Memory.get(MAR.getValue()));
		MBR.setValue(0);		IR.setValue(0);		MFR.setValue(0);

		setHalted(false);
		ALU.reset();

		// Reset Interal Register Values (and Displays)
		IAR.setValue(0);		IARLabel.setText("0");
		IRR.setValue(0);		IRRLabel.setText("0");
		RS1.setValue(0);		RS1Label.setText("0");

		// Reset Flags
		resetFlags();
		currentInstruction = null;		updateCurrentInstructionDisplay();
		OPCode = 0;		ix = 0;		GPRSelect = 0;
		IndirectFlagg = 0;		memoryLocation = 0;
	}

	/**
	 * Reset internal CPU flags.
	 */
	private void resetFlags() {	targetLocation = ""; registerType = "";	useIxi = true;	}

	/**
	 * Returns the corresponding general purpose register based on the input. Returns gpr0 by default
	 * @param gpr -> the integer value corresponding to a gpr
	 * @return -> the general purpose register to return
	 */
	public Register selectGpr(int gpr) {
		if      (gpr == 1) { return GPR1; } 
        else if (gpr == 2) { return GPR2; } 
        else if (gpr == 3) { return GPR3; }
		return GPR0;
	}

	/**
	 * Returns the corresponding floating purpose register based on the input. Returns fr0 by default
	 * @param fpr -> the integer value corresponding to a floating point register
	 * @return -> the general purpose register to return
	 */
	public RegisterFloat selectFpr(int fpr) {
		if (fpr == 1) {	return R1; }
		if (fpr > 1) { handleMachineFault(Common.ILLEGAL_OPERATION_CODE); }
		return R0;
	}

	/**
	 * Returns the corresponding index register based on the input. Returns ix1 by default
	 * @param ixr -> the integer value corresponding to an index register
	 * @return -> the index register to return
	 */
	public Register selectIxr(int ixr) {
		if (ixr == 2) { return IX2; } 
        else if (ixr == 3) { return IX3; } 
        else { return IX1; }
	}
	
	/**
	 * Used to parse the binary instruction into its appropriate values
	 */
	private void parseInstruction() {
		String binary = IR.getBinaryStringValue();
		// set the switches to the value of the instruction for display purposes
		InputSwitches.setSwitchValue(binary);

		// fetch register values and store in memory
		OPCode = Integer.parseInt(binary.substring(0, 6), 2);
		GPRSelect = Integer.parseInt(binary.substring(6, 8), 2);
		ix = Integer.parseInt(binary.substring(8, 10), 2);
		IndirectFlagg = Integer.parseInt(binary.substring(10, 11), 2);
		memoryLocation = Integer.parseInt(binary.substring(11), 2);

		// used for shift and rotate instructions
		ShiftLeft = Integer.parseInt(binary.substring(8, 9), 2);
		ShiftRight = Integer.parseInt(binary.substring(9, 10), 2);
		Count = Integer.parseInt(binary.substring(12), 2);

		RX = selectGpr(GPRSelect);	RY = selectGpr(ix);
	}

	private void loadBootProgram() {
		System.out.println("Loading boot program");
		try {
            // Not sure if this will work on all computers.. Check after Jar File
			InputStream instream = getClass().getResourceAsStream("/programs/boot.txt");
			if (instream == null) {
				System.out.println("[ERROR] Boot Program is null.");
				return;
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(instream));
			String line;

			while ( (line = br.readLine()) != null) {
				String[] instr = line.split("\\s+");
				int new_location = Memory.hexToDec(instr[0]) + Common.BOOT_PROGRAM_ADDRESS;
				int new_value = Memory.hexToDec(instr[1]);
				System.out.println(new_value + " inserted into memory location " + new_location);
				Memory.insert(new_value, new_location);
			}
			System.out.println("[SUCCESS] Loaded boot program into memory");
		} catch (Exception e) {
			System.out.println("[ERROR] No boot program found: " + e.getMessage());
		}
	}

	/**
	 * Outputs the value from the specified register to the machine console
	 * @param r --> the register whose value is displayed. 
     * If r is storing a character, the ascii value of r's value will be displayed
	 */
	public void printToConsole(Register r) {
		// print just the number in the register instead of ascii value
		if (program1) {
			DevConsole.append(r.getValue() + " \n");
		} else {
			// handle "enter" character
			if (r.getValue() == 13) { DevConsole.append("\n"); } 
            else { DevConsole.append(Character.toString(r.getChar())); }
		}
	}

	/**
	 * Get input from the keyboard. Input can be either a character or an integer, assuming the integer is within
	 * the range that the register can hold.
	 */
	public void getKeyboardInput() {
		Register r = selectGpr(RS1.getValue());
		char c;
		do {
			boolean tooLarge = false;
			String input = JOptionPane.showInputDialog(mainPanel, "Enter a character or number");
			if (input == null) {
				String quitMessage = "You must enter a character or number.\nWould you like to quit the simulator?";
				int quit = JOptionPane.showConfirmDialog(mainPanel, quitMessage);
				if (quit == JOptionPane.YES_OPTION) {
					System.exit(0);
				} else {
					continue;
				}
			}
			// test if valid number
			try {
				int num = Integer.parseInt(input);
				// if the num is too large
				if (num > Math.pow(2, r.getLength())) {
					JOptionPane.showMessageDialog(mainPanel, "Number you entered is too large.\n0..."+ Math.pow(2, r.getLength()));
					//System.out.println("User entered too large of a number");
					tooLarge = true;
				} else {
					// otherwise, valid num so set the register value
					r.setValue(num);
					InputText.setText("" + num);
					System.out.println("Register " + r.getName() + " loaded with value " + num);
					return;
				}
			} catch (NumberFormatException e) {
				System.out.println("Error when parsing keyboard input: " + e.getMessage());
			}

			if (!tooLarge) {
				// test if valid char
				if (input.length() == 1) {
					c = input.charAt(0);
					r.setValue(c);
					InputText.setText("" + c);

					System.out.println("Register " + r.getName() + " loaded with character " + c);
					return;
				} else if (input.length() == 0) {
					// enter key (ascii 10)
					r.setValue('\n');
					InputText.setText("\n");
					return;
				}

				JOptionPane.showMessageDialog(mainPanel, "Input must be a character or a number");
			}
		} while(true);
	}

	/**
	 * Used to either pause or resume the machine
	 * @param isHalted --> if true, disables the front panel buttons (halted)
	 */
	public void setHalted(boolean isHalted) {
		// Enable/disable buttons accordingly. But, don't resume CPU execution until user presses run/single step
		halt.setSelected(isHalted);		run.setEnabled(!isHalted);		singleStep.setEnabled(!isHalted);
	}

	/**
	 * Creating a thread specifically for the simulator to allow interactions while processing instructions
	 */
	private void createRunThread() {
		runThread = new Thread("runThread") {
			@Override
			public void run() {
				while (true) {
					// execute instructions while the machine is not halted
					if (!paused) {
						singleInstructionCycle();
						if (PC.getValue() + 1 == 2048) { break; }
					} else {
						if (PC.getValue() != nextPc) { nextPc = PC.getValue() + 1; }
						try { sleep(10); } catch (java.lang.InterruptedException ignored) {}
					}
				}
			}
		};
	}

	/**
	 * Add Listeners for 3 main run buttons
     * Run, Step and Halt.
	 */
	private void addListeners() {
		run.addActionListener(ae -> {
			// Start the CPU if it wasn't running; otherwise un-pause the machine
			if (!runThread.isAlive()) {	runThread.start();	}
			if (!Memory.getRunningUserProgram()) {
				Memory.setRunningUserProgram(true);
				reset();
			}
			paused = false;
		});

		halt.addItemListener(e -> {
			// If the checkbox was checked, halt the execution
			if (e.getStateChange() == ItemEvent.SELECTED) {
				setHalted(true);
				paused = true;
                System.out.println(this.Memory.getMemoryString());
			} else {
				// otherwise, un-halt the machine; don't resume execution though --> lets user decide next action
				setHalted(false);
			}
		});

		// run one instruction cycle
		singleStep.addActionListener(e -> {
			if (!Memory.getRunningUserProgram()) {
				Memory.setRunningUserProgram(true);
				reset();
			}
			singleInstructionCycle();
		});
	}

	private void addGeneralPurposeRegisters() {
        // GPU -> CPU
		GPR0 = new Register("GPR 0", 16, InputSwitches, true);
		GPR1 = new Register("GPR 1", 16, InputSwitches, true);
		GPR2 = new Register("GPR 2", 16, InputSwitches, true);
		GPR3 = new Register("GPR 3", 16, InputSwitches, true);

		// GPR -> GUI
		Utilities.addComponent(GPR0.getLabel(), mainPanel, 0, 0, 1);
		Utilities.addComponent(GPR0.getTextField(), mainPanel, 1, 0, 1);
		Utilities.addComponent(GPR0.getLoad(), mainPanel, 2, 0, 1);

		Utilities.addComponent(GPR1.getLabel(), mainPanel, 0, 1, 1);
		Utilities.addComponent(GPR1.getTextField(), mainPanel, 1, 1, 1);
		Utilities.addComponent(GPR1.getLoad(), mainPanel, 2, 1, 1);

		Utilities.addComponent(GPR2.getLabel(), mainPanel, 0, 2, 1);
		Utilities.addComponent(GPR2.getTextField(), mainPanel, 1, 2, 1);
		Utilities.addComponent(GPR2.getLoad(), mainPanel, 2, 2, 1);

		Utilities.addComponent(GPR3.getLabel(), mainPanel, 0, 3, 1);
		Utilities.addComponent(GPR3.getTextField(), mainPanel, 1, 3, 1);
		Utilities.addComponent(GPR3.getLoad(), mainPanel, 2, 3, 1);
	}

	private void addIndexRegisters() {
		// IX -> CPU
		IX1 = new Register("IX 1", 16, InputSwitches, true);
		IX2 = new Register("IX 2", 16, InputSwitches, true);
		IX3 = new Register("IX 3", 16, InputSwitches, true);

		// IX -> GUI
		Utilities.addComponent(IX1.getLabel(), mainPanel, 0, 6, 1);
		Utilities.addComponent(IX1.getTextField(), mainPanel, 1, 6, 1);
		Utilities.addComponent(IX1.getLoad(), mainPanel, 2, 6, 1);

		Utilities.addComponent(IX2.getLabel(), mainPanel, 0, 7, 1);
		Utilities.addComponent(IX2.getTextField(), mainPanel, 1, 7, 1);
		Utilities.addComponent(IX2.getLoad(), mainPanel, 2, 7, 1);

		Utilities.addComponent(IX3.getLabel(), mainPanel, 0, 8, 1);
		Utilities.addComponent(IX3.getTextField(), mainPanel, 1, 8, 1);
		Utilities.addComponent(IX3.getLoad(), mainPanel, 2, 8, 1);
	}

	private void addFloatRegisters(){
        // FLOPR -> CPU
		R0 = new RegisterFloat("FR 0", 16, InputSwitches, true);
		R1 = new RegisterFloat("FR 1", 16, InputSwitches, true);

        // FLOPR -> GUI		
        Utilities.addComponent(R0.getLabel(), mainPanel, 0, 9, 1);
		Utilities.addComponent(R0.getTextField(), mainPanel, 1, 9, 1);
		Utilities.addComponent(R0.getLoad(), mainPanel, 2, 9, 1);

		Utilities.addComponent(R1.getLabel(), mainPanel, 0, 10, 1);
		Utilities.addComponent(R1.getTextField(), mainPanel, 1, 10, 1);
		Utilities.addComponent(R1.getLoad(), mainPanel, 2, 10, 1);
	}

	private void addPC() {
		// PC -> CPU
		PC = new Register("PC", 12, InputSwitches, false);
		nextPc = 0;

        // PC -> GUI
		Utilities.addComponent(PC.getLabel(), mainPanel, 6, 0, 1);
		Utilities.addComponent(PC.getTextField(), mainPanel, 7, 0, 1, GridBagConstraints.LINE_END);
		Utilities.addComponent(PC.getLoad(), mainPanel, 8, 0, 1);
	}

	private void addMAR() {
		// MAR -> CPU
		MAR = new Register("MAR", 12, InputSwitches, false);
		MARMem = new Register("Mem @ MAR", 16, InputSwitches, false);

        // MAR -> GUI
		Utilities.addComponent(MAR.getLabel(), mainPanel, 6, 1, 1);
		Utilities.addComponent(MAR.getTextField(), mainPanel, 7, 1, 1, GridBagConstraints.LINE_END);
		Utilities.addComponent(MAR.getLoad(), mainPanel, 8, 1, 1);

		Utilities.addComponent(MARMem.getLabel(), mainPanel, 9, 1, 1);
		Utilities.addComponent(MARMem.getTextField(), mainPanel, 10, 1, 1, GridBagConstraints.LINE_START);
	}

	private void addMBR() {
		// MBR -> CPU
		MBR = new Register("MBR", 16, InputSwitches, true);

        // MBR -> GUI
		Utilities.addComponent(MBR.getLabel(), mainPanel, 6, 2, 1);
		Utilities.addComponent(MBR.getTextField(), mainPanel, 7, 2, 1, GridBagConstraints.LINE_END);
		Utilities.addComponent(MBR.getLoad(), mainPanel, 8, 2, 1);
	}

	private void addIR() {
		// IR -> CPU
		IR = new Register("IR", 16, InputSwitches, false);

        // IR -> GUI
		Utilities.addComponent(IR.getLabel(), mainPanel, 6, 3, 1);
		Utilities.addComponent(IR.getTextField(), mainPanel, 7, 3, 1, GridBagConstraints.LINE_END);
	}

	private void addMFR() {
		// MFR -> CPU
		MFR = new Register("MFR", 4, InputSwitches, false);

        // MFR -> GUI
		Utilities.addComponent(MFR.getLabel(), mainPanel, 6, 4, 1);
		Utilities.addComponent(MFR.getTextField(), mainPanel, 7, 4, 1, GridBagConstraints.LINE_END);
	}

	/**
	 * Run, Step, Halt Buttons
	 */
	private void addRunHalt() {
		run = new JButton("RUN");
		singleStep = new JButton("STEP");
		halt = new JToggleButton("HALT");

		Utilities.addComponent(singleStep, mainPanel, 1, 11, 1);
		Utilities.addComponent(run, mainPanel, 2, 11, 1);
		Utilities.addComponent(halt, mainPanel, 3, 11, 1);
	}

	private void addInternalRegisters() {
		IAR = new Register("IAR", 16, InputSwitches, true);
		IARLabel = new JLabel("" + IAR.getValue());

		IRR = new Register("IRR", 16, InputSwitches, true);
		IRRLabel = new JLabel("" + IRR.getValue());
		IRRLabel.setPreferredSize(new Dimension(30, 10));
        
		RS1 = new Register("RS1", 2, InputSwitches, false);
		RS1Label = new JLabel("" + RS1.getValue());
        
		Utilities.addComponent(IRR.getLabel(), mainPanel, 9, 3, 1);
		Utilities.addComponent(IRR.getTextField(), mainPanel, 10, 3, 1);
		Utilities.addComponent(IRRLabel, mainPanel, 11, 3, 1);

		Utilities.addComponent(IAR.getLabel(), mainPanel, 9, 2, 1);
		Utilities.addComponent(IAR.getTextField(), mainPanel, 10, 2, 1);
		Utilities.addComponent(IARLabel, mainPanel, 11, 2, 1);
		
        Utilities.addComponent(RS1.getLabel(), mainPanel, 9, 0, 1);
		Utilities.addComponent(RS1.getTextField(), mainPanel, 10, 0, 1, GridBagConstraints.LINE_END);
		Utilities.addComponent(RS1Label, mainPanel, 11, 0, 1);
	}

	private void addRxRy() {
		RX = new Register("RX", 16, InputSwitches, true);
		RY = new Register("RY", 16, InputSwitches, true);
	}

	/**
	 * Adds the IO devices (printer, keyboard) to the front panel
	 */
	private void addIODevices() {
		JLabel printerLabel = new JLabel("Console Printer");
		DevConsole = new JTextArea(20, 20);
		JScrollPane scroll = new JScrollPane(DevConsole);
		DevConsole.setEditable(false);
		DevConsole.setLineWrap(true);

		Utilities.addComponent(printerLabel, mainPanel, 6, 6, 3, GridBagConstraints.CENTER);
		Utilities.addComponent(scroll, mainPanel, 6, 7, 3, 5, GridBagConstraints.CENTER);

		JLabel keyboardLabel = new JLabel("Last Keyboard Value");
		InputText = new JTextField("", 10);
		keyboardLabel.setHorizontalAlignment(JTextField.RIGHT);
		InputText.setEditable(false);

		Utilities.addComponent(keyboardLabel, mainPanel, 10, 9, 2, GridBagConstraints.CENTER);
		Utilities.addComponent(InputText, mainPanel, 10, 10, 3, GridBagConstraints.CENTER);
	}

	public void addCurrentInstructionDisplay() {
		currentInstructionDisplay = new JLabel("Instruction: N/A");

		Utilities.addComponent(currentInstructionDisplay, mainPanel, 10, 12, 2, GridBagConstraints.LINE_START);
	}

	/* GETTERS AND SETTERS */

	/**
	 * Sets the target location for the instruction
	 * @param targetLocation -> either 'REGISTER' or 'MEMORY'
	 */
	public void setTargetLocation(String targetLocation) {
		this.targetLocation = targetLocation;
	}

	/**
	 * Sets the type of register to use for the instruction
	 * @param registerType -> either 'GPR' or 'IXR'
	 */
	public void setRegisterType(String registerType) {
		this.registerType = registerType;
	}

	public void updateCurrentInstructionDisplay() {
		String display;
		if (currentInstruction == null) {
			display = "Current Instruction: N/A";
		} else {
			display = "Current Instruction: " + currentInstruction;
		}
		currentInstructionDisplay.setText(display);
	}

	public boolean getRunningBoot() {
		return runningBoot;
	}

	public void setRunningBoot(boolean b) {
		runningBoot = b;
	}

	public boolean getRunningUserProgram() {
		return Memory.getRunningUserProgram();
	}

	public Register getMbr() {
		return MBR;
	}

	public Register getRs1() {
		return RS1;
	}

	public Register getIar() {
		return IAR;
	}

	public Register getIrr() {
		return IRR;
	}
	
	public int getAL() {
		return ShiftLeft;
	}
	
	public int getLR() {
		return ShiftRight;
	}
	
	public int getCount() {
		return Count;
	}

	public String getExponent(Register r){
		String binary = r.getBinaryStringValue();
		return binary.substring(1,8);
	}

	public String getMantissa(Register r){
		String binary = r.getBinaryStringValue();
		return binary.substring(8,16);
	}

	/**
	 * Gets the value of the two IX bits from the parsed instruction
	 * @return the value of the two IX bits --> ranges from [0, 3]
	 */
	public int getIxBits() {
		return ix;
	}

	public Memory getMemory() {
		return Memory;
	}

	public ALU getAlu() {
		return ALU;
	}

	public void setIxiflag(boolean flag) {
		useIxi = flag;
	}

	public int getGpr() {
		return GPRSelect;
	}

	public void setNextPc(int pcValue) {
		nextPc = pcValue;
		// on branches, account for user memory difference
		if (Memory.getRunningUserProgram() && MFR.getValue() == 0) {
			nextPc += Common.USER_PROGRAM_OFFSET;
		}
	}

	public void displayRaw(boolean b) {
		program1 = b;
	}

	public JToggleButton getHalt() { return halt; }

	public Register getPc() { return PC; }

	public Register getRx() { return RX; }	
	public Register getRy() { return RY; }

	public Register getMfr() { return MFR; }

	public Register getGpr0() { return GPR0; }
	public Register getGpr1() { return GPR1; }
	public Register getGpr2() { return GPR2; }
	public Register getGpr3() { return GPR3; }

	public Register getIx1() { return IX1; }
	public Register getIx2() { return IX2; }
	public Register getIx3() { return IX3; }

	public RegisterFloat getFr0() { return R0; }
	public RegisterFloat getFr1() { return R1; }
}