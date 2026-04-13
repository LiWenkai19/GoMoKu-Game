module org.example.gomoku_game {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens org.example.gomoku_game to javafx.fxml;
    exports org.example.gomoku_game;
}