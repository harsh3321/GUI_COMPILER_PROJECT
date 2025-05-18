import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class JavaGUICompilerApp extends JFrame {
    private JTextArea codeArea;
    private JTextArea outputArea;
    private File currentFile = null;

    public JavaGUICompilerApp() {
        setTitle("Java GUI Compiler");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        codeArea = new JTextArea(20, 60);
        JScrollPane scrollPane = new JScrollPane(codeArea);

        outputArea = new JTextArea(10, 60);
        outputArea.setEditable(false);
        JScrollPane outputScroll = new JScrollPane(outputArea);

        JButton compileButton = new JButton("Compile & Run");
        compileButton.addActionListener(e -> compileAndRun());

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(compileButton, BorderLayout.NORTH);
        bottomPanel.add(new JLabel("Output/Error:"), BorderLayout.CENTER);
        bottomPanel.add(outputScroll, BorderLayout.SOUTH);

        setJMenuBar(createMenuBar());
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem newItem = new JMenuItem("New");
        newItem.addActionListener(e -> {
            codeArea.setText("");
            currentFile = null;
        });
        JMenuItem openItem = new JMenuItem("Open");
        openItem.addActionListener(e -> openFile());
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(e -> saveFile());
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);

        menuBar.add(fileMenu);
        return menuBar;
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(currentFile))) {
                codeArea.read(reader, null);
            } catch (IOException e) {
                showError("Error opening file: " + e.getMessage());
            }
        }
    }

    private void saveFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
                codeArea.write(writer);
            } catch (IOException e) {
                showError("Error saving file: " + e.getMessage());
            }
        }
    }

    private void compileAndRun() {
        try {
            if (currentFile == null) {
                showError("Please save the file before compiling.");
                return;
            }

            String filePath = currentFile.getAbsolutePath();
            String fileDir = currentFile.getParent();
            String fileName = currentFile.getName();
            String className = fileName.substring(0, fileName.lastIndexOf('.'));

            if (!codeArea.getText().contains("class " + className)) {
                showError("Class name must match file name: " + className);
                return;
            }

            ProcessBuilder pbCompile = new ProcessBuilder("javac", filePath);
            pbCompile.directory(new File(fileDir));
            Process compileProcess = pbCompile.start();
            compileProcess.waitFor();
            String compileErrors = readStream(compileProcess.getErrorStream());

            if (!compileErrors.isEmpty()) {
                outputArea.setText("Compilation Failed:\n" + compileErrors);
                return;
            }

            ProcessBuilder pbRun = new ProcessBuilder("java", "-cp", fileDir, className);
            pbRun.directory(new File(fileDir));
            Process runProcess = pbRun.start();
            runProcess.waitFor();

            String output = readStream(runProcess.getInputStream());
            String errors = readStream(runProcess.getErrorStream());

            outputArea.setText("--- Output ---\n" + output + "\n--- Errors ---\n" + errors);
        } catch (IOException | InterruptedException e) {
            showError("Error during compile/run: " + e.getMessage());
        }
    }

    private String readStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(JavaGUICompilerApp::new);
    }
}
