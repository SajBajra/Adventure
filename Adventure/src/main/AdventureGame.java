package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

public class AdventureGame extends JPanel implements ActionListener {
    private static final int SCREEN_WIDTH = 800;
    private static final int SCREEN_HEIGHT = 600;
    private static final int PLAYER_SIZE = 20;
    private static final int GAME_SPEED = 120;
    private static final int BULLET_SIZE = 5;
    private static final int DRAGON_SIZE = 100;
    private static final int MAX_HEALTH = 100;
    private static final int DRAGON_BEAM_WIDTH = 10; // Width of the dragon's beam
    private static final int PLAYER_BEAM_RANGE = 600; // Max distance for player beam

    private int playerX, playerY;
    private int playerHealth = MAX_HEALTH;
    private Timer timer;
    private HashMap<Point, String> gameMap;
    private boolean hasGun = false;
    private boolean dragonDefeated = false;
    private boolean dragonAppeared = false; // Dragon appearance flag
    private int dragonX, dragonY;
    private int dragonHealth = MAX_HEALTH;
    private boolean dragonBeamActive = false; // Dragon beam state
    private int dragonBeamX, dragonBeamY; // Position of the dragon's beam
    private int playerBeamX, playerBeamY; // Position of the player's beam
    private boolean playerBeamActive = false; // Player beam state

    public AdventureGame() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    movePlayer(-10, 0); // Move left
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    movePlayer(10, 0); // Move right
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    movePlayer(0, -10); // Move up
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    movePlayer(0, 10); // Move down
                } else if (e.getKeyCode() == KeyEvent.VK_SPACE && hasGun) {
                    shootBullet(); // Shoot bullet if has gun
                }
                checkCollectibles();
                repaint();
            }
        });

        initializeGame();
        timer = new Timer(1000 / GAME_SPEED, this);
        timer.start();
    }

    private void initializeGame() {
        playerX = SCREEN_WIDTH / 2;
        playerY = SCREEN_HEIGHT / 2;
        gameMap = new HashMap<>();
        setupGameMap();
        dragonX = 600; // Initial dragon position
        dragonY = 300; // Initial dragon position
    }

    private void setupGameMap() {
        // Setup game locations and collectibles
        gameMap.put(new Point(100, 100), "Gun");
        gameMap.put(new Point(300, 200), "Key");
        gameMap.put(new Point(700, 500), "DragonDoor"); // Door to dragon area
    }

    private void movePlayer(int deltaX, int deltaY) {
        playerX += deltaX;
        playerY += deltaY;
        playerX = Math.max(0, Math.min(SCREEN_WIDTH - PLAYER_SIZE, playerX));
        playerY = Math.max(0, Math.min(SCREEN_HEIGHT - PLAYER_SIZE, playerY));
        checkDragonCollision();
    }

    private void shootBullet() {
        playerBeamActive = true;
        playerBeamX = playerX + PLAYER_SIZE / 2; // Start bullet from player center
        playerBeamY = playerY; // Start bullet from player position
    }

    private void checkCollectibles() {
        for (Point point : gameMap.keySet()) {
            if (point.x == playerX && point.y == playerY) {
                String item = gameMap.get(point);
                switch (item) {
                    case "Gun":
                        hasGun = true;
                        break;
                    case "Key":
                        // Add logic for keys if needed
                        break;
                    case "DragonDoor":
                        // Check if all items are collected before transporting
                        if (hasGun) {
                            transportToDragonArea();
                        }
                        break;
                }
                gameMap.remove(point); // Remove collected item
                break;
            }
        }
    }

    private void transportToDragonArea() {
        playerX = 300; // Move player to the dragon area
        playerY = 300;
        dragonHealth = MAX_HEALTH; // Reset dragon health for the encounter
        dragonAppeared = true; // Set dragon appearance flag to true
    }

    private void checkDragonCollision() {
        if (!dragonAppeared) return; // Only check if the dragon has appeared

        Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);
        Rectangle dragonRect = new Rectangle(dragonX, dragonY, DRAGON_SIZE, DRAGON_SIZE);
        
        if (playerRect.intersects(dragonRect)) {
            playerHealth -= 10; // Decrease player health
            if (playerHealth <= 0) {
                JOptionPane.showMessageDialog(this, "Game Over! You were defeated by the dragon.");
                System.exit(0);
            }
        }
    }

    private void moveDragon() {
        if (!dragonAppeared) return; // Only move if the dragon is active

        // Simple AI: Move towards the player aggressively
        if (dragonX < playerX) {
            dragonX += 2; // Move right faster
        } else if (dragonX > playerX) {
            dragonX -= 2; // Move left faster
        }

        if (dragonY < playerY) {
            dragonY += 2; // Move down faster
        } else if (dragonY > playerY) {
            dragonY -= 2; // Move up faster
        }

        checkBulletCollision();

        // Activate dragon beam attack
        if (Math.random() < 0.05) { // 5% chance to fire beam
            dragonBeamActive = true;
            dragonBeamX = dragonX + DRAGON_SIZE / 2; // Beam starts from dragon's center
            dragonBeamY = dragonY + DRAGON_SIZE; // Start at dragon's bottom
        }
    }

    private void checkBulletCollision() {
        // Check for player beam collision with dragon
        if (playerBeamActive) {
            Rectangle playerBeam = new Rectangle(playerBeamX, playerBeamY, BULLET_SIZE, BULLET_SIZE);
            if (playerBeam.intersects(new Rectangle(dragonX, dragonY, DRAGON_SIZE, DRAGON_SIZE))) {
                dragonHealth -= 20; // Decrease dragon health
                playerBeamActive = false; // Deactivate player beam on hit
                if (dragonHealth <= 0) {
                    JOptionPane.showMessageDialog(this, "You defeated the dragon!");
                    System.exit(0);
                }
            } else {
                playerBeamY -= 10; // Move the beam upward
                if (playerBeamY < 0) {
                    playerBeamActive = false; // Deactivate if it goes off-screen
                }
            }
        }
    }

    private void checkDragonBeamCollision() {
        if (dragonBeamActive) {
            Rectangle beam = new Rectangle(dragonBeamX, dragonBeamY, DRAGON_BEAM_WIDTH, 300); // Set beam length
            Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);
            if (beam.intersects(playerRect)) {
                playerHealth -= 5; // Damage to player from beam
                if (playerHealth <= 0) {
                    JOptionPane.showMessageDialog(this, "Game Over! You were defeated by the dragon.");
                    System.exit(0);
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawArena(g); // Draw the arena border
        drawPlayer(g);
        drawDragon(g);
        drawGameMap(g);
        drawHealth(g); // Draw health number at the top
        drawDragonBeam(g); // Draw dragon beam if active
        drawPlayerBeam(g); // Draw player beam if active
    }

    private void drawArena(Graphics g) {
        g.setColor(Color.YELLOW);
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT); // Fill background with yellow
        g.setColor(Color.BLACK);
        g.fillRect(10, 10, SCREEN_WIDTH - 20, SCREEN_HEIGHT - 20); // Inner black area
    }

    private void drawPlayer(Graphics g) {
        g.setColor(Color.CYAN);
        g.fillRect(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);
    }

    private void drawDragon(Graphics g) {
        if (dragonAppeared) {
            // Draw a more complex dragon shape
            g.setColor(Color.DARK_GRAY);
            g.fillOval(dragonX, dragonY, DRAGON_SIZE, DRAGON_SIZE); // Body
            g.setColor(Color.GREEN);
            g.fillPolygon(new int[]{dragonX, dragonX + DRAGON_SIZE / 2, dragonX + DRAGON_SIZE},
                          new int[]{dragonY, dragonY - 50, dragonY}, 3); // Wings
            g.setColor(Color.RED);
            g.fillOval(dragonX + 30, dragonY + 30, 20, 20); // Eye
            g.setColor(Color.BLACK);
            g.fillOval(dragonX + 35, dragonY + 35, 5, 5); // Pupil
        }
    }

    private void drawGameMap(Graphics g) {
        g.setColor(Color.WHITE);
        for (Point point : gameMap.keySet()) {
            g.fillRect(point.x, point.y, 10, 10); // Draw collectibles
        }
    }

    private void drawPlayerBeam(Graphics g) {
        if (playerBeamActive) {
            g.setColor(Color.RED);
            g.fillRect(playerBeamX, playerBeamY, BULLET_SIZE, BULLET_SIZE); // Draw player's beam
            if (playerBeamY < 0) {
                playerBeamActive = false; // Deactivate if it goes off-screen
            }
        }
    }

    private void drawHealth(Graphics g) {
        g.setColor(Color.WHITE);
        g.drawString("Health: " + playerHealth, 10, 20); // Display health as a number at the top
    }

    private void drawDragonBeam(Graphics g) {
        if (dragonBeamActive) {
            g.setColor(Color.ORANGE);
            g.fillRect(dragonBeamX, dragonBeamY, DRAGON_BEAM_WIDTH, 300); // Draw dragon beam
            dragonBeamActive = false; // Deactivate the beam after drawing
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        moveDragon();
        checkDragonBeamCollision(); // Check for player hit by dragon's beam
        repaint();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Adventure Game");
        AdventureGame game = new AdventureGame();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
