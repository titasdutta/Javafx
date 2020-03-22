package org.example;

import com.sun.tools.javac.Main;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * JavaFX App
 */
public class App extends Application {

    public static final int TILE_SIZE = 80;
    public static final int COLS = 6;
    public static final int ROWS = 6;

    public static int redWin = 0;
    public static int yelWin = 0;

    public static String whoWon = "";

    public boolean redMove = true;
    private static Disc[][] grid = new Disc[COLS][ROWS];

    private static Pane discRoot = new Pane();

    private Text winner = new Text("Winner: " + whoWon);


    private Text redScore = new Text("Player1 Wins: " + redWin);
    private Text blueScore = new Text("Player2 Wins: " + yelWin);


    private int lastRow = 0;
    private int lastCol = 0;
    private Disc discPtr = null;

    private Pane createContent() {
        Pane root = new Pane();


        root.getChildren().add(discRoot);

        Shape gridShape = makeGrid();



//        Button c1 = new Button("Column 1");
//        Button c2 = new Button("Column 2");
//        Button c3 = new Button("Column 3");
//        Button c4 = new Button("Column 4");
//        Button c5 = new Button("Column 5");
//        Button c6 = new Button("Column 6");
//
//        TilePane r = new TilePane();

//        r.getChildren().add(c1);
//        r.getChildren().add(c2);
//        r.getChildren().add(c3);
//        r.getChildren().add(c4);
//        r.getChildren().add(c5);
//        r.getChildren().add(c6);


//
//        root.getChildren().add(r);

        //root.getChildren().addAll(menuBar);

//        TextField t = new TextField("Save");
//
//        root.getChildren().add(t);
        root.getChildren().add(gridShape);

        root.getChildren().addAll(makeColumns());

        return root;
    }

    private Shape makeGrid() {

        Shape shape = new Rectangle((COLS + 1) * TILE_SIZE, (ROWS + 1) * TILE_SIZE);

        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {


                Circle circle = new Circle(TILE_SIZE / 2);
                circle.setCenterX(TILE_SIZE/2);
                circle.setCenterY(TILE_SIZE/2);

                circle.setTranslateX(x * (TILE_SIZE + 5) + TILE_SIZE / 4);
                circle.setTranslateY(y * (TILE_SIZE + 5) + TILE_SIZE / 4);

                shape = Shape.subtract(shape, circle);
            }
        }

        shape.setFill(Color.BLUE);

        return shape;

    }





    private List<Rectangle> makeColumns() {

        List<Rectangle> list = new ArrayList<>();


        for (int x = 0; x < COLS; x++) {
            Rectangle rect = new Rectangle(TILE_SIZE, (ROWS + 1) * TILE_SIZE);
            rect.setTranslateX(x * (TILE_SIZE + 5) + TILE_SIZE / 4);
            rect.setFill(Color.TRANSPARENT);

            final int col = x;
            rect.setOnMouseClicked(e -> placeDisc(new Disc(redMove), col));

            list.add(rect);
        }

        return list;
    }


    public static class Disc extends Circle {
        private boolean red;
        public Disc(boolean red) {
            super(TILE_SIZE/2, red? Color.RED: Color.YELLOW);
            this.red = red;

            setCenterX(TILE_SIZE / 2);
            setCenterY(TILE_SIZE / 2);

        }
    }
    private void placeDisc(Disc disc, int col) {

        int row = ROWS - 1;

        do {
            if (!getDisc(col, row).isPresent())
                break;

            row--;
        } while (row >= 0);

        if (row < 0) {
            return;
        }

        // logical
        lastRow = row;
        lastCol = col;
        grid[col][row] = disc;

        // visual
        discPtr = disc;
        discRoot.getChildren().add(disc);

        disc.setTranslateX(col * (TILE_SIZE + 5) + TILE_SIZE / 4);
        disc.setTranslateY(row * (TILE_SIZE + 5) + TILE_SIZE / 4);

        if (gameEnded(col, row)) {
            gameOver();
        }
        redMove = !redMove;


    }


    private boolean gameEnded(int col, int row ) {

        List<Point2D> vertical = IntStream.rangeClosed(row - 3, row + 3)
                .mapToObj(r -> new Point2D(col, r))
                .collect(Collectors.toList());

        List<Point2D> horizontal = IntStream.rangeClosed(col - 3, col + 3)
                .mapToObj(c -> new Point2D(c, row))
                .collect(Collectors.toList());

        Point2D topLeft = new Point2D(col - 3, row - 3);
        List<Point2D> diagonal1 = IntStream.rangeClosed(0, 6)
                .mapToObj(i -> topLeft.add(i, i))
                .collect(Collectors.toList());

        Point2D botLeft = new Point2D(col - 3, row + 3);
        List<Point2D> diagonal2 = IntStream.rangeClosed(0, 6)
                .mapToObj(i -> botLeft.add(i, -i))
                .collect(Collectors.toList());

        return checkRange(vertical) || checkRange(horizontal)
                || checkRange(diagonal1) || checkRange(diagonal2);

    }

    private boolean checkRange(List<Point2D> points) {
        int chain = 0;

        for (Point2D p : points) {
            int column = (int) p.getX();
            int row = (int) p.getY();

            Disc disc = getDisc(column, row).orElse(new Disc(!redMove));
            if (disc.red == redMove) {
                chain++;
                if (chain == 4) {
                    return true;
                }
            } else {
                chain = 0;
            }
        }

        return false;
    }



    private void gameOver() {
        // Game over message
        // (redMove ? "RED" :  "BLUE")
        // new game;

        whoWon = (redMove? "Player 1" : "Player 2");
        winner.setText("Winner: " + whoWon);


        if (redMove) {
            redWin += 1;

            redScore.setText("Player 1: " + redWin);
        }
        else {
            yelWin += 1;
            blueScore.setText("Player 2: " + yelWin);
        }

        System.out.println(grid);
        discRoot.getChildren().removeAll();
        discRoot = new Pane();
        grid = new Disc[ROWS][COLS];

        System.out.println(grid);


        start(new Stage());


    }

    private Optional<Disc> getDisc(int col, int row) {
        if (col < 0 || col >= COLS
                || row < 0 || row >= ROWS) {
            return Optional.empty();
        }

        return Optional.ofNullable(grid[col][row]);
    }



    private void undoFunc() {
        grid[lastCol][lastRow] = null;
        discRoot.getChildren().remove(discPtr);
    }

    private void newGame(Stage stage) {

        stage.close();

        discRoot.getChildren().removeAll();
        discRoot = new Pane();
        grid = new Disc[ROWS][COLS];
        start(new Stage());
    }

    private void saveGame(Stage stage) {
        FileChooser fil_chooser = new FileChooser();

        File file = fil_chooser.showSaveDialog(stage);

        if (file != null) {
            saveToFile(file);
        }


    }

    private void saveToFile(File file) {
        try {
            PrintWriter writer;
            writer = new PrintWriter(file);
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLS; j++) {

                    if (grid[i][j] != null) {
                        if (grid[i][j].red == true) {
                            writer.print(1);
                        } else {
                            writer.print(2);
                        }
                    } else {
                        writer.print(0);
                    }

                }
                writer.println();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadGame(Stage stage) {
        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showOpenDialog(stage);



        if (file != null) {
            fillBoard(file);
        }

    }

    private void fillBoard(File file) {
        try {
            FileReader f = new FileReader(file);
            BufferedReader br = new BufferedReader(f);
            String ch;
            List<String> tmp = new ArrayList<String>();
            int count = 0;

            do {
                    ch = br.readLine();
                    for (int i=ch.length()-1; i >= 0; i--) {
                        if (ch != null) {
                            if (ch.charAt(i) == '1') {
                                placeDisc(new Disc(true), count);
                            } else if (ch.charAt(i) == '2') {
                                placeDisc(new Disc(false), count);
                            } else {
                                continue;
                            }
                        }

                    }
                count++;
            } while (ch != null);

            System.out.println(tmp);
//            for(int i=tmp.size()-1;i>=0;i--) {
//                out.print(tmp.get(i)+"<br/>");
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    @Override
    public void start(Stage stage) {

        VBox vb = new VBox();
        vb.setPrefHeight((COLS+3)*TILE_SIZE);
        vb.setPrefWidth((COLS+3)*TILE_SIZE);

        /** MenuBar **/
        MenuBar menuBar = new MenuBar();


        Label label1 = new Label("Undo");
        label1.setOnMouseClicked(mouseEvent->{undoFunc();});
        Menu mUndo = new Menu("", label1);

        Label label2 = new Label("new Game");
        label2.setOnMouseClicked(mouseEvent -> {newGame(stage);});
        Menu mSave = new Menu("", label2);

        Label label3 = new Label("Save Game");
        label3.setOnMouseClicked(mouseEvent -> {saveGame(stage);});
        Menu mNew = new Menu("", label3);

        Label label4 = new Label("Load Game");
        label4.setOnMouseClicked(mouseEvent -> {loadGame(stage);});
        Menu mLoad = new Menu("", label4);

        menuBar.getMenus().addAll(mUndo, mSave, mNew, mLoad);
        vb.getChildren().add(menuBar);


        EventHandler<ActionEvent> e1 = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e)
            {
                placeDisc(new Disc(redMove), 0);
            }
        };

        EventHandler<ActionEvent> e2 = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e)
            {
                placeDisc(new Disc(redMove), 1);
            }
        };

        EventHandler<ActionEvent> e3 = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e)
            {
                placeDisc(new Disc(redMove), 2);
            }
        };

        EventHandler<ActionEvent> e4 = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e)
            {
                placeDisc(new Disc(redMove), 3);
            }
        };
        EventHandler<ActionEvent> e5 = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e)
            {
                placeDisc(new Disc(redMove), 4);
            }
        };

        EventHandler<ActionEvent> e6 = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e)
            {
                placeDisc(new Disc(redMove), 5);
            }
        };

        /** Buttons **/
        Button c1 = new Button("Column 1");
        c1.setOnAction(e1);
        Button c2 = new Button("Column 2");
        c2.setOnAction(e2);
        Button c3 = new Button("Column 3");
        c3.setOnAction(e3);
        Button c4 = new Button("Column 4");
        c4.setOnAction(e4);
        Button c5 = new Button("Column 5");
        c5.setOnAction(e5);
        Button c6 = new Button("Column 6");
        c6.setOnAction(e6);






        HBox hb = new HBox();
        hb.setPrefWidth(((ROWS+2)*TILE_SIZE));

        hb.getChildren().addAll(c1, c2, c3, c4, c5, c6);

        vb.getChildren().add(hb);

        var scoreBox = new HBox(8);
        scoreBox.getChildren().addAll(redScore, blueScore);

        vb.getChildren().add(createContent());
        vb.getChildren().add(winner);
        vb.getChildren().add(scoreBox);
        var scene = new Scene(vb, TILE_SIZE*8, TILE_SIZE*9);
        stage.setScene(scene);
        stage.setTitle("Lab4 - ConnectFour");


        stage.show();


    }

    public static void main(String[] args) {
        launch(args);
    }

}