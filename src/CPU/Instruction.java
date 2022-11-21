/**
 * CSCI 6461 - Fall 2022
 * 
 * Instruction - Contains all Instructions needed for P1-3
 * * P2 had a massive switch statement, P3 now has an enum construction 
 */

package CPU;

import Common.Common;
import Common.Utilities;
import Memory.Memory;

public enum Instruction {

    // 0 - HLT
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
    
    // 1 - LDR
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
    
    // 2 - STR
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
    
    // 3 - LDA
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
	
    // 4 - AMR
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
	
    // 5 - SMR
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
	
    // 6 - AIR
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
	
    // 7 - SIR
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
    
    // 10 - JZ
    JZ (8, "JZ") {
        @Override
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

    // 11 - JNE
    JNE (9, "JNE") {
        @Override
        public void decode(CPU cpu) { }

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
    // 12 - JCC
    JCC (10, "JCC") {
        @Override
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
    // 13 - JMA
    JMA (11, "JMA") {
        @Override
        public void decode(CPU cpu) {}

        @Override
        public void execute(CPU cpu) {
        	cpu.setNextPc(cpu.getIar().getValue());
        	// cpu.setGpr(false);
        }
    },
    // 14 - JSR
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
    // 15 - RFS
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
    // 16 - SOB
    SOB (14, "SOB") {
        @Override
        public void decode(CPU cpu) {}

        @Override
        public void execute(CPU cpu) {
            Register r = cpu.selectGpr(cpu.getRs1().getValue());
            r.setValue(r.getValue() - 1);
            if (r.getValue() > 0) {
                cpu.setNextPc(cpu.getIar().getValue());
            }
        }
    },
    
    // 17 - JGE
    JGE (15, "JGE") {
        @Override
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
	// 20 - MLT
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
	// 21 - DVD
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
	// 22 - TRR
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
	// 23 - AND
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
	// 24 - ORR
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
	// 25 - NOT
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
    // 30 - TRAP
    TRAP (24, "TRAP") {
        @Override
        public void decode(CPU cpu) {}

        @Override
        public void execute(CPU cpu) {
            cpu.setNextPc(0);
            cpu.getMemory().insert(cpu.getPc().getValue()+1,2);
        }
    },
	// 31 - SRC
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
	// 32 - RRC
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
    
    // 33 - FADD
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

    // 34 - FSUB
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
    
    // 35 - VADD
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

    
    // 36 - VSUB 
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

    // 37 - CNVRT
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
   
    // 41 - LDX
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
    
    // 42 - STX
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

    
    // 50 - DLFR
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

    // 51 - STFR
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
    },
    
    // 61 - IN
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
    
    // 62 - OUT
    OUT (50, "OUT") {
        @Override
        public void decode(CPU cpu) {
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
    
    // 63 - CHK
    CHK (51, "CHK") {
        @Override
        public void decode(CPU cpu) {}

        @Override
        public void execute(CPU cpu) {}
    };


    private final int opcode;
    private final String name;

    Instruction(int opcode, String name) {
        this.opcode = opcode;
        this.name = name;
    }

    /**
     * Decode portion of the instruction
     * @param cpu -> A reference to the cpu to be able to access internal registers
     */
    public abstract void decode(CPU cpu);

    /**
     * `Execute` portion of the instruction
     * @param cpu -> A reference to the cpu to be able to access internal registers
     */
    public abstract void execute(CPU cpu);

    /**
     * Returns the Instruction enum based on the opcode value
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


    public String toString() { return name; }
}