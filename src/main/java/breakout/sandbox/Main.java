package breakout.sandbox;

import breakout.engine.base.GameScene;
import breakout.engine.base.GameSourceLoader;
import breakout.sandbox.scene.MainScene;
import breakout.sandbox.scene.MenuScene;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.Duration;

/////////////////////////////////////////////////////////////////////////
/// THIS GAME IS MADE BY HSUAN-KAI LIAO, A STUDENT AT DUKE UNIVERSITY ///
/////////////////////////////////////////////////////////////////////////

public class Main extends Application {
  // window constant
  public static final String TITLE = "Breakout Example";
  public static final int WIDTH = 700;
  public static final int HEIGHT = 700;
  public static final Paint BACKGROUND = Paint.valueOf("#404040");
  public static final int FRAMES_PER_SECOND = 60;

  // resources
  public static final String IMAGE_PATH = "breakout/image";
  public static final String LEVEL_PATH = "breakout/level";

  // instance variables
  private Timeline gameLoop;
  private Scene FXScene;

  @Override
  public void start(Stage stage) {
    // Load all the files and configurations
    GameSourceLoader.loadAll(IMAGE_PATH, LEVEL_PATH);

    // initialize the scene
    FXScene = new Scene(new Group(), WIDTH, HEIGHT, BACKGROUND);
    stage.setResizable(false);
    stage.setTitle(TITLE);
    stage.setScene(FXScene);
    stage.show();
    GameScene.setInnerScene(FXScene);

    // add the game scenes
    GameScene.addScene(MenuScene.class);
    GameScene.addScene(MainScene.class);
    GameScene.setActiveScene(MenuScene.class);

    // initialize the game loop
    gameLoop = new Timeline();
    gameLoop.setCycleCount(Timeline.INDEFINITE);
    gameLoop.getKeyFrames().add(new KeyFrame(Duration.seconds(1.0 / FRAMES_PER_SECOND), e -> GameScene.step(1.0 / FRAMES_PER_SECOND)));

    // start the game
    gameLoop.play();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
