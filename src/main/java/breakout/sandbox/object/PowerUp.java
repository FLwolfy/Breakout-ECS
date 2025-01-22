package breakout.sandbox.object;

import breakout.engine.base.GameObject;
import breakout.engine.base.GameScene;
import breakout.engine.component.CircleCollider;
import breakout.engine.component.PhysicsHandler;
import breakout.engine.component.RenderHandler;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;

public class PowerUp extends GameObject {
  public static final double POWERUP_SIZE = 4;
  public static final int POWERUP_AMOUNT = 4;
  public static final double POWERUP_SPEED = 250;
  public static final double POWERUP_ROTATION_SPEED = 3;
  public static final Point2D POWERUP_START_POSITION = new Point2D(50, 10);

  // accessible variables
  public boolean isActive = true;

  // instance variables
  private boolean previousActiveState = true;
  private RenderHandler renderHandler;
  private CircleCollider collider;
  private PhysicsHandler physicsHandler;

  private double elapsedTime = 0;

  @Override
  public int TAG() {
    return 4;
  }

  @Override
  public void awake() {
    renderHandler = attachComponent(RenderHandler.class);
    collider = attachComponent(CircleCollider.class);
    physicsHandler = attachComponent(PhysicsHandler.class);

    transform.position = new Point2D(POWERUP_START_POSITION.getX() * getScene().uW, POWERUP_START_POSITION.getY() * getScene().uH);
    renderHandler.setImage(createStarShape(POWERUP_SIZE * getScene().uW, POWERUP_SIZE * getScene().uW));
    physicsHandler.isStatic = true;
    physicsHandler.velocity = new Point2D((Math.random() * 2 - 1) * POWERUP_SPEED * GameScene.deltaTime, 0);
    physicsHandler.angularVelocity = (Math.random() * 2 - 1) * POWERUP_ROTATION_SPEED;
    collider.setShape(new Circle(getScene().uW * POWERUP_SIZE / 2));
    collider.isTrigger = true;
  }

  @Override
  public void update() {
    setPowerUpColor();
    checkWallCollide();
  }

  private void setPowerUpColor() {
    if (previousActiveState && !isActive) {
      previousActiveState = false;
      ((Polygon) renderHandler.getImage()).setFill(Color.TRANSPARENT);
      return;
    } else if (!previousActiveState && isActive) {
      previousActiveState = true;
    }

    if (isActive) {
      // Accumulate elapsed time
      elapsedTime += GameScene.deltaTime;
      if (elapsedTime > Math.PI * 2) {
        elapsedTime -= Math.PI * 2;
      }

      // Compute new color based on elapsed time
      double red = (Math.sin(elapsedTime) + 1) / 2;
      double green = (Math.sin(elapsedTime + Math.PI / 2) + 1) / 2;
      double blue = (Math.sin(elapsedTime + Math.PI) + 1) / 2;

      ((Polygon) renderHandler.getImage()).setFill(Color.color(red, green, blue)); // Update color
    }
  }

  private void checkWallCollide() {
    // Bounce back from the edge
    if (transform.position.getX() < collider.radiusX && physicsHandler.velocity.getX() < 0
        || transform.position.getX() > getScene().width - collider.radiusY && physicsHandler.velocity.getX() > 0) {
      physicsHandler.velocity = new Point2D(-physicsHandler.velocity.getX(), 0);
    }
  }

  private Polygon createStarShape(double width, double height) {
    Polygon star = new Polygon();
    double centerX = width / 2;
    double centerY = height / 2;
    double radius = Math.min(width, height) / 2;
    for (int i = 0; i < 10; i++) {
      double angle = Math.PI / 5 * i;
      double r = (i % 2 == 0) ? radius : radius / 2.5;
      double x = centerX + r * Math.cos(angle);
      double y = centerY - r * Math.sin(angle);
      star.getPoints().addAll(x, y);
    }
    return star;
  }
}
