package breakout.engine.base;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarFile;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class GameSourceLoader {
  private static final GameSourceLoader instance = new GameSourceLoader();

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
    try (InputStream is = getResourceAsStream(imagePath)) {
      if (is == null) {
        throw new IOException("Resource not found: " + imagePath);
      }
      Image image = new Image(is);
      String imageName = Paths.get(imagePath).getFileName().toString().split("\\.")[0];
      instance.IMAGES.put(imageName, new ImageView(image));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Load all images from the given directory
   */
  public static void loadAllImages(String imageDirectory) {
    try {
      // Get the directory as a URL
      URL resourceUrl = GameSourceLoader.class.getClassLoader().getResource(imageDirectory);
      if (resourceUrl == null) {
        throw new IOException("Directory not found: " + imageDirectory);
      }

      // Check if the resource is inside a JAR
      if (resourceUrl.toString().startsWith("jar")) {
        // Handle case where resources are inside a JAR file
        try (JarFile jarFile = new JarFile(new File(resourceUrl.toURI()))) {
          jarFile.stream()
              .filter(entry -> !entry.isDirectory() && entry.getName().startsWith(imageDirectory)
                  && (entry.getName().endsWith(".png") || entry.getName().endsWith(".jpg") || entry.getName().endsWith(".jpeg")))
              .forEach(entry -> {
                String imagePath = entry.getName();
                // Load image from JAR entry
                try (InputStream is = GameSourceLoader.class.getClassLoader().getResourceAsStream(imagePath)) {
                  if (is != null) {
                    loadImage(imagePath, is);
                  } else {
                    System.err.println("Failed to load image: " + imagePath);
                  }
                } catch (IOException e) {
                  e.printStackTrace();
                }
              });
        }
      } else {
        // Handle case where resources are on the filesystem
        var directoryPath = Paths.get(resourceUrl.toURI());
        Files.list(directoryPath)
            .filter(Files::isRegularFile)
            .filter(file -> {
              String fileName = file.getFileName().toString().toLowerCase();
              return fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg");
            })
            .forEach(file -> {
              try {
                loadImage(imageDirectory + "/" + file.getFileName().toString(), Files.newInputStream(file));
              } catch (IOException e) {
                e.printStackTrace();
              }
            });
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to load images from directory: " + imageDirectory, e);
    }
  }

  private static void loadImage(String imagePath, InputStream is) {
    try {
      Image image = new Image(is);
      String imageName = Paths.get(imagePath).getFileName().toString().split("\\.")[0];
      instance.IMAGES.put(imageName, new ImageView(image));
    } catch (Exception e) {
      System.err.println("Error loading image: " + imagePath);
      e.printStackTrace();
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
    try (InputStream is = getResourceAsStream(levelPath)) {
      if (is == null) {
        throw new IOException("Resource not found: " + levelPath);
      }
      var lines = new String(is.readAllBytes()).lines().toList();
      boolean insideLevel = false;
      ArrayList<ArrayList<Character>> levelDataList = new ArrayList<>();
      int maxCols = 0;

      for (String line : lines) {
        String trimmedLine = line.trim();
        if (trimmedLine.isEmpty() || trimmedLine.startsWith("#") || trimmedLine.startsWith("//")) {
          continue;
        }
        if (trimmedLine.startsWith("@BEGIN")) {
          insideLevel = true;
          continue;
        }
        if (trimmedLine.startsWith("@END")) {
          break;
        }
        if (insideLevel) {
          String[] values = trimmedLine.split(",");
          ArrayList<Character> rowData = new ArrayList<>();
          for (String value : values) {
            value = value.trim();
            if (value.length() == 1) {
              rowData.add(value.charAt(0));
            } else {
              throw new IllegalArgumentException("Invalid value found: " + value);
            }
          }
          maxCols = Math.max(maxCols, rowData.size());
          levelDataList.add(rowData);
        }
      }

      char[][] levelData = new char[levelDataList.size()][maxCols];
      for (int row = 0; row < levelDataList.size(); row++) {
        ArrayList<Character> rowData = levelDataList.get(row);
        for (int col = 0; col < rowData.size(); col++) {
          levelData[row][col] = rowData.get(col);
        }
      }

      String levelName = Paths.get(levelPath).getFileName().toString().split("\\.")[0];
      instance.LEVELS.put(levelName, levelData);

    } catch (IOException e) {
      throw new RuntimeException("Failed to load level from path: " + levelPath, e);
    }
  }

  /**
   * Load all levels from the given directory
   */
  public static void loadAllLevels(String levelDirectory) {
    try {
      URL resourceUrl = getResourceUrl(levelDirectory);
      var directoryPath = Paths.get(resourceUrl.toURI());
      Files.list(directoryPath)
          .filter(Files::isRegularFile)
          .filter(file -> file.getFileName().toString().endsWith(".level"))
          .forEach(file -> loadLevel(levelDirectory + "/" + file.getFileName()));
    } catch (Exception e) {
      throw new RuntimeException("Failed to load levels from directory: " + levelDirectory, e);
    }
  }

  /**
   * Get level data from the LEVELS pool by name
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

    try (FileInputStream input = new FileInputStream(configFile)) {
      properties.load(input);
    } catch (IOException e) {
      System.out.println("Failed to load the configuration file. Proceeding with an empty configuration.");
    }

    try (FileOutputStream output = new FileOutputStream(configFile)) {
      ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
      ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
      objectStream.writeObject(obj);
      objectStream.close();
      String serializedObject = Base64.getEncoder().encodeToString(byteStream.toByteArray());

      properties.setProperty(name, serializedObject);
      properties.store(output, "Serialized Properties");
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
    File file = new File(configFile);
    if (!file.exists()) {
      System.out.println("Configuration file does not exist: " + configFile);
      return;
    }

    try (FileInputStream input = new FileInputStream(configFile)) {
      properties.load(input);
    } catch (IOException e) {
      throw new RuntimeException("Cannot load the configuration file: " + configFile, e);
    }

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
    File file = new File(configFile);
    if (!file.exists()) {
      System.out.println("Configuration file does not exist: " + configFile);
      return;
    }

    try (FileInputStream input = new FileInputStream(configFile)) {
      properties.load(input);
    } catch (IOException e) {
      throw new RuntimeException("Cannot load the configuration file: " + configFile, e);
    }

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

  /**
   * Retrieve an InputStream for a resource
   */
  private static InputStream getResourceAsStream(String resource) {
    InputStream is = GameSourceLoader.class.getClassLoader().getResourceAsStream(resource);
    if (is == null) {
      throw new RuntimeException("Resource not found: " + resource);
    }
    return is;
  }

  /**
   * Retrieve a URL for a resource
   */
  private static URL getResourceUrl(String resource) {
    URL resourceUrl = GameSourceLoader.class.getClassLoader().getResource(resource);
    if (resourceUrl == null) {
      throw new RuntimeException("Resource not found: " + resource);
    }
    return resourceUrl;
  }
}
