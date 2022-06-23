import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.imageio.*;
import java.io.File;

class Sprite{
    Point gamePos;
    Point prevGamePos;
    Point velocity;
    Point acceleration;
    String imageSrc;
    Dimension hitBox;
    Image image;
    Image[][] images;
    boolean gravity;
    int pixel;
    int tile;

    public void init(String imageSrc, Point gamePos, int pixel, boolean gravity){
        this.imageSrc = imageSrc;
        this.gamePos = gamePos;
        this.prevGamePos = new Point(gamePos);
        this.pixel = pixel;
        this.gravity = gravity;
        velocity = new Point(0,0);
        acceleration = new Point(0,0);
    }

    public Sprite(String imageSrc, Point gamePos, int pixel, boolean gravity){
        init(imageSrc, gamePos, pixel, gravity);
        createImage();
        hitBox = new Dimension(image.getWidth(null), image.getHeight(null));
    }

    public Sprite(String imageSrc, Point gamePos, Dimension hitBox, int pixel, boolean gravity){
        init(imageSrc, gamePos, pixel, gravity);
        this.hitBox = hitBox;

        tile = 16*pixel;
        try{
            image = ImageIO.read(new File(imageSrc));
        }
        catch (Exception e){
        }

        int rows = hitBox.height/tile;
        int cols = hitBox.width/tile;
        images = new Image[rows][cols];

        for (int i = 0; i < rows; i++){
            for (int j = 0; j < cols; j++){
                images[i][j] = image.getScaledInstance(tile, tile, 0);
            }
        }
                image = image.getScaledInstance(tile, tile, 0);

    }

    public void createImage(){
        try{
            image = ImageIO.read(new File(imageSrc));
        }
        catch (Exception e){
        }

        image = image.getScaledInstance(image.getWidth(null)*pixel, image.getHeight(null)*pixel, 0);
    }

    public boolean isMoving(){
        if (velocity.x != 0 || velocity.y != 0){
            return true;
        }
        else{
            return false;
        }
    }

    public void updatePrevGamePos(){
        prevGamePos.setLocation(gamePos);
    }
}
