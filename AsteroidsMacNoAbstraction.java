// javac Asteroids.java
// java Asteroids

import java.util.Vector;
import java.util.Random;

import java.time.LocalTime;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;

import javax.imageio.ImageIO;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.awt.Graphics;
import java.awt.Graphics2D;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;

public class AsteroidsMacNoAbstraction {
    public AsteroidsMacNoAbstraction() {
        setup();
    }

    public static void setup() {
        appFrame = new JFrame("Asteroids");
        XOFFSET = 0;
        YOFFSET = 0;
        WINWIDTH = 500;
        WINHEIGHT = 500;

        pi = 3.14159265358979;
        twoPi = 2.0 * 3.14159265358979;

        endgame = false;
        enemyAlive = false;

        p1UpPressed = false;
        p1DownPressed = false;
        p1LeftPressed = false;
        p1RightPressed = false;
        p1FirePressed = false;
        p2UpPressed = false;
        p2DownPressed = false;
        p2LeftPressed = false;
        p2RightPressed = false;
        p2FirePressed = false;

        p1width = 25;
        p1height = 25;
        p2width = p1width;
        p2height = p1height;
        p1originalX = (double)XOFFSET + ((double)WINWIDTH / 2.0) -
                (p1width / 2.0) - 60.0;
        p1originalY = (double)YOFFSET + ((double)WINHEIGHT / 2.0) -
                (p1height / 2.0);
        p2originalX = (double)XOFFSET + ((double)WINWIDTH / 2.0) -
                (p2width / 2.0) + 60.0;
        p2originalY = p1originalY;

        playerBullets = new Vector<ImageObject>();
        playerBulletsTimes = new Vector<Long>();
        bulletWidth = 5;
        playerbulletlifetime = 1600L; // 0.75
        enemybulletlifetime = 1600L;
        explosionlifetime = 800L;

        playerbulletgap = 1;
        flamecount = 1;
        flamecount2 = 1;
        flamewidth = 12.0;
        expcount = 1;

        level = 3;

        asteroids = new Vector<ImageObject>();
        asteroidsTypes = new Vector<Integer>();
        ast1width = 32;
        ast2width = 21;
        ast3width = 26;

        background = loadImage("space.png");
        player = loadImage("player.png");
        flame1 = loadImage("flameleft.png");
        flame2 = loadImage("flamecenter.png");
        flame3 = loadImage("flameright.png");
        flame4 = loadImage("blueflameleft.png");
        flame5 = loadImage("blueflamecenter.png");
        flame6 = loadImage("blueflameright.png");

        ast1 = loadImage("ast1.png");
        ast2 = loadImage("ast2.png");
        ast3 = loadImage("ast3.png");

        playerBullet = loadImage("playerbullet.png");
        enemyShip = loadImage("enemy.png");
        enemyBullet = loadImage("enemybullet.png");
        exp1 = loadImage("explosion1.png");
        exp2 = loadImage("explosion2.png");
    }

    private static class Animate implements Runnable {
        public void run() {
            while (endgame == false) {
                if (gamePanel != null) {
                    gamePanel.repaint();
                }

                try {
                    Thread.sleep(32);
                }
                catch(InterruptedException e) {
                    // NOP
                }
            }
        }
    }

    private static void insertPlayerBullet(ImageObject player) {
        ImageObject bullet = new ImageObject(0, 0, bulletWidth,
                bulletWidth, player.getAngle());
        lockrotateObjAroundObjtop(bullet, player, player.getWidth() / 2.0);
        playerBullets.addElement(bullet);
        playerBulletsTimes.addElement(System.currentTimeMillis());
    }

    private static void insertEnemyBullet() {
        try {
            // randomize angle here
            Random randomNumbers = new Random(LocalTime.now().getNano());

            ImageObject bullet = new ImageObject(enemy.getX() +
                    enemy.getWidth()/2.0, enemy.getY() + enemy.getHeight() / 2.0,
                    bulletWidth, bulletWidth, randomNumbers.nextInt(360));
            //lockrorateObjAroundObjbottom(bullet, enemy, enemy.getWidth()/2.0);
            enemyBullets.addElement(bullet);
            enemyBulletsTimes.addElement(System.currentTimeMillis());
        }
        catch(java.lang.NullPointerException jlnpe) {
            // NOP
        }
    }

    private static class PlayerMover implements Runnable {
        public PlayerMover(int playerIdInput) {
            velocitystep = 0.01;
            rotatestep = 0.01;
            playerId = playerIdInput;
        }
        public void run() {
            while (endgame == false) {
                try {
                    Thread.sleep(10);
                }
                catch(InterruptedException e) {
                    // NOP
                }
                ImageObject player = (playerId == 1) ? p1 : p2;
                if (player == null) {
                    continue;
                }

                boolean upPressed = (playerId == 1) ? p1UpPressed : p2UpPressed;
                boolean downPressed = (playerId == 1) ? p1DownPressed : p2DownPressed;
                boolean leftPressed = (playerId == 1) ? p1LeftPressed : p2LeftPressed;
                boolean rightPressed = (playerId == 1) ? p1RightPressed : p2RightPressed;
                boolean firePressed = (playerId == 1) ? p1FirePressed : p2FirePressed;

                double velocity = (playerId == 1) ? p1velocity : p2velocity;

                if (upPressed == true) {
                    velocity = velocity + velocitystep;
                }
                if (downPressed == true) {
                    velocity = velocity - velocitystep;
                }
                if (leftPressed == true) {
                    if (velocity < 0) {
                        player.rotate(-rotatestep);
                    }
                    else {
                        player.rotate(rotatestep);
                    }
                }
                if (rightPressed == true) {
                    if (velocity < 0) {
                        player.rotate(rotatestep);
                    }
                    else {
                        player.rotate(-rotatestep);
                    }
                }
                if (firePressed == true) {
                    long now = System.currentTimeMillis();
                    long lastFireMs = (playerId == 1) ? p1LastFireMs : p2LastFireMs;
                    if (now - lastFireMs > playerbulletlifetime / 4.0) {
                        insertPlayerBullet(player);
                        if (playerId == 1) {
                            p1LastFireMs = now;
                        }
                        else {
                            p2LastFireMs = now;
                        }
                    }
                }

                player.move(-velocity * Math.cos(player.getAngle() -
                        pi / 2.0), velocity * Math.sin(player.getAngle()
                        - pi / 2.0));
                player.screenWrap(XOFFSET, XOFFSET + WINWIDTH,
                        YOFFSET, YOFFSET + WINHEIGHT);
                if (playerId == 1) {
                    p1velocity = velocity;
                }
                else {
                    p2velocity = velocity;
                }
                logPlayerState();
            }
        }
        private double velocitystep;
        private double rotatestep;
        private int playerId;
    }

    private static class FlameMover implements Runnable {
        public FlameMover(ImageObject playerInput, ImageObject flameInput) {
            player = playerInput;
            flame = flameInput;
            gap = 7.0;
        }
        public void run() {
            while (endgame == false) {
                if (player != null && flame != null) {
                    lockrotateObjAroundObjbottom(flame, player, gap);
                }
            }
        }
        private ImageObject player;
        private ImageObject flame;
        private double gap;
    }

    private static class AsteroidsMover implements Runnable {
        public AsteroidsMover() {
            velocity = 0.1;
            spinstep = 0.01;
            spindirection = new Vector<Integer>();
        }

        public void run() {
            Random randomNumbers = new Random(LocalTime.now().getNano());
            for (int i = 0; i < asteroids.size(); i++) {
                spindirection.addElement(randomNumbers.nextInt(2));
            }
            while (endgame == false) {
                try {
                    Thread.sleep(1);
                }
                catch (InterruptedException e) {
                    // NOP
                }

                try {
                    for (int i = 0; i < asteroids.size(); i++) {
                        if (spindirection.elementAt(i) < 1) {
                            asteroids.elementAt(i).spin(-spinstep);
                        }
                        else {
                            asteroids.elementAt(i).spin(spinstep);
                        }
                        asteroids.elementAt(i).move(-velocity *
                                        Math.cos(asteroids.elementAt(i).getAngle() - pi / 2.0),
                                velocity * Math.sin(asteroids.elementAt(i).getAngle() - pi / 2.0));
                        asteroids.elementAt(i).screenWrap(XOFFSET, XOFFSET + WINWIDTH,
                                YOFFSET, YOFFSET + WINHEIGHT);
                    }
                }
                catch(java.lang.ArrayIndexOutOfBoundsException jlaioobe) {
                    // NOP
                }
            }
        }
        private double velocity;
        private double spinstep;
        private Vector<Integer> spindirection;
    }

    public static class PlayerBulletsMover implements Runnable {
        public PlayerBulletsMover() {
            velocity = 1.0;
        }
        public void run() {
            while (endgame == false) {
                try {
                    // controls bullet speed
                    Thread.sleep(4);
                }
                catch(InterruptedException e) {
                    // NOP
                }

                try {
                    for (int i = 0; i < playerBullets.size(); i++) {
                        playerBullets.elementAt(i).move(-velocity * Math.cos(playerBullets.elementAt(i).getAngle() - pi / 2.0),
                                velocity * Math.sin(playerBullets.elementAt(i).getAngle() - pi / 2.0));
                        playerBullets.elementAt(i).screenWrap(XOFFSET, XOFFSET + WINWIDTH,
                                YOFFSET, YOFFSET + WINHEIGHT);

                        if (System.currentTimeMillis() - playerBulletsTimes.elementAt(i)
                                > playerbulletlifetime) {
                            playerBullets.remove(i);
                            playerBulletsTimes.remove(i);
                        }
                    }
                }
                catch (java.lang.ArrayIndexOutOfBoundsException aie) {
                    playerBullets.clear();
                    playerBulletsTimes.clear();
                }
            }
        }
        private double velocity;
    }

    private static class EnemyShipMover implements Runnable {
        public EnemyShipMover() {
            velocity = 1.0;
        }
        public void run() {
            while (endgame == false && enemyAlive == true) {
                try
                {
                    Thread.sleep(10);
                }
                catch(InterruptedException e) {
                    //NOP
                }
                try {
                    enemy.move(-velocity * Math.cos(enemy.getAngle() - pi / 2.0),
                            velocity * Math.sin(enemy.getAngle() - pi / 2.0));
                    enemy.screenWrap(XOFFSET, XOFFSET + WINWIDTH, YOFFSET, YOFFSET + WINHEIGHT);
                }
                catch (java.lang.NullPointerException jlnpe) {
                    // NOP
                }

                try {
                    if (enemyAlive == true) {
                        if (enemyBullets.size() == 0) {
                            insertEnemyBullet();
                        }
                        else if (System.currentTimeMillis() - enemyBulletsTimes.elementAt(enemyBulletsTimes.size() - 1)
                                > enemybulletlifetime / 4.0) {
                            insertEnemyBullet();
                        }
                    }
                }
                catch (java.lang.ArrayIndexOutOfBoundsException aioobe) {
                    // NOP
                }
            }
        }
        private double velocity;
    }

    private static class EnemyBulletsMover implements Runnable {
        public EnemyBulletsMover() {
            velocity = 1.2;
        }
        public void run() {
            while (endgame == false && enemyAlive == true) {
                try {
                    // controls bullet speed
                    Thread.sleep(4);
                }
                catch (InterruptedException e) {
                    // NOP
                }

                try {
                    for (int i = 0; i < enemyBullets.size(); i++) {
                        enemyBullets.elementAt(i).move(-velocity * Math.cos(enemyBullets.elementAt(i).getAngle() - pi / 2.0),
                                velocity * Math.sin(enemyBullets.elementAt(i).getAngle() - pi / 2.0));
                        enemyBullets.elementAt(i).screenWrap(XOFFSET, XOFFSET + WINWIDTH, YOFFSET, YOFFSET + WINHEIGHT);

                        if (System.currentTimeMillis() - enemyBulletsTimes.elementAt(i) > enemybulletlifetime) {
                            enemyBullets.remove(i);
                            enemyBulletsTimes.remove(i);
                        }
                    }
                }
                catch (java.lang.ArrayIndexOutOfBoundsException aie) {
                    enemyBullets.clear();
                    enemyBulletsTimes.clear();
                }
            }
        }
        private double velocity;
    }

    private static class CollisionChecker implements Runnable {
        public void run() {
            Random randomNumbers = new Random (LocalTime.now().getNano());
            while (endgame == false) {
                try {
                    // compares all asteroids to all player bullets
                    for (int i = 0; i < asteroids.size(); i++) {
                        for (int j = 0; j < playerBullets.size(); j++) {
                            if (collisionOccurs(asteroids.elementAt(i),
                                    playerBullets.elementAt(j)) == true) {
                                logCollision("asteroid vs player bullet");
                                // delete asteroid
                                // show explosion animation
                                // replace old asteroid with two new, smaller asteroids
                                // at same place, random directions.
                                double posX = asteroids.elementAt(i).getX();
                                double posY = asteroids.elementAt(i).getY();

                                // create explosion!
                                explosions.addElement(new ImageObject(posX, posY, 27, 24, 0.0));
                                explosionsTimes.addElement(System.currentTimeMillis());

                                // create two new asteroids of type 2
                                if (asteroidsTypes.elementAt(i) == 1) {
                                    asteroids.addElement(new ImageObject(posX, posY, ast2width, ast2width, (double) (randomNumbers.nextInt(360))));
                                    asteroidsTypes.addElement(2);
                                    asteroids.remove(i);
                                    asteroidsTypes.remove(i);
                                    playerBullets.remove(j);
                                    playerBulletsTimes.remove(j);
                                }

                                // create two new asteroids of type 3
                                if (asteroidsTypes.elementAt(i) == 2) {
                                    asteroids.addElement(new ImageObject(posX, posY, ast3width, ast3width, (double) (randomNumbers.nextInt(360))));
                                    asteroidsTypes.addElement(3);
                                    asteroids.remove(i);
                                    asteroidsTypes.remove(i);
                                    playerBullets.remove(j);
                                    playerBulletsTimes.remove(j);
                                }

                                // delete asteroids
                                if (asteroidsTypes.elementAt(i) == 3) {
                                    asteroids.remove(i);
                                    asteroidsTypes.remove(i);
                                    playerBullets.remove(j);
                                    playerBulletsTimes.remove(j);
                                }
                            }
                        }
                    }

                    // compare all asteroids to player
                    for (int i = 0; i < asteroids.size(); i++) {
                        if ((p1 != null && collisionOccurs(asteroids.elementAt(i), p1) == true)
                                || (p2 != null && collisionOccurs(asteroids.elementAt(i), p2) == true)) {
                            logCollision("asteroid vs player");
                            endgame = true;
                            System.out.println("Game Over. You lose!");
                        }
                    }

                    try {
                        // compare all player bullets to enemy ship
                        for (int i = 0; i < playerBullets.size(); i++) {
                            if (collisionOccurs(playerBullets.elementAt(i), enemy) == true) {
                                logCollision("player bullet vs enemy");
                                double posX = enemy.getX();
                                double posY = enemy.getY();

                                // create explosion!
                                explosions.addElement(new ImageObject(posX, posY, 27, 24, 0.0));
                                explosionsTimes.addElement(System.currentTimeMillis());

                                playerBullets.remove(i);
                                playerBulletsTimes.remove(i);
                                enemyAlive = false;
                                enemy = null;
                                enemyBullets.clear();
                                enemyBulletsTimes.clear();
                            }
                        }

                        // compare enemy ship to player
                        //TODO
                        if ((p1 != null && collisionOccurs(enemy, p1) == true)
                                || (p2 != null && collisionOccurs(enemy, p2) == true)) {
                            logCollision("enemy vs player");
                            endgame = true;
                            System.out.println("Game Over. You Lose!");
                        }

                        // compare all enemy bullets to player
                        for (int i = 0; i < enemyBullets.size(); i++) {
                            if ((p1 != null && collisionOccurs(enemyBullets.elementAt(i), p1) == true)
                                    || (p2 != null && collisionOccurs(enemyBullets.elementAt(i), p2) == true)) {
                                logCollision("enemy bullet vs player");
                                endgame = true;
                                System.out.println("Game Over. You Lose!");
                            }
                        }
                    }
                    catch(java.lang.NullPointerException jlnpe) {
                        // NOP
                    }
                }
                catch (java.lang.ArrayIndexOutOfBoundsException jlaioobe) {
                    //NOP
                }
            }
        }
    }

    private static class WinChecker implements Runnable {
        public void run() {
            while (endgame == false) {
                if (asteroids.size() == 0) {
                    endgame = true;
                    System.out.println("Game Over. You Lose!");
                }
            }
        }
    }

    private static void generateAsteroids() {
        asteroids = new Vector<ImageObject>();
        asteroidsTypes = new Vector<Integer>();
        Random randomNumbers = new Random(LocalTime.now().getNano());

        for (int i = 0; i < level; i++) {
            ImageObject asteroid = new ImageObject (XOFFSET +
                    (double) (randomNumbers.nextInt(WINWIDTH)), YOFFSET +
                    (double) (randomNumbers.nextInt(WINHEIGHT)), ast1width, ast1width,
                    (double) (randomNumbers.nextInt(360)));
            int attempts = 0;
            while (((p1 != null && collisionOccurs(asteroid, p1) == true)
                    || (p2 != null && collisionOccurs(asteroid, p2) == true))
                    && attempts < 20) {
                asteroid.moveto(XOFFSET + (double) (randomNumbers.nextInt(WINWIDTH)),
                        YOFFSET + (double) (randomNumbers.nextInt(WINHEIGHT)));
                attempts++;
            }
            asteroids.addElement(asteroid);
            asteroidsTypes.addElement(1);
        }
    }

    private static void generateEnemy() {
        try {
            Random randomNumbers = new Random(LocalTime.now().getNano());
            enemy = new ImageObject (XOFFSET + (double) (randomNumbers.nextInt(WINWIDTH)),
                    YOFFSET + (double) (randomNumbers.nextInt(WINHEIGHT)), 29.0, 16.0,
                    (double) (randomNumbers.nextInt(360)));
        }
        catch (java.lang.IllegalArgumentException jliae) {
            // NOP
        }
    }

    // *dist is a distance between the two objects at the bottom of objInner
    private static void lockrotateObjAroundObjbottom (ImageObject objOuter, ImageObject objInner, double dist) {
        objOuter.moveto(objInner.getX() + objOuter.getWidth() + (objInner.getWidth() / 2.0 +
                (dist + objInner.getWidth() / 2.0) * Math.cos(-objInner.getAngle() + pi / 2.0))
                / 2.0, objInner.getY() - objOuter.getHeight() + (dist + objInner.getHeight() / 2.0)
                * Math.sin(-objInner.getAngle() / 2.0));
        objOuter.setAngle(objInner.getAngle());
    }

    // *dist is a distance between the two objects at the top of the inner object
    private static void lockrotateObjAroundObjtop (ImageObject objOuter, ImageObject objInner, double dist) {
        objOuter.moveto(objInner.getX() + objOuter.getWidth() + (objInner.getWidth() / 2.0 +
                (dist + objInner.getWidth() / 2.0) * Math.cos(objInner.getAngle() + pi / 2.0))
                / 2.0, objInner.getY() - objOuter.getHeight() + (dist + objInner.getHeight() / 2.0)
                * Math.sin(objInner.getAngle() / 2.0));
        objOuter.setAngle(objInner.getAngle());
    }

    private static AffineTransformOp rotateImageObject (ImageObject obj) {
        AffineTransform at = AffineTransform.getRotateInstance(-obj.getAngle(),
                obj.getWidth() / 2.0, obj.getHeight() / 2.0);
        AffineTransformOp atop = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        return atop;
    }

    private static AffineTransformOp spinImageObject (ImageObject obj) {
        AffineTransform at = AffineTransform.getRotateInstance(-obj.getInternalAngle(),
                obj.getWidth() / 2.0, obj.getHeight() / 2.0);
        AffineTransformOp atop = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        return atop;
    }

    private static void backgroundDraw(Graphics2D g2D) {
        if (background == null) {
            return;
        }
        g2D.drawImage(background, XOFFSET, YOFFSET, null);
    }

    private static void enemyBulletsDraw(Graphics2D g2D) {
        if (enemyBullets == null || enemyBullet == null) {
            return;
        }
        for(int i = 0; i < enemyBullets.size(); i++) {
            g2D.drawImage(enemyBullet, (int) (enemyBullets.elementAt(i).getX() + 0.5),
                    (int) (enemyBullets.elementAt(i).getY() + 0.5), null);
        }
    }

    private static void enemyDraw(Graphics2D g2D) {
        if (Boolean.TRUE.equals(enemyAlive) && enemyShip != null) {
            try {
                g2D.drawImage(enemyShip, (int) (enemy.getX() + 0.5), (int) (enemy.getY() + 0.5), null);
            }
            catch (java.lang.NullPointerException jlnpe) {
                // NOP
            }
        }
    }

    private static void playerBulletsDraw(Graphics2D g2D) {
        if (playerBullets == null || playerBullet == null) {
            return;
        }
        try {
            for (int i = 0; i < playerBullets.size(); i ++) {
                g2D.drawImage(rotateImageObject(playerBullets.elementAt(i)).filter(playerBullet, null),
                        (int) (playerBullets.elementAt(i).getX() + 0.5), (int) (playerBullets.elementAt(i).getY() + 0.5), null);
            }
        }
        catch (java.lang.ArrayIndexOutOfBoundsException aioobe) {
            playerBullets.clear();
            playerBulletsTimes.clear();
        }
    }

    private static void playerDraw(Graphics2D g2D) {
        if (player == null) {
            return;
        }
        if (p1 != null) {
            g2D.drawImage(rotateImageObject(p1).filter(player, null), (int) (p1.getX() + 0.5), (int) (p1.getY() + 0.5), null);
        }
        if (p2 != null) {
            g2D.drawImage(rotateImageObject(p2).filter(player, null), (int) (p2.getX() + 0.5), (int) (p2.getY() + 0.5), null);
        }
    }

    private static void flameDraw(Graphics2D g2D) {
        if (flames != null && p1UpPressed == true) {
            if (flamecount == 1) {
                if (flame1 != null) {
                    g2D.drawImage(rotateImageObject(flames).filter(flame1, null), (int) (flames.getX() + 0.5), (int) (flames.getY() + 0.5), null);
                }
                flamecount = 1 + ((flamecount + 1) % 3);
            }
            else if (flamecount == 2) {
                if (flame2 != null) {
                    g2D.drawImage(rotateImageObject(flames).filter(flame2, null), (int) (flames.getX() + 0.5), (int) (flames.getY() + 0.5), null);
                }
                flamecount = 1 + ((flamecount + 1) % 3);
            }
            else if (flamecount == 3) {
                if (flame3 != null) {
                    g2D.drawImage(rotateImageObject(flames).filter(flame3, null), (int) (flames.getX() + 0.5), (int) (flames.getY() + 0.5), null);
                }
                flamecount = 1 + ((flamecount + 1) % 3);
            }
        }
        else if (flames != null && p1DownPressed == true) {
            if (flamecount == 1) {
                if (flame4 != null) {
                    g2D.drawImage(rotateImageObject(flames).filter(flame4, null), (int) (flames.getX() + 0.5), (int) (flames.getY() + 0.5), null);
                }
                flamecount = 1 + ((flamecount + 1) % 3);
            }
            else if (flamecount == 2) {
                if (flame5 != null) {
                    g2D.drawImage(rotateImageObject(flames).filter(flame5, null), (int) (flames.getX() + 0.5), (int) (flames.getY() + 0.5), null);
                }
                flamecount = 1 + ((flamecount + 1) % 3);
            }
            else if (flamecount == 3) {
                if (flame6 != null) {
                    g2D.drawImage(rotateImageObject(flames).filter(flame6, null), (int) (flames.getX() + 0.5), (int) (flames.getY() + 0.5), null);
                }
                flamecount = 1 + ((flamecount + 1) % 3);
            }
        }

        if (flames2 != null && p2UpPressed == true) {
            if (flamecount2 == 1) {
                if (flame1 != null) {
                    g2D.drawImage(rotateImageObject(flames2).filter(flame1, null), (int) (flames2.getX() + 0.5), (int) (flames2.getY() + 0.5), null);
                }
                flamecount2 = 1 + ((flamecount2 + 1) % 3);
            }
            else if (flamecount2 == 2) {
                if (flame2 != null) {
                    g2D.drawImage(rotateImageObject(flames2).filter(flame2, null), (int) (flames2.getX() + 0.5), (int) (flames2.getY() + 0.5), null);
                }
                flamecount2 = 1 + ((flamecount2 + 1) % 3);
            }
            else if (flamecount2 == 3) {
                if (flame3 != null) {
                    g2D.drawImage(rotateImageObject(flames2).filter(flame3, null), (int) (flames2.getX() + 0.5), (int) (flames2.getY() + 0.5), null);
                }
                flamecount2 = 1 + ((flamecount2 + 1) % 3);
            }
        }
        else if (flames2 != null && p2DownPressed == true) {
            if (flamecount2 == 1) {
                if (flame4 != null) {
                    g2D.drawImage(rotateImageObject(flames2).filter(flame4, null), (int) (flames2.getX() + 0.5), (int) (flames2.getY() + 0.5), null);
                }
                flamecount2 = 1 + ((flamecount2 + 1) % 3);
            }
            else if (flamecount2 == 2) {
                if (flame5 != null) {
                    g2D.drawImage(rotateImageObject(flames2).filter(flame5, null), (int) (flames2.getX() + 0.5), (int) (flames2.getY() + 0.5), null);
                }
                flamecount2 = 1 + ((flamecount2 + 1) % 3);
            }
            else if (flamecount2 == 3) {
                if (flame6 != null) {
                    g2D.drawImage(rotateImageObject(flames2).filter(flame6, null), (int) (flames2.getX() + 0.5), (int) (flames2.getY() + 0.5), null);
                }
                flamecount2 = 1 + ((flamecount2 + 1) % 3);
            }
        }
    }

    private static void asteroidsDraw(Graphics2D g2D) {
        if (asteroids == null || asteroidsTypes == null) {
            return;
        }
        for (int i = 0; i < asteroids.size(); i++) {
            if (asteroidsTypes.elementAt(i) == 1) {
                if (ast1 != null) {
                    g2D.drawImage(spinImageObject(asteroids.elementAt(i)).filter(ast1, null), (int) (asteroids.elementAt(i).getX() + 0.5),
                            (int) (asteroids.elementAt(i).getY() + 0.5), null);
                }
            }
            if (asteroidsTypes.elementAt(i) == 2) {
                if (ast2 != null) {
                    g2D.drawImage(spinImageObject(asteroids.elementAt(i)).filter(ast2, null), (int) (asteroids.elementAt(i).getX() + 0.5),
                            (int) (asteroids.elementAt(i).getY() + 0.5), null);
                }
            }
            if (asteroidsTypes.elementAt(i) == 3) {
                if (ast3 != null) {
                    g2D.drawImage(spinImageObject(asteroids.elementAt(i)).filter(ast3, null), (int) (asteroids.elementAt(i).getX() + 0.5),
                            (int) (asteroids.elementAt(i).getY() + 0.5), null);
                }
            }
        }
    }

    private static void explosionsDraw(Graphics2D g2D) {
        if (explosions == null || explosionsTimes == null || exp1 == null || exp2 == null) {
            return;
        }
        for (int i = 0; i < explosions.size(); i++) {
            if (System.currentTimeMillis() - explosionsTimes.elementAt(i) > explosionlifetime) {
                try {
                    explosions.remove(i);
                    explosionsTimes.remove(i);
                }
                catch (java.lang.NullPointerException jlnpe) {
                    explosions.clear();
                    explosionsTimes.clear();
                }
            }
            else {
                if (expcount == 1) {
                    g2D.drawImage(exp1, (int) (explosions.elementAt(i).getX() + 0.5),
                            (int) (explosions.elementAt(i).getY() + 0.5), null);
                    expcount = 2;
                }
                else if (expcount == 2) {
                    g2D.drawImage(exp2, (int) (explosions.elementAt(i).getX() + 0.5),
                            (int) (explosions.elementAt(i).getY() + 0.5), null);
                    expcount = 1;
                }
            }
        }
    }

    private static class GamePanel extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2D = (Graphics2D) g;
            backgroundDraw(g2D);
            asteroidsDraw(g2D);
            explosionsDraw(g2D);
            enemyBulletsDraw(g2D);
            enemyDraw(g2D);
            playerBulletsDraw(g2D);
            playerDraw(g2D);
            flameDraw(g2D);
        }
    }

    private static class KeyPressed extends AbstractAction {
        public KeyPressed() {
            action = "";
        }
        public KeyPressed (String input) {
            action = input;
        }

        public void actionPerformed (ActionEvent e) {
            if (action.equals("W")) {
                p1UpPressed = true;
                logKey("pressed", "W");
            }
            if (action.equals("S")) {
                p1DownPressed = true;
                logKey("pressed", "S");
            }
            if (action.equals("A")) {
                p1LeftPressed = true;
                logKey("pressed", "A");
            }
            if (action.equals("D")) {
                p1RightPressed = true;
                logKey("pressed", "D");
            }
            if (action.equals("F")) {
                p1FirePressed = true;
                logKey("pressed", "F");
            }
            if (action.equals("UP")) {
                p2UpPressed = true;
                logKey("pressed", "UP");
            }
            if (action.equals("DOWN")) {
                p2DownPressed = true;
                logKey("pressed", "DOWN");
            }
            if (action.equals("LEFT")) {
                p2LeftPressed = true;
                logKey("pressed", "LEFT");
            }
            if (action.equals("RIGHT")) {
                p2RightPressed = true;
                logKey("pressed", "RIGHT");
            }
            if (action.equals("J")) {
                p2FirePressed = true;
                logKey("pressed", "J");
            }
        }
        private String action;
    }

    private static class KeyReleased extends AbstractAction {
        public KeyReleased() {
            action = "";
        }

        public KeyReleased (String input) {
            action = input;
        }

        public void actionPerformed (ActionEvent e) {
            if (action.equals("W")) {
                p1UpPressed = false;
                logKey("released", "W");
            }
            if (action.equals("S")) {
                p1DownPressed = false;
                logKey("released", "S");
            }
            if (action.equals("A")) {
                p1LeftPressed = false;
                logKey("released", "A");
            }
            if (action.equals("D")) {
                p1RightPressed = false;
                logKey("released", "D");
            }
            if (action.equals("F")) {
                p1FirePressed = false;
                logKey("released", "F");
            }
            if (action.equals("UP")) {
                p2UpPressed = false;
                logKey("released", "UP");
            }
            if (action.equals("DOWN")) {
                p2DownPressed = false;
                logKey("released", "DOWN");
            }
            if (action.equals("LEFT")) {
                p2LeftPressed = false;
                logKey("released", "LEFT");
            }
            if (action.equals("RIGHT")) {
                p2RightPressed = false;
                logKey("released", "RIGHT");
            }
            if (action.equals("J")) {
                p2FirePressed = false;
                logKey("released", "J");
            }
        }
        private String action;
    }

    private static class QuitGame implements ActionListener {
        public void actionPerformed (ActionEvent e) {
            endgame = true;
        }
    }

    public static class StartGame implements ActionListener {
        public void actionPerformed (ActionEvent e) {
            endgame = true;
            enemyAlive = true;

            p1UpPressed = false;
            p1DownPressed = false;
            p1LeftPressed = false;
            p1RightPressed = false;
            p1FirePressed = false;
            p2UpPressed = false;
            p2DownPressed = false;
            p2LeftPressed = false;
            p2RightPressed = false;
            p2FirePressed = false;

            p1 = new ImageObject (p1originalX, p1originalY, p1width, p1height, 0.0);
            p1velocity = 0.0;
            p2 = new ImageObject (p2originalX, p2originalY, p2width, p2height, 0.0);
            p2velocity = 0.0;
            generateEnemy();

            flames = new ImageObject(p1originalX + p1width / 2.0, p1originalY + p1height, flamewidth, flamewidth, 0.0);
            flames2 = new ImageObject(p2originalX + p2width / 2.0, p2originalY + p2height, flamewidth, flamewidth, 0.0);
            flamecount = 1;
            flamecount2 = 1;
            expcount = 1;
            p1LastFireMs = 0;
            p2LastFireMs = 0;

            try {
                Thread.sleep(50);
            }
            catch (InterruptedException ie) {
                // NOP
            }

            playerBullets = new Vector<ImageObject>();
            playerBulletsTimes = new Vector<Long>();
            enemyBullets = new Vector<ImageObject>();
            enemyBulletsTimes = new Vector<Long>();
            explosions = new Vector<ImageObject>();
            explosionsTimes = new Vector<Long>();
            generateAsteroids();
            endgame = false;

            Thread t1 = new Thread(new Animate());
            Thread t2 = new Thread(new PlayerMover(1));
            Thread t3 = new Thread(new FlameMover(p1, flames));
            Thread t4 = new Thread(new AsteroidsMover());
            Thread t5 = new Thread(new PlayerBulletsMover());
            Thread t6 = new Thread(new EnemyShipMover());
            Thread t7 = new Thread(new EnemyBulletsMover());
            Thread t8 = new Thread(new CollisionChecker());
            Thread t9 = new Thread(new WinChecker());
            Thread t10 = new Thread(new PlayerMover(2));
            Thread t11 = new Thread(new FlameMover(p2, flames2));

            t1.start();
            t2.start();
            t3.start();
            t4.start();
            t5.start();
            t6.start();
            t7.start();
            t8.start();
            t9.start();
            t10.start();
            t11.start();
        }
    }

    private static class GameLevel implements ActionListener {
        public int decodeLevel (String input) {
            int ret = 3;
            if (input.equals("One")) {
                ret = 1;
            }
            else if (input.equals("Two")) {
                ret = 2;
            }
            else if (input.equals("Three")) {
                ret = 3;
            }
            else if (input.equals("Four")) {
                ret = 4;
            }
            else if (input.equals("Five")) {
                ret = 5;
            }
            else if (input.equals("Six")) {
                ret = 6;
            }
            else if (input.equals("Seven")) {
                ret = 7;
            }
            else if (input.equals("Eight")) {
                ret = 8;
            }
            else if (input.equals("Nine")) {
                ret = 9;
            }
            else if (input.equals("Ten")) {
                ret = 10;
            }
            return ret;
        }
        public void actionPerformed (ActionEvent e) {
            JComboBox cb = (JComboBox) e.getSource();
            String textLevel = (String) cb.getSelectedItem();
            level = decodeLevel(textLevel);
        }
    }

    private static Boolean isInside(double p1x, double p1y, double p2x1,
                                    double p2y1, double p2x2, double p2y2) {
        Boolean ret = false;
        if (p1x > p2x1 && p1x < p2x2) {
            if (p1y > p2y1 && p1y < p2y2) {
                ret = true;
            }
            if (p1y > p2y2 && p1y < p2y1) {
                ret = true;
            }
        }
        if (p1x > p2x2 && p1x < p2x1) {
            if (p1y > p2y1 && p1y < p2y2) {
                ret = true;
            }
            if (p1y > p2y2 && p1y < p2y1) {
                ret = true;
            }
        }
        return ret;
    }

    private static Boolean collisionOccursCoordinates (double p1x1, double p1y1,
                                                       double p1x2, double p1y2, double p2x1, double p2y1, double p2x2, double p2y2) {
        Boolean ret = false;
        if (isInside(p1x1, p1y1, p2x1, p2y1, p2x2, p2y2) == true ) {
            ret = true;
        }
        if (isInside(p1x1, p1y2, p2x1, p2y1, p2x2, p2y2) == true ) {
            ret = true;
        }
        if (isInside(p1x2, p1y1, p2x1, p2y1, p2x2, p2y2) == true ) {
            ret = true;
        }
        if (isInside(p1x2, p1y2, p2x1, p2y1, p2x2, p2y2) == true ) {
            ret = true;
        }
        if (isInside(p2x1, p2y1, p1x1, p1y1, p1x2, p1y2) == true ) {
            ret = true;
        }
        if (isInside(p2x1, p2y2, p1x1, p1y1, p1x2, p1y2) == true ) {
            ret = true;
        }
        if (isInside(p2x2, p2y1, p1x1, p1y1, p1x2, p1y2) == true ) {
            ret = true;
        }
        if (isInside(p2x2, p2y2, p1x1, p1y1, p1x2, p1y2) == true ) {
            ret = true;
        }
        return ret;
    }

    private static Boolean collisionOccurs (ImageObject obj1, ImageObject obj2) {
        Boolean ret = false;
        if (collisionOccursCoordinates(obj1.getX(), obj1.getY(), obj1.getX() + obj1.getWidth(),
                obj1.getY() + obj1.getHeight(), obj2.getX(), obj2.getY(), obj2.getX() + obj2.getWidth(),
                obj2.getY() + obj2.getHeight()) == true) {
            ret = true;
        }
        return ret;
    }

    private static class ImageObject {
        public ImageObject(){}
        public ImageObject (double xinput, double yinput,
                            double xwidthinput, double yheightinput, double angleinput) {
            x = xinput;
            y = yinput;
            xwidth = xwidthinput;
            yheight = yheightinput;
            angle = angleinput;
            internalangle = 0.0;
            coords = new Vector<Double>();
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getWidth() {
            return xwidth;
        }

        public double getHeight() {
            return yheight;
        }

        public double getAngle() {
            return angle;
        }

        public double getInternalAngle() {
            return internalangle;
        }

        public void setAngle (double angleinput) {
            angle = angleinput;
        }

        public void setInternalAngle (double internalangleinput) {
            internalangle = internalangleinput;
        }

        public Vector<Double> getCoords() {
            return coords;
        }

        public void setCoords (Vector<Double> coordsinput) {
            coords = coordsinput;
            generateTriangles();
        }

        public void generateTriangles() {
            triangles = new Vector<Double>();
            // format: (0, 1), (2, 3), (4, 5) is the (x, y) coords of a triangle

            // get center point of all coordinates
            comX = getComX();
            comY = getComY();

            for (int i = 0; i < coords.size(); i = i + 2) {
                triangles.addElement(coords.elementAt(i));
                triangles.addElement(coords.elementAt(i + 1));
                triangles.addElement(coords.elementAt((i + 2) % coords.size()));
                triangles.addElement(coords.elementAt((i + 3) % coords.size()));
                triangles.addElement(comX);
                triangles.addElement(comY);
            }
        }

        public void printTriangles() {
            for (int i = 0; i < triangles.size(); i = i + 6) {
                System.out.println("p0x: " + triangles.elementAt(i) + ", p0y: " + triangles.elementAt(i + 1));
                System.out.println("p1x: " + triangles.elementAt(i + 2) + ", p1y: " + triangles.elementAt(i + 3));
                System.out.println("p2x: " + triangles.elementAt(i + 4) + ", p2y: " + triangles.elementAt(i + 5));

            }
        }

        public double getComX() {
            double ret = 0;
            if (coords.size() > 0) {
                for (int i = 0; i < coords.size(); i = i + 2) {
                    ret = ret + coords.elementAt(i);
                }
                ret = ret / (coords.size() / 2.0);
            }
            return ret;
        }

        public double getComY() {
            double ret = 0;
            if (coords.size() > 0) {
                for (int i = 0; i < coords.size(); i = i + 2) {
                    ret = ret + coords.elementAt(i);
                }
                ret = ret / (coords.size() / 2.0);
            }
            return ret;
        }

        public void move (double xinput, double yinput) {
            x = x + xinput;
            y = y + yinput;
        }

        public void moveto (double xinput, double yinput) {
            x = xinput;
            y = yinput;
        }

        public void screenWrap (double leftEdge, double rightEdge, double topEdge, double bottomEdge) {
            if (x > rightEdge) {
                moveto(leftEdge, getY());
            }
            if (x < leftEdge) {
                moveto(rightEdge, getY());
            }
            if (y > bottomEdge) {
                moveto(getX(), topEdge);
            }
            if (y < topEdge) {
                moveto(getX(), bottomEdge);
            }
        }

        public void rotate (double angleinput) {
            angle = angle + angleinput;
            while (angle > twoPi) {
                angle = angle - twoPi;
            }

            while (angle < 0) {
                angle = angle + twoPi;
            }
        }

        public void spin (double internalangleinput) {
            internalangle = internalangle + internalangleinput;
            while (internalangle > twoPi) {
                internalangle = internalangle - twoPi;
            }

            while (internalangle < 0) {
                internalangle = internalangle + twoPi;
            }
        }
        private double x;
        private double y;
        private double xwidth;
        private double yheight;
        private double angle;
        private double internalangle;
        private Vector<Double> coords;
        private Vector<Double> triangles;
        private double comX;
        private double comY;
    }

    private static void bindKey (JComponent target, String input) {
        target.getInputMap(IFW).put(KeyStroke.getKeyStroke("pressed " + input), input + " pressed");
        target.getActionMap().put(input + " pressed", new KeyPressed(input));

        target.getInputMap(IFW).put(KeyStroke.getKeyStroke("released " + input), input + " released");
        target.getActionMap().put(input + " released", new KeyReleased(input));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setup();
                appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                appFrame.setResizable(false);

                gamePanel = new GamePanel();
                gamePanel.setPreferredSize(new Dimension(WINWIDTH, WINHEIGHT));
                appFrame.getContentPane().add(gamePanel, "Center");

                JPanel myPanel = new JPanel();

                // added for game mode selection
                String[] gameModes = {"Singleplayer", "Multiplayer"};
                JComboBox<String> gameModeMenu = new JComboBox<String>(gameModes);
                gameModeMenu.setSelectedIndex(0);
                gameModeMenu.addActionListener(new GameLevel());
                myPanel.add(gameModeMenu);

                String[] levels = {"One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten"};
                JComboBox<String> levelMenu = new JComboBox<String>(levels);
                levelMenu.setSelectedIndex(2);
                levelMenu.addActionListener(new GameLevel());
                myPanel.add(levelMenu);

                JButton newGameButton = new JButton("New Game");
                newGameButton.addActionListener(new StartGame());
                myPanel.add(newGameButton);

                JButton quitButton = new JButton("Quit Game");
                quitButton.addActionListener(new QuitGame());
                myPanel.add(quitButton);

                JComponent keyTarget = appFrame.getRootPane();
                bindKey(keyTarget, "UP");
                bindKey(keyTarget, "DOWN");
                bindKey(keyTarget, "LEFT");
                bindKey(keyTarget, "RIGHT");
                bindKey(keyTarget, "J");

                // keybindings for multiplayer mode
                bindKey(keyTarget, "W");
                bindKey(keyTarget, "A");
                bindKey(keyTarget, "S");
                bindKey(keyTarget, "D");
                bindKey(keyTarget, "F");

                appFrame.getContentPane().add(myPanel, "South");
                appFrame.pack();
                appFrame.setLocationRelativeTo(null);
                appFrame.setVisible(true);
            }
        });
    }

    private static Boolean endgame;
    private static Boolean enemyAlive;
    private static BufferedImage background;
    private static BufferedImage player;

    private static Boolean p1UpPressed;
    private static Boolean p1DownPressed;
    private static Boolean p1LeftPressed;
    private static Boolean p1RightPressed;
    private static Boolean p1FirePressed;
    private static Boolean p2UpPressed;
    private static Boolean p2DownPressed;
    private static Boolean p2LeftPressed;
    private static Boolean p2RightPressed;
    private static Boolean p2FirePressed;

    private static ImageObject p1;
    private static double p1width;
    private static double p1height;
    private static double p1originalX;
    private static double p1originalY;
    private static double p1velocity;
    private static long p1LastFireMs;

    private static ImageObject p2;
    private static double p2width;
    private static double p2height;
    private static double p2originalX;
    private static double p2originalY;
    private static double p2velocity;
    private static long p2LastFireMs;

    private static ImageObject enemy;
    private static BufferedImage enemyShip;
    private static BufferedImage enemyBullet;
    private static Vector<ImageObject> enemyBullets;
    private static Vector<Long> enemyBulletsTimes;
    private static Long enemybulletlifetime;

    private static Vector<ImageObject> playerBullets;
    private static Vector<Long> playerBulletsTimes;
    private static double bulletWidth;
    private static BufferedImage playerBullet;
    private static Long playerbulletlifetime;
    private static double playerbulletgap;

    private static ImageObject flames;
    private static ImageObject flames2;
    private static BufferedImage flame1;
    private static BufferedImage flame2;
    private static BufferedImage flame3;
    private static BufferedImage flame4;
    private static BufferedImage flame5;
    private static BufferedImage flame6;

    private static int flamecount;
    private static int flamecount2;
    private static double flamewidth;

    private static int level;

    private static Vector<ImageObject> asteroids;
    private static Vector<Integer> asteroidsTypes;
    private static BufferedImage ast1;
    private static BufferedImage ast2;
    private static BufferedImage ast3;
    private static double ast1width;
    private static double ast2width;
    private static double ast3width;

    private static Vector<ImageObject> explosions;
    private static Vector<Long> explosionsTimes;
    private static Long explosionlifetime;
    private static BufferedImage exp1;
    private static BufferedImage exp2;
    private static int expcount;

    private static int XOFFSET;
    private static int YOFFSET;
    private static int WINWIDTH;
    private static int WINHEIGHT;

    private static double pi;
    private static double twoPi;

    private static JFrame appFrame;
    private static GamePanel gamePanel;

    private static final int IFW = JComponent.WHEN_IN_FOCUSED_WINDOW;

    private static final boolean DEBUG = true;
    private static final long PLAYER_LOG_INTERVAL_MS = 200;
    private static long lastPlayerLogMs = 0;

    private static void logImageLoad(String name, BufferedImage img) {
        if (DEBUG == false) {
            return;
        }
        System.out.println("[IMG] " + name + " loaded=" + (img != null));
    }

    private static void logKey(String action, String key) {
        if (DEBUG == false) {
            return;
        }
        System.out.println("[KEY] " + action + " " + key);
    }

    private static void logPlayerState() {
        if (DEBUG == false) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastPlayerLogMs < PLAYER_LOG_INTERVAL_MS) {
            return;
        }
        lastPlayerLogMs = now;
        if (p1 != null) {
            System.out.println("[PLAYER1] x=" + p1.getX() + " y=" + p1.getY() + " v=" + p1velocity + " angle=" + p1.getAngle());
        }
        if (p2 != null) {
            System.out.println("[PLAYER2] x=" + p2.getX() + " y=" + p2.getY() + " v=" + p2velocity + " angle=" + p2.getAngle());
        }
    }

    private static void logCollision(String type) {
        if (DEBUG == false) {
            return;
        }
        System.out.println("[COLLISION] " + type);
    }

    private static BufferedImage loadImage(String name) {
        String resourcePath = "/subDirectory/" + name;
        try (InputStream stream = AsteroidsMacNoAbstraction.class.getResourceAsStream(resourcePath)) {
            if (stream != null) {
                BufferedImage img = ImageIO.read(stream);
                logImageLoad(name, img);
                return img;
            }
        }
        catch (IOException ioe) {
            System.out.println("[IMG] load failed: " + name + " (resource) " + ioe.getMessage());
        }

        File file = new File("subDirectory", name);
        if (!file.exists()) {
            file = new File(System.getProperty("user.dir"), "subDirectory/" + name);
        }
        try {
            BufferedImage img = ImageIO.read(file);
            logImageLoad(name, img);
            return img;
        }
        catch (IOException ioe) {
            System.out.println("[IMG] load failed: " + name + " (file) " + ioe.getMessage());
            return null;
        }
    }

}
