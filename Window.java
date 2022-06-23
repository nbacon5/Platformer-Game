import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.imageio.*;
import java.io.File;
import java.util.ArrayList;

class Window extends JPanel{
    private int width;
    private int height;
    private int pixel = 10;
    private int groundSpeed = pixel/2;
    private int tile = 16*pixel;

    private Sprite player;
    private ArrayList<Sprite> objects;
    private ArrayList<Sprite> sprites;
    private boolean right;
    private boolean left;
    private boolean up;
    private boolean down;
    private final int gravity = 1;
    private final int jumpForce = -20;
    private boolean onGround;
    private Image background;
    private boolean mirror = false;

    public Window(int width, int height){
        this.width = width;
        this.height = height;

        this.addKeyListener(new Controller());
        
        objects = new ArrayList<Sprite>();

        background = null;
        try{
            background = ImageIO.read(new File("background.png"));
        }
        catch (Exception e){
        }
        background = background.getScaledInstance(width, height, 0);

        load();

        SwingWorker worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground(){
                long now;
                long updateTime;
                long wait;

                final int TARGET_FPS = 60;
                final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;

                while (true) {
                    now = System.nanoTime();

                        //System.out.println("s");
                    physics();
                        //System.out.println("j");
                    //collisions();
                    repaint();

                    updateTime = System.nanoTime() - now;
                    wait = (OPTIMAL_TIME - updateTime) / 1000000;

                    try {
                        Thread.sleep(wait);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            protected void done(){}
        };

        worker.execute();
    }

    public void load(){
        player = new Sprite("sprite.png", new Point(width/2, height/2), pixel, true); 
        objects.add(new Sprite("sprite.png", new Point(width*2, height/2), pixel, true)); 
        objects.add(new Sprite("sprite.png", new Point(width/2, height/4), pixel, true)); 
        objects.add(new Sprite("sprite.png", new Point(width/4, height/2), pixel, true)); 
        objects.add(new Sprite("tile.png", new Point(width/2 - tile*10, height/2 + player.hitBox.height), new Dimension(tile*20, tile), pixel, false));
        objects.add(new Sprite("black.png", new Point(width/2 - tile*10, height/2 + player.hitBox.height + tile), new Dimension(tile*20, tile*2), pixel, false));

        sprites = new ArrayList<Sprite>(objects);
        sprites.add(player);
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create(); 
        g2d.drawImage(background, 0, 0, null);
        if (!mirror) g2d.drawImage(player.image, width/2, height/2, player.image.getWidth(null), player.image.getHeight(null), null);
        else g2d.drawImage(player.image, width/2 + player.image.getWidth(null), height/2, -player.image.getWidth(null), player.image.getHeight(null), null);
        g2d.drawRect(width/2, height/2, player.hitBox.width, player.hitBox.height);
        drawObjects(g2d);
        g2d.dispose();
        //System.out.println("g");
    }

    public void drawObjects(Graphics g){
        int winPosX = -1;
        int winPosY = -1;
        for (Sprite object : objects){
            winPosX = object.gamePos.x - player.gamePos.x + width/2;
            winPosY = object.gamePos.y - player.gamePos.y + height/2;
            if (object.images != null){
                for (int i = 0; i < object.images.length; i++){
                    for (int j = 0; j < object.images[i].length; j++){
                        g.drawImage(object.image, winPosX + tile*j, winPosY + tile*i, null);
                    }
                }
            }
            else{
                if (object.image != null) g.drawImage(object.image, winPosX, winPosY, null);
                g.drawRect(winPosX, winPosY, object.hitBox.width, object.hitBox.height);
            }
        }
    }

    public void collisions(Sprite movingSprite){
        Rectangle movingSpriteBox = new Rectangle(movingSprite.gamePos, movingSprite.hitBox);
        Rectangle spriteBox = null;
        Rectangle intersection = null;
        for (Sprite sprite : sprites){
            spriteBox = new Rectangle(sprite.gamePos, sprite.hitBox);
            if (movingSpriteBox.intersects(spriteBox) && sprite != movingSprite){
                intersection = movingSpriteBox.intersection(spriteBox);
                //System.out.println(movingSprite.gamePos +" "+ movingSprite.prevGamePos);
                    if (movingSprite.velocity.x > 0 && intersection.x == spriteBox.x && movingSprite.prevGamePos.x < movingSprite.gamePos.x){ //right
                        //System.out.println("right");
                        movingSprite.gamePos.translate(-intersection.width, 0);
                    }
                    else if (movingSprite.velocity.x < 0 && intersection.x + intersection.width == spriteBox.x + spriteBox.width && movingSprite.prevGamePos.x > movingSprite.gamePos.x){ //left
                        //System.out.println("left");
                        movingSprite.gamePos.translate(intersection.width, 0);
                    }
                    else if (movingSprite.velocity.y < 0 && intersection.y + intersection.height == spriteBox.y + spriteBox.height && movingSprite.prevGamePos.y > movingSprite.gamePos.y){ //up
                        //System.out.println("up");
                        movingSprite.gamePos.translate(0, intersection.height);
                        movingSprite.acceleration.y += 3;
                    }
                    else if (movingSprite.velocity.y > 0 && intersection.y == spriteBox.y && movingSprite.prevGamePos.y < movingSprite.gamePos.y){ //down
                        //System.out.println("down");
                        movingSprite.gamePos.translate(0, -intersection.height);
                        movingSprite.acceleration.move(movingSprite.acceleration.x, 0);
                        movingSprite.velocity.move(movingSprite.velocity.x, 0);
                        if (movingSprite == player) onGround = true;
                    }
            }
        }
    }    

    public void updatePrevGamePos(){
        player.prevGamePos.move(player.gamePos.x, player.gamePos.y);
        for (Sprite object : objects){
            object.prevGamePos.move(object.gamePos.x, object.gamePos.y);
        }
    }

    public void physics(){
        playerMovement();
        gravity();
        applyForces();
    }

    public void gravity(){
        for (Sprite sprite : sprites){
            if (sprite.gravity == true) sprite.acceleration.translate(0, gravity); 
        }
    }

    //Up and Down reversed?
    public void playerMovement(){
        player.velocity.move(0, 0);
        if (up && onGround){
            player.acceleration.translate(0, jumpForce);
        }

        if (right){
            mirror = false;
            player.velocity.move(groundSpeed, 0);
        }

        else if (left){
            mirror = true;
            player.velocity.move(-groundSpeed, 0);
        }

    }

    public void applyForces(){
        onGround = false;

        for (Sprite sprite : sprites){
            sprite.velocity.translate(sprite.acceleration.x, sprite.acceleration.y);

            sprite.updatePrevGamePos();
            sprite.gamePos.translate(sprite.velocity.x, 0);
            collisions(sprite);

            sprite.updatePrevGamePos();
            sprite.gamePos.translate(0, sprite.velocity.y);
            collisions(sprite);
        }

    }

    public void resetMovement(){
        right = false;
        left = false;
        up = false;
        down = false;
    }

    class Controller implements KeyListener{
        @Override
        public void keyPressed(KeyEvent e){
            switch (e.getKeyCode()){
                case (KeyEvent.VK_RIGHT):
                    //resetMovement();
                    right = true;
                    break;
                case (KeyEvent.VK_LEFT):
                    //resetMovement();
                    left = true;
                    break;
                case (KeyEvent.VK_UP):
                    //resetMovement();
                    up = true;
                    break;
                case (KeyEvent.VK_DOWN):
                    //resetMovement();
                    down = true;
                    break;
            }
        }

        public void keyReleased (KeyEvent e){
            switch (e.getKeyCode()){
                case (KeyEvent.VK_RIGHT):
                    right = false;
                    break;
                case (KeyEvent.VK_LEFT):
                    left = false;
                    break;
                case (KeyEvent.VK_UP):
                    up = false;
                    break;
                case (KeyEvent.VK_DOWN):
                    down = false;
                    break;
            }
        }

        public void keyTyped (KeyEvent e){}
    }
}
