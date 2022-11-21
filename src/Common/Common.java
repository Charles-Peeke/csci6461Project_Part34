/**
 * CSCI 6461 - Fall 2022
 * 
 * This is a common file that contains variables that hardly change that are used in multiple locations
 */

package Common;

public class Common {
    
    // Shortcuts to save results of ALU and other instructions
    public static final String REGISTER = "REGISTER";
    public static final String MEMORY = "MEMORY";
    public static final String GPR = "GPR";
    public static final String IXR = "IXR";
    public static final String FPR = "FPR";

    // Faults - 
    public static final int ILLEGAL_MEMORY_ADDRESS_RESERVED_LOCATION = 0;
    public static final int ILLEGAL_TRAP_CODE = 1;
    public static final int ILLEGAL_OPERATION_CODE = 2;
    public static final int ILLEGAL_MEMORY_ADDRESS_OUT_OF_BOUNDS = 3;

    // Offsets for the boot program to not overwrite the fixed addresses
    public static final int BOOT_PROGRAM_ADDRESS = 8;
    public static final int BOOT_PROGRAM_LENGTH = 54;

    // User Programs should be stored after the boot program
    public static final int USER_PROGRAM_OFFSET = BOOT_PROGRAM_ADDRESS + BOOT_PROGRAM_LENGTH + BOOT_PROGRAM_ADDRESS;

    // For program2: used to store the paragraph. (user-memory location 1024)
    public static final int PROGRAM2_PARAGRAPH_INDEX = 1024;

    public Common() {
    }
}