package breakout.engine.base;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

public abstract class GameScene {
  // static variables
  public static double deltaTime;
  private static final Map<Class<? extends GameScene>, GameScene> allScenes = new HashMap<>();
  private static final ArrayList<EventHandler<ActionEvent>> subscribedActions = new ArrayList<>();
  private static final ArrayList<GameComponent> allComponentsToBeUpdated = new ArrayList<>();
  private static EventHandler<ActionEvent> sceneChangeAction;
  private static Scene scene;
  private static GameScene previousScene;
  private static GameScene currentScene;

  // scene attributes
  public final double width;
  public final double height;
  public final double uW;
  public final double uH;
  public boolean isActive;

  // instance variables
  private final ArrayList<GameObject> allObjects;
  
  // Inputs
  private KeyCode keyInput;
  private MouseButton mouseInput;
  private Point2D mouseCursor;

  public GameScene() {
    width = scene.getWidth();
    height = scene.getHeight();
    uW = width / 100;
    uH = height / 100;

    allObjects = new ArrayList<>();

    keyInput = null;
    mouseCursor = new Point2D(0, 0);
  }

  private void updateInputHandler() {
    scene.setOnKeyPressed(e -> keyInput = e.getCode());
    scene.setOnKeyReleased(e -> {
      if (e.getCode() == keyInput) {
        keyInput = null;
      }
    });
    scene.setOnMousePressed(e -> mouseInput = e.getButton());
    scene.setOnMouseReleased(e -> {
      if (e.getButton() == mouseInput) {
        mouseInput = null;
      }
    });
    scene.setOnMouseMoved(e -> mouseCursor = new Point2D(e.getX(), e.getY()));
  }

  /* API BELOW */
  /**
   * Set the inner JAVAFX scene of the game.
   */
  public static void setInnerScene(Scene scene) {
    GameScene.scene = scene;
  }

  /**
   * Get the inner JAVAFX scene of the game.
   */
  public static Scene getInnerScene() {
    return scene;
  }

  /**
   * Add a new scene to the game. The first scene added will be the current scene.
   * Once the scene is added, there is NO WAY you can remove it.
   */
  public static <T extends GameScene> void addScene(Class<T> sceneClass) {
    if (allScenes.containsKey(sceneClass)) {
      return;
    }
    try {
      T scene = sceneClass.getDeclaredConstructor().newInstance();

      allScenes.put(sceneClass, scene);
      if (currentScene == null) {
        currentScene = scene;
      }

      subscribedActions.add(e -> scene.start());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Get the scene of the specified class type.
   */
  public static <T extends GameScene> T getScene(Class<T> sceneClass) {
    return (T) allScenes.get(sceneClass);
  }

  /**
   * Set the active scene to the specified scene.
   */
  public static <T extends GameScene> void setActiveScene(Class<T> sceneClass) {
    if (allScenes.containsKey(sceneClass)) {
      GameScene newScene = allScenes.get(sceneClass);

      // change the scene
      if (currentScene != null) {
        previousScene = currentScene;
        subscribedActions.add(e -> { previousScene.isActive = false; previousScene.onSleep(); });
      }
      currentScene = newScene;
      subscribedActions.add(event -> { currentScene.isActive = true; currentScene.onActive(); });

      // clear the previous scene
      ((Group) getInnerScene().getRoot()).getChildren().setAll();

      // passing inputs
      currentScene.updateInputHandler();
      currentScene.mouseCursor = previousScene.mouseCursor;
      currentScene.keyInput = previousScene.keyInput;
      currentScene.mouseInput = previousScene.mouseInput;

      return;
    }
    throw new RuntimeException("The scene is not found.");
  }

  /**
   * The actions that will be performed for current scene in every frame.
   */
  public static void step(double elapsedTime) {
    if (currentScene == null) {
      throw new RuntimeException("No scene is currently active.");
    }

    // scene change
    if (sceneChangeAction != null) {
      sceneChangeAction.handle(new ActionEvent(currentScene, null));
      sceneChangeAction = null;
      return;
    }

    // update the current scene delta time
    deltaTime = elapsedTime;

    // check subscribed actions
    int actionsCount = subscribedActions.size();
    for (int i = 0; i < actionsCount; i++) {
      EventHandler<ActionEvent> action = subscribedActions.get(i);
      action.handle(new ActionEvent(currentScene, null));
    }
    subscribedActions.subList(0, actionsCount).clear();

    // gameobjects update
    for (int i = 0; i < currentScene.allObjects.size(); i++) {
      currentScene.allObjects.get(i).update();

      // check the components to be updated
      allComponentsToBeUpdated.addAll(currentScene.allObjects.get(i).getAllAttachedComponents().values());
    }

    // gamecomponent update
    allComponentsToBeUpdated.sort((c1, c2) -> Integer.compare(c1.COMPONENT_UPDATE_ORDER(), c2.COMPONENT_UPDATE_ORDER()));
    for (GameComponent component : allComponentsToBeUpdated) {
      component.update();
    }
    allComponentsToBeUpdated.clear();

    // the very last update
    currentScene.lateUpdate();
  }

  /**
   * Change the current scene to the specified scene.
   * This method will be called very lastly in the current frame.
   */
  public <T extends GameScene> void changeScene(Class<T> sceneClass) {
    if (sceneChangeAction == null) {
      sceneChangeAction = e -> setActiveScene(sceneClass);
    } else {
      throw new RuntimeException("Scene change is already in progress.");
    }
  }

  /**
   * Register the object onto this scene for synchronous frame updates.
   */
  protected void registerObject(GameObject object) {
    // Add object to the list
    allObjects.add(object);

    // give the gamescene reference to the object
    try {
      Field sceneField = GameObject.class.getDeclaredField("attachedScene");
      sceneField.setAccessible(true);
      sceneField.set(object, this);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    
    // call the awake() method and subscribe the start() method
    object.awake();
    subscribedActions.add(e -> object.start());
  }

  /**
   * Unregister the object from this scene.
   * This will call the onDestroy() method immediately and the object will be removed from the scene in the very next frame.
   */
  protected void unregisterObject(GameObject object) {
    // called the onDestroy() method
    object.onDestroy();

    // detach all the components
    for (Class<? extends GameComponent> componentClass : object.getAllAttachedComponents().keySet()) {
      object.detachComponent(componentClass);
    }

    // Reset the gamescene reference of the object back to null
    try {
      Field awakeField = GameObject.class.getDeclaredField("attachedScene");
      awakeField.setAccessible(true);
      awakeField.set(object, null);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }

    // Subscribe the removal of the object
    subscribedActions.add(e -> allObjects.remove(object));
  }

  /**
   * Instantiate a new object of the specified class type and register it onto this scene.
   * Return the reference of the instantiated object.
   */
  protected <T extends GameObject> T instantiateObject(Class<T> objectClass) {
    try {
      T object = objectClass.getDeclaredConstructor().newInstance();
      registerObject(object);
      return object;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Subscribe a given action to be executed in the very next upcoming frame.
   * The subscribed actions will only be executed once.
   */
  public void subscribeAction(EventHandler<ActionEvent> action) {
    subscribedActions.add(action);
  }

  /**
   * Get the input key keycode.
   * After the keycode have been used, it will be no longer active until the next input.
   */
  public KeyCode getKeyInput() {
    KeyCode temp = keyInput;
    keyInput = null;
    return temp;
  }

  /**
   * Get the input mouse button.
   */
  public MouseButton getMouseInput() {
    MouseButton temp = mouseInput;
    mouseInput = null;
    return temp;
  }

  /**
   * Get the mouse cursor position vector.
   */
  public Point2D getMouseCursor() {
    return mouseCursor;
  }

  /* OVERRIDABLE METHODS BELOW */

  /**
   * Called in the very first frame when the gamescene is added on the game (before the first onActive()).
   * This method should be overridden by subclasses as needed.
   */
  public void start() {}

  /**
   * Called in the very next frame when the scene is active.
   * This method should be overridden by subclasses as needed.
   */
  public void onActive() {}

  /**
   * Called in the very next frame when the scene is no longer active.
   * This method should be overridden by subclasses as needed.
   */
  public void onSleep() {}

  /**
   * Called very lastly in every frame to do the final calculation.
   * This is often used for handling inputs and scene changes.
   * This method should be overridden by subclasses as needed.
   */
  public void lateUpdate() {}
}
