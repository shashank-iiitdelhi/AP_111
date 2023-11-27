package com.example.game;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
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

    private Pane root;
    private Canvas canvas;
    private GraphicsContext gc;
    private Player player;
    private List<Platform> platforms;
    private StartScreen startScreen;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
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

        primaryStage.show();
    }

    private void startGame() {
        root.getChildren().remove(startScreen);

        platforms = generateRandomPlatforms(10);
        Platform firstPlatform = platforms.get(0);
        player = new Player(firstPlatform.getX() + firstPlatform.getWidth()-20, firstPlatform.getY()+10);

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                gc.clearRect(0, 0, WIDTH, HEIGHT);

                for (Platform platform : platforms) {
                    platform.render(gc);
                }
                player.render(gc);
            }
        }.start();
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
}
