package breakout.engine.base;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javafx.scene.image.ImageView;

public class GameSourceLoader {
  // singleton method
  private static final GameSourceLoader instance = new GameSourceLoader();

  // singleton instance variables
  private final Map<String, ImageView> IMAGES = new HashMap<>();
  private final Map<String, char[][]> LEVELS = new HashMap<>();
  private final Map<String, Object> PROPERTIES = new HashMap<>();

  /**
   * Load all images and levels from the given directories
   */
  public static void loadAll(String imageDirectory, String levelDirectory) {
    loadAllImages(imageDirectory);
    loadAllLevels(levelDirectory);
    loadAllSerializableProperties();
  }

  /**
   * Load image from the given path
   */
  public static void loadImage(String imagePath) {
    try {
      // Load image
      ImageView image = new ImageView(imagePath);

      // Extract image name from the path
      String imageName = Paths.get(imagePath).getFileName().toString().split("\\.")[0];

      // Add image into image pool
      instance.IMAGES.put(imageName, image);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Load all images from the given directory
   */
  public static void loadAllImages(String imageDirectory) {
    try {
      // Get resource URL
      URL resourceUrl = getResourceUrl(imageDirectory);

      // Get directory path
      var directoryPath = Paths.get(resourceUrl.toURI());
      Files.list(directoryPath)
          .filter(Files::isRegularFile)
          .filter(file -> {
            String fileName = file.getFileName().toString().toLowerCase();
            return fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg");
          })
          .forEach(file -> loadImage(imageDirectory + "/" + file.getFileName()));
    } catch (Exception e) {
      throw new RuntimeException("Failed to load images from directory: " + imageDirectory, e);
    }
  }

  /**
   * Get image from the image pool by name
   */
  public static ImageView getImage(String imageName) {
    return new ImageView(instance.IMAGES.get(imageName).getImage());
  }

  /**
   * Load level from the given path
   */
  public static void loadLevel(String levelPath) {
    try {
      // Read all lines from the level file
      Path path = Paths.get(levelPath);
      var lines = Files.readAllLines(path);

      // Find the lines between @BEGIN and @END markers
      boolean insideLevel = false;

      // Temporary list to store level data
      ArrayList<ArrayList<Character>> levelDataList = new ArrayList<>();
      int maxCols = 0; // To track the maximum number of columns

      // Process each line
      for (String line : lines) {
        // Trim the line and ignore if it's empty or starts with a comment (# or //)
        String trimmedLine = line.trim();
        if (trimmedLine.isEmpty() || trimmedLine.startsWith("#") || trimmedLine.startsWith("//")) {
          // Skip comment or empty lines
          continue;
        }

        if (trimmedLine.startsWith("@BEGIN")) {
          insideLevel = true;
          continue; // Skip the @BEGIN line
        }
        if (trimmedLine.startsWith("@END")) {
          break; // Stop reading lines after @END
        }

        if (insideLevel) {
          // Split the line by commas and parse the values
          String[] values = trimmedLine.split(",");
          ArrayList<Character> rowData = new ArrayList<>();

          for (String value : values) {
            value = value.trim();
            if (value.length() == 1) {
              // Single character value
              rowData.add(value.charAt(0));
            } else {
              throw new IllegalArgumentException(
                  "Invalid value found (not a single character or number): " + value
              );
            }
          }

          // Update the maximum column count
          maxCols = Math.max(maxCols, rowData.size());
          levelDataList.add(rowData);
        }
      }

      // Convert the ArrayList to a 2D array (Byte[][])
      char[][] levelData = new char[levelDataList.size()][maxCols];
      for (int row = 0; row < levelDataList.size(); row++) {
        ArrayList<Character> rowData = levelDataList.get(row);
        for (int col = 0; col < rowData.size(); col++) {
          levelData[row][col] = rowData.get(col);
        }
      }

      // Store the loaded level in the LEVELS map using the level file name (without extension)
      String levelName = path.getFileName().toString().split("\\.")[0];
      instance.LEVELS.put(levelName, levelData);

    } catch (IOException e) {
      throw new RuntimeException("Failed to load level from path: " + levelPath, e);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Failed to parse a numeric value in the level file.", e);
    }
  }

  /**
   * Load all levels from the given directory
   */
  public static void loadAllLevels(String levelDirectory) {
    try {
      // Get resource URL for the directory
      URL resourceUrl = getResourceUrl(levelDirectory);

      // Get directory path
      var directoryPath = Paths.get(resourceUrl.toURI());

      // List and load all level files in the directory
      Files.list(directoryPath)
          .filter(Files::isRegularFile)
          .filter(file -> file.getFileName().toString().endsWith(".level"))
          .forEach(file -> loadLevel(file.toString()));

    } catch (Exception e) {
      throw new RuntimeException("Failed to load levels from directory: " + levelDirectory, e);
    }
  }

  /**
   * Get level data from the LEVELS pool by level name
   */
  public static char[][] getLevel(String levelName) {
    return instance.LEVELS.get(levelName);
  }

  /**
   * Save a serializable object to the configuration file
   */
  public static void saveSerializableProperty(String name, Serializable obj) {
    String configFile = "config.properties";
    Properties properties = new Properties();

    // ensure the configuration file exists
    File file = new File(configFile);
    if (!file.exists()) {
      try {
        if (!file.createNewFile()) {
          throw new RuntimeException("Cannot create configuration file: " + configFile);
        }
      } catch (IOException e) {
        throw new RuntimeException("Cannot create configuration file: " + configFile, e);
      }
    }

    // load the configuration file
    try (FileInputStream input = new FileInputStream(configFile)) {
      properties.load(input);
    } catch (IOException e) {
      System.out.println("Failed to load the configuration file. Proceeding with an empty configuration.");
    }

    // serialize the object and store it in the configuration file
    try (FileOutputStream output = new FileOutputStream(configFile)) {
      ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
      ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
      objectStream.writeObject(obj);
      objectStream.close();
      String serializedObject = Base64.getEncoder().encodeToString(byteStream.toByteArray());

      properties.setProperty(name, serializedObject);
      properties.store(output, "Serialized Properties");

      // Store the object in the PROPERTIES map
      instance.PROPERTIES.put(name, obj);
    } catch (IOException e) {
      throw new RuntimeException("Cannot store the serialized properties: " + name, e);
    }
  }

  /**
   * Load a serializable object from the configuration file and store it in PROPERTIES
   */
  public static void loadSerializableProperty(String name) {
    String configFile = "config.properties";
    Properties properties = new Properties();

    // Ensure the configuration file exists
    File file = new File(configFile);
    if (!file.exists()) {
      System.out.println("Configuration file does not exist: " + configFile);
      return;
    }

    // Load the configuration file
    try (FileInputStream input = new FileInputStream(configFile)) {
      properties.load(input);
    } catch (IOException e) {
      throw new RuntimeException("Cannot load the configuration file: " + configFile, e);
    }

    // Deserialize the object and store it in PROPERTIES
    String serializedObject = properties.getProperty(name);
    if (serializedObject == null) {
      throw new IllegalArgumentException("Serializable object not found: " + name);
    }

    try {
      byte[] data = Base64.getDecoder().decode(serializedObject);
      ObjectInputStream objectStream = new ObjectInputStream(new ByteArrayInputStream(data));
      Object obj = objectStream.readObject();
      instance.PROPERTIES.put(name, obj);
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException("Cannot deserialize the object: " + name, e);
    }
  }

  /**
   * Read all serializable objects from the configuration file and store them in PROPERTIES
   */
  public static void loadAllSerializableProperties() {
    String configFile = "config.properties";
    Properties properties = new Properties();

    // Ensure the configuration file exists
    File file = new File(configFile);
    if (!file.exists()) {
      System.out.println("Configuration file does not exist: " + configFile);
      return;
    }

    // Load the configuration file
    try (FileInputStream input = new FileInputStream(configFile)) {
      properties.load(input);
    } catch (IOException e) {
      throw new RuntimeException("Cannot load the configuration file: " + configFile, e);
    }

    // Deserialize all objects and store them in PROPERTIES
    for (String name : properties.stringPropertyNames()) {
      String serializedObject = properties.getProperty(name);
      try {
        byte[] data = Base64.getDecoder().decode(serializedObject);
        ObjectInputStream objectStream = new ObjectInputStream(new ByteArrayInputStream(data));
        Object obj = objectStream.readObject();
        instance.PROPERTIES.put(name, obj);
      } catch (IOException | ClassNotFoundException e) {
        throw new RuntimeException("Cannot deserialize the object: " + name, e);
      }
    }
  }

  /**
   * Get a deserialized object from PROPERTIES by name
   */
  public static Object getSerializableProperty(String name) {
    return instance.PROPERTIES.get(name);
  }


  private static URL getResourceUrl(String resourceDirectory) {
    var resourceUrl = GameSourceLoader.class.getClassLoader().getResource(resourceDirectory);
    if (resourceUrl == null) {
      throw new IllegalArgumentException("Resource directory not found: " + resourceDirectory);
    }

    // Check if the resource URL is a file system directory
    if (!resourceUrl.getProtocol().equals("file")) {
      throw new UnsupportedOperationException("Resource directory is not a file system directory: " + resourceDirectory);
    }

    return resourceUrl;
  }

}
