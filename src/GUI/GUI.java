package GUI;

import javax.swing.*;

import CPU.*;
import Common.Common;
import Common.Utilities;
import Memory.Memory;

import java.awt.*;
import java.io.*;

public class GUI {
    // main frame and panels to hold other components
    private final JFrame frame;
    private final JPanel mainPanel;

    // Initial program load
    private JButton ipl;
    private JFileChooser fileChooser;

    // Memory reference
    private final Memory memory;

    // CPU reference
    private final CPU cpu;

    private final InputSwitches switches;

    /**
     * FrontPanel constructor to initialize every part of the machine, most of which
     * is added to the panels and displayed to the user.
     */
    public GUI() {
        frame = new JFrame("FA22 CS6461 Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1330, 800));
        // center the gui on the screen on initialization
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(new Color(173, 216, 230));

        // initialize the panels
        mainPanel = new JPanel(new GridBagLayout());

        // initialize all the parts
        switches = new InputSwitches();
        memory = new Memory();
        cpu = new CPU(mainPanel, memory, switches);

        addIPL();
        addListeners();
        resetMachineState();

        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.add(switches.getPanel());
        frame.add(mainPanel);
        frame.pack();
    }

    public void startMachine() {
        frame.setVisible(true);
        cpu.boot();
    }

    /**
     * Resets the cpu and the front panel switches
     */
    private void resetMachineState() {
        // reset CPU registers
        cpu.reset();

        // reset the switches
        switches.reset();
    }

    /**
     * Adds the IPL button to the front panel
     */
    private void addIPL() {
        fileChooser = new JFileChooser();
        ipl = new JButton("IPL");

        ipl.setForeground(Color.red);
        ipl.setBackground(Color.black);

        Utilities.addComponent(ipl, mainPanel, 0, 11, 1);
    }

    /**
     * Reads in values from the IPL.txt file, that stores two hexadecimal numbers
     * per line as follows:
     * XXXX XXXX
     * where the first hex number is a machine address, and the second number is the
     * content to be stored
     * at that address. Note that because we don't actually want user programs
     * inserting into memory 0,
     * the program's instructions will load starting at location octal 10 + length
     * of boot program + octal 10
     */
    private void initialProgramLoad() {
        try {
            String s;
            int returnVal = fileChooser.showOpenDialog(frame);
            // if a file was successfully selected
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                memory.setRunningUserProgram(true);
                // reset the machine and memory values
                resetMachineState();
                // reset user memory to load a new program
                memory.resetUserMemory();

                File f = fileChooser.getSelectedFile();
                BufferedReader br = new BufferedReader(new FileReader(f));

                // updates memory after reading in IPL.txt
                if (f.getName().equalsIgnoreCase("testing.txt")) {
                    while ((s = br.readLine()) != null) {
                        String[] line = s.split("\\s+");
                        int new_location = Integer.parseInt(line[0], 2);
                        int new_value = Integer.parseInt(line[1], 2);
                        System.out.println(new_value + " inserted into memory location " + new_location);
                        memory.insert(new_value, new_location);
                    }
                    cpu.setProgram1(true);
                    br.close();
                }else {
                    while ((s = br.readLine()) != null) {
                        String[] line = s.split("\\s+");
                        int new_location = memory.hexToDec(line[0]);
                        int new_value = memory.hexToDec(line[1]);
                        System.out.println(new_value + " inserted into memory location " + new_location);
                        memory.insert(new_value, new_location);
                    }
    
                    br.close();
                }

                
                System.out.println(memory.getMemoryString());
                String success = "IPL file loaded successfully. Press Run or Single Step to execute the program.";
                JOptionPane.showMessageDialog(frame, success, "Success", JOptionPane.INFORMATION_MESSAGE);

                // program2 specific memory loading
                if (f.getName().equalsIgnoreCase("program2.txt")) {
                    loadParagraph();
                    cpu.setProgram1(false);
                } else if (f.getName().equalsIgnoreCase("program1.txt")) {
                    cpu.setProgram1(true);
                }

                memory.setRunningUserProgram(false);
            }
        } catch (Exception e) {
            String error = "There was an error loading the IPL file";
            JOptionPane.showMessageDialog(frame, error, "Error", JOptionPane.INFORMATION_MESSAGE);
            System.out.println(error + ": " + e.getMessage());
            memory.setRunningUserProgram(false);
        }
    }

    /**
     * Loads a paragraph of 6 sentences into memory location 1024
     *
     * Used for program2.txt
     */
    private void loadParagraph() {
        System.out.println("Loading paragraph");
        // load boot program
        try {
            InputStream instream = getClass().getResourceAsStream("/programs/paragraph.txt");
            if (instream == null) {
                System.out.println("Error reading paragraph; input stream is null");
                return;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(instream));
            String line;

            int location = Common.PROGRAM2_PARAGRAPH_INDEX;
            while ((line = br.readLine()) != null) {
                String[] sentence = line.split("");
                for (String c : sentence) {
                    memory.insert(c.charAt(0), location);
                    location++;
                }
                // add "enter" character
                memory.insert(13, location);
                location++;
            }
            // insert EOT to indicate end of paragraph
            memory.insert(4, location);
            System.out.println("Loaded paragraph into memory");

            String success = "6 sentence paragraph loaded into memory location 1024 (for program 2)";
            JOptionPane.showMessageDialog(frame, success, "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            System.out.println("No paragraph file found: " + e.getMessage());
        }
    }

    /**
     * Adds listeners for the IPL and view memory buttons
     */
    private void addListeners() {
        // Load new program
        ipl.addActionListener(ae -> initialProgramLoad());
    }
}
