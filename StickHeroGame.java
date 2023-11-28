package com.example.game;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StickHeroGame extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private boolean isMousePressed = false;

    private double barHeight = 0.0;

    private double bridgelength = 0.0;

    private Pane root;
    private Canvas canvas;
    private GraphicsContext gc;
    private Player player;
    private List<Platform> platforms;
    private StartScreen startScreen;
    private List<Bridge> bridges;
    //private List<Cherries> cherries;
    int cherriesForBridges = 0;
    List<Cherries> cherriesList;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Image icon = new Image("file:Ninja.png");
        primaryStage.getIcons().add(icon);
        primaryStage.setTitle("Stick Hero Game");
        root = new Pane();
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setScene(scene);

        // Load the custom image for the background
        Image backgroundImage = new Image("file:background.jpg");
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                new BackgroundSize(800, 600, false, false, false, false)
        );
        root.setBackground(new javafx.scene.layout.Background(background));

        canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);

        startScreen = new StartScreen(() -> startGame());
        root.getChildren().add(startScreen);

        bridges = new ArrayList<>();

        primaryStage.show();
    }

    private void startGame() {
        root.getChildren().remove(startScreen);

        platforms = generateRandomPlatforms(10);
        cherriesList = generateRandomCherries(platforms);
        Platform firstPlatform = platforms.get(0);
        player = new Player(firstPlatform.getX() + firstPlatform.getWidth() - 20, firstPlatform.getY() + 10);

        new AnimationTimer() {
            private static final double ROTATION_SPEED = 0.5; // Adjust this value to control rotation speed


            @Override
            public void handle(long now) {
                gc.clearRect(0, 0, WIDTH, HEIGHT);
                // In your AnimationTimer's handle method

//                for (Cherries cherries : cherries) {
//                    cherries.render(gc);
//                }
                for (int i = 0; i < platforms.size() - 1; i++) {
//                    double startX = platforms.get(i).getX() + platforms.get(i).getWidth();
//                    double endX = platforms.get(i + 1).getX();
//                    // Place one cherry between each pair of platforms
//                    double cherryX = startX + (endX - startX) / 2;
//                    cherryCounter++;
//                    Cherries cherriesBridge = new Cherries(cherryX);
//                    cherriesList.add(cherriesBridge);
                    // Render the platform
                    platforms.get(i).render(gc);

                }
//                for (int i = 0; i < bridges.size() - 1; i++) {
//
//                    double startX = bridges.get(i).getTopX();
//                    double height = bridges.get(i).getHeight();
//                    // Place cherries at equal intervals between bridges
//                    for (int j = 0; j < 5; j++) {  // Adjust the number of cherries as needed
//                        double cherriesX = startX + (height) * (j + 1) / 6.0;  // Adjust divisor for desired interval
//                        Cherries cherriesBridge = new Cherries(cherriesX);
//                        cherriesList.add(cherriesBridge);
//                    }
//                }

// In your game loop
                for (int i = 0; i<cherriesList.size();i++) {
                    System.out.println("Rendering cherry at X: " + cherriesList.get(i).getX());
                    cherriesList.get(i).render(gc);
                }
                for (Bridge bridge : bridges) {
                    bridge.update(); // Update the bridge to control rotation speed
                    bridge.render(gc);

                    // Check if rotation is complete
                    if (bridge.isRotationComplete()) {
                        // Move the ninja across the bridge
                        moveNinjaAcrossBridge(bridge);
                    }
                }

                if (isMousePressed) {
                    // Draw the extending bar dynamically
                    drawDynamicBridge();
                }

                player.render(gc);
            }

            private void moveNinjaAcrossBridge(Bridge bridge) {
                double ninjaX = player.getX();
                double ninjaY = player.getY();
                // Check if the ninja is on the bridge
                if (ninjaX <= (bridge.getTopX() + bridgelength)) {
                    player.setX(ninjaX + ROTATION_SPEED);
                }
            }

        }.start();

        Scene scene = canvas.getScene();
        scene.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                isMousePressed = true;
            }
        });

        scene.setOnMouseReleased(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                isMousePressed = false;
                saveBar();
            }
        });
    }

    private void saveBar() {
        // Create a new Bridge with the current barHeight and add it to the bridges list
        Bridge newBridge = new Bridge(player.getX(), barHeight, Color.BROWN);
        bridges.add(newBridge);
        bridgelength=barHeight;

        // Set the angle of the new bridge
        //newBridge.update();

        barHeight = 0.0; // Reset the bar height after saving
    }

    private void drawDynamicBridge() {
        // Draw the extending bar dynamically
        barHeight += 5;
        gc.setFill(Color.BROWN);
        gc.fillRect(player.getX() - 5, HEIGHT - 10 - barHeight, 10, barHeight);

    }

    private List<Platform> generateRandomPlatforms(int count) {
        List<Platform> platforms = new ArrayList<>();
        int x = 200;

        Random random = new Random();

        for (int i = 0; i < count; i++) {
            int platformWidth = random.nextInt(100) + 50;
            platforms.add(new Platform(x, platformWidth));
            x += platformWidth + random.nextInt(150) + 50;
        }

        return platforms;
    }
    private List<Cherries> generateRandomCherries(List<Platform> platforms){
        List<Cherries> cherries = new ArrayList<>();
        Random random= new Random();
        Random random1 = new Random();
        boolean isCherries = random.nextBoolean();
        if(isCherries && cherriesForBridges<platforms.size()-1) {
            double startX = platforms.get(cherriesForBridges).getX()+ platforms.get(cherriesForBridges).getWidth();
            double endX = platforms.get(cherriesForBridges+1).getX();
            double x = random1.nextInt((int)(endX - startX)) ;
            cherries.add(new Cherries(x));
        }
        cherriesForBridges++;
        return cherries;
    }

    public class Player {
        private static final double WIDTH = 80;
        private static final double HEIGHT = 80;

        private double x;
        private double y;

        private Image ninjaImage = new Image("file:Ninja.png"); // Custom Ninja Model

        public Player(double x, double y) {
            this.x = x;
            this.y = y - HEIGHT;
        }

        public void render(GraphicsContext gc) {
            // Use ImagePattern to fill the player rectangle with the ninja image
            gc.setFill(new ImagePattern(ninjaImage));
            gc.fillRect(x - WIDTH / 2, y, WIDTH, HEIGHT);
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }
    }

    public static class Platform {
        private double x;
        private double width;

        public Platform(double x, double width) {
            this.x = x;
            this.width = width;
        }

        public void render(GraphicsContext gc) {
            gc.setFill(Color.GREEN);
            gc.fillRect(x, HEIGHT - 10, width, 10);
        }

        public double getX() {
            return x;
        }

        public double getWidth() {
            return width;
        }

        public double getY() {
            return HEIGHT - 10;
        }
    }

    public static class Bridge {
        private double x;
        private double height;
        private Color color;
        private double angle; // New variable to store the rotation angle
        private boolean rotationComplete;

        public Bridge(double x, double height, Color color) {
            this.x = x;
            this.height = height;
            this.color = color;
            this.angle = 0.0;
            this.rotationComplete = false;
        }

        public void render(GraphicsContext gc) {
            // Save the current state of the graphics context
            gc.save();

            // Translate to the base of the bridge
            gc.translate(x + 5, HEIGHT - 10);

            // Rotate the graphics context
            gc.rotate(angle);

            // Fill the rectangle with the rotated graphics context
            gc.setFill(color);
            gc.fillRect(-5, -height, 10, height);

            // Restore the graphics context to its original state
            gc.restore();
        }

        public void update() {
            // Increment the angle based on the rotation speed
            if (!rotationComplete) {
                angle += 0.5;
            }

            // Normalize the angle to keep it within bounds
            if (angle >= 90.0) {
                angle = 90.0;
                rotationComplete = true;
            }
        }

        public boolean isRotationComplete() {
            return rotationComplete;
        }

        public double getTopX() {
            return x + 5;
        }

        public double getTopY() {
            return HEIGHT - 10 - height;
        }

        public double getHeight(){
            return height;
        }
    }

    public static class StartScreen extends Pane {
        public StartScreen(Runnable onStartCallback) {
            setPrefSize(WIDTH, HEIGHT);

            // Load the custom image for the background
            Image backgroundImage = new Image("file:background.jpg");
            BackgroundImage background = new BackgroundImage(
                    backgroundImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.DEFAULT,
                    new BackgroundSize(800, 600, false, false, false, false)
            );
            setBackground(new javafx.scene.layout.Background(background));

            // Set the name of the game
            javafx.scene.text.Text gameName = new javafx.scene.text.Text("Stick Hero");
            gameName.setFont(Font.font("Arial", FontWeight.BOLD, 80));
            gameName.setFill(Color.BLACK);
            gameName.setTextAlignment(TextAlignment.CENTER);
            gameName.setLayoutX((WIDTH - gameName.getBoundsInLocal().getWidth()) / 2);
            gameName.setLayoutY(HEIGHT / 4);

            // Create a styled start button
            Button startButton = new Button("Start Game");
            startButton.setStyle("-fx-font-size: 24; -fx-background-color: #FF0000;");
            startButton.setOnAction(event -> onStartCallback.run());
            startButton.setLayoutX(gameName.getLayoutX() + 120); // On the same x-axis as "Stick Hero"
            startButton.setLayoutY(HEIGHT / 2);

            getChildren().addAll(gameName, startButton);
        }
    }
    public static class Cherries{
        private static final int WIDTH = 30;
        private static final int HEIGHT = 30;
        private double cherryX;
        private double cherryY = 10;
        private Image cherryImage = new Image("file:Cherry.png"); // Custom Ninja Model

        public void render(GraphicsContext gc) {
            // Use ImagePattern to fill the player rectangle with the ninja image
            gc.setFill(new ImagePattern(cherryImage));
            gc.fillRect(cherryX , cherryY, WIDTH, HEIGHT);
        }

        public double getX() {
            return cherryX;
        }

        public void setX(double cherryX) {
            this.cherryX = cherryX;
        }

        public double getY() {
            return getY();
        }

        public Cherries(double cherryX) {
            this.cherryX = cherryX;
        }
    }
}



