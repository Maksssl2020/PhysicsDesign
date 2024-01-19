package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ObliqueProjection extends JFrame {
    private static final int MIN_INITIAL_SPEED_VALUE = 5;
    private static final int MAX_INITIAL_SPEED_VALUE = 10;
    private static final int MIN_THROW_ANGLE_VALUE = 1;
    private static final int MAX_THROW_ANGLE_VALUE = 90;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int MARGIN_LENGTH = 100;
    private static final int OX_LENGTH = 11;
    private static final int OY_LENGTH = 6;
    private static final int SCALE_FACTOR = 60;
    private final double g = 9.81;
    private final double dt = 0.1;
    private double initialSpeed = 0;
    private double throwAngle = 0;
    private double x = 0;
    private double y = 0;
    private double vx = 0;
    private double vy = 0;
    private volatile boolean running = false;
    private double maxHeight = 0;
    private double maxSpeed = 0;
    private double range = 0;
    private double currentSpeed = 0;
    private Thread simulationThread;
    private JTextField currentSpeedTextField;

    public ObliqueProjection() {
        setTitle("Symulacja rzutu ukośnego"); // Simulation of oblique projection
        setSize(WIDTH, HEIGHT);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        JPanel mainPanel = createMainPanel();
        addButtonsAndFieldsToTheMainPanel(mainPanel);
        setVisible(true);
    }

    private JPanel createMainPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g1) {
                super.paintComponent(g1);
                paintCoordinateSystemAndPoint(g1);
            }
        };
    }

    private void addButtonsAndFieldsToTheMainPanel(JPanel mainPanel) {
        JButton startButton = new JButton("Start");
        JTextField initialSpeedTextField = new JTextField("7.5", 5);
        JTextField throwAngleTextField = new JTextField("45", 5);
        currentSpeedTextField = new JTextField(String.valueOf(currentSpeed), 5);

        startButton.addActionListener((ActionEvent e) -> {
            if (simulationThread != null) {
                simulationThread.interrupt();
            }
            if (isInitialSpeedDataCorrect(initialSpeedTextField.getText())) return;
            if (isThrowAngleDataCorrect(throwAngleTextField.getText())) return;
            prepareDataToStartSimulation();
            startSimulation(mainPanel);
        });

        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener((ActionEvent e) -> {
            running = false;
        });

        JButton continueButton = new JButton("Kontynuuj"); // Continue
        continueButton.addActionListener((ActionEvent e) -> {
            if (!running) {
                startSimulation(mainPanel);
            }
        });

        mainPanel.add(new JLabel("Prędkość początkowa: ")); // Initial speed:
        mainPanel.add(initialSpeedTextField);
        mainPanel.add(new JLabel("Kąt rzutu: ")); // Throw angle:
        mainPanel.add(throwAngleTextField);
        mainPanel.add(new JLabel("Aktualna prędkość (m/s): ")); // Actual speed (m/s):
        mainPanel.add(currentSpeedTextField);
        mainPanel.add(startButton);
        mainPanel.add(stopButton);
        mainPanel.add(continueButton);
        add(mainPanel);
    }

    private boolean isInitialSpeedDataCorrect(String initialSpeedTextData) {
        initialSpeed = Double.parseDouble(initialSpeedTextData);
        if (initialSpeed < MIN_INITIAL_SPEED_VALUE || initialSpeed > MAX_INITIAL_SPEED_VALUE) {
            showDataErrorMessage("Prędkość początkowa musi być w zakresie od 5 do 10 m/s!"); // Initial speed must be between 5 and 10 m/s!
            return true;
        }
        return false;
    }

    private boolean isThrowAngleDataCorrect(String throwAngleTextData) {
        throwAngle = Double.parseDouble(throwAngleTextData);
        if (throwAngle < MIN_THROW_ANGLE_VALUE || throwAngle > MAX_THROW_ANGLE_VALUE) {
            showDataErrorMessage("Kąt rzutu musi być w zakresie od 1 do 90 stopni!"); // Throw angle must be between 1 and 90 degrees!
            return true;
        }
        return false;
    }

    private void showDataErrorMessage(String errorMessage) {
        JOptionPane.showMessageDialog(this, errorMessage, "Nieprawidłowe dane!", JOptionPane.ERROR_MESSAGE); // Invalid data!
    }

    private void prepareDataToStartSimulation() {
        double radians = Math.toRadians(throwAngle);
        vx = initialSpeed * Math.cos(radians);
        vy = initialSpeed * Math.sin(radians);
        x = 0;
        y = 0;
        maxHeight = 0;
        maxSpeed = 0;
    }

    private void paintCoordinateSystemAndPoint(Graphics g) {
        int margin = HEIGHT - MARGIN_LENGTH;

        for (int i = 0; i <= OX_LENGTH; i++) {
            int xPosition = MARGIN_LENGTH + i * SCALE_FACTOR;
            g.drawLine(xPosition, margin, xPosition, margin - OY_LENGTH * SCALE_FACTOR);
            g.drawString(Integer.toString(i), xPosition - 5, margin + 20);
        }

        for (int i = 0; i <= OY_LENGTH; i++) {
            int yPosition = margin - i * SCALE_FACTOR;
            g.drawLine(MARGIN_LENGTH, yPosition, MARGIN_LENGTH + OX_LENGTH * SCALE_FACTOR, yPosition);
            g.drawString(Integer.toString(i), MARGIN_LENGTH - 30, yPosition + 5);
        }

        g.setColor(Color.BLUE);
        g.fillOval((int) (MARGIN_LENGTH + x * SCALE_FACTOR), (int) (margin - y * SCALE_FACTOR), 10, 10);
    }


    private void startSimulation(JPanel panel) {
        running = true;
        simulationThread = new Thread(() -> {
            while (y >= 0) {
                if (!running) {
                    break;
                }

                updatePositionData();
                updateCurrentSpeed();
                updateSummaryData();
                panel.repaint();

                try {
                    Thread.sleep((long) (dt * 1000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            if (y < 0) {
                displaySummaryMessage();
                running = false;
            }
        });
        simulationThread.start();
    }

    private void updatePositionData() {
        x += vx * dt;
        y += vy * dt;
        vy -= g * dt;
    }

    private void updateSummaryData() {
        if (currentSpeed > maxSpeed) {
            maxSpeed = currentSpeed;
        }

        if (y > maxHeight) {
            maxHeight = y;
        }

        range = x;
    }

    private void updateCurrentSpeed() {
        currentSpeed = Math.sqrt(vx * vx + vy * vy);
        SwingUtilities.invokeLater(() -> currentSpeedTextField.setText(String.format("%.2f", currentSpeed)));
    }


    private void displaySummaryMessage() {
        StringBuilder finalMessageBuilder = new StringBuilder()
                .append(String.format("Prędkość początkowa: %.1f m/s\n", initialSpeed)) // Initial speed:
                .append(String.format("Kąt rzutu: %.1f stopni\n", throwAngle)) // Throw angle:
                .append(String.format("Zasięg: %.2f m\n", range)) // Range:
                .append(String.format("Maksymalna wysokość: %.2f m\n", maxHeight)) // Maximum height:
                .append(String.format("Maksymalna prędkość: %.2f m/s", maxSpeed)); // Maximum speed:

        JOptionPane.showMessageDialog(this,
                finalMessageBuilder.toString(),
                "Podsumowanie", JOptionPane.INFORMATION_MESSAGE); // Summary
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ObliqueProjection::new);
    }
}
