package breakout.sandbox.object;

import breakout.engine.base.GameObject;
import breakout.engine.component.BoxCollider;
import breakout.engine.component.PhysicsHandler;
import breakout.engine.component.RenderHandler;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Paddle extends GameObject {
  public static final double PADDLE_WIDTH = 15;
  public static final double PADDLE_HEIGHT = 2;
  public static final Point2D PADDLE_START_POSITION = new Point2D(50, 88);

  // instance variables
  private PhysicsHandler physicsHandler;
  private RenderHandler renderHandler;
  private BoxCollider collider;

  @Override
  public int TAG() {
    return 2;
  }

  @Override
  public void awake() {
    physicsHandler = attachComponent(PhysicsHandler.class);
    renderHandler = attachComponent(RenderHandler.class);
    collider = attachComponent(BoxCollider.class);

    transform.position = new Point2D(PADDLE_START_POSITION.getX() * getScene().uW, PADDLE_START_POSITION.getY() * getScene().uH);
    physicsHandler.isStatic = true;
    physicsHandler.applyAirResistance = true;
    physicsHandler.airResistancePercentage = 0.9;
    renderHandler.setImage(new Rectangle(PADDLE_WIDTH * getScene().uW, PADDLE_HEIGHT * getScene().uH, Color.WHEAT));
    collider.setShape(new Rectangle(PADDLE_WIDTH * getScene().uW, PADDLE_HEIGHT * getScene().uH));
  }

  @Override
  public void update() {
    // TODO Auto-generated method stub

  }

}
