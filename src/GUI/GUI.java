/**
 * CSCI 6461 - Fall 2022
 * 
 * GUI Class serves to interact with the CPU and display it to the user.
 */

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
    private final JFrame mainFrame;  private final JPanel mainPanel;

    // Initial program load
    private JButton IPLButton;   private JFileChooser fileChooser;

    // Memory reference
    private final CPU cpu; private final Memory memory;
    private final InputSwitches inputerSwitches;

    /**
     * FrontPanel constructor to initialize every part of the machine, most of which
     * is added to the panels and displayed to the user.
     */
    public GUI() {
        mainFrame = new JFrame("FA22 CS6461 Simulator");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setPreferredSize(new Dimension(1330, 800));
        // center the gui on the screen on initialization
        mainFrame.setLocationRelativeTo(null);
        mainFrame.getContentPane().setBackground(new Color(173, 216, 230));

        // Panel within the larger Frame
        mainPanel = new JPanel(new GridBagLayout());

        inputerSwitches = new InputSwitches();
        memory = new Memory();
        cpu = new CPU(mainPanel, memory, inputerSwitches);

        addIPL();
        addListeners();
        resetCPUandInput();

        mainFrame.setLayout(new BoxLayout(mainFrame.getContentPane(), BoxLayout.Y_AXIS));
        mainFrame.add(inputerSwitches.getPanel());
        mainFrame.add(mainPanel);
        mainFrame.pack();
    }

    public void startMachine() {
        mainFrame.setVisible(true);
        cpu.boot();
    }

    /**
     * Resets the CPU and the user Input Switches
     */
    private void resetCPUandInput() {
        cpu.reset();
        inputerSwitches.reset();
    }

    /**
     * Adds the IPL button to the front panel
     */
    private void addIPL() {
        fileChooser = new JFileChooser();
        IPLButton = new JButton("IPL");

        IPLButton.setForeground(Color.red);
        IPLButton.setBackground(Color.darkGray);

        Utilities.addComponent(IPLButton, mainPanel, 0, 11, 1);
    }

    /**
     * Reads in values from the IPL.txt file, that stores two hexadecimal numbers
     * per line as follows:
     * XXXX XXXX
     * This can be translated to the full binary instruction and broken down to each component of an effective address
     * 
     * It can be extended to support binary instructions, etc. by changing the functionality of the reader
     * Shown with the testing case for 'testing.txt'
     */
    private void initialProgramLoad() {
        try {
            String s;
            int returnVal = fileChooser.showOpenDialog(mainFrame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                memory.setRunningUserProgram(true);

                // Reset Machine to load user program
                resetCPUandInput();

                File file = fileChooser.getSelectedFile();
                BufferedReader reader = new BufferedReader(new FileReader(file));
                if (file.getName().equalsIgnoreCase("testing.txt")) {
                    while ((s = reader.readLine()) != null) {
                        String[] line = s.split("\\s+");
                        int new_location = Integer.parseInt(line[0], 2);
                        int new_value = Integer.parseInt(line[1], 2);
                        System.out.println(new_value + " inserted into memory location " + new_location);
                        memory.insert(new_value, new_location);
                    }
                    cpu.displayRaw(true); // Displays inpput values
                    reader.close();
                }else {
                    while ((s = reader.readLine()) != null) {
                        String[] line = s.split("\\s+");
                        int new_location = memory.hexToDec(line[0]);
                        int new_value = memory.hexToDec(line[1]);
                        System.out.println(new_value + " inserted into memory location " + new_location);
                        memory.insert(new_value, new_location);
                    }
                    reader.close();
                }

                
                System.out.println(memory.getMemoryString());
                String success = "IPL file loaded successfully. Press Run or Single Step to execute the program.";
                JOptionPane.showMessageDialog(mainFrame, success, "Success", JOptionPane.INFORMATION_MESSAGE);

                // program2 specific memory loading
                if (file.getName().equalsIgnoreCase("program2.txt")) {
                    loadParagraph();
                    cpu.displayRaw(false);
                } else if (file.getName().equalsIgnoreCase("program1.txt")) {
                    cpu.displayRaw(true);
                }

                memory.setRunningUserProgram(false);
            }
        } catch (Exception e) {
            String error = "There was an error loading the IPL file";
            JOptionPane.showMessageDialog(mainFrame, error, "Error", JOptionPane.INFORMATION_MESSAGE);
            System.out.println(error + ": " + e.getMessage());
            memory.setRunningUserProgram(false);
        }
    }

    /**
     * Loads a paragraph of 6 sentences into memory location 1024 (1/2 of allocated memory)
     *
     * Used for program2.txt
     */
    private void loadParagraph() {
        System.out.println("Started Loading paragraph");
        try {
            int returnVal = fileChooser.showOpenDialog(mainFrame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();

                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
    
                int location = Common.PROGRAM2_PARAGRAPH_INDEX;
                while ((line = reader.readLine()) != null) {
                    String[] sentence = line.split("");
                    for (String c : sentence) {
                        memory.insert(c.charAt(0), location);
                        location++;
                    }
                    // Enter at the end of each line
                    memory.insert(13, location);
                    location++;
                }
                reader.close();
                // EOT to indicate end of paragraph
                memory.insert(4, location);
                System.out.println("Loaded paragraph into memory");
    
                String success = "6 sentence paragraph loaded into memory location 1024 (for program 2)";
                JOptionPane.showMessageDialog(mainFrame, success, "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Failed to load file.\nPlease Try Again.", "Error", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            System.out.println("No paragraph file found: " + e.getMessage());
        }
    }

    /**
     * Adds listeners for the IPL and view memory buttons
     */
    private void addListeners() {
        // Load new program
        IPLButton.addActionListener(ae -> initialProgramLoad());
    }
}
