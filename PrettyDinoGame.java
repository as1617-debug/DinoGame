// Save as PrettyDinoGame.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class PrettyDinoGame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Dino Run");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            GamePanel panel = new GamePanel();
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            panel.start();
        });
    }
}

class GamePanel extends JPanel implements Runnable, KeyListener {
    public static final int WIDTH = 900;
    public static final int HEIGHT = 300;

    private Dino dino;
    private ArrayList<Obstacle> obstacles;
    private ArrayList<Cloud> clouds;

    private Thread thread;
    private boolean running = false;
    private boolean gameOver = false;

    private Random rand = new Random();
    private int score = 0;
    private long lastSpawn = 0;
    private long lastCloudSpawn = 0;
    private int speed = 8;
    private Color skyColor = new Color(240, 248, 255);

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(skyColor);
        setFocusable(true);
        addKeyListener(this);

        dino = new Dino(70, HEIGHT - 90);
        obstacles = new ArrayList<>();
        clouds = new ArrayList<>();
    }

    public void start() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        long lastTime = System.currentTimeMillis();
        while (running) {
            long now = System.currentTimeMillis();
            long elapsed = now - lastTime;
            if (elapsed > 1000 / 60) { // ~60 FPS
                updateGame(elapsed);
                repaint();
                lastTime = now;
            }
        }
    }

    private void updateGame(long elapsed) {
        if (gameOver) return;

        dino.update(elapsed);

        // Spawn obstacles
        if (System.currentTimeMillis() - lastSpawn > 1300 + rand.nextInt(800)) {
            obstacles.add(new Obstacle(WIDTH, HEIGHT - 70, 30 + rand.nextInt(20), 50));
            lastSpawn = System.currentTimeMillis();
        }

        // Spawn clouds
        if (System.currentTimeMillis() - lastCloudSpawn > 3000 + rand.nextInt(2000)) {
            clouds.add(new Cloud(WIDTH, 30 + rand.nextInt(120)));
            lastCloudSpawn = System.currentTimeMillis();
        }

        // Update obstacles
        Iterator<Obstacle> it = obstacles.iterator();
        while (it.hasNext()) {
            Obstacle ob = it.next();
            ob.update(speed);
            if (ob.getX() + ob.getWidth() < 0) it.remove();
            if (ob.getBounds().intersects(dino.getBounds())) gameOver = true;
        }

        // Update clouds
        Iterator<Cloud> cl = clouds.iterator();
        while (cl.hasNext()) {
            Cloud c = cl.next();
            c.update(2);
            if (c.getX() + c.getWidth() < 0) cl.remove();
        }

        score++;
        // Gradually change to night and back
        float phase = (float)Math.sin(score / 5000.0);
        skyColor = new Color(
            (int)(240 - 40 * (1 - phase)),
            (int)(248 - 80 * (1 - phase)),
            (int)(255 - 120 * (1 - phase))
        );

        if (score % 1000 == 0) speed++; // Increase difficulty
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(skyColor);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw clouds
        for (Cloud c : clouds) c.draw(g2);

        // Draw ground
        g2.setColor(new Color(210, 180, 140));
        g2.fillRect(0, HEIGHT - 40, WIDTH, 50);
        g2.setColor(new Color(139, 69, 19));
        for (int i = 0; i < WIDTH; i += 20) {
            g2.fillRect(i, HEIGHT - 40, 10, 3);
        }

        // Draw dino and obstacles
        dino.draw(g2);
        for (Obstacle ob : obstacles) ob.draw(g2);

        // Draw score
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Consolas", Font.BOLD, 18));
        g2.drawString("Score: " + score, WIDTH - 150, 30);

        // Game Over text
        if (gameOver) {
            g2.setFont(new Font("Consolas", Font.BOLD, 40));
            String msg = "GAME OVER";
            int w = g2.getFontMetrics().stringWidth(msg);
            g2.drawString(msg, (WIDTH - w) / 2, HEIGHT / 2 - 20);
            g2.setFont(new Font("Consolas", Font.PLAIN, 20));
            String retry = "Press SPACE to Restart";
            int rw = g2.getFontMetrics().stringWidth(retry);
            g2.drawString(retry, (WIDTH - rw) / 2, HEIGHT / 2 + 20);
        }

        g2.dispose();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (gameOver) {
                obstacles.clear();
                clouds.clear();
                dino.reset();
                score = 0;
                speed = 8;
                gameOver = false;
            } else {
                dino.jump();
            }
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}

class Dino {
    private int x, y, width = 45, height = 50;
    private double velocityY = 0;
    private final double gravity = 0.002;
    private boolean onGround = true;
    private final int groundY;

    public Dino(int x, int groundY) {
        this.x = x;
        this.groundY = groundY;
        this.y = groundY;
    }

    public void reset() {
        velocityY = 0;
        onGround = true;
        y = groundY;
    }

    public void jump() {
        if (onGround) {
            velocityY = -1.0;
            onGround = false;
        }
    }

    public void update(long elapsed) {
        if (!onGround) {
            velocityY += gravity * elapsed;
            y += (int)(velocityY * elapsed);
            if (y >= groundY) {
                y = groundY;
                velocityY = 0;
                onGround = true;
            }
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public void draw(Graphics2D g) {
        g.setColor(new Color(50, 50, 50));
        g.fillRoundRect(x, y, width, height, 10, 10);
        g.setColor(Color.WHITE);
        g.fillOval(x + 25, y + 10, 8, 8);
        g.setColor(Color.BLACK);
        g.fillOval(x + 27, y + 12, 3, 3);
    }
}

class Obstacle {
    private int x, y, width, height;
    private Color color = new Color(34, 139, 34);

    public Obstacle(int x, int y, int width, int height) {
        this.x = x; this.y = y;
        this.width = width; this.height = height;
    }

    public void update(int speed) {
        x -= speed;
    }

    public void draw(Graphics2D g) {
        g.setColor(color);
        g.fillRoundRect(x, y, width, height, 6, 6);
    }

    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
    public int getX() { return x; }
    public int getWidth() { return width; }
}

class Cloud {
    private int x, y, width = 60, height = 25;
    private Color color = new Color(255, 255, 255, 200);

    public Cloud(int x, int y) { this.x = x; this.y = y; }

    public void update(int speed) { x -= speed / 2; }

    public void draw(Graphics2D g) {
        g.setColor(color);
        g.fillOval(x, y, width, height);
        g.fillOval(x + 15, y - 10, width, height);
        g.fillOval(x + 30, y, width, height);
    }

    public int getX() { return x; }
    public int getWidth() { return width; }
}
