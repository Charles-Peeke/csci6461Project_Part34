package CPU;

import Common.Common;
import Common.Utilities;
import Memory.Memory;


public enum Instruction {

    // octal 0
    HALT (0, "HLT") {
        @Override
        public void decode(CPU cpu) {}

        @Override
        public void execute(CPU cpu) {
            cpu.setHalted(true);
            // terminate the boot program if running; otherwise handle user program termination
            if (cpu.getRunningBoot()) {
                cpu.setRunningBoot(false);
            } else if (cpu.getRunningUserProgram()) {
                cpu.handleProgramTermination();
            }
            // otherwise not running boot or user program; probably running tests
        }
    },
    /**
     * Load register from memory
     * r <-- c(EA)
     * Octal 1
     */
    LDR (1, "LDR") {
        @Override
        public void decode(CPU cpu) {
            cpu.setTargetLocation(Common.REGISTER);
            cpu.setRegisterType(Common.GPR);
        }

        @Override
        public void execute(CPU cpu) {
            // irr = mbr
            cpu.getIrr().setValue(cpu.getMbr().getValue());
        }
    },
    /**
     * Store register to memory
     * memory(EA) <-- c(r)
     * Octal 2
     */
    STR (2, "STR") {
        @Override
        public void decode(CPU cpu) {
            cpu.setTargetLocation(Common.MEMORY);
            cpu.setRegisterType(Common.GPR);
        }

        @Override
        public void execute(CPU cpu) {
            // set the irr to the value in the specified gpr
            int val = cpu.selectGpr(cpu.getRs1().getValue()).getValue();
            cpu.getIrr().setValue(val);
        }
    },
    /**
     * Load register with address
     * r <-- EA
     * Octal 3
     */
    LDA (3, "LDA") {
        @Override
        public void decode(CPU cpu) {
            cpu.setTargetLocation(Common.REGISTER);
            cpu.setRegisterType(Common.GPR);
        }

        @Override
        public void execute(CPU cpu) {
            // irr = iar
            cpu.getIrr().setValue(cpu.getIar().getValue());
        }
    },
	/** Add Memory to Register
	 * 
	 * r <- content of register + content of EA
	 * Octal 4
	 */
    AMR (4, "AMR") {
        @Override
        public void decode(CPU cpu) {
        	cpu.setTargetLocation(Common.REGISTER);
        	cpu.setRegisterType(Common.GPR);
        }

        @Override
        public void execute(CPU cpu) {
            Register r = cpu.selectGpr(cpu.getRs1().getValue());
            int valToAdd = cpu.getMemory().get(cpu.getIar().getValue());
            // check for fault
            int fault = cpu.getMemory().getMemoryFault();
            if (fault != -1) {
                cpu.handleMachineFault(fault);
            } else {
                cpu.getAlu().add(cpu.getIrr(), r, valToAdd);
            }
        }
    },
	/** Subtract Memory From Register
	 * 
	 * r <- content of register - content of EA
	 * Octal 5
	 */
    SMR (5, "SMR") {
        @Override
        public void decode(CPU cpu) {
        	cpu.setTargetLocation(Common.REGISTER);
        	cpu.setRegisterType(Common.GPR);
        }

        @Override
        public void execute(CPU cpu) {
            Register r = cpu.selectGpr(cpu.getRs1().getValue());
            int valToAdd = cpu.getMemory().get(cpu.getIar().getValue());

            int fault = cpu.getMemory().getMemoryFault();
            if (fault != -1) {
                cpu.handleMachineFault(fault);
            } else {
                cpu.getAlu().subtract(cpu.getIrr(), r, valToAdd);
            }
        }
    },
	/**Add Immediate to register 
	 *
	 * r <- content of register + Immediate
	 * if Immediate = 0 -> do nothing
	 * if content of register = 0 
	 * -> load register with Immediate
	 * Octal 6
	 */
    AIR (6, "AIR") {
        @Override
        public void decode(CPU cpu) {
        	cpu.setTargetLocation(Common.REGISTER);
        	cpu.setRegisterType(Common.GPR);
            cpu.setIxiflag(false); // IX and I are ignored in this instruction
        }

        @Override
        public void execute(CPU cpu) {
        	Register r = cpu.selectGpr(cpu.getRs1().getValue());
            // store value in Irr
            cpu.getAlu().add(cpu.getIrr(), r, cpu.getIar().getValue());
        }
    },
	/** Subtract Immediate from register 
	 * 
	 * r <- content of register - Immediate
	 * Octal 7
	 */
    SIR (7, "SIR") {
        @Override
        public void decode(CPU cpu) {
        	cpu.setTargetLocation(Common.REGISTER);
        	cpu.setRegisterType(Common.GPR);
            cpu.setIxiflag(false); // IX and I are ignored in this instruction
        }

        @Override
        public void execute(CPU cpu) {
            Register r = cpu.selectGpr(cpu.getRs1().getValue());
            // store value in Irr
            cpu.getAlu().subtract(cpu.getIrr(), r, cpu.getIar().getValue());
        }
    },
    /**
     * Jump if content of register = 0.
     * octal 10
     */
    JZ (8, "JZ") {
        @Override
        // nothing to be done in the decode portion; only PC is updated
        public void decode(CPU cpu) {}

        @Override
        public void execute(CPU cpu) {
            // get value from the gpr
            int val = cpu.selectGpr(cpu.getRs1().getValue()).getValue();
            // branch if register value is 0; otherwise pc will increment as normal
            if (val == 0) {
                cpu.setNextPc(cpu.getIar().getValue());
            }
        }
    },
    /**
     * Jump if content of register != 0.
     * octal 11
     */
    JNE (9, "JNE") {
        @Override
        public void decode(CPU cpu) {
        	//do nothing, only PC is updated
        }

        @Override
        public void execute(CPU cpu) {
            // get value from the gpr
            int val = cpu.selectGpr(cpu.getRs1().getValue()).getValue();
            // branch if register value is 0; otherwise pc will increment as normal
            if (val != 0) {
                cpu.setNextPc(cpu.getIar().getValue());
            }
        }
    },
    /**
     * Jump if condition code
     * cc replaces r for this instruction. cc takes value 0, 1, 2, 3
     * if the bit in the specified condition code register is set, PC <-- EA
     * Otherwise, increment PC as normal
     * Octal 12
     */
    JCC (10, "JCC") {
        @Override
        // do nothing, only PC is updated
        public void decode(CPU cpu) {}

        @Override
        public void execute(CPU cpu) {
            // if the condition code is set
            if (cpu.getAlu().getCc(cpu.getRs1().getValue()) == 1) {
                // PC <-- EA
                cpu.setNextPc(cpu.getIar().getValue());
            }
        }
    },
    /**
     * Unconditional jump --> load PC with EA
     * Octal 13
     */
    JMA (11, "JMA") {
        @Override
        //do nothing, only PC is updated
        public void decode(CPU cpu) {}

        @Override
        public void execute(CPU cpu) {
        	cpu.setNextPc(cpu.getIar().getValue());
        	// cpu.setGpr(false);
        }
    },
    /**
     * Jump to address, save PC in gpr3
     * R0 should contain pointer to arguments
     * Argument list should end with -1 (all 1s) as value
     * octal 14
     */
    JSR (12, "JSR") {
        @Override
        public void decode(CPU cpu) {
        	cpu.setTargetLocation(Common.REGISTER);
        	cpu.setRegisterType(Common.GPR);
        }

        @Override
        public void execute(CPU cpu) {
            // want to store in gpr3
            cpu.getRs1().setValue(3);
            cpu.getIrr().setValue(cpu.getPc().getValue() + 1);
            cpu.setNextPc(cpu.getIar().getValue());
        	// Argument list should end with -1 (all 1s) value --> stored in R0 (on the user)
        }
    },
    /**
     * Return from subroutine w/ return code as Immed portion (optional)
     * stored in the instruction's address field.
     * R0 <-- Immed; PC <-- c(R3)
     * IX, I fields are ignored
     *
     * Octal 15
     */
    RFS (13, "RFS") {
        @Override
        public void decode(CPU cpu) {
            cpu.setTargetLocation(Common.REGISTER);
            cpu.setRegisterType(Common.GPR);
            // ignore Ix, I fields
            cpu.setIxiflag(false);
        }

        @Override
        public void execute(CPU cpu) {
            // don't consider IX, I fields
            cpu.setIxiflag(false);
            // want to store in gpr0
            cpu.getRs1().setValue(0);
            // load irr with immediate portion
            cpu.getIrr().setValue(cpu.getIar().getValue());
            int returnAddress = cpu.getGpr3().getValue();
            // subtract the user offset since it is being accounted for twice
            if (cpu.getMemory().getRunningUserProgram()) {
                returnAddress -= Common.USER_PROGRAM_OFFSET;
            }
            // load PC with R3 value
            cpu.setNextPc(returnAddress);
        }
    },
    /**
     * Subtract one and branch
     * only branch if c(r) > 0
     * octal 16
     */
    SOB (14, "SOB") {
        @Override
        public void decode(CPU cpu) {}

        @Override
        public void execute(CPU cpu) {
            Register r = cpu.selectGpr(cpu.getRs1().getValue());
            r.setValue(r.getValue() - 1);
            // only branch if c(r) > 0
            if (r.getValue() > 0) {
                cpu.setNextPc(cpu.getIar().getValue());
            }
        }
    },
    /**
     * Jump greater than or equal to
     * if c(r) >=, then PC <-- EA
     * Else PC <-- PC + 1
     */
    // octal 17
    JGE (15, "JGE") {
        @Override
        // do nothing, only PC is updated
        public void decode(CPU cpu) {}

        @Override
        public void execute(CPU cpu) {
            // get value from the gpr
            int val = cpu.selectGpr(cpu.getRs1().getValue()).getValue();
            // branch if c(r) >= 0
            if (val >= 0) {
                cpu.setNextPc(cpu.getIar().getValue());
            }
        }
    },
	/** Multiply Register by Register
	 * 
	 * RX and RX+1 = content of RX * content of RY
	 * RX and RY must be 0 or 2
	 * Note: Rx takes the place of the GPR bits in the instruction
	 * Note: Ry takes the place of the IXR bits " "
	 * RX contains higher order bits
	 * RX + 1 contains lower order bits of the result
	 * if overflow then set OVERFLOW
	 * Octal 20
	 */
    MLT (16, "MLT") {
        @Override
        public void decode(CPU cpu) {}

        @Override
        public void execute(CPU cpu) {
            Register RX = cpu.getRx();
            Register RY = cpu.getRy();
            int RXNum = cpu.getGpr();
            int RYNum = cpu.getIxBits();

            if (RXNum != 0 && RXNum != 2) {
                System.out.println("Invalid RX register for MLT. Expected 0, 2 but got " + RXNum);
                cpu.handleMachineFault(Common.ILLEGAL_OPERATION_CODE);
                return;
            } else if (RYNum != 0 && RYNum != 2) {
                System.out.println("Invalid RY register for MLT. Expected 0, 2 but got " + RYNum);
                cpu.handleMachineFault(Common.ILLEGAL_OPERATION_CODE);
                return;
            }

            cpu.getAlu().multiply(RX, RY, cpu.selectGpr(RXNum+1));
        }
    },
	/** Divide Register by Register
	 * RX and RX + 1 = content of RX / content of RY
	 * RX and RY must be 0 or 2
	 * RX contains the quotient
	 * RX + 1 contains the remainder
	 * if content of RY = 0
	 * then set conditional code register 3 to 1
	 * DIVZERO flag is set
	 * Octal 21
	 */
    DVD (17, "DVD") {
        @Override
        public void decode(CPU cpu) {}

        @Override
        public void execute(CPU cpu) {
            Register RX = cpu.getRx();
            Register RY = cpu.getRy();
            int RXNum = cpu.getGpr();
            int RYNum = cpu.getIxBits();

            if (RXNum != 0 && RXNum != 2) {
                System.out.println("Invalid RX register for DVD. Expected 0, 2 but got " + RXNum);
                cpu.handleMachineFault(Common.ILLEGAL_OPERATION_CODE);
                return;
            } else if (RYNum != 0 && RYNum != 2) {
                System.out.println("Invalid RY register for DVD. Expected 0, 2 but got " + RYNum);
                cpu.handleMachineFault(Common.ILLEGAL_OPERATION_CODE);
                return;
            }

            cpu.getAlu().divide(RX, RY, cpu.selectGpr(RXNum+1));
        }
    },
	/** Test the Equality of Register and Register
	 * 
	 * if content of RX = content of RY
	 * then set conditional code register4 to 1
	 * else conditional code register = 0
	 * Octal 22
	 */
    TRR (18, "TRR") {
        @Override
        public void decode(CPU cpu) {}

        @Override
        public void execute(CPU cpu) {
            Register RX = cpu.getRx();
            Register RY = cpu.getRy();
            cpu.getAlu().testEquality(RX, RY);
        }
    },
	/** Content of RX = content of RX AND content of RY
	 *  Octal 23
	 */
    AND (19, "AND") {
        @Override
        public void decode(CPU cpu) {}

        @Override
        public void execute(CPU cpu) {
            Register RX = cpu.getRx();
            Register RY = cpu.getRy();
            RX.setValue(cpu.getAlu().logicalAnd(RX, RY));
        }
    },
	/** Content of RX = content of RX ORR content of RY
	 *  Octal 24
	 */
    ORR (20, "ORR") {
        @Override
        public void decode(CPU cpu) {}

        @Override
        public void execute(CPU cpu) {
            Register RX = cpu.getRx();
            Register RY = cpu.getRy();
            RX.setValue(cpu.getAlu().logicalOr(RX, RY));
        }
    },
	/** Content of RX = NOT content of RX
	 *  Octal 25
	 */
    NOT (21, "NOT") {
        @Override
        public void decode(CPU cpu) {}

        @Override
        public void execute(CPU cpu) {
        	// cpu.getRx().setValue(cpu.selectGpr(cpu.getRs1().getValue()).getValue());
            Register RX = cpu.getRx();
            RX.setValue(cpu.getAlu().logicalNot(RX));
        }
    },
    /**  todo: IMPLEMENTED IN PHASE 3
     * Traps to memory location 0
     * PC + 1 is stored in memory location 2
     * Memory location can have 16 entries (for each routine)
     * When trap instruction is executed
     * it goes to routine whose address is in mem location 0
     * executes that instruction
     * and returns to instruction at mem location 2
     * the PC + 1 of the trap instruction is now at mem location 2
     * Octal 30
     */
    TRAP (24, "TRAP") {
        @Override
        public void decode(CPU cpu) {}

        @Override
        public void execute(CPU cpu) {
            cpu.setNextPc(0);
            cpu.getMemory().insert(cpu.getPc().getValue()+1,2);
        }
    },
	/**Shift Register by Count
	 * content of register is shifted
	 * R is 6th and 7th bit
	 * A/L is 8th bit from left
	 * L/R is 9th bit from left
	 * shift left if L/R = 1 else shift right if L/R = 0
	 * Count = 1 -> 15
	 * if Count = 0, then no shift
	 * Octal 31
	 */
    SRC (25, "SRC") {
        @Override
        public void decode(CPU cpu) {
            cpu.setTargetLocation(Common.REGISTER);
            cpu.setRegisterType(Common.GPR);
        }

        @Override
        public void execute(CPU cpu) {
            Register r = cpu.selectGpr(cpu.getRs1().getValue());
        	int count = cpu.getCount();
            int LR = cpu.getLR();
        	int AL = cpu.getAL();

            int result = cpu.getAlu().shift(r, count, LR, AL);

            cpu.getIrr().setValue(result);
        }
    },
	/** Rotate Register by Count
	 * content of register rotated
	 * Left if L/R = 1 and right if L/R = 0
	 * and if A/L = 1
	 * Octal 32
	 */
    RRC (26, "RRC") {
        @Override
        public void decode(CPU cpu) {}

        @Override
        public void execute(CPU cpu) {
        	int Count = cpu.getCount();
        	int AL = cpu.getAL();
        	int LR = cpu.getLR();
        	String rV = Integer.toBinaryString(cpu.selectGpr(cpu.getRs1().getValue()).getValue());
        	String rValue = "0".repeat(16-rV.length()) + rV; 
        	String result = "";
        	
        	if(Count == 0) {
        		return;
        	}
        	
        	if(AL == 1) { // Rotate
        		if(LR == 1) { // Rotate Left
        			String saveRotated = rValue.substring(0,Count);
        			String saveParsed = rValue.substring(Count);
        			result = saveParsed + saveRotated;
        			cpu.selectGpr(cpu.getRs1().getValue()).setValue(Integer.parseInt(result,2));
        		}else if(LR == 0) { // Rotate Right
        			String saveRotated = rValue.substring(Count);
        			String saveParsed = rValue.substring(0,16-Count);
        			result = saveRotated + saveParsed;
        			cpu.selectGpr(cpu.getRs1().getValue()).setValue(Integer.parseInt(result,2));
        		}
        	}
        }
    },
    /**
     * Load index register from memory
     * Xx <-- c(EA)
     * Octal 41
     */

    LDX (33, "LDX") {
        @Override
        public void decode(CPU cpu) {
            int ixVal = cpu.getIxBits();
            if (ixVal == 0) {
                System.out.println("Fault: no index register specified in LDX");
                cpu.handleMachineFault(Common.ILLEGAL_OPERATION_CODE);
            }
            cpu.getRs1().setValue(ixVal);
            cpu.setTargetLocation(Common.REGISTER);
            cpu.setRegisterType(Common.IXR);
        }

        @Override
        public void execute(CPU cpu) {
            // irr = mbr
            cpu.getIrr().setValue(cpu.getMbr().getValue());
        }
    },
    /**
     * Store index register to memory
     * memory(EA) <-- c(Xx)
     * Octal 42
     */
    STX (34, "STX") {
        @Override
        public void decode(CPU cpu) {
            int ixVal = cpu.getIxBits();
            if (ixVal == 0) {
                System.out.println("Fault: no index register specified in LDX");
                cpu.handleMachineFault(Common.ILLEGAL_OPERATION_CODE);
            }
            cpu.getRs1().setValue(ixVal);
            cpu.setTargetLocation(Common.MEMORY);
            cpu.setRegisterType(Common.IXR);
        }

        @Override
        public void execute(CPU cpu) {
            // set the irr to the value in the specified index register
            int val = cpu.selectIxr(cpu.getRs1().getValue()).getValue();
            cpu.getIrr().setValue(val);
        }
    },
    /**
     * Input character to register from device
     * Octal 61
     */
    IN (49, "IN") {
        @Override
        public void decode(CPU cpu) {
            // Ignore ix, i for this instruction
            cpu.setIxiflag(false);
        }

        @Override
        public void execute(CPU cpu) {
            int devId = cpu.getIar().getValue();
            if (devId == DEVID.KEYBOARD.getId()) {
                // cpu handles setting register value
                cpu.getKeyboardInput();
            } else if (devId == DEVID.CARD_READER.getId()) {
                // todo: what is the card reader? do we need to implement?
            } else {
                // otherwise, invalid devid for this instruction
                System.out.println("Invalid devid for IN instruction");
                cpu.handleMachineFault(Common.ILLEGAL_OPERATION_CODE);
            }
        }
    },
    /**
     * Output character to device from register
     * Octal 62
     */
    OUT (50, "OUT") {
        @Override
        public void decode(CPU cpu) {
            // Ignore ix, i for this instruction
            cpu.setIxiflag(false);
        }

        @Override
        public void execute(CPU cpu) {
            int devId = cpu.getIar().getValue();
            if (devId == DEVID.PRINTER.getId()) {
                // output register to console
                cpu.printToConsole(cpu.selectGpr(cpu.getRs1().getValue()));
            } else {
                // otherwise, invalid devid for this instruction
                System.out.println("Invalid devid for IN instruction");
                cpu.handleMachineFault(Common.ILLEGAL_OPERATION_CODE);
            }
        }
    },
    // octal 63
	// Check Device Status to Register
	// content of register = device status
    CHK (51, "CHK") {
        @Override
        public void decode(CPU cpu) {}

        @Override
        public void execute(CPU cpu) {}
    },

    /**
     * octal 33
     * Floating Add Memory to Register
     * c(fr) <- c(fr) + c(EA)
     * c(fr) <- c(fr) + c(c(EA)) if 1 bit set
     * fr must be 0 or 1
     * OVERFLOW may be set
     */
    FADD (27, "FADD"){
        @Override
        public void decode(CPU cpu) {
            cpu.setTargetLocation(Common.FPR);
        }

        @Override
        public void execute(CPU cpu) {
            RegisterFloat fr = cpu.selectFpr(cpu.getRs1().getValue());
            int eaContent = cpu.getMemory().get(cpu.getIar().getValue());
            cpu.getAlu().add(fr,fr,eaContent);



        }
    },

    /**
     * octal 34
     * Floating Subtract Memory From Register
     * c(fr) <- c(fr) - c(EA)
     * c(fr) <- c(fr) - c(c(EA)) if 1 bit set
     * fr must be 0 or 1
     * UNDERFLOW may be set
     */
    FSUB(28, "FSUB"){
        @Override
        public void decode(CPU cpu) {
            cpu.setTargetLocation(Common.FPR);
        }

        @Override
        public void execute(CPU cpu) {
            RegisterFloat fr = cpu.selectFpr(cpu.getRs1().getValue());
            int eaContent = cpu.getMemory().get(cpu.getIar().getValue());
            cpu.getAlu().subtract(fr,fr,eaContent);
        }
    },
    /**
     * octal 35
     * Vector Add
     * fr contains the length of the vectors
     * c(EA) or c(c(EA)), if I bit set, is address of first vector
     * c(EA+1) or c(c(EA+1)), if I bit set, is address of the second vector
     * Let V1 be vector at address; Let V2 be vector at address+1
     * Then, V1[i] = V1[i]+ V2[i], i = 1, c(fr).
     */
    VADD(29, "VADD") {
        @Override
        public void decode(CPU cpu) {}

        @Override
        public void execute(CPU cpu) {
            Memory memory = cpu.getMemory();
            // todo: not sure if there is a different way to get the length for the floating point register
            int length = cpu.selectFpr(cpu.getRs1().getValue()).getValue();

            // get address of v1
            int v1Address = cpu.getIar().getValue();
            int v2Address = v1Address + 1;

            int v1 = memory.get(v1Address);
            int v2 = memory.get(v2Address);

            for (int i = 0; i < length; i++) {
                int sum = memory.get(v1 + i) + memory.get(v2+i);
                memory.insert(sum, v1+i);
            }
        }
    },

    /**
     * octal 36
     * Vector Subtract
     * fr contains the length of the vectors
     * c(EA) or c(c(EA)), if I bit set is address of first vector
     * c(EA+1) or c(c(EA+1)), if I bit set is address of the second vector
     * Let V1 be vector at address; Let V2 be vector at address+1
     * Then, V1[i] = V1[i] - V2[i], i = 1, c(fr).
     */
    VSUB(30, "VSUB"){
        @Override
        public void decode(CPU cpu) {}

        @Override
        public void execute(CPU cpu) {
            Memory memory = cpu.getMemory();

            // get an integer representation of the floating point value
            RegisterFloat fr = cpu.selectFpr(cpu.getRs1().getValue());
            int signAndExponent = fr.getSignAndExponent();
            int mantissa = fr.getMantissa();
            int length = Utilities.floatToFixed(signAndExponent, mantissa);

            // get address of v1
            int v1Address = cpu.getIar().getValue();
            int v2Address = v1Address + 1;

            int v1 = memory.get(v1Address);
            int v2 = memory.get(v2Address);

            for (int i = 0; i < length; i++) {
                int diff = memory.get(v1 + i) - memory.get(v2+i);
                memory.insert(diff, v1+i);
            }
        }
    },

    /**
     * octal 37
     * Convert to Fixed/FloatingPoint:
     * If F = 0, convert c(EA) to a fixed point number and store in r.
     * If F = 1, convert c(EA) to a floating point number and store in FR0.
     * The r register contains the value of F before the instruction is executed.
     */
    CNVRT(31, "CNVRT"){
        @Override
        public void decode(CPU cpu) {}

        @Override
        public void execute(CPU cpu) {
            // F is the value in the specified general purpose register
            Register r = cpu.selectGpr(cpu.getRs1().getValue());
            int F = r.getValue();
            if (F == 0) {
                // convert c(EA) to a fixed point number and store in r
                // C(EA) is the exponent, c(EA+1) is the mantissa
                int signAndExponent = cpu.getMemory().get(cpu.getIar().getValue());
                int mantissa = cpu.getMemory().get(cpu.getIar().getValue()+1);
                int fixed = Utilities.floatToFixed(signAndExponent, mantissa);
                r.setValue(fixed);
            } else if (F == 1) {
                // convert c(EA) to a floating point number and store in FR0
                int value = cpu.getMemory().get(cpu.getIar().getValue());
                int floating = Utilities.fixedToFloat(value);
                cpu.getFr0().setValue(floating);
            } else {
                // F has to be 0 or 1
                cpu.handleMachineFault(Common.ILLEGAL_OPERATION_CODE);
            }
        }
    },

    /**
     * octal 50
     * Load Floating Register From Memory, fr = 0..1
     * fr <− c(EA), c(EA+1)
     * fr <- c(c(EA), c(EA)+1), if I bit set
     */
    LDFR(40, "LDFR"){
        @Override
        public void decode(CPU cpu) {
            cpu.setTargetLocation(Common.REGISTER);
            cpu.setRegisterType(Common.FPR);
        }

        @Override
        public void execute(CPU cpu) {
            // loaded values handled in CPU.java
        }
    },

    /**
     * octal 51
     * Store Floating Register To Memory, fr = 0..1
     * EA, EA+1 <− c(fr)
     * c(EA), c(EA)+1 <- c(fr), if I-bit set
     */
    STFR(41, "STFR"){
        @Override
        public void decode(CPU cpu) {
            cpu.setTargetLocation(Common.MEMORY);
            cpu.setRegisterType(Common.FPR);
        }

        @Override
        public void execute(CPU cpu) {
            // loaded values handled in CPU.java
        }
    };


    private final int opcode;
    private final String name;

    Instruction(int opcode, String name) {
        this.opcode = opcode;
        this.name = name;
    }

    /**
     * Decode portion of the instruction --> mainly used to set rs1 and corresponding flags according to the type of
     * instruction (e.g. where to store the result of the instruction: LDR is in the REGISTER).
     * @param cpu -> A reference to the cpu to be able to access internal registers
     */
    public abstract void decode(CPU cpu);

    /**
     * `Execute` portion of the instruction --> mainly used to set the irr in preparation for the `deposit` step.
     * @param cpu -> A reference to the cpu to be able to access internal registers
     */
    public abstract void execute(CPU cpu);

    /**
     * Returns the Instruction enum based on the opcode value. VeRY useful since we don't need to use an `if`
     * statement for eveRY case.
     * @param opcode -> the opcode value obtained from parsing the instruction
     * @return -> the corresponding Instruction enum if found, otherwise null
     */
    public static Instruction getInstruction(int opcode) {
        for (Instruction i : Instruction.values()) {
            if (i.opcode == opcode) {
                return i;
            }
        }
        return null;
    }


    public String toString() {
        return name;
    }
}