package breakout.engine.UI;

import breakout.engine.component.RenderHandler;
import breakout.engine.base.GameObject;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

public class UIWidget extends GameObject {

  // attributes
  public double width;
  public double height;

  // Settings
  public boolean isHighlightedOnHover = false;
  public boolean isClickable = false;

  // instance variables
  private boolean isHovered;
  private boolean isSliding;
  private Point2D targetPosition;
  private double slideSpeed;
  private EventHandler<ActionEvent> slideReachEvent;
  private Node image;
  private Node highlightedImage;
  private EventHandler<ActionEvent> onClick;

  // Components
  private RenderHandler renderHandler;

  @Override
  public int TAG() {
    return 0;
  }

  @Override
  public void awake() {
    renderHandler = attachComponent(RenderHandler.class);

    transform.position = new Point2D((double) getScene().width / 2, (double) getScene().height / 2);

    isHovered = false;
    isSliding = false;

    setImage(new Rectangle(100, 100, Paint.valueOf("#F0F0F0")));
    setHighlightedImage(new Rectangle(100, 100, Paint.valueOf("#F0F000")));
  }

  @Override
  public void update() {
    Point2D mousePos = getScene().getMouseCursor();

    // Check if the widget is sliding
    if (targetPosition != null) {
      slideTowards(targetPosition, slideSpeed, slideReachEvent);
    }

    // Check if the cursor is hovered on the widget
    if (isClickable || isHighlightedOnHover) {
      if (isHovered) {
        if (mousePos.getX() < transform.position.getX() - renderHandler.width / 2 || mousePos.getX() > transform.position.getX() + renderHandler.width / 2 ||
            mousePos.getY() < transform.position.getY() - renderHandler.height / 2 || mousePos.getY() > transform.position.getY() + renderHandler.height / 2) {
          isHovered = false;
          if (isHighlightedOnHover) {
            renderHandler.setImage(image);
          }
        }
      } else {
        if (mousePos.getX() > transform.position.getX() - renderHandler.width / 2 && mousePos.getX() < transform.position.getX() + renderHandler.width / 2 &&
            mousePos.getY() > transform.position.getY() - renderHandler.height / 2 && mousePos.getY() < transform.position.getY() + renderHandler.height / 2) {
          isHovered = true;
          if (isHighlightedOnHover) {
            renderHandler.setImage(highlightedImage);
          }
        }
      }
    }

    // Check if the widget is clicked
    if (isClickable && isHovered && getScene().getMouseInput() == MouseButton.PRIMARY) {
      renderHandler.setImage(image);
      isHovered = false;
      onClick.handle(new ActionEvent(this, null));
    }
  }

  public void setImage(Node image) {
    this.image = image;
    renderHandler.setImage(image);
  }

  public Node getImage() {
    return image;
  }

  public void setHighlightedImage(Node highlightedImage) {
    this.highlightedImage = highlightedImage;
  }

  public Node getHighlightedImage() {
    return highlightedImage;
  }

  public void setOnClick(EventHandler<ActionEvent> onClick) {
    this.onClick = onClick;
  }

  public void slideTowards(Point2D target, double speed, EventHandler<ActionEvent> onReach) {
    if (!isSliding) {
      isSliding = true;
      targetPosition = target;
      slideSpeed = speed;
      slideReachEvent = onReach;
    }

    if (transform.position.distance(target) < speed) {
      stopSliding();

      return;
    }

    Point2D direction = target.subtract(transform.position);
    transform.position = transform.position.add(direction.multiply(speed));
  }

  public void stopSliding() {
    transform.position = targetPosition;

    isSliding = false;
    targetPosition = null;
    slideSpeed = 0;

    if (slideReachEvent != null) {
      EventHandler<ActionEvent> currentEvent = this.slideReachEvent;
      slideReachEvent.handle(new ActionEvent(this, null));
      if (currentEvent == this.slideReachEvent) {
        slideReachEvent = null;
      }
    }
  }
}
