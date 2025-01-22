package breakout.sandbox.object;

import breakout.engine.base.GameObject;
import breakout.engine.component.BoxCollider;
import breakout.engine.component.PhysicsHandler;
import breakout.engine.component.RenderHandler;
import java.util.Map;
import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class Brick extends GameObject {
  public static final double BRICK_WIDTH = 10;
  public static final double BRICK_HEIGHT = 4;
  public static final double STROKE_SIZE = 0.5;
  public static final Color STROKE_COLOR = Color.DARKGRAY;
  public static final Color STROKE_SPECIAL_COLOR = Color.GOLD;
  public static final double ANIMATION_FADE_TIME = 0.6;
  public static final double EXPLOSION_ZOOM_RATE = 3;
  public static final Color EXPLOSION_STROKE_COLOR = Color.ORANGERED;
  public static final Point2D LASER_SIZE = new Point2D(110, 1);
  public static final Map<Character, Color> BRICK_COLORS = Map.of(
    '0', Color.TRANSPARENT,
    '1', Color.WHITE,
    '2', Color.YELLOW,
    '3', Color.ORANGE,
    '4', Color.GREEN,
    '5', Color.BLUE,
    'x', Color.RED,
    'l', Color.DARKVIOLET
  );

  // accessible variables
  public int lives = 0;

  // instance variables
  private char type;
  private Transition animation;
  private PhysicsHandler physicsHandler;
  private RenderHandler renderHandler;
  private BoxCollider collider;

  @Override
  public int TAG() {
    return 3;
  }

  @Override
  public void awake() {
    physicsHandler = attachComponent(PhysicsHandler.class);
    renderHandler = attachComponent(RenderHandler.class);
    collider = attachComponent(BoxCollider.class);

    transform.position = new Point2D((double) getScene().width / 2, (double) (getScene().height * 2) / 3);
    physicsHandler.isStatic = true;
    collider.setShape(new Rectangle(BRICK_WIDTH * getScene().uW, BRICK_HEIGHT * getScene().uH));
  }

  @Override
  public void update() {
    // check life
    if (lives <= 0) {
      collider.isTrigger = true;
    } else {
      collider.isTrigger = false;
    }
  }

  public void setBlockType(char type) {
    this.type = type;
    if (animation != null) {
      animation.stop();
      transform.scale = new Point2D(1, 1);
      collider.canSameTagCollide = false;
      collider.isTrigger = false;
    }

    // Create the image
    Rectangle img;
    img = new Rectangle((BRICK_WIDTH - STROKE_SIZE) * getScene().uW, (BRICK_HEIGHT - STROKE_SIZE) * getScene().uH, BRICK_COLORS.get(type));
    img.setStrokeWidth(STROKE_SIZE * getScene().uW);
    if (type == '0') {
      img.setStroke(Color.TRANSPARENT);
    } else if (Character.isDigit(type)) {
      img.setStroke(STROKE_COLOR);
    } else {
      img.setStroke(STROKE_SPECIAL_COLOR);
    }

    // Normal blocks
    if (Character.isDigit(type)) {
      lives = Character.getNumericValue(type);
    }

    // Explosive blocks
    else if (type == 'x') {
      lives = 1;
    }

    // Lazer blocks
    else if (type == 'l') {
      lives = 1;
    }

    // Set the image
    renderHandler.setImage(img);
  }

  public void takeDamage() {
    if (lives > 0) {
      lives--;
    } else {
      return;
    }

    // Normal blocks
    if (Character.isDigit(type)) {
      setBlockType((char) (type - 1));
    }

    // Explosive blocks
    else if (type == 'x') {
      if (lives == 0) {
        handleExplode();
      }
    }

    // Lazer blocks
    else if (type == 'l') {
      if (lives == 0) {
        handleLaser();
      }
    }
  }

  private void handleExplode() {
    // Increase size
    transform.scale = new Point2D(EXPLOSION_ZOOM_RATE, EXPLOSION_ZOOM_RATE);
    Rectangle img = (Rectangle) getComponent(RenderHandler.class).getImage();
    img.setStroke(EXPLOSION_STROKE_COLOR);

    // Check collision of bricks
    collider.canSameTagCollide = true;
    collider.isTrigger = true;
    collider.setOnTriggerEnter(c -> {
      if (c.gameObject.TAG() == 3) {
        ((Brick) c.gameObject).takeDamage();
      }
    });

    // Fade out
    FadeTransition fade = new FadeTransition(Duration.seconds(ANIMATION_FADE_TIME), img);
    fade.setFromValue(1.0);
    fade.setToValue(0.0);
    fade.setOnFinished(e -> {
      setBlockType('0');
      collider.canSameTagCollide = false;
      transform.scale = new Point2D(1, 1);
      img.setStroke(STROKE_COLOR);
    });
    fade.play();

    // Set animation
    if (animation != null) {
      animation.stop();
    }
    animation = fade;
  }

  private void handleLaser() {
    // Increase size
    Point2D offset = transform.position.subtract(new Point2D(50 * getScene().uW, transform.position.getY()));
    transform.position = new Point2D(50 * getScene().uW, transform.position.getY());
    transform.scale = new Point2D(
        LASER_SIZE.getX() / getComponent(RenderHandler.class).getImage().getBoundsInLocal().getWidth() * getScene().uW,
        LASER_SIZE.getY() / getComponent(RenderHandler.class).getImage().getBoundsInLocal().getHeight() * getScene().uH
    );
    Rectangle img = (Rectangle) getComponent(RenderHandler.class).getImage();
    img.setStroke(EXPLOSION_STROKE_COLOR);

    // Check collision of bricks
    collider.canSameTagCollide = true;
    collider.isTrigger = true;
    collider.setOnTriggerEnter(c -> {
      if (c.gameObject.TAG() == 3) {
        ((Brick) c.gameObject).takeDamage();
      }
    });

    // Fade out
    FadeTransition fade = new FadeTransition(Duration.seconds(ANIMATION_FADE_TIME), img);
    fade.setFromValue(1.0);
    fade.setToValue(0.0);
    fade.setOnFinished(e -> {
      setBlockType('0');
      collider.canSameTagCollide = false;
      transform.position = transform.position.add(offset);
      transform.scale = new Point2D(1, 1);
      img.setStroke(STROKE_COLOR);
    });
    fade.play();

    // Set animation
    if (animation != null) {
      animation.stop();
    }
    animation = fade;
  }

}

