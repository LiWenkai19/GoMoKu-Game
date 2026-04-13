package org.example.gomoku_game;

import java.util.Scanner;
import java.util.Stack;

class GomokuGame {
    private int[][] board;          // 0: empty, 1: player1's stone, 2: player2's stone
    private int currentPlayer;      // 1: player1, 2: player2
    private boolean gameOver;       // true: game over, false: game not over
    private int winner;             // 0: no winner, 1: player 1 wins, 2: player 2 wins

    private int boardSize;              // size of the board

    private Scanner scanner;
    // scanner for human input

    // Stack to store move history
    private Stack<int[]> moveHistory;
    // Stack to store redo history
    private Stack<int[]> redoHistory;

    // Get black and white steps
    private int blackSteps=0;
    private int whiteSteps=0;

    public GomokuGame(int boardSize) {
        if (boardSize < 5 || boardSize > 22) {
            throw new IllegalArgumentException("Board size should be between 5 and 20.");
        }
        this.boardSize = boardSize;
        board = new int[boardSize][boardSize];            // init to be all zeros
        currentPlayer = 1;
        gameOver = false;
        winner = 0;
        scanner = new Scanner(System.in);
        moveHistory=new Stack<>();
        redoHistory=new Stack<>();
    }

    public GomokuGame() {
        this(20);
    }

    // Check if the current player has won
    public boolean checkWin(int x, int y) {
        int[][][] directionLines = {{{0, 1}, {0, -1}},                // vertical
                {{1, 0}, {-1, 0}},                // horizontal
                {{1, 1}, {-1, -1}},               // diagonal
                {{1, -1}, {-1, 1}}};              // anti-diagonal
        for (int[][] oppositeDirs : directionLines) {
            int count = 1;
            for (int[] direction: oppositeDirs) {
                int dx = direction[0];
                int dy = direction[1];
                for (int i = 1; i < 5; i++) {
                    int newX = x + i * dx;
                    int newY = y + i * dy;
                    if (!isValidPosition(newX, newY) || board[newX][newY] != board[x][y]) {
                        break;
                    }
                    count++;
                    if (count >= 5) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public boolean move(int x, int y) {
        // place a piece at (x, y) for the current player, and then switch to the other player
        if (gameOver) {
            return false;
        }

        if (!isValidPosition(x, y)) {
            return false;
        }

        if(x>=boardSize-1 || y>=boardSize-1){
            return false;
        }

        if (board[x][y] != 0) {
            return false;
        }
        
        board[x][y] = currentPlayer;
        moveHistory.push(new int[]{x,y,currentPlayer});
        redoHistory.clear();

        if (checkWin(x, y)) {
            gameOver = true;
            winner = currentPlayer;
        }

        // Count the step of each player
        if(currentPlayer==1){
            blackSteps++;
        }else{
            whiteSteps++;
        }

        currentPlayer = currentPlayer == 1 ? 2 : 1;      // switch player
        return true;
    }

    // Undo the last move
    public boolean undo(){
        if(moveHistory.isEmpty()){
            return false;
        }
        int[] lastMove=moveHistory.pop();
        int x=lastMove[0];
        int y=lastMove[1];
        int player=lastMove[2];

        board[x][y]=0;
        redoHistory.push(new int[]{x,y,player});
        currentPlayer=player;

        if(gameOver && winner==player){
            gameOver=false;
            winner=0;
        }

        // Recalculate the steps and max length
        recalculate();
        return true;
    }

    // Redo the last undo
    public boolean redo(){
        if(redoHistory.isEmpty()){
            return false;
        }
        int[] nextMove=redoHistory.pop();
        int x=nextMove[0];
        int y=nextMove[1];
        int player=nextMove[2];

        board[x][y]=player;
        moveHistory.push(new int[]{x,y,player});
        currentPlayer=player==1?2:1;

        if(checkWin(x,y)){
            gameOver=true;
            winner=player;
        }

        // Recalculate the steps and max length
        recalculate();
        return true;
    }

    // Check if the position is valid
    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < boardSize && y >= 0 && y < boardSize;
    }
    // Check if the game is over
    public boolean isGameOver() {
        return gameOver;
    }
    // Get the winner
    public int getWinner() {
        return winner;
    }
    // Get the board
    public int[][] getBoard() {
        return board;
    }
    // Get current player
    public int getCurrentPlayer(){
        return currentPlayer;
    }
    // Set current player
    public void setCurrentPlayer(int player){
        this.currentPlayer=player;
    }

    public void render() {                      // render the board, console version now
        System.out.println();
        // print the separation line
        System.out.println("---".repeat(boardSize+1));       // 1 + boardSize, 1 for the first column (preserved for row num)
        // print the column number, 1-start
        StringBuilder sb = new StringBuilder();
        sb.append("   ");
        for (int i = 0; i < boardSize; i++) {
            String prefix = " ";
            String suffix = (i < 9 | i == boardSize - 1) ? " " : "";
            sb.append(prefix).append(i + 1).append(suffix);
        }
        System.out.println(sb.toString());
        for (int i = 0; i < boardSize; i++) {
            // print the row number
            int rowNum = i + 1;
            System.out.print(rowNum + (rowNum < 10 ? "  ":" "));
            for (int j = 0; j < boardSize; j++) {
                if (board[i][j] == 0) {
                    System.out.print(" + ");    // empty
                } else if (board[i][j] == 1) {
                    System.out.print(" X ");    // player1
                } else {
                    System.out.print(" O ");    // player2
                }
            }
            System.out.println();
        }
    }

    public int[] getHumanInput() { // get the human input, console version now
        int[] input = new int[2];
        System.out.println();   // print a new line for better appearance
        System.out.println("Player " + currentPlayer + "'s turn.");
        System.out.println("Please input the row number and column number, separated by space:");
        input[0] = scanner.nextInt();
        input[1] = scanner.nextInt();
        // `scanner.close()` will also close `System.in`, so don't close it here
        return input;
    }

    public void play() {
        while (!gameOver) {
            render();
            int[] input = getHumanInput();
            boolean moveSuccess = move(input[0]-1, input[1]-1);
            if (!moveSuccess) {
                System.out.println("Invalid move! Please try again.");
            }
        }
        render();
        System.out.println("Game over! The winner is player " + winner + "!");
        scanner.close();
    }

    // Get the number of steps for black
    public int getBlackSteps(){
        return blackSteps;
    }

    // Get the number of steps for white
    public int getWhiteSteps(){
        return whiteSteps;
    }

    // Get the maximum length of all unbroken row for each player
    public int getMaxLength(int player){
        int maxLength=0;
        for(int i=0;i<boardSize;i++){
            for(int j=0;j<boardSize;j++){
                if(board[i][j]==player){
                    maxLength=Math.max(maxLength,checkMaxLength(i,j,player));
                }
            }
        }
        return maxLength;
    }

    // Check the maximum length of all unbroken row for each player
    private int checkMaxLength(int x,int y,int player){
        int maxLength=1;
        int[][][] directionLines={{{0,1},{0,-1}},{{1,0},{-1,0}},{{1,1},{-1,-1}},{{1,-1},{-1,1}}};
        for(int[][] oppositeDirections:directionLines){
            int count=1;
            for(int[] direction:oppositeDirections){
                int dx=direction[0];
                int dy=direction[1];
                for(int i=1;i<5;i++){
                    int newX=x+i*dx;
                    int newY=y+i*dy;
                    if(!isValidPosition(newX,newY) || board[newX][newY]!=player){
                        break;
                    }
                    count++;
                }
            }
            maxLength=Math.max(maxLength,count);
        }
        return maxLength;
    }

    // Recalculate the steps and max length
    private void recalculate(){
        blackSteps=0;
        whiteSteps=0;
        for(int i=0;i<boardSize;i++){
            for(int j=0;j<boardSize;j++){
                if(board[i][j]==1){
                    blackSteps++;
                }else if(board[i][j]==2){
                    whiteSteps++;
                }
            }
        }
    }

}