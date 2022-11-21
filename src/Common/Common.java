package Common;

public class Common {
    // used to specify deposit locations
    public static final String REGISTER = "REGISTER";
    public static final String MEMORY = "MEMORY";
    public static final String GPR = "GPR";
    public static final String IXR = "IXR";
    public static final String FPR = "FPR";

    // machine faults
    public static final int ILLEGAL_MEMORY_ADDRESS_RESERVED_LOCATION = 0;
    public static final int ILLEGAL_TRAP_CODE = 1;
    public static final int ILLEGAL_OPERATION_CODE = 2;
    public static final int ILLEGAL_MEMORY_ADDRESS_OUT_OF_BOUNDS = 3;

    // octal 10
    public static final int BOOT_PROGRAM_ADDRESS = 8;
    public static final int BOOT_PROGRAM_LENGTH = 54;
    // location to load user programs to
    public static final int USER_PROGRAM_OFFSET = BOOT_PROGRAM_ADDRESS + BOOT_PROGRAM_LENGTH + BOOT_PROGRAM_ADDRESS;

    // For program2: used to store the paragraph. (user-memory location 1024)
    public static final int PROGRAM2_PARAGRAPH_INDEX = 1024;

    public Common() {
    }
}