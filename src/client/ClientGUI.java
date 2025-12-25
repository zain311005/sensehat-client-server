import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientGUI2 extends JFrame {

    // CHANGE THIS to your Raspberry Pi IP address
    private static final String SERVER_IP = "192.168.1.100";
    private static final int SERVER_PORT = 2003;

    private final JTextArea outputArea = new JTextArea(10, 40);
    private final JLabel statusLabel = new JLabel("Status: Not connected");
    private final JLabel avgLabel = new JLabel("Average: -");
    private final JLabel minLabel = new JLabel("Min: -");
    private final JLabel maxLabel = new JLabel("Max: -");

    private List<Double> lastReadings = new ArrayList<>();
    private Mode lastMode = Mode.NONE;

    enum Mode { NONE, TEMP, HUMID }

    public ClientGUI2() {
        setTitle("Sense HAT Client (Temperature/Humidity)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Buttons
        JButton btnTemp = new JButton("Get Temperature Readings");
        JButton btnHumid = new JButton("Get Humidity Readings");
        JButton btnAvg = new JButton("Average");
        JButton btnMin = new JButton("Min");
        JButton btnMax = new JButton("Max");
        JButton btnPrint = new JButton("Print All");

        JPanel topPanel = new JPanel(new GridLayout(2, 3, 8, 8));
        topPanel.add(btnTemp);
        topPanel.add(btnHumid);
        topPanel.add(btnPrint);
        topPanel.add(btnAvg);
        topPanel.add(btnMin);
        topPanel.add(btnMax);

        // Output Area
        outputArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(outputArea);

        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        statsPanel.add(avgLabel);
        statsPanel.add(minLabel);
        statsPanel.add(maxLabel);
        statsPanel.add(statusLabel);

        add(topPanel, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(statsPanel, BorderLayout.SOUTH);

        // Actions
        btnTemp.addActionListener(e -> requestData(Mode.TEMP));
        btnHumid.addActionListener(e -> requestData(Mode.HUMID));

        btnAvg.addActionListener(e -> showAverage());
        btnMin.addActionListener(e -> showMin());
        btnMax.addActionListener(e -> showMax());
        btnPrint.addActionListener(e -> printAll());

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void requestData(Mode mode) {
        lastMode = mode;
        outputArea.setText("");
        avgLabel.setText("Average: -");
        minLabel.setText("Min: -");
        maxLabel.setText("Max: -");

        statusLabel.setText("Status: Connecting...");

        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             OutputStream out = socket.getOutputStream()) {

            // Send option: '1' for temp, '2' for humidity
            if (mode == Mode.TEMP) out.write('1');
            else out.write('2');
            out.flush();

            String line = in.readLine();
            if (line == null || line.startsWith("ERROR")) {
                statusLabel.setText("Status: Server error");
                outputArea.setText("Server returned: " + line);
                return;
            }

            lastReadings = parseReadings(line);

            statusLabel.setText("Status: Received " + lastReadings.size() + " readings");
            outputArea.append("Received readings:\n" + line + "\n");

            // Auto-show average after fetch (nice UX)
            showAverage();

        } catch (Exception ex) {
            statusLabel.setText("Status: Connection failed");
            outputArea.setText("Error: " + ex.getMessage() + "\n"
                    + "Check SERVER_IP and that Python server is running.");
        }
    }

    private List<Double> parseReadings(String line) {
        List<Double> readings = new ArrayList<>();
        String[] parts = line.trim().split("\\s+");
        for (String p : parts) {
            try {
                readings.add(Double.parseDouble(p));
            } catch (NumberFormatException ignored) {}
        }
        return readings;
    }

    private void showAverage() {
        if (lastReadings.isEmpty()) {
            outputArea.append("\nNo data. Click Temperature/Humidity first.\n");
            return;
        }
        double avg = lastReadings.stream().mapToDouble(x -> x).average().orElse(0);
        avg = round2(avg);
        avgLabel.setText("Average: " + avg);

        applyComfortColor(avg);
        outputArea.append("\nAverage = " + avg + "\n");
    }

    private void showMin() {
        if (lastReadings.isEmpty()) {
            outputArea.append("\nNo data. Click Temperature/Humidity first.\n");
            return;
        }
        double min = lastReadings.stream().mapToDouble(x -> x).min().orElse(0);
        min = round2(min);
        minLabel.setText("Min: " + min);
        outputArea.append("\nMin = " + min + "\n");
    }

    private void showMax() {
        if (lastReadings.isEmpty()) {
            outputArea.append("\nNo data. Click Temperature/Humidity first.\n");
            return;
        }
        double max = lastReadings.stream().mapToDouble(x -> x).max().orElse(0);
        max = round2(max);
        maxLabel.setText("Max: " + max);
        outputArea.append("\nMax = " + max + "\n");
    }

    private void printAll() {
        if (lastReadings.isEmpty()) {
            outputArea.append("\nNo data. Click Temperature/Humidity first.\n");
            return;
        }
        outputArea.append("\nAll readings:\n");
        for (int i = 0; i < lastReadings.size(); i++) {
            outputArea.append("Reading " + (i + 1) + ": " + lastReadings.get(i) + "\n");
        }
    }

    private void applyComfortColor(double avg) {
        // Temperature thresholds:
        // Cold <15°C, Comfortable 15–22°C, Hot >22°C
        // Humidity thresholds:
        // Dry <55%, Sticky 55–65%, Oppressive >65%

        Color color;

        if (lastMode == Mode.TEMP) {
            if (avg > 22) color = new Color(195, 100, 120);          // red-ish
            else if (avg >= 15) color = new Color(120, 190, 120);    // green-ish
            else color = new Color(120, 150, 210);                   // blue-ish
        } else if (lastMode == Mode.HUMID) {
            if (avg > 65) color = new Color(195, 100, 120);          // red-ish
            else if (avg >= 55) color = new Color(220, 200, 120);    // yellow-ish
            else color = new Color(120, 150, 210);                   // blue-ish
        } else {
            return;
        }

        getContentPane().setBackground(color);
        repaint();
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI2::new);
    }
}
