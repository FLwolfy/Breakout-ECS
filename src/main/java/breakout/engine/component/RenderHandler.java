package breakout.engine.component;

import breakout.engine.base.GameComponent;
import breakout.engine.base.GameScene;
import java.util.ArrayList;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

public class RenderHandler extends GameComponent {
  private static final ArrayList<RenderHandler> allRenderHandlers = new ArrayList<>();

  // Settings
  public int renderOrder = 0;

  // accessible variables
  public double rawWidth;
  public double rawHeight;
  public double width;
  public double height;
  
  // instance variables
  private int oldRenderOrder;
  private Node image;

  @Override
  public int COMPONENT_UPDATE_ORDER() {
    return 3;
  }

  @Override
  public void onAttached() {
    oldRenderOrder = 0;
    rawWidth = 0;
    rawHeight = 0;
    width = 0;
    height = 0;

    allRenderHandlers.add(this);
  }
  
  @Override
  public void update() {
    if (image == null) {
      return;
    }

    // update the root render order
    if (GameScene.getInnerScene().getRoot().getChildrenUnmodifiable().isEmpty() || oldRenderOrder != renderOrder) {
      updateRootOrder();
      oldRenderOrder = renderOrder;
    }
    
    // update the image position
    Bounds bounds = image.getBoundsInLocal();
    double centerX = bounds.getMinX() + bounds.getWidth() / 2;
    double centerY = bounds.getMinY() + bounds.getHeight() / 2;

    image.getTransforms().clear();
    image.getTransforms().addAll(
        new Translate(
            transform.position.getX() - centerX,
            transform.position.getY() - centerY
        ),
        new Rotate(
            transform.rotation,
            centerX,
            centerY
        ),
        new Scale(
            transform.scale.getX(),
            transform.scale.getY(),
            centerX,
            centerY
        )
    );
    width = image.getBoundsInParent().getWidth();
    height = image.getBoundsInParent().getHeight();
  }

  @Override
  public void onDetached() {
    allRenderHandlers.remove(this);
    updateRootOrder();
  }

  public void setImage(Node image) {
    this.image = image;
    this.rawWidth = image.getBoundsInParent().getWidth();
    this.rawHeight = image.getBoundsInParent().getHeight();

    updateRootOrder();
    update();
  }
  
  public Node getImage() {
    return image;
  }

  private static void updateRootOrder() {
    ((Group) GameScene.getInnerScene().getRoot()).getChildren().setAll(
      RenderHandler.allRenderHandlers.stream()
        .filter(handler -> handler.getImage() != null && handler.gameObject.getScene() != null && handler.gameObject.getScene().isActive)
        .sorted((h1, h2) -> Integer.compare(h2.renderOrder, h1.renderOrder))
        .map(RenderHandler::getImage)
        .toList()
    );
  }
}
