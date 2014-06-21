package codetoJar;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalTime;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 *
 * @author Quincunx
 */
public class DeciClock {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DeciClock();
        });
    }
    static final int SCREEN_RES = Toolkit.getDefaultToolkit().getScreenResolution();
    // The size of the clock; we are letting the clock fill the panel.
    // The clock is going to be 2 inches wide. We add extra pixels so
    // the close button is more accessible.
    static Dimension PANEL_DIM = new Dimension((int) (SCREEN_RES * 2.1), (int) (SCREEN_RES * 2.1));
    // The panel is completely transparent
    static final Color PANEL_COLOR = new Color(0, 0, 0, 0);
    // These colors are for the hands of the clock. They are initialized
    // like this so that they are transparent.
    static final Color[] COLORS = {new Color(255, 255, 255, 150), // White
                                   new Color(0, 0, 255, 150), // Blue
                                   new Color(255, 0, 0, 150), // Red
                                   new Color(0, 255, 0, 150), // Green
                                   new Color(0, 255, 255, 150)};  // Cyan
    // The location of the close "button"
    static final Point CLOSE_LOCATION = new Point(SCREEN_RES * 2 - SCREEN_RES / 10, SCREEN_RES / 10);

    LocalTime time;
    JFrame frame;
    JPanel panel;
    boolean mouseOverClose; // true iff the mouse is over the close button

    public DeciClock() {
        time = LocalTime.now();
        mouseOverClose = false;
        initFrame();
        initPanel();
        startGUI();
        Timer timer = new Timer(30, (e) -> {
            time = LocalTime.now();
            frame.repaint();
        });
        timer.start();
    }

    private void initFrame() {
        frame = new JFrame();
        frame.setUndecorated(true);
        frame.setOpacity(0.9f);
        frame.setBackground(PANEL_COLOR);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setAlwaysOnTop(true);

        MouseAdapter mouse = new MouseAdapter() {
            boolean mouseOver = false;
            int xOff = 0;
            int yOff = 0;

            @Override
            public void mouseDragged(MouseEvent e) {
                frame.setLocation(e.getXOnScreen() - xOff, e.getYOnScreen() - yOff);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mouseOverClose = Math.abs(e.getX() - CLOSE_LOCATION.x) < SCREEN_RES / 6
                                 && Math.abs(e.getY() - CLOSE_LOCATION.y) < SCREEN_RES / 6;
                if (mouseOver != mouseOverClose) {
                    frame.repaint();
                    mouseOver = mouseOverClose;
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (mouseOverClose) {
                    mouseOverClose = false;
                    frame.repaint();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                xOff = e.getX();
                yOff = e.getY();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (mouseOverClose) {
                    System.exit(0);
                }
            }
        };

        frame.addMouseMotionListener(mouse);
        frame.addMouseListener(mouse);
    }

    private void initPanel() {
        panel = new JPanel() {
            @Override
            public void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);

                double[] times = getTimes(time);

                Graphics2D g = (Graphics2D) graphics;
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                   RenderingHints.VALUE_ANTIALIAS_ON);

                int radius = SCREEN_RES;
                int x = super.getWidth() / 2;
                int y = super.getHeight() / 2;
                drawClockBody(g, radius, x, y);

                g.setStroke(new BasicStroke(4));

                drawCloseButton(g);
                drawHands(g, times, x, y, radius);
            }
        };
        panel.setPreferredSize(PANEL_DIM);
        panel.setBackground(PANEL_COLOR);
    }

    private void drawClockBody(Graphics2D g, int radius, int x, int y) {
        g.setColor(Color.GRAY);
        g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        g.setColor(Color.DARK_GRAY);
        g.setStroke(new BasicStroke(2));
        int length = SCREEN_RES / 5;
        for (int i = 0; i < 10; i++) {
            double sin = Math.sin(i * 36 * Math.PI / 180);
            double cos = Math.cos(i * 36 * Math.PI / 180);
            g.drawLine(x + (int) (sin * radius),
                       y + (int) (cos * radius),
                       x + (int) (sin * (radius - length)),
                       y + (int) (cos * (radius - length)));
        }
    }

    private void drawCloseButton(Graphics g) {
        // Here we make a space around the close button that can be clicked.
        // The transparent window does not allow the mouse to click it otherwise
        // An alpha of 1 is basically invisible so it suits our purpose well
        g.setColor(new Color(0, 0, 0, 1));
        int dist = SCREEN_RES / 6;
        g.fillOval(CLOSE_LOCATION.x - dist,
                   CLOSE_LOCATION.y - dist,
                   dist * 2, dist * 2);

        // Actual Close button drawing.
        g.setColor(mouseOverClose ? Color.RED : new Color(125, 0, 0));
        int vary = SCREEN_RES / 20;
        g.drawLine(CLOSE_LOCATION.x - vary,
                   CLOSE_LOCATION.y - vary,
                   CLOSE_LOCATION.x + vary,
                   CLOSE_LOCATION.y + vary);
        g.drawLine(CLOSE_LOCATION.x - vary,
                   CLOSE_LOCATION.y + vary,
                   CLOSE_LOCATION.x + vary,
                   CLOSE_LOCATION.y - vary);
    }

    private void drawHands(Graphics g, double[] times, int x, int y, int radius) {
        int length = radius - 2;
        for (int index = 0; index < times.length; index++) {
            // 36 * (value of hand we are accessing)
            // We are dividing the clock into 10s, so 36 degrees
            double degrees = 36 * times[times.length - index - 1]; // start with second hand
            g.setColor(COLORS[COLORS.length - index - 1]);
            int xOff = (int) Math.round(Math.sin(-degrees * Math.PI / 180) * length);
            int yOff = (int) Math.round(Math.cos(-degrees * Math.PI / 180) * length);
            g.drawLine(x, y, x - xOff, y - yOff);
            length -= SCREEN_RES / 10; // Shorten clocks.
        }
    }

    private static double[] getTimes(LocalTime time) {
        int hour = time.getHour();
        int min = time.getMinute();
        int sec = time.getSecond();
        int nano = time.getNano();
        double currentTime = hour;
        currentTime += min / 60.0;
        currentTime += sec / 3600.0;
        currentTime += nano / 1e9 / 3600.0;
        double deciDays = (currentTime * 5 / 12) % 10;
        double centiDays = (currentTime * 50 / 12) % 10;
        double milliDays = (currentTime * 500 / 12) % 10;
        double deciMilliDays = (currentTime * 5000 / 12) % 10;
        double centiMilliDays = (currentTime * 50000 / 12) % 10;

        return new double[]{deciDays, centiDays, milliDays, deciMilliDays,
                            centiMilliDays};
    }

    private void startGUI() {
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
