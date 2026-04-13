package org.example.gomoku_game;


import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;



public class GomokuGameFX extends Application {
    private static final int CELL_SIZE = 35;
    private static final int BOARD_SIZE = 22;
    private GomokuGame game;
    private Label currentPlayerLabel;
    private Label resultLabel;
    private Label timeLabel;
    private Timeline timeline;
    private int timeRemaining;
    private Canvas canvas;
    private GraphicsContext gc;
    private int hoverX=-1;
    private int hoverY=-1;
    private Timeline flashTimeline;
    private Label blackStepsLabel;
    private Label whiteStepsLabel;
    private Label blackMaxLengthLabel;
    private Label whiteMaxLengthLabel;

    @Override
    public void start(Stage primaryStage) {
        game = new GomokuGame(BOARD_SIZE);
        StackPane root = new StackPane();
        Canvas canvas = new Canvas(CELL_SIZE * BOARD_SIZE, CELL_SIZE * BOARD_SIZE);
        root.getChildren().add(canvas);
        gc=canvas.getGraphicsContext2D();
        drawBoard(gc);

        currentPlayerLabel=new Label("Current Player: Player 1");
        resultLabel=new Label("");
        timeLabel=new Label("Time: 30s");

        blackStepsLabel=new Label("Black Steps: 0");
        whiteStepsLabel=new Label("White Steps: 0");
        blackMaxLengthLabel=new Label("Black Max Length: 0");
        whiteMaxLengthLabel=new Label("White Max Length: 0");

        VBox blackstatistic=new VBox(blackStepsLabel, blackMaxLengthLabel);
        blackstatistic.setSpacing(5);
        VBox whitestatistic=new VBox(whiteStepsLabel, whiteMaxLengthLabel);
        whitestatistic.setSpacing(5);

        BorderPane statisticPane=new BorderPane();
        statisticPane.setLeft(blackstatistic);
        statisticPane.setCenter(whitestatistic);
        BorderPane.setMargin(statisticPane,new Insets(10));

        BorderPane borderPane=new BorderPane();
        borderPane.setCenter(canvas);

        // Menu
        MenuBar menuBar=new MenuBar();
        Menu gameMenu=new Menu("Game");
        MenuItem resetItem=new MenuItem("Start a new game");
        MenuItem exitItem=new MenuItem("Exit the game");
        MenuItem undoItem=new MenuItem("Undo");
        MenuItem redoItem=new MenuItem("Redo");

        // When clicking the "Start a new game" button
        resetItem.setOnAction(e -> {
            resetGame(gc);
        });

        // When clicking the "Exit the game" button
        exitItem.setOnAction(e -> {
            primaryStage.close();
        });

        // When clicking the "Undo" button
        undoItem.setOnAction(e -> {
            if(game.undo()){
                drawBoard(gc);
                currentPlayerLabel.setText("Current Player: Player "+game.getCurrentPlayer());
                resultLabel.setText("");
                resetTimer();
                updatestatistic();
            }else{
                System.out.println("No moves to undo!");
            }
        });

        // When clicking the "Redo" button
        redoItem.setOnAction(e -> {
            if(game.redo()){
                drawBoard(gc);
                currentPlayerLabel.setText("Current Player: Player "+game.getCurrentPlayer());
                resultLabel.setText("");
                resetTimer();
                updatestatistic();
            }else{
                System.out.println("No moves to redo!");
            }
        });

        gameMenu.getItems().addAll(exitItem, resetItem, undoItem, redoItem);
        menuBar.getMenus().add(gameMenu);

        borderPane.setTop(menuBar);

        HBox bottomPane=new HBox();
        bottomPane.getChildren().addAll(currentPlayerLabel, timeLabel, resultLabel, blackstatistic, whitestatistic);
        HBox.setMargin(currentPlayerLabel,new Insets(10));
        HBox.setMargin(timeLabel,new Insets(10));
        HBox.setMargin(resultLabel,new Insets(10));
        HBox.setMargin(blackstatistic,new Insets(10));
        HBox.setMargin(whitestatistic,new Insets(10));
        bottomPane.setSpacing(10);

        BorderPane bottomContainer=new BorderPane();
        bottomContainer.setTop(bottomPane);
        bottomContainer.setBottom(statisticPane);
        BorderPane.setMargin(bottomContainer,new Insets(10));

        borderPane.setBottom(bottomContainer);

        // Mouse click event
        canvas.setOnMouseClicked(e -> {
            int x = (int) ((e.getX()-20) / CELL_SIZE);
            int y = (int) ((e.getY()-20) / CELL_SIZE);
            //System.out.println(x);
            //System.out.println(y);

            if(x>=0 && x<BOARD_SIZE-1 && y>=0 && y<BOARD_SIZE-1){
                if (game.move(x, y)) {
                    drawBoard(gc);

                    if (game.isGameOver()) {
                        System.out.println("Game over! The winner is player " + game.getWinner() + "!");
                        resultLabel.setText("Game over! The winner is player "+game.getWinner()+"!");
                        currentPlayerLabel.setText("");
                        stopTimer();
                        timeLabel.setVisible(false);
                        resultLabel.setVisible(true);
                        updatestatistic();
                    }else{
                        currentPlayerLabel.setText("Current Player: Player "+game.getCurrentPlayer());
                        resetTimer();
                        updatestatistic();
                    }
                } else {
                    System.out.println("Invalid move!");
                    flashInvalidMove(x,y);
                }
            }


        });

        // Handle mouse movement
        canvas.setOnMouseMoved(e -> {
            int x=(int)((e.getX()-20)/CELL_SIZE);
            int y=(int)((e.getY()-20)/CELL_SIZE);
            if(hoverX!=x || hoverY!=y){
                hoverX=x;
                hoverY=y;
                drawBoard(gc);
            }
        });

        // Handle mouse exit
        canvas.setOnMouseExited(e -> {
            hoverX=-1;
            hoverY=-1;
            drawBoard(gc);
        });

        Scene scene=new Scene(borderPane, CELL_SIZE * BOARD_SIZE, CELL_SIZE*BOARD_SIZE+120);
        primaryStage.setTitle("Gomoku Game");
        primaryStage.setScene(scene);
        primaryStage.show();



        // Initialize the timer
        timeRemaining=30;
        timeline=new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                timeRemaining--;
                timeLabel.setText("Time: "+timeRemaining+"s");
                if(timeRemaining<=0){
                    stopTimer();
                    switchPlayer();
                    resetTimer();
                }
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);

        // Start the timer
        resetTimer();

        updatestatistic();
    }

    // Draw the board
    private void drawBoard(GraphicsContext gc) {
        gc.clearRect(0, 0, CELL_SIZE * BOARD_SIZE, CELL_SIZE * BOARD_SIZE);
        gc.setStroke(Color.BLACK);
        for (int i = 1; i < BOARD_SIZE; i++) {
            gc.strokeLine(i * CELL_SIZE, CELL_SIZE, i * CELL_SIZE, CELL_SIZE * (BOARD_SIZE-1));
            gc.strokeLine(CELL_SIZE, i * CELL_SIZE, CELL_SIZE * (BOARD_SIZE-1), i * CELL_SIZE);

        }
        int[][] board = game.getBoard();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                double x = i * CELL_SIZE + CELL_SIZE/2.0 + 3.5;
                double y = j * CELL_SIZE + CELL_SIZE/2.0 + 3.5;
                double w = CELL_SIZE * 0.8;
                double h = CELL_SIZE * 0.8;
                if (board[i][j] == 1) {
                    gc.setFill(Color.BLACK);
                    gc.fillOval(x, y, w, h);
                    gc.strokeOval(x, y, w, h);
                } else if (board[i][j] == 2) {
                    gc.setFill(Color.WHITE);
                    gc.fillOval(x, y, w, h);
                    gc.strokeOval(x, y, w, h);
                }
            }
        }

        // Draw hover effect
        if(hoverX!=-1 && hoverY!=-1 && game.getBoard()[hoverX][hoverY]==0 && hoverX<BOARD_SIZE-1 && hoverY<BOARD_SIZE-1){
            double x=hoverX*CELL_SIZE+CELL_SIZE/2.0+3.5;
            double y=hoverY*CELL_SIZE+CELL_SIZE/2.0+3.5;
            double w=CELL_SIZE*0.8;
            double h=CELL_SIZE*0.8;
            gc.setStroke(Color.BLUE);
            gc.strokeOval(x,y,w,h);
        }
    }

    // Start a new game
    private void resetGame(GraphicsContext gc){
        game=new GomokuGame(BOARD_SIZE);
        drawBoard(gc);
        currentPlayerLabel.setText("Current Player: Player 1");
        resultLabel.setText("");
        resultLabel.setVisible(false);
        timeLabel.setVisible(true);
        resetTimer();
        updatestatistic();
    }

    // Reset the timer
    private void resetTimer(){
        timeRemaining=30;
        timeLabel.setText("Time: "+timeRemaining+"s");
        timeline.playFromStart();
    }

    // Stop the timer
    private void stopTimer(){
        timeline.stop();
    }

    // Switch player
    private void switchPlayer(){
        game.setCurrentPlayer(game.getCurrentPlayer()==1?2:1);
        currentPlayerLabel.setText("Current Player: Player "+game.getCurrentPlayer());
    }

    // Flash the invalid move
    private void flashInvalidMove(int x,int y){
        if(x>=0 && x<BOARD_SIZE-1 && y>=0 && y<BOARD_SIZE-1){
            if(flashTimeline!=null){
                flashTimeline.stop();
            }
            double centerX=x*CELL_SIZE+CELL_SIZE;
            double centerY=y*CELL_SIZE+CELL_SIZE;
            double radius=CELL_SIZE*0.4;

            flashTimeline=new Timeline(
                    new KeyFrame(Duration.ZERO, e ->{
                        gc.setFill(Color.RED);
                        gc.fillOval(centerX-radius, centerY-radius, radius*2, radius*2);
                    }),
                    new KeyFrame(Duration.millis(100),e ->{
                        gc.clearRect(centerX-radius, centerY-radius, radius*2, radius*2);
                    }),
                    new KeyFrame(Duration.millis(200),e ->{
                        gc.setFill(Color.RED);
                        gc.fillOval(centerX-radius, centerY-radius, radius*2, radius*2);
                    }),
                    new KeyFrame(Duration.millis(300),e ->{
                        gc.clearRect(centerX-radius, centerY-radius, radius*2, radius*2);
                    })
            );
            flashTimeline.setCycleCount(3);
            flashTimeline.play();
        }


    }

    // Update statistic
    private void updatestatistic(){
        blackStepsLabel.setText("Black Steps: "+game.getBlackSteps());
        whiteStepsLabel.setText("White Steps: "+game.getWhiteSteps());
        blackMaxLengthLabel.setText("Black Max Length: "+game.getMaxLength(1));
        whiteMaxLengthLabel.setText("White Max Length: "+game.getMaxLength(2));
    }
    public static void main(String[] args) {
        launch(args);
    }
}