import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class DinoGame extends JPanel implements Runnable, KeyListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 400;
    private static final int GROUND = 350;
    private static final int GRAVITY = 1;
    private static final int JUMP_STRENGTH = -15;

    private Thread gameThread;
    private boolean isRunning = true;
    private boolean isJumping = false;
    private int dinoY = GROUND;
    private int dinoVelocity = 0;
    private int obstacleX = WIDTH;
    private int score = 0;

    public DinoGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.WHITE);
        setFocusable(true);
        addKeyListener(this);
    }

    public void startGame() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        while (isRunning) {
            update();
            repaint();
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void update() {
        // Update dino position
        if (isJumping) {
            dinoY += dinoVelocity;
            dinoVelocity += GRAVITY;
            if (dinoY >= GROUND) {
                dinoY = GROUND;
                isJumping = false;
            }
        }

        // Update obstacle position
        obstacleX -= 5;
        if (obstacleX < 0) {
            obstacleX = WIDTH;
            score++;
        }

        // Check for collision
        if (obstacleX < 100 && dinoY >= GROUND - 50) {
            isRunning = false;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw dino
        g.setColor(Color.BLACK);
        g.fillRect(50, dinoY, 50, 50);

        // Draw obstacle
        g.setColor(Color.RED);
        g.fillRect(obstacleX, GROUND - 50, 50, 50);

        // Draw score
        g.setColor(Color.BLACK);
        g.drawString("Score: " + score, 10, 20);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE && !isJumping) {
            isJumping = true;
            dinoVelocity = JUMP_STRENGTH;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Dino Game");
        DinoGame game = new DinoGame();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        game.startGame();
    }
}