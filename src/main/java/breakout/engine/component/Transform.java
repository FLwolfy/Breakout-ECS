package breakout.engine.component;

import breakout.engine.base.GameComponent;
import javafx.geometry.Point2D;

public class Transform extends GameComponent {
  public Point2D position;
  public Point2D scale;
  public double rotation;

  @Override
  public int COMPONENT_UPDATE_ORDER() {
    return 0;
  }

  @Override
  public void onAttached() {
    position = new Point2D(0, 0);
    scale = new Point2D(1, 1);
    rotation = 0;
  }
}
