package breakout.engine.base;

import breakout.engine.component.Transform;

public abstract class GameComponent {
  /**
   * The GameObject that this gamecomponent is attached to.
   */
  public GameObject gameObject;

  /**
   * The Transform component of the GameObject that this gamecomponent is attached to.
   */
  public Transform transform;

  /**
   * The order of the update of this component.
   * The smaller the number, the earlier the update.
   * This method MUST be declared for frame update use.
   */
  public abstract int COMPONENT_UPDATE_ORDER();

  /* OVERRIDABLE METHODS BELOW */

  /**
   * Called in the very next frame to do the start-ups.
   * This method is called right before its update method.
   * This method should be overridden by subclasses as needed.
   */
  public void start() {}

  /**
   * Called every frame to update the component behavior of the object.
   * This method is called before its attached gameobject's update method.
   * This method should be overridden by subclasses as needed.
   */
  public void update() {}

  /**
   * Called at the moment right after the gamecomponent is attached on a gameobject.
   * Often times you should do the initializations here.
   * This method should be overridden by subclasses as needed.
   */
  public void onAttached() {}

  /**
   * Called at the moment right after the gamecomponent is detached on a gameobject.
   * This method should be overridden by subclasses as needed.
   */
  public void onDetached() {}
}
