package breakout.sandbox.scene;

import breakout.engine.UI.UIWidget;
import breakout.engine.base.GameScene;
import breakout.engine.base.GameSourceLoader;
import breakout.engine.component.BoxCollider;
import breakout.engine.component.CircleCollider;
import breakout.engine.component.PhysicsHandler;
import breakout.engine.component.RenderHandler;
import breakout.sandbox.object.Bouncer;
import breakout.sandbox.object.Brick;
import breakout.sandbox.object.Paddle;
import breakout.sandbox.object.PowerUp;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class MainScene extends GameScene {
  public static final double PADDLE_CONTROL_IMPULSE = 200;
  public static final double PADDLE_MIN_SPEED = 50;
  public static final double PADDLE_FRACTION_RATE = 0.6;
  public static final double PADDLE_RANDOM_ANGLE = 10;
  public static final double PADDLE_MAX_REFLECT_ANGLE = 65;

  public static final int SCORE_DECREASE_PER_SECOND = 2;
  public static final int SCORE_DECREASE_PER_DEATH = 150;
  public static final int SCORE_INCREASE_PER_BRICK = 100;
  public static final int SCORE_INCREASE_PER_HIT = 20;

  public static final double UI_SLIDE_SPEED = 3;

  public static final double BRICKS_GAP = 1.2;

  public static final double POWERUP_EFFECT_DURATION = 10;
  public static final int POWERUP_EXTRALIFE_AMOUNT = 1;
  public static final double POWERUP_PADDLEEXTENSION_RATE = 1.5;

  public static final int LIVES = 5;

  // Accessible Level Data
  public char[][] level;
  public int levelIndex = 0;

  // UI Elements
  private UIWidget darkBackground;
  private UIWidget mainBoard;
  private UIWidget boardText;
  private UIWidget topBar;
  private UIWidget bottomBar;
  private UIWidget scoreBoard;
  private UIWidget livesLevelBoard;
  private UIWidget resetButton;
  private UIWidget homeButton;
  private UIWidget nextButton;
  private UIWidget tutorialText;
  private UIWidget tutorialPaddle;
  private UIWidget tutorialBouncer;

  // pause state
  private boolean isPaused = false;
  private boolean isInputFrozen = false;
  private Point2D pauseBouncerVelocity;
  private Point2D pausePaddleVelocity;
  private Point2D pausePowerUpVelocity;

  // Level entities
  private int highLevelIndex = 0;
  private double powerUpEffectElapsedTime = 0;
  private int powerUpType = -1;
  private Bouncer bouncer;
  private ArrayList<Paddle> paddles;
  private ArrayList<Brick> bricks;
  private PowerUp powerUp;

  // Components
  private PhysicsHandler bouncerPhysics;
  private CircleCollider bouncerCollider;

  // player information
  private int lives = LIVES;
  private int extraLives = 0;
  private double score = 0;
  private Map<Integer, Integer> highScore = new HashMap<>();
  private boolean isGameOver = false;
  private boolean isCheated = false;

  @Override
  public void start() {
    bricks = new ArrayList<>();
    paddles = new ArrayList<>(3);

    // Load high level index
    Object highScoreObj = GameSourceLoader.getSerializableProperty("HIGHSCORE");
    if (highScoreObj != null) {
      highScore = (Map<Integer, Integer>) highScoreObj;
    }

    // Load high level index
    Object highLevelIndexObj = GameSourceLoader.getSerializableProperty("HIGHLEVEL");
    if (highLevelIndexObj != null) {
      highLevelIndex = (int) highLevelIndexObj;
      levelIndex = highLevelIndex;
    }

    System.out.println("High Level Index: " + highLevelIndex);

    setBouncer();
    setPaddles();
    setPowerUp();
    setUI();
  }

  @Override
  public void onActive() {
    reset();
  }

  @Override
  public void lateUpdate() {
    handlePowerUpEffect();
    decreaseScore();
    updateUI();
    handleGameState();
    handleInputs();
  }

  private void updateUI() {
    // Update the score board
    Text scoreText = (Text) scoreBoard.getImage();
    highScore.putIfAbsent(levelIndex, 0);

    if (isCheated) {
      scoreText.setFill(Color.RED);
      scoreText.setText("High Score: " + highScore.get(levelIndex) + "\n\n[CHEATED] Score: " + (int) Math.ceil(score));
    } else {
      scoreText.setFill(Color.DARKGREEN);
      scoreText.setText("High Score: " + highScore.get(levelIndex) + "\n\nScore: " + (int) Math.ceil(score));
    }

    // Update the lives board
    Text livesText = (Text) livesLevelBoard.getImage();
    if (extraLives > 0) {
      livesText.setText("Level: " + levelIndex + "    Lives: " + lives + " + " + extraLives);
    } else {
      livesText.setText("Level: " + levelIndex + "    Lives: " + lives);
    }
  }

  private void setUI() {
    // gameover background
    darkBackground = instantiateObject(UIWidget.class);
    darkBackground.transform.position = new Point2D(50 * uW, 50 * uH);
    darkBackground.setImage(new Rectangle((100 * uW), 100 * uH, Color.TRANSPARENT)); // hide it first
    darkBackground.getComponent(RenderHandler.class).renderOrder = 5;

    // gameover board
    mainBoard = instantiateObject(UIWidget.class);
    mainBoard.setImage(new Rectangle(50 * uW, 70 * uH, Color.LIGHTGREEN));
    mainBoard.transform.position = new Point2D(50 * uW, -100 * uH); // hide it first
    mainBoard.getComponent(RenderHandler.class).renderOrder = 3;

    // board text
    Text finishText = new Text("Game Over");
    finishText.setFont(Font.font("System", FontWeight.BOLD, 8 * uW));
    finishText.setFill(Color.GREEN);
    finishText.setTextAlignment(TextAlignment.CENTER);
    finishText.setTextOrigin(VPos.CENTER);
    boardText = instantiateObject(UIWidget.class);
    boardText.transform.position = new Point2D(50 * uW, -75 * uH); // hide it first
    boardText.setImage(finishText);
    boardText.getComponent(RenderHandler.class).renderOrder = 2;

    // top bar
    topBar = instantiateObject(UIWidget.class);
    topBar.setImage(new Rectangle(100 * uW, 7 * uH, Color.LIGHTYELLOW));
    BoxCollider topBarCollider = topBar.attachComponent(BoxCollider.class);
    topBar.transform.position = new Point2D(50 * uW, 3 * uH);
    topBarCollider.setShape(new Rectangle(100 * uW, 7 * uH));
    topBar.getComponent(RenderHandler.class).renderOrder = 4;

    // bottom bar
    bottomBar = instantiateObject(UIWidget.class);
    bottomBar.setImage(new Rectangle(100 * uW, 7 * uH, Color.LIGHTPINK));
    bottomBar.transform.position = new Point2D(50 * uW, 97 * uH);
    bottomBar.getComponent(RenderHandler.class).renderOrder = 4;

    // score board
    Text scoreText = new Text("High Score:0\n\nScore: 0");
    scoreText.setFont(new Font(4 * uW));
    scoreText.setFill(Color.DARKGREEN);
    scoreText.setTextAlignment(TextAlignment.CENTER);
    scoreText.setTextOrigin(VPos.BASELINE);
    scoreBoard = instantiateObject(UIWidget.class);
    scoreBoard.setImage(scoreText);
    scoreBoard.transform.position = new Point2D(50 * uW, -1 * uH);
    scoreBoard.getComponent(RenderHandler.class).renderOrder = 2;

    // lives board
    Text livesText = new Text("Level: " + levelIndex + "    Lives: " + lives);
    livesText.setFont(new Font(4 * uW));
    livesText.setFill(Color.BLUE);
    livesText.setTextAlignment(TextAlignment.CENTER);
    livesText.setTextOrigin(VPos.BASELINE);
    livesLevelBoard = instantiateObject(UIWidget.class);
    livesLevelBoard.setImage(livesText);
    livesLevelBoard.transform.position = new Point2D(50 * uW, 97 * uH);
    livesLevelBoard.getComponent(RenderHandler.class).renderOrder = 2;

    // reset button
    Text resetText = new Text("↺");
    resetText.setFont(Font.font("System", FontWeight.BOLD, 7 * uW));
    resetText.setFill(Color.DARKBLUE);
    resetText.setTextAlignment(TextAlignment.CENTER);
    resetText.setTextOrigin(VPos.BASELINE);
    Text resetHighlightedText = new Text("↺");
    resetHighlightedText.setFont(Font.font("System", FontWeight.BOLD, 7 * uW));
    resetHighlightedText.setFill(Color.DARKGREEN);
    resetHighlightedText.setTextAlignment(TextAlignment.CENTER);
    resetHighlightedText.setTextOrigin(VPos.BASELINE);
    resetButton = instantiateObject(UIWidget.class);
    resetButton.setImage(resetText);
    resetButton.isHighlightedOnHover = true;
    resetButton.setHighlightedImage(resetHighlightedText);
    resetButton.isClickable = true;
    resetButton.setOnClick(e -> {
      reset();
    });
    resetButton.transform.position = new Point2D(3.5 * uW, 3.5 * uH);
    resetButton.getComponent(RenderHandler.class).renderOrder = 2;

    // home button
    Text homeText = new Text("↩");
    homeText.setFont(Font.font("System", FontWeight.BOLD, 8.5 * uW));
    homeText.setFill(Color.DARKBLUE);
    homeText.setTextAlignment(TextAlignment.CENTER);
    homeText.setTextOrigin(VPos.BASELINE);
    Text optionsHighlightedText = new Text("↩");
    optionsHighlightedText.setFont(Font.font("System", FontWeight.BOLD, 8.5 * uW));
    optionsHighlightedText.setFill(Color.DARKGREEN);
    optionsHighlightedText.setTextAlignment(TextAlignment.CENTER);
    optionsHighlightedText.setTextOrigin(VPos.BASELINE);
    homeButton = instantiateObject(UIWidget.class);
    homeButton.setImage(homeText);
    homeButton.isHighlightedOnHover = true;
    homeButton.setHighlightedImage(optionsHighlightedText);
    homeButton.isClickable = true;
    homeButton.setOnClick(e -> {
      // Win case
      if (isGameOver && lives > 0) {
        levelIndex++;
      }

      // Normal case
      changeScene(MenuScene.class);
    });
    homeButton.transform.position = new Point2D(96.5 * uW, 3 * uH);
    homeButton.getComponent(RenderHandler.class).renderOrder = 2;

    // next button
    Text nextText = new Text("→");
    nextText.setFont(Font.font("System", FontWeight.BOLD, 16 * uW));
    nextText.setFill(Color.DARKBLUE);
    nextText.setTextAlignment(TextAlignment.CENTER);
    nextText.setTextOrigin(VPos.BASELINE);
    Text nextHighlightedText = new Text("→");
    nextHighlightedText.setFont(Font.font("System", FontWeight.BOLD, 16 * uW));
    nextHighlightedText.setFill(Color.YELLOW);
    nextHighlightedText.setTextAlignment(TextAlignment.CENTER);
    nextHighlightedText.setTextOrigin(VPos.BASELINE);
    nextButton = instantiateObject(UIWidget.class);
    nextButton.isClickable = true;
    nextButton.isHighlightedOnHover = true;
    nextButton.setImage(nextText);
    nextButton.setHighlightedImage(nextHighlightedText);
    nextButton.setOnClick(e -> {
      levelIndex++;
      reset();
    });
    nextButton.transform.position = new Point2D(50 * uW, -75 * uH);
    nextButton.getComponent(RenderHandler.class).renderOrder = 2;

    // tutorial text
    Text tutorialText = new Text("Use the ← and → arrow keys to move the paddle to\nbounce the ball! The ball's bounce direction shifts\nwith the paddle's movement direction.");
    tutorialText.setFont(Font.font("System", FontWeight.BOLD, 3.2 * uW));
    tutorialText.setFill(Color.DARKBLUE);
    tutorialText.setTextAlignment(TextAlignment.CENTER);
    tutorialText.setTextOrigin(VPos.BASELINE);
    this.tutorialText = instantiateObject(UIWidget.class);
    this.tutorialText.setImage(tutorialText);
    this.tutorialText.transform.position = new Point2D(50 * uW, -70 * uH);
    this.tutorialText.getComponent(RenderHandler.class).renderOrder = 2;

    // tutorial paddle
    tutorialPaddle = instantiateObject(UIWidget.class);
    tutorialPaddle.setImage(new Rectangle(Paddle.PADDLE_WIDTH * uW, Paddle.PADDLE_HEIGHT * uH, Color.WHEAT));
    tutorialPaddle.transform.position = new Point2D(50 * uW, -50 * uH);
    tutorialPaddle.getComponent(RenderHandler.class).renderOrder = 2;

    // tutorial bouncer
    tutorialBouncer = instantiateObject(UIWidget.class);
    tutorialBouncer.setImage(new Circle(Bouncer.BOUNCER_SIZE * uW, Color.WHITE));
    tutorialBouncer.transform.position = new Point2D(50 * uW, -20 * uH);
    tutorialBouncer.getComponent(RenderHandler.class).renderOrder = 2;

  }

  private void setBouncer() {
    // Setup Bouncer
    bouncer = instantiateObject(Bouncer.class);
    bouncerCollider = bouncer.getComponent(CircleCollider.class);
    bouncerPhysics = bouncer.getComponent(PhysicsHandler.class);
    bouncer.getComponent(RenderHandler.class).renderOrder = 10;
    bouncerCollider.setOnTriggerExit(collider -> {
      // Paddle collision
      if (collider.gameObject.TAG() == 2) {
        // Bounce off with player paddle control
        Point2D newVelocity = bouncerPhysics.velocity.add(paddles.getFirst().getComponent(PhysicsHandler.class).velocity.multiply(PADDLE_FRACTION_RATE)).normalize().multiply(Bouncer.BOUNCER_SPEED * deltaTime);
        double angle = Math.toDegrees(Math.atan2(newVelocity.getY(), newVelocity.getX())) + 90;

        // Bounce off with random angle offset
        double randomAngle = (Math.random() * 2 - 1) * PADDLE_RANDOM_ANGLE;
        newVelocity = new Point2D(
            newVelocity.getX() * Math.cos(Math.toRadians(randomAngle)) - newVelocity.getY() * Math.sin(Math.toRadians(randomAngle)),
            newVelocity.getX() * Math.sin(Math.toRadians(randomAngle)) + newVelocity.getY() * Math.cos(Math.toRadians(randomAngle))
        );

        // Limit the bounce angle
        if (Math.abs(angle + randomAngle) <= PADDLE_MAX_REFLECT_ANGLE || Math.abs(angle + randomAngle) >= 180 - PADDLE_MAX_REFLECT_ANGLE) {
          bouncerPhysics.velocity = newVelocity;
        } else {
          double clampedAngle = Math.signum(angle + randomAngle) * PADDLE_MAX_REFLECT_ANGLE - 90;
          newVelocity = new Point2D(
              Math.cos(Math.toRadians(clampedAngle)),
              Math.sin(Math.toRadians(clampedAngle))
          ).multiply(newVelocity.magnitude());
          bouncerPhysics.velocity = newVelocity;
        }
      }

      // Brick collision
      if (collider.gameObject.TAG() == 3) {
        Brick brick = (Brick) collider.gameObject;
        if (brick.lives > 0) {
          brick.takeDamage();
          if (brick.lives <= 0) {
            score += SCORE_INCREASE_PER_BRICK;
          } else {
            score += SCORE_INCREASE_PER_HIT;
          }
        }
      }
    });
  }

  private void setPaddles() {
    // Setup Paddles
    for (int i = 0; i < 3; i++) {
      Paddle paddle = instantiateObject(Paddle.class);
      paddles.add(paddle);
      paddle.getComponent(RenderHandler.class).renderOrder = 9;
      paddle.transform.position = new Point2D((Paddle.PADDLE_START_POSITION.getX() - 100 + 100 * i) * uW, Paddle.PADDLE_START_POSITION.getY() * uH);
    }
  }

  private void setLevelBricks() {
    if (level == null) {
      throw new RuntimeException("Level data is not set!");
    }

    // Calculate the center of the bricks
    int totalRows = level.length;
    int totalCols = level[0].length;

    double brickCenterX = totalCols % 2 == 1 ? 50 : 50 + (Brick.BRICK_WIDTH + BRICKS_GAP) / 2;
    double brickCenterY = ((int)(totalRows / 2) + 1) * (Brick.BRICK_HEIGHT + BRICKS_GAP) + 10;

    // Calculate the total number of bricks needed based on the level data
    int requiredBricksCount = 0;
    for (int row = 0; row < level.length; row++) {
      for (int col = 0; col < level[row].length; col++) {
        if (level[row][col] != 0) {
          requiredBricksCount++; // Count non-zero brick types
        }
      }
    }

    int currentBricksCount = bricks.size();

    // If there are fewer bricks than required, instantiate more bricks
    if (currentBricksCount < requiredBricksCount) {
      int bricksToAdd = requiredBricksCount - currentBricksCount;
      for (int i = 0; i < bricksToAdd; i++) {
        Brick brick = instantiateObject(Brick.class);
        brick.getComponent(RenderHandler.class).renderOrder = 9;
        bricks.add(brick);
      }
    }

    // If there are more bricks than required, unregister excess bricks
    else if (currentBricksCount > requiredBricksCount) {
      int bricksToRemove = currentBricksCount - requiredBricksCount;
      for (int i = 0; i < bricksToRemove; i++) {
        Brick brickToRemove = bricks.getLast();
        unregisterObject(brickToRemove);
        bricks.removeLast();
      }
    }

    // Iterate through the level data and set positions for the bricks
    int brickIndex = 0;
    for (int row = 0; row < level.length; row++) {
      for (int col = 0; col < level[row].length; col++) {
        char brickType = level[row][col];

        // Only create a brick if the brickType is not 0
        Brick brick = bricks.get(brickIndex);

        // Set the position of the brick, applying the offset to center it
        double brickX = brickCenterX + (col - ((int)(totalCols / 2))) * (Brick.BRICK_WIDTH + BRICKS_GAP);
        double brickY = brickCenterY + (row - ((int)(totalRows / 2))) * (Brick.BRICK_HEIGHT + BRICKS_GAP);

        brick.transform.position = new Point2D(brickX * uW, brickY * uH);
        brick.setBlockType(brickType);

        brickIndex++; // Increment the brickIndex to assign to the next brick

      }
    }
  }

  private void setPowerUp() {
    powerUp = instantiateObject(PowerUp.class);
    powerUp.transform.position = new Point2D(PowerUp.POWERUP_START_POSITION.getX() * uW, PowerUp.POWERUP_START_POSITION.getY() * uH);
    powerUp.getComponent(RenderHandler.class).renderOrder = 9;
    powerUp.getComponent(CircleCollider.class).setOnTriggerStay(collider -> {
      if (collider.gameObject.TAG() == 1) {
        // Activate the power up
        if (powerUp.isActive) {
          powerUp.isActive = false;
          powerUpType = (int) (Math.random() * PowerUp.POWERUP_AMOUNT);
        }
      }
    });
  }

  private void reset() {
    isGameOver = false;
    isCheated = false;

    // Reset player stats
    lives = LIVES;
    score = 0;

    level = GameSourceLoader.getLevel("LEVEL_" + levelIndex);
    while (level == null && levelIndex >= 0) {
      levelIndex--;
      level = GameSourceLoader.getLevel("LEVEL_" + levelIndex);
    }
    if (level == null) {
      throw new RuntimeException("Failed to load level data for level: " + levelIndex);
    }

    // Reset the bouncer
    bouncer.transform.position = new Point2D(Bouncer.BOUNCER_START_POSITION.getX() * uW, Bouncer.BOUNCER_START_POSITION.getY() * uH);
    bouncerPhysics.velocity = new Point2D(0, -Bouncer.BOUNCER_SPEED * deltaTime);

    // Reset the paddle
    for (int i = 0; i < 3; i++) {
      paddles.get(i).transform.position = new Point2D((Paddle.PADDLE_START_POSITION.getX() - 100 + 100 * i) * uW, Paddle.PADDLE_START_POSITION.getY() * uH);
      paddles.get(i).getComponent(PhysicsHandler.class).velocity = Point2D.ZERO;
    }

    // Reset the bricks and Powerups in the next frame (to avoid collision bugs)
    subscribeAction(e -> {
      setLevelBricks();
      powerUp.isActive = true;
      powerUp.transform.position = new Point2D(PowerUp.POWERUP_START_POSITION.getX() * uW, PowerUp.POWERUP_START_POSITION.getY() * uH);
    });

    // Reset Powerup Effects
    powerUpEffectElapsedTime = POWERUP_EFFECT_DURATION;
    handlePowerUpEffect();

    // Reset the UI
    ((Rectangle) darkBackground.getImage()).setFill(Color.TRANSPARENT);
    mainBoard.stopSliding();
    boardText.stopSliding();
    scoreBoard.stopSliding();
    nextButton.stopSliding();
    scoreBoard.transform.position = new Point2D(50 * uW, -1 * uH);
    nextButton.transform.position = new Point2D(50 * uW, -75 * uH);

    // Pause the game
    isInputFrozen = true;
    pause(true);

    // show level information
    darkBackground.setImage(new Rectangle(100 * uW, 100 * uH, Color.rgb(0, 0, 0, 0.3)));
    ((Text) homeButton.getImage()).setFill(Color.GRAY);
    ((Text) resetButton.getImage()).setFill(Color.GRAY);
    homeButton.isClickable = false;
    resetButton.isClickable = false;
    homeButton.isHighlightedOnHover = false;
    resetButton.isHighlightedOnHover = false;
    mainBoard.transform.position = new Point2D(-100 * uW, 50 * uH);
    mainBoard.setImage(new Rectangle(50 * uW, 25 * uH, Color.LIGHTSTEELBLUE));
    mainBoard.slideTowards(new Point2D(50 * uW, 50 * uH), 3 * UI_SLIDE_SPEED * deltaTime, e1 -> {
      mainBoard.slideTowards(new Point2D(150 * uW, 50 * uH), 4 * UI_SLIDE_SPEED * deltaTime, e2 -> {
        mainBoard.setImage(new Rectangle(50 * uW, 70 * uH, Color.LIGHTGREEN));
        mainBoard.transform.position = new Point2D(50 * uW, -100 * uH);

        // Check tutorial
        if (levelIndex == 0) {
          tutorial();
        } else {
          darkBackground.setImage(new Rectangle(100 * uW, 100 * uH, Color.TRANSPARENT));
          ((Text) homeButton.getImage()).setFill(Color.DARKBLUE);
          ((Text) resetButton.getImage()).setFill(Color.DARKBLUE);
          homeButton.isClickable = true;
          resetButton.isClickable = true;
          homeButton.isHighlightedOnHover = true;
          resetButton.isHighlightedOnHover = true;
          pause(false);
          isInputFrozen = false;
        }
      });
    });

    boardText.transform.position = new Point2D(-50 * uW, 50 * uH);
    ((Text) boardText.getImage()).setText("Level " + levelIndex);
    ((Text) boardText.getImage()).setFill(Color.DARKBLUE);
    boardText.slideTowards(new Point2D(50 * uW, 50 * uH), 3 * UI_SLIDE_SPEED * deltaTime, e1 -> {
      boardText.slideTowards(new Point2D(150 * uW, 50 * uH), 4 * UI_SLIDE_SPEED * deltaTime, e2 -> {
        boardText.transform.position = new Point2D(50 * uW, -75 * uH);
        ((Text) boardText.getImage()).setFill(Color.DARKGREEN);
      });
    });

    updateUI();
  }

  private void handleInputs() {
    KeyCode key = getKeyInput();

    if (isGameOver) {
      return;
    }

    /////// Move the paddle ///////
    if (!isInputFrozen) {
      if (key == KeyCode.LEFT) {
        for (Paddle paddle : paddles) {
          paddle.getComponent(PhysicsHandler.class).applyImpulse(new Point2D(-PADDLE_CONTROL_IMPULSE * deltaTime, 0));
        }
      }
      if (key == KeyCode.RIGHT) {
        for (Paddle paddle : paddles) {
          paddle.getComponent(PhysicsHandler.class).applyImpulse(new Point2D(PADDLE_CONTROL_IMPULSE * deltaTime, 0));
        }
      }
    }

    // Limit the paddle speed
    for (Paddle paddle : paddles) {
      PhysicsHandler paddlePhysics = paddle.getComponent(PhysicsHandler.class);
      if (Math.abs(paddlePhysics.velocity.getX()) < PADDLE_MIN_SPEED * deltaTime) {
        paddlePhysics.velocity = Point2D.ZERO;
      }
    }

    // Rotate the paddle
    Paddle firstPaddle = paddles.getFirst();
    Paddle lastPaddle = paddles.getLast();
    if (firstPaddle.transform.position.getX() < -(100 + Paddle.PADDLE_WIDTH) * uW) {
      firstPaddle.transform.position = lastPaddle.transform.position.add(new Point2D(100 * uW, 0));
      paddles.removeFirst();
      paddles.add(firstPaddle);
    } else if (lastPaddle.transform.position.getX() > (100 + Paddle.PADDLE_WIDTH) * uW) {
      lastPaddle.transform.position = firstPaddle.transform.position.add(new Point2D(-100 * uW, 0));
      paddles.removeLast();
      paddles.addFirst(lastPaddle);
    }

    /////// Hot Keys ///////
    if (!isInputFrozen && !isGameOver && key != null) {
      switch (key) {
        case L:
          isCheated = true;
          lives++;
          break;
        case R:
          isCheated = true;
          // Reset the bouncer
          bouncer.transform.position = new Point2D(Bouncer.BOUNCER_START_POSITION.getX() * uW, Bouncer.BOUNCER_START_POSITION.getY() * uH);
          bouncerPhysics.velocity = new Point2D(0, -Bouncer.BOUNCER_SPEED * deltaTime);

          // Reset the paddle
          for (int i = 0; i < 3; i++) {
            paddles.get(i).transform.position = new Point2D((Paddle.PADDLE_START_POSITION.getX() - 100 + 100 * i) * uW, Paddle.PADDLE_START_POSITION.getY() * uH);
            paddles.get(i).getComponent(PhysicsHandler.class).velocity = Point2D.ZERO;
          }
          break;
        case S:
          reset();
          break;
        case P:
          pause(!isPaused);
          break;
      }
      if (key.isDigitKey()) {
        levelIndex = key.ordinal() - 24;
        reset();
      }
    }


  }

  private void handleGameState() {
    if (isPaused) {
      return;
    }

    // Check if the player has lost
    if (bouncer.transform.position.getY() > height - Bouncer.BOUNCER_SIZE * uW) {
      decreaseLives();
      score -= SCORE_DECREASE_PER_DEATH;
      if (!isGameOver) {
        if (lives <= 0) {
          gameOver(false);
          bouncer.transform.position = new Point2D(50 * uW, -Bouncer.BOUNCER_SIZE * uH);
          return;
        } else {
          // Reset the bouncer
          bouncer.transform.position = new Point2D(Bouncer.BOUNCER_START_POSITION.getX() * uW, Bouncer.BOUNCER_START_POSITION.getY() * uH);
          bouncerPhysics.velocity = new Point2D(0, -Bouncer.BOUNCER_SPEED * deltaTime);

          // Reset the paddle
          for (int i = 0; i < 3; i++) {
            paddles.get(i).transform.position = new Point2D((Paddle.PADDLE_START_POSITION.getX() - 100 + 100 * i) * uW, Paddle.PADDLE_START_POSITION.getY() * uH);
            paddles.get(i).getComponent(PhysicsHandler.class).velocity = Point2D.ZERO;
          }
        }
      }
    }

    // Check if the player has won
    if (!isGameOver) {
      for (Brick brick : bricks) {
        if (brick.lives > 0) {
          return;
        }
      }
      gameOver(true);
    }
  }

  private void handlePowerUpEffect() {
    //////////////////////////////////////////////////////////////////////////////////////////////
    ///////// THIS CAN POSSIBLY BE CHANGE INTO DIFFERENT CLASSES INHERITING FROM POWERUP /////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    // Double Speed
    if (powerUpType == 0) {
      if (powerUpEffectElapsedTime < POWERUP_EFFECT_DURATION) {
        bouncerPhysics.velocity = bouncerPhysics.velocity.normalize().multiply(Bouncer.BOUNCER_SPEED * 2 * deltaTime);
        powerUpEffectElapsedTime += deltaTime;
      } else {
        bouncerPhysics.velocity = bouncerPhysics.velocity.normalize().multiply(Bouncer.BOUNCER_SPEED * deltaTime);
        powerUpType = -1;
        powerUpEffectElapsedTime = 0;
        powerUp.isActive = true;
      }
    }

    // Extra life
    else if (powerUpType == 1) {
      if (powerUpEffectElapsedTime < POWERUP_EFFECT_DURATION) {
        if (powerUpEffectElapsedTime == 0) {
          extraLives += POWERUP_EXTRALIFE_AMOUNT;
        }
        powerUpEffectElapsedTime += deltaTime;
      } else {
        extraLives = 0;
        powerUpType = -1;
        powerUpEffectElapsedTime = 0;
        powerUp.isActive = true;
      }
    }

    // Paddle Extention
    else if (powerUpType == 2) {
      if (powerUpEffectElapsedTime < POWERUP_EFFECT_DURATION) {
        if (powerUpEffectElapsedTime == 0) {
          for (Paddle paddle : paddles) {
            paddle.transform.scale = new Point2D(POWERUP_PADDLEEXTENSION_RATE, 1);
          }
        }
        powerUpEffectElapsedTime += deltaTime;
      } else {
        for (Paddle paddle : paddles) {
          paddle.transform.scale = new Point2D(1, 1);
        }
        powerUpType = -1;
        powerUpEffectElapsedTime = 0;
        powerUp.isActive = true;
      }
    }

    // Slippery Paddle
    else if (powerUpType == 3) {
      if (powerUpEffectElapsedTime < POWERUP_EFFECT_DURATION) {
        if (powerUpEffectElapsedTime == 0) {
          for (Paddle paddle : paddles) {
            ((Rectangle) paddle.getComponent(RenderHandler.class).getImage()).setFill(Color.LIGHTBLUE);
            paddle.getComponent(PhysicsHandler.class).applyAirResistance = false;
          }
        }
        powerUpEffectElapsedTime += deltaTime;
      } else {
        for (Paddle paddle : paddles) {
          ((Rectangle) paddle.getComponent(RenderHandler.class).getImage()).setFill(Color.WHEAT);
          paddle.getComponent(PhysicsHandler.class).applyAirResistance = true;
        }
        powerUpType = -1;
        powerUpEffectElapsedTime = 0;
        powerUp.isActive = true;
      }
    }
  }

  private void decreaseLives() {
    if (!isGameOver && !isPaused) {
      if (extraLives > 0) {
        extraLives--;
      } else {
        lives--;
      }
    }

    if (lives < 0) {
      lives = 0;
    }
  }

  private void decreaseScore() {
    if (!isGameOver && !isPaused && score > 0) {
      score -= SCORE_DECREASE_PER_SECOND * deltaTime;
    }

    if (score < 0) {
      score = 0;
    }
  }

  private void gameOver(boolean isWin) {
    isGameOver = true;
    bouncerPhysics.velocity = Point2D.ZERO;
    powerUp.getComponent(PhysicsHandler.class).velocity = Point2D.ZERO;

    // Update high score
    if (highScore.get(levelIndex) == null || highScore.get(levelIndex) < score) {
      highScore.put(levelIndex, (int) Math.ceil(score));
      GameSourceLoader.saveSerializableProperty("HIGHSCORE", (Serializable) highScore);
    }

    // Game Over UI
    ((Rectangle) darkBackground.getImage()).setFill(Color.rgb(0, 0, 0, 0.3));
    if (GameSourceLoader.getLevel("LEVEL_" + (levelIndex + 1)) == null) {
      ((Text) boardText.getImage()).setText("Game\nCompleted");
      ((Text) nextButton.getImage()).setText("↩");
      ((Text) nextButton.getHighlightedImage()).setText("↩");
      nextButton.setOnClick(e -> {
        changeScene(MenuScene.class);
      });
    } else if (isWin) {
      // Update High Level Index
      if (levelIndex + 1 > highLevelIndex) {
        highLevelIndex = levelIndex + 1;
        GameSourceLoader.saveSerializableProperty("HIGHLEVEL", highLevelIndex);
      }

      ((Text) boardText.getImage()).setText("You Win");
      ((Text) nextButton.getImage()).setText("→");
      ((Text) nextButton.getHighlightedImage()).setText("→");
      nextButton.setOnClick(e -> {
        levelIndex++;
        reset();
      });
    } else {
      ((Text) boardText.getImage()).setText("Game Over");
      ((Text) nextButton.getImage()).setText("↺");
      ((Text) nextButton.getHighlightedImage()).setText("↺");
      nextButton.setOnClick(e -> {
        reset();
      });
    }
    mainBoard.slideTowards(new Point2D(50 * uW, 50 * uH), UI_SLIDE_SPEED * deltaTime, null);
    boardText.slideTowards(new Point2D(50 * uW, 25 * uH), UI_SLIDE_SPEED * deltaTime, null);
    scoreBoard.slideTowards(new Point2D(50 * uW, 50 * uH), UI_SLIDE_SPEED * deltaTime, null);
    nextButton.slideTowards(new Point2D(50 * uW, 70 * uH), UI_SLIDE_SPEED * deltaTime, null);
  }

  private void pause(boolean isPaused) {
    if (isPaused) {
      pauseBouncerVelocity = bouncerPhysics.velocity;
      bouncerPhysics.velocity = Point2D.ZERO;

      pausePaddleVelocity = paddles.getFirst().getComponent(PhysicsHandler.class).velocity;
      paddles.getFirst().getComponent(PhysicsHandler.class).velocity = Point2D.ZERO;

      pausePowerUpVelocity = powerUp.getComponent(PhysicsHandler.class).velocity;
      powerUp.getComponent(PhysicsHandler.class).velocity = Point2D.ZERO;
    } else {
      bouncerPhysics.velocity = pauseBouncerVelocity;

      for (Paddle paddle : paddles) {
        paddle.getComponent(PhysicsHandler.class).velocity = pausePaddleVelocity;
      }

      powerUp.getComponent(PhysicsHandler.class).velocity = pausePowerUpVelocity;
    }

    this.isPaused = isPaused;
  }

  private void tutorial() {
    mainBoard.setImage(new Rectangle(70 * uW, 30 * uH, Color.LIGHTSTEELBLUE));
    ((Text) boardText.getImage()).setText("Tutorial");
    ((Text) boardText.getImage()).setFill(Color.LIGHTSKYBLUE);
    mainBoard.slideTowards(new Point2D(50 * uW, 50 * uH), 2 * UI_SLIDE_SPEED * deltaTime, null);
    boardText.slideTowards(new Point2D(50 * uW, 25 * uH), 2 * UI_SLIDE_SPEED * deltaTime, null);
    tutorialText.slideTowards(new Point2D(50 * uW, 42 * uH), 2 * UI_SLIDE_SPEED * deltaTime, null);
    tutorialBouncer.slideTowards(new Point2D(50 * uW, 55 * uH), 2 * UI_SLIDE_SPEED * deltaTime, null);

    tutorialPaddle.slideTowards(new Point2D(50 * uW, 60 * uH), 2 * UI_SLIDE_SPEED * deltaTime, e1 ->
        // Animation Here
        tutorialPaddle.slideTowards(new Point2D(30 * uW, 60 * uH), 3 * UI_SLIDE_SPEED * deltaTime, e2 ->
            tutorialPaddle.slideTowards(new Point2D(70 * uW, 60 * uH), 3 * UI_SLIDE_SPEED * deltaTime, e3 ->
                tutorialPaddle.slideTowards(new Point2D(50 * uW, 60 * uH), 3 * UI_SLIDE_SPEED * deltaTime, e4 -> {
                  ((Text) nextButton.getImage()).setFill(Color.LIGHTGREEN);
                  nextButton.transform.position = new Point2D(78 * uW, 58 * uH);
                  nextButton.setOnClick(e5 -> {
                    // reset UI
                    ((Text) homeButton.getImage()).setFill(Color.DARKBLUE);
                    homeButton.isClickable = true;
                    homeButton.isHighlightedOnHover = true;

                    ((Text) resetButton.getImage()).setFill(Color.DARKBLUE);
                    resetButton.isClickable = true;
                    resetButton.isHighlightedOnHover = true;

                    darkBackground.setImage(new Rectangle(100 * uW, 100 * uH, Color.TRANSPARENT));

                    mainBoard.setImage(new Rectangle(50 * uW, 70 * uH, Color.LIGHTGREEN));
                    mainBoard.transform.position = new Point2D(50 * uW, -100 * uH);

                    ((Text) boardText.getImage()).setFill(Color.DARKGREEN);
                    boardText.transform.position = new Point2D(50 * uW, -75 * uH);

                    tutorialText.transform.position = new Point2D(50 * uW, -70 * uH);
                    tutorialBouncer.transform.position = new Point2D(50 * uW, -20 * uH);
                    tutorialPaddle.transform.position = new Point2D(50 * uW, -50 * uH);

                    nextButton.transform.position = new Point2D(50 * uW, -75 * uH);
                    ((Text) nextButton.getImage()).setFill(Color.DARKBLUE);
                    nextButton.setOnClick(e6 -> {
                      levelIndex++;
                      reset();
                    });

                    pause(false);
                    isInputFrozen = false;
                  });
                })
            )
        )
    );
  }
}
