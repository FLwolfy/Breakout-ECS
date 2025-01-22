package breakout.sandbox.scene;

import breakout.engine.UI.UIWidget;
import breakout.engine.component.CircleCollider;
import breakout.engine.component.PhysicsHandler;
import breakout.engine.component.RenderHandler;
import breakout.engine.base.GameScene;
import breakout.engine.base.GameSourceLoader;
import breakout.sandbox.object.Bouncer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class MenuScene extends GameScene {
  private static final double BOUNCER_AMOUNT = 15;
  private static final double BOARD_SPEED = 5;

  private int highestLevel = 0;

  private UIWidget dukeLogo;
  private UIWidget title;
  private UIWidget startButton;
  private UIWidget text;
  private UIWidget optionButton;
  private UIWidget optionBoard;
  private UIWidget optionBoardText;
  private ArrayList<UIWidget> levels = new ArrayList<>();

  private Set<Bouncer> bouncers = new HashSet<>();

  @Override
  public void onActive() {
    reset();
    subscribeAction(e -> reset());
  }

  @Override
  public void start() {
    // load highest level
    highestLevel = GameSourceLoader.getSerializableProperty("HIGHLEVEL") == null ? 0 : (int) GameSourceLoader.getSerializableProperty("HIGHLEVEL");

    // Setup UI widgets
    setDukeLogo();
    setTitle();
    setStartButton();
    setStartText();
    setOptionButton();
    setOptionBoard();
    setBouncers();
    setLevels();
  }

  @Override
  public void lateUpdate() {
    // Start Button Color Change
    double time = System.currentTimeMillis() % 5000 / 5000.0;
    Color color = Color.hsb(time * 360, 0.5, 1.0, 0.5);
    ((Rectangle) startButton.getImage()).setFill(color);

    // Menu Bouncer Reset
    for (Bouncer b : bouncers) {
      CircleCollider c = b.getComponent(CircleCollider.class);
      PhysicsHandler p = b.getComponent(PhysicsHandler.class);
      if (p.applyGravity && b.transform.position.getY() > height + c.radiusY) {
        b.transform.position = new Point2D(50 * uW, 30 * uH);
        p.velocity = new Point2D((Math.random() - 0.5) * 2 * Bouncer.BOUNCER_SPEED * GameScene.deltaTime, (Math.random() - 0.8) * 2 * Bouncer.BOUNCER_SPEED * GameScene.deltaTime);
      }
    }
  }

  private void setDukeLogo() {
    dukeLogo = instantiateObject(UIWidget.class);
    dukeLogo.isClickable = true;
    dukeLogo.transform.position = new Point2D(50 * uW, 30 * uH);
    ImageView dukeImg = GameSourceLoader.getImage("DUKE_LOGO");
    dukeLogo.setImage(dukeImg);
    dukeLogo.transform.scale = new Point2D(50 * uW / dukeImg.getImage().getWidth() , 50 * uW / dukeImg.getImage().getHeight());
    PhysicsHandler dukeLogoPhysics = dukeLogo.attachComponent(PhysicsHandler.class);
    CircleCollider dukeLogoTrigger = dukeLogo.attachComponent(CircleCollider.class);
    RenderHandler dukeLogoRender = dukeLogo.getComponent(RenderHandler.class);
    dukeLogoRender.renderOrder = 8;
    dukeLogoTrigger.setShape(new Circle(dukeImg.getImage().getWidth() / 2));
    dukeLogoTrigger.isTrigger = true;
    dukeLogoPhysics.isStatic = true;
    dukeLogo.setOnClick(e -> {
      double angularVelocity = dukeLogo.getComponent(PhysicsHandler.class).angularVelocity;
      if (angularVelocity == 0 && dukeLogoTrigger.isWithin(getMouseCursor())) {
        dukeLogo.getComponent(PhysicsHandler.class).angularVelocity = 20 * deltaTime;
        for (Bouncer b : bouncers) {
          PhysicsHandler p = b.getComponent(PhysicsHandler.class);

          b.transform.position = new Point2D(50 * uW, 30 * uH);
          p.velocity = new Point2D((Math.random() - 0.5) * 2 * Bouncer.BOUNCER_SPEED * deltaTime, (Math.random() - 0.8) * 2 * Bouncer.BOUNCER_SPEED * deltaTime);
          p.applyGravity = true;
        }
      }
    });
  }

  private void setTitle() {
    Text titleText = new Text("Break⚙ut");
    titleText.setFont(new Font(12 * uW));
    titleText.setFill(Color.YELLOWGREEN);
    titleText.setTextAlignment(TextAlignment.CENTER);
    titleText.setTextOrigin(VPos.CENTER);
    title = instantiateObject(UIWidget.class);
    title.transform.position = new Point2D(50 * uW, 70 * uH);
    title.setImage(titleText);
    title.getComponent(RenderHandler.class).renderOrder = 8;
  }

  private void setStartButton() {
    startButton = instantiateObject(UIWidget.class);
    startButton.isHighlightedOnHover = true;
    startButton.isClickable = true;
    startButton.setOnClick(e -> {
      GameScene.getScene(MainScene.class).levelIndex = highestLevel;
      changeScene(MainScene.class);
    });
    startButton.transform.scale = new Point2D(1, 0.5);
    startButton.transform.position = new Point2D(50 * uW, 90 * uH);
    startButton.getComponent(RenderHandler.class).renderOrder = 8;
  }

  private void setStartText() {
    Text startText = new Text("Start");
    startText.setFont(new Font(5 * uW));
    startText.setTextAlignment(TextAlignment.CENTER);
    startText.setTextOrigin(VPos.CENTER);
    text = instantiateObject(UIWidget.class);
    text.transform.position = new Point2D(50 * uW, 90 * uH);
    text.setImage(startText);
    text.getComponent(RenderHandler.class).renderOrder = 8;
  }

  private void setBouncers() {
    for (int i = 0; i < BOUNCER_AMOUNT; i++) {
      Bouncer b = instantiateObject(Bouncer.class);
      RenderHandler r = b.getComponent(RenderHandler.class);
      PhysicsHandler p = b.getComponent(PhysicsHandler.class);
      CircleCollider c = b.getComponent(CircleCollider.class);

      b.transform.position = new Point2D(50 * uW, -2 * Bouncer.BOUNCER_SIZE * uH);
      p.velocity = Point2D.ZERO;
      b.canBounceGround = true;
      c.canSameTagCollide = true;
      p.applyAirResistance = true;
      p.airResistancePercentage = 0.12;
      p.gravitation = 15;
      r.renderOrder = 9;
      r.setImage(new Circle(Bouncer.BOUNCER_SIZE * uW, Color.color(Math.random(), Math.random(), Math.random())));

      bouncers.add(b);
    }
  }

  private void setOptionButton() {
    Text optionText = new Text("⚙︎");
    optionText.setFont(Font.font("System", FontWeight.BOLD, 15 * uW));
    optionText.setFill(Color.DARKBLUE);
    optionText.setTextAlignment(TextAlignment.CENTER);
    optionText.setTextOrigin(VPos.BASELINE);
    Text resetHighlightedText = new Text("⚙︎");
    resetHighlightedText.setFont(Font.font("System", FontWeight.BOLD, 15 * uW));
    resetHighlightedText.setFill(Color.DARKGREEN);
    resetHighlightedText.setTextAlignment(TextAlignment.CENTER);
    resetHighlightedText.setTextOrigin(VPos.BASELINE);
    optionButton = instantiateObject(UIWidget.class);
    optionButton.setImage(optionText);
    optionButton.setHighlightedImage(resetHighlightedText);
    optionButton.isHighlightedOnHover = true;
    optionButton.isClickable = true;
    optionButton.setOnClick(e -> toggleOptionBoard());
    optionButton.transform.position = new Point2D(95 * uW, 5 * uH);
    optionButton.getComponent(RenderHandler.class).renderOrder = 8;
  }

  private void setOptionBoard() {
    // Option Board
    optionBoard = instantiateObject(UIWidget.class);
    optionBoard.transform.position = new Point2D(-35 * uW, 50 * uH); // Hide the option board first
    optionBoard.setImage(new Rectangle(70 * uW, 80 * uH, Color.color(0.5, 1.0, 0.7, 0.9)));
    optionBoard.getComponent(RenderHandler.class).renderOrder = 8;

    // Option Board Text
    optionBoardText = instantiateObject(UIWidget.class);
    Text optionBoardTextContent = new Text("Why not try to click on the Duke logo?");
    optionBoardTextContent.setFont(new Font(4 * uW));
    optionBoardTextContent.setFill(Color.DEEPSKYBLUE);
    optionBoardTextContent.setTextAlignment(TextAlignment.CENTER);
    optionBoardTextContent.setTextOrigin(VPos.CENTER);
    optionBoardText.setImage(optionBoardTextContent);
    optionBoardText.transform.position = new Point2D(-35 * uW, 85 * uH);
    optionBoardText.getComponent(RenderHandler.class).renderOrder = 7;
  }

  private void setLevels() {
    for (int i = 0; i < 2 * (highestLevel + 1); i++) {
      Point2D position = new Point2D(-35 + 2 * (i / 2 % 2 - 0.5) * 16, 18 + (i / 4) * 12);

      if (i < levels.size()) {
        levels.get(i).transform.position = new Point2D(position.getX() * uW, position.getY() * uH);
        continue;
      }

      // Button
      if (i % 2 == 0) {
        UIWidget level = instantiateObject(UIWidget.class);
        level.isClickable = true;
        level.isHighlightedOnHover = true;
        level.transform.position = new Point2D(position.getX() * uW, position.getY() * uH);
        level.setImage(new Rectangle(20 * uW, 10 * uH, Color.color(0.0, 0.5, 1, 0.9)));
        level.setHighlightedImage(new Rectangle(20 * uW, 10 * uH, Color.color(1, 1, 0.5, 0.9)));
        int finalI = i;
        level.setOnClick(e -> {
          GameScene.getScene(MainScene.class).levelIndex = finalI / 2;
          changeScene(MainScene.class);
        });
        level.getComponent(RenderHandler.class).renderOrder = 7;
        levels.add(level);
      }

      // Text
      else {
        Text levelText = new Text("Level " + (i / 2));
        levelText.setFont(new Font(4 * uW));
        levelText.setFill(Color.BLACK);
        levelText.setTextAlignment(TextAlignment.CENTER);
        levelText.setTextOrigin(VPos.CENTER);
        UIWidget level = instantiateObject(UIWidget.class);
        level.transform.position = new Point2D(position.getX() * uW, position.getY() * uH);
        level.setImage(levelText);
        level.getComponent(RenderHandler.class).renderOrder = 7;
        levels.add(level);
      }

    }
  }

  private void toggleOptionBoard() {
    optionButton.isClickable = false;
    optionButton.isHighlightedOnHover = false;

    if (startButton.isClickable) {
      dukeLogo.isClickable = false;
      startButton.isClickable = false;
      startButton.isHighlightedOnHover = false;
      optionBoard.slideTowards(new Point2D(50 * uW, 50 * uH), BOARD_SPEED * deltaTime, e -> {
        optionButton.isClickable = true;
        optionButton.isHighlightedOnHover = true;
      });
      optionBoardText.slideTowards(new Point2D(50 * uW, 85 * uH), BOARD_SPEED * deltaTime, null);
      for (int i = 0; i < levels.size(); i++) {
        double positionX = 50 + 2 * (i / 2 % 2 - 0.5) * 16;
        levels.get(i).slideTowards(new Point2D(positionX * uW, levels.get(i).transform.position.getY()), BOARD_SPEED * deltaTime, null);
      }
    } else {
      dukeLogo.isClickable = true;
      startButton.isClickable = true;
      startButton.isHighlightedOnHover = true;
      optionBoard.slideTowards(new Point2D(-35 * uW, 50 * uH), BOARD_SPEED * deltaTime, e -> {
        optionButton.isClickable = true;
        optionButton.isHighlightedOnHover = true;
      });
      optionBoardText.slideTowards(new Point2D(-35 * uW, 85 * uH), BOARD_SPEED * deltaTime, null);
      for (int i = 0; i < levels.size(); i++) {
        double positionX = -35 + 2 * (i / 2 % 2 - 0.5) * 16;
        levels.get(i).slideTowards(new Point2D(positionX * uW, levels.get(i).transform.position.getY()), BOARD_SPEED * deltaTime, null);
      }
    }


  }

  private void reset() {
    highestLevel = GameSourceLoader.getSerializableProperty("HIGHLEVEL") == null ? 0 : (int) GameSourceLoader.getSerializableProperty("HIGHLEVEL");

    // dukeLogo
    dukeLogo.getComponent(PhysicsHandler.class).angularVelocity = 0;
    dukeLogo.transform.rotation = 0;
    dukeLogo.isClickable = true;

    // startButton
    startButton.isClickable = true;
    startButton.isHighlightedOnHover = true;

    // optionButton
    optionButton.isClickable = true;
    optionButton.isHighlightedOnHover = true;

    // bouncers
    for (Bouncer b : bouncers) {
      b.transform.position = new Point2D(50 * uW, -2 * Bouncer.BOUNCER_SIZE * uH);
      b.getComponent(PhysicsHandler.class).velocity = Point2D.ZERO;
      b.getComponent(PhysicsHandler.class).applyGravity = false;
    }

    // optionBoard
    if (optionBoard.transform.position.getX() != -35 * uW) {
      optionBoard.stopSliding();
      optionBoardText.stopSliding();
      for (UIWidget level : levels) {
        level.stopSliding();
      }
    }
    optionBoard.transform.position = new Point2D(-35 * uW, 50 * uH);
    optionBoardText.transform.position = new Point2D(-35 * uW, 85 * uH);

    // levels
    setLevels();
  }

}
