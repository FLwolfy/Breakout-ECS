package breakout.sandbox.object;

import breakout.engine.component.CircleCollider;
import breakout.engine.component.PhysicsHandler;
import breakout.engine.component.RenderHandler;
import breakout.engine.base.GameObject;
import breakout.engine.base.GameScene;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Bouncer extends GameObject {
  public static final double BOUNCER_SPEED = 300;
  public static final double BOUNCER_SIZE = 1.5;
  public static final Point2D BOUNCER_START_POSITION = new Point2D(50, 83);

  // settings
  public boolean canBounceGround = false;

  // instance variables
  private PhysicsHandler physicsHandler;
  private RenderHandler renderHandler;
  private CircleCollider collider;

  @Override
  public int TAG() {
    return 1;
  }

  @Override
  public void awake() {
    physicsHandler = attachComponent(PhysicsHandler.class);
    renderHandler = attachComponent(RenderHandler.class);
    collider = attachComponent(CircleCollider.class);

    transform.position = new Point2D(BOUNCER_START_POSITION.getX() * getScene().uW, BOUNCER_START_POSITION.getY() * getScene().uH);
    physicsHandler.isStatic = false;
    physicsHandler.velocity = new Point2D(0, -BOUNCER_SPEED * GameScene.deltaTime);
    renderHandler.setImage(new Circle(getScene().uW * BOUNCER_SIZE, Color.WHITE));
    collider.setShape(new Circle(getScene().uW * BOUNCER_SIZE));
  }

  @Override
  public void update() {
    checkWallCollision();
  }

  private void checkWallCollision() {
    // Bounce back from the edge
    if (transform.position.getX() < collider.radiusX && physicsHandler.velocity.getX() < 0
        || transform.position.getX() > getScene().width - collider.radiusX && physicsHandler.velocity.getX() > 0) {
      physicsHandler.velocity = new Point2D(-physicsHandler.velocity.getX(), physicsHandler.velocity.getY());
    }
    if (transform.position.getY() < collider.radiusY && physicsHandler.velocity.getY() < 0
        || ((canBounceGround) && (transform.position.getY() > getScene().height - collider.radiusY && physicsHandler.velocity.getY() > 0))) {
      physicsHandler.velocity = new Point2D(physicsHandler.velocity.getX(), -physicsHandler.velocity.getY());
    }
  }
}
