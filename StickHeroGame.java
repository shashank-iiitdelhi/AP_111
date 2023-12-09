package com.example.game;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.*;

public class StickHeroGame extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private boolean isMousePressed = false;

    private boolean isspacebarpressed = false;
    private List<int[]> platformEnds = new ArrayList<>();
    private boolean isGameOver = false;

    private double barHeight = 0.0;

    private double bridgelength = 0.0;

    private double lastplatformmiddle;

    private double screenPosition = 200;

    private int cherriesCollected = 0;

    private int highestScore = 0;
    private boolean rotcomp = false;

    private boolean isBridgeSaved = false;

    private int bridge_no=0;

    private int score=0;

    private int test=0;

    private Pane root;
    private Canvas canvas;
    private GraphicsContext gc;
    private Player player;
    private List<Platform> platforms;
    private List<Cherry> cherries;
    private StartScreen startScreen;
    private List<Bridge> bridges;

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

        bridges = new ArrayList<>();
        primaryStage.show();
    }
    private List<Cherry> generateRandomCherries() {
        List<Cherry> cherries = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < platforms.size() - 1; i++) {
            Platform currentPlatform = platforms.get(i);
            Platform nextPlatform = platforms.get(i + 1);
            // Randomly decide if a cherry should appear between the two side-by-side platforms
            if (random.nextInt(100) < 50) {
                double max = nextPlatform.getX()-21;
                double min = currentPlatform.getX()+currentPlatform.getWidth();
                double cherryX = random.nextDouble(max - min) + min;
                double cherryY = HEIGHT + 10 - currentPlatform.getHeight(); // Just above the platform
                cherries.add(new Cherry(cherryX, cherryY));
            }

        }
        return cherries;
    }

    private void collectCherry(Cherry cherry) {
        cherriesCollected++;
        Iterator<Cherry> iterator = cherries.iterator();
        while (iterator.hasNext()) {
            Cherry currentCherry = iterator.next();
            if (currentCherry == cherry) {
                iterator.remove(); // Safely remove the cherry using the iterator
                break; // Exit the loop since we found the matching cherry
            }
        }

        // Increase the player's score
        score += 2;
    }

    private void startGame() {
        root.getChildren().remove(startScreen);

        platforms = generateRandomPlatforms(100);
        cherries=generateRandomCherries();
        Platform firstPlatform = platforms.get(0);
        player = new Player(firstPlatform.getX() + firstPlatform.getWidth() - 20, firstPlatform.getY() + 10);

        new AnimationTimer() {
            private static final double ROTATION_SPEED = 1; // Adjust this value to control rotation speed

            @Override
            public void handle(long now) {
                gc.clearRect(0, 0, WIDTH, HEIGHT);

                // Display the score at the top
                gc.setFill(Color.BLACK);
                gc.setFont(Font.font(20));
                gc.fillText("Score: " + score, 10, 30);

                gc.setFill(Color.RED);
                gc.setFont(Font.font(20));
                gc.fillText("Cherries: " + cherriesCollected, WIDTH - 150, 30);

                // Check if the game is over
                if (isGameOver) {
                    displayEndScreen();
                    stop(); // Stop the AnimationTimer
                }

                for (Platform platform : platforms) {
                    platform.render(gc);
                }

                try {
                    for (Cherry cherry : new ArrayList<>(cherries)) {
                        cherry.render(gc);

                        if (player.isFlipped && (int) player.getX() == (int) cherry.getX()) {
                            collectCherry(cherry);
                        }
                    }
                } catch (ConcurrentModificationException e) {

                }

                for (Bridge bridge : bridges) {
                    rotcomp=false;
                    bridge.update(); // Update the bridge to control rotation speed
                    bridge.render(gc);
                    if (bridge.angle==90 && bridge.b_no==bridge_no){
                        rotcomp=true;
                    }

                    boolean cT = isCollisonTrue();
                    if(player.isFlipped && cT){
                        isGameOver = true;
                    }

                    // Check if rotation is complete
                    if (rotcomp && bridge.b_no==bridge_no) {
                        // Move the ninja across the bridge
                        moveNinjaAcrossBridge(bridge);
                    }
                }

                if (isMousePressed) {
                    // Draw the extending bar dynamically

                    drawDynamicBridge();
                }
                player.render(gc);

                if (isspacebarpressed) {
                    player.flip();
                    isspacebarpressed = false; // Reset the flag to prevent continuous flipping
                }
            }

            public boolean isCollisonTrue(){
                boolean isNinjaPlatformCollide = false;
                for(int[] x:platformEnds){
                    if(player.getX()>x[0] && player.getX()<x[1]){
                        isNinjaPlatformCollide = true;
                    }
                }
                return isNinjaPlatformCollide;
            }

            private void moveNinjaAcrossBridge(Bridge bridge) {
                double ninjaX = player.getX();
                // Check if the ninja is on the bridge
                if (ninjaX < (bridge.getTopX() + bridgelength)) {
                    player.setX(ninjaX + 1);
                }
                else if(ninjaX == (bridge.getTopX() + bridgelength)){
                    boolean isOnPlatform = false;
                    for (int[] platformRange : platformEnds) {
                        if (ninjaX >= platformRange[0] && ninjaX <= platformRange[1]) {
                            isOnPlatform = true;
                            score++;
                            screenPosition = platformRange[1] - WIDTH / 2; // Adjusted to center the screen
                            updateEntities(); // Update player and bridges positions
                            player.setX(player.getX() + 1);
                            break;
                        }
                    }

                    if (!isOnPlatform) {
                        for (int[] platformRange : platformEnds) {
                            if (ninjaX > platformRange[1]) {
                                lastplatformmiddle= (double) (platformRange[1] + platformRange[0]) /2;
                            }
                            else{
                                break;
                            }
                        }
                        isGameOver = true;

                    }
                }
            }
            private void updateEntities() {
                player.setX(player.getX() - screenPosition); // Move player back
                for (Bridge bridge : bridges) {
                    bridge.setX(bridge.getX() - screenPosition); // Move bridges back
                }
                for (Platform platform : platforms) {
                    platform.setX(platform.getX() - screenPosition); // Move platforms back
                }
                for (Cherry cherry : cherries) {
                    cherry.setX(cherry.getX() - screenPosition); // Move cherries back
                }
                // Update platform ends
                updatePlatformEnds();
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
        Scene scene2 = canvas.getScene();
        scene2.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                isspacebarpressed=true;
            }
        });
        scene2.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                isspacebarpressed=false;
            }
        });
    }

    // Add a method to update the platform ends
    private void updatePlatformEnds() {
        platformEnds.clear();
        for (Platform platform : platforms) {
            int startX = platform.getX_int();
            int endX = startX + (int)platform.getWidth();
            platformEnds.add(new int[]{startX, endX});
        }
    }

    // Add a method to display the end screen
    private void displayEndScreen() {
        if (score > highestScore) {
            highestScore = score;
        }

        // Create an end screen pane
        EndScreen endScreen = new EndScreen(score,cherriesCollected,highestScore,() -> restartGame());

        // Add the end screen to the root pane
        root.getChildren().add(endScreen);
    }

    // Add a method to restart the game
    private void restartGame() {
        // Reset game variables
        isGameOver = false;
        isBridgeSaved = false;
        score = 0;
        bridges.clear();
        platforms.clear();
        platformEnds.clear();
        screenPosition = 200;

        // Remove the end screen if it exists
        root.getChildren().removeIf(node -> node instanceof EndScreen);

        // Restart the AnimationTimer
        startGame();
    }

    private void saveBar() {
        // Create a new Bridge with the current barHeight and add it to the bridges list
        bridge_no+=1;
        Bridge newBridge = new Bridge(player.getX(), barHeight, Color.BROWN,bridge_no);
        bridges.add(newBridge);
        bridgelength=barHeight;

        isBridgeSaved = true;

        barHeight = 0.0; // Reset the bar height after saving
    }

    private void drawDynamicBridge() {
        // Draw the extending bar dynamically
        barHeight += 5;
        gc.setFill(Color.BROWN);
        gc.fillRect(player.getX() - 5, HEIGHT - 100 - barHeight, 10, barHeight);

    }

    private List<Platform> generateRandomPlatforms(int count) {
        List<Platform> platforms = new ArrayList<>();
        int x = (int) screenPosition;

        Random random = new Random();

        for (int i = 0; i < count; i++) {
            int platformWidth = random.nextInt(100) + 50;
            platforms.add(new Platform(x, platformWidth,100));
            platformEnds.add(new int[]{x, x + platformWidth});
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
        private boolean isFlipped = false; // Flag to track the flip state

        public Player(double x, double y) {
            this.x = x;
            this.y = y - HEIGHT;
        }

        public void render(GraphicsContext gc) {
            if (isFlipped) {
                gc.save();
                gc.translate(x, y + HEIGHT); // Move to the bottom of the player
                gc.scale(1, -1); // Flip vertically by scaling the Y-axis
                gc.drawImage(ninjaImage, -WIDTH / 2, -60, WIDTH, HEIGHT);
                gc.restore();
            }
//            if(isGameOver){
//                gc.drawImage(ninjaImage,x - WIDTH / 2, y, WIDTH, HEIGHT);
//                int animationDuration = 2000;
//                Timeline timeline = new Timeline(
//                        new KeyFrame(Duration.ZERO, new KeyValue(gc.getTransform().tyProperty(), y)),
//                        new KeyFrame(Duration.millis(animationDuration), new KeyValue(gc.getTransform().tyProperty(), StickHeroGame.HEIGHT))
//                );
//            }
            else {
                gc.drawImage(ninjaImage, x - WIDTH / 2, y, WIDTH, HEIGHT);
            }
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

        public void setY(double y) {
            this.y = y;
        }

        public void flip() {
            // Toggle the flip state
            isFlipped = !isFlipped;
        }
    }

    public static class Platform {
        private double x;
        private double width;
        private double height; // New variable to represent platform height

        public Platform(double x, double width, double height) {
            this.x = x;
            this.width = width;
            this.height = height;
        }

        public void render(GraphicsContext gc) {
            gc.setFill(Color.GREEN);
            gc.fillRect(x, HEIGHT - height, width, height);
        }

        public double getX() {
            return x;
        }

        public int getX_int(){
            return (int)x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getWidth() {
            return width;
        }

        public double getHeight() {
            return height;
        }

        public double getY() {
            return HEIGHT - height;
        }
    }

    public static class Cherry {
        private double x;
        private double y;
        private Circle circle;

        public Cherry(double x, double y) {
            this.x = x;
            this.y = y;
            this.circle = new Circle(x, y, 10, Color.RED);
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public void render(GraphicsContext gc) {
            gc.setFill(Color.RED);
            gc.fillOval(x, y, 20, 20);
        }

        public Circle getCircle() {
            return circle;
        }
    }

    public static class Bridge {
        private double x;
        private double height;
        private Color color;
        private double angle; // New variable to store the rotation angle
        private boolean rotationComplete;

        public void setX(double x) {
            this.x = x;
        }

        public double getX() {
            return x;
        }

        private int b_no;

        public Bridge(double x, double height, Color color,int b_no) {
            this.x = x;
            this.height = height;
            this.color = color;
            this.angle = 0.0;
            this.rotationComplete = false;
            this.b_no=b_no;
        }

        public void render(GraphicsContext gc) {
            // Save the current state of the graphics context
            gc.save();

            // Translate to the base of the bridge
            gc.translate(x + 5, HEIGHT - 100);

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
                //rotationComplete = true;
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
    // Add the EndScreen class
    public static class EndScreen extends Pane {
        public EndScreen(int score,int cherriesCollected,int highestScore, Runnable onRestartCallback) {
            setPrefSize(WIDTH, HEIGHT);

            // Create text for end screen
            javafx.scene.text.Text endText = new javafx.scene.text.Text("Game Over\nScore: " + score+"\nCherries Collected: " + cherriesCollected+"\nHighest Score: " + highestScore);
            endText.setFont(Font.font("Arial", FontWeight.BOLD, 40));
            endText.setFill(Color.RED);
            endText.setTextAlignment(TextAlignment.CENTER);
            endText.setLayoutX((WIDTH - endText.getBoundsInLocal().getWidth()) / 2);
            endText.setLayoutY(HEIGHT / 3);

            // Create a restart button
            Button restartButton = new Button("Restart Game");
            restartButton.setStyle("-fx-font-size: 24; -fx-background-color: #00FF00;");
            restartButton.setOnAction(event -> onRestartCallback.run());
            restartButton.setLayoutX((WIDTH - restartButton.getWidth()) / 2);
            restartButton.setLayoutY(2 * HEIGHT / 3);

            // Add elements to the end screen
            getChildren().addAll(endText, restartButton);
        }
    }
}

