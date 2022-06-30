package game2048;

import java.util.Formatter;
import java.util.Observable;


/** The state of a game of 2048.
 *  @author Zhimei Wang
 */
public class Model extends Observable {
    /** Current contents of the board. */
    private Board board;
    /** Current score. */
    private int score;
    /** Maximum score so far.  Updated when game ends. */
    private int maxScore;
    /** True iff game is ended. */
    private boolean gameOver;

    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to board.tile(c, r).  Be careful! It works like (x, y) coordinates.
     */

    /** Largest piece value. */
    public static final int MAX_PIECE = 2048;

    /** A new 2048 game on a board of size SIZE with no pieces
     *  and score 0. */
    public Model(int size) {
        board = new Board(size);
        score = maxScore = 0;
        gameOver = false;
    }

    /** A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (row, col) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes. */
    public Model(int[][] rawValues, int score, int maxScore, boolean gameOver) {
        int size = rawValues.length;
        board = new Board(rawValues, score);
        this.score = score;
        this.maxScore = maxScore;
        this.gameOver = gameOver;
    }

    /** Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     *  0 <= COL < size(). Returns null if there is no tile there.
     *  Used for testing. Should be deprecated and removed.
     *  */
    public Tile tile(int col, int row) {
        return board.tile(col, row);
    }

    /** Return the number of squares on one side of the board.
     *  Used for testing. Should be deprecated and removed. */
    public int size() {
        return board.size();
    }

    /** Return true iff the game is over (there are no moves, or
     *  there is a tile with value 2048 on the board). */
    public boolean gameOver() {
        checkGameOver();
        if (gameOver) {
            maxScore = Math.max(score, maxScore);
        }
        return gameOver;
    }

    /** Return the current score. */
    public int score() {
        return score;
    }

    /** Return the current maximum game score (updated at end of game). */
    public int maxScore() {
        return maxScore;
    }

    /** Clear the board to empty and reset the score. */
    public void clear() {
        score = 0;
        gameOver = false;
        board.clear();
        setChanged();
    }

    /** Add TILE to the board. There must be no Tile currently at the
     *  same position. */
    public void addTile(Tile tile) {
        board.addTile(tile);
        checkGameOver();
        setChanged();
    }

    /** Tilt the board toward SIDE. Return true iff this changes the board.
     *
     * 1. If two Tile objects are adjacent in the direction of motion and have
     *    the same value, they are merged into one Tile of twice the original
     *    value and that new value is added to the score instance variable
     * 2. A tile that is the result of a merge will not merge again on that
     *    tilt. So each move, every tile will only ever be part of at most one
     *    merge (perhaps zero).
     * 3. When three adjacent tiles in the direction of motion have the same
     *    value, then the leading two tiles in the direction of motion merge,
     *    and the trailing tile does not.
     * */
    public boolean tilt(Side side) {
        boolean changed;
        changed = false;

        // TODO: Modify this.board (and perhaps this.score) to account
        // for the tilt to the Side SIDE. If the board changed, set the
        // changed local variable to true.


        // check if the side is set to north or not, if no then shifts the view of the board such that the board behaves
        // as if the side is north

        board.setViewingPerspective(side);


        // iterate over each column and check if the tiles in the column can be moved, if so set changed to true
        for (int col = 0; col < board.size(); col++){

            if (checkVertMovable(col, board.size() - 1) == true) {
                changed = true;
            }

        }

        // after finished, change the view perspective back to north so that north is north, side is side (i.e. not north)
        board.setViewingPerspective(Side.NORTH);

        checkGameOver();
        if (changed) {
            setChanged();
        }
        return changed;
    }

    /**
     * helper method that look at the tile in a column and check if it can be moved vertically up
     * @param col is the column number of the target tile
     * @param row is the row number of the target tile
     * @return true if the tile can be moved vertically
     */
    private boolean checkVertMovable(int col, int row){
        boolean movable = false;

        // iterate over the row number below the current tile and see if there are empty space or tile that can be merged
        // only goes to the second last row (will not check last row because it will cause index out of bound for other
        // helper methods)
        for (int rowNum = row; rowNum > 0; rowNum--) {

            // Tile check_tile starts with the tile below the input tile
            Tile check_tile = board.tile(col, rowNum);

            // if check_tile is not null, check the tiles below it and see if there is one that has the same value as
            // check_tile thus movable (can be merged upwards with check_tile)
            if (check_tile != null) {

                // check if any tile below the current one is not null
                if (restHasVal(col,rowNum)){

                    // get the row number of the next non-null tile
                    int next_row = getNextValRow(col, rowNum);

                    // tile next_tile is the next non-null tile
                    // check if tile a has the same value as check_tile, if so merge them and change the score
                    Tile next_tile = board.tile(col, next_row);
                    if (checkVal(check_tile, next_tile)){
                        board.move(col, rowNum, next_tile);
                        movable = true;
                        score += (check_tile.value() * 2);
                    }
                }
            }
            // if check_tile is null, check if the rest of the row is empty or has two tiles that can be merged
            else{
                if (restHasVal(col,rowNum)){

                    // get the row number of the tile below check_tile that has value and store the tile called next_tile
                    int next_row = getNextValRow(col, rowNum);
                    Tile next_tile = board.tile(col, next_row);

                    // get the row number of the tile below next_tile that has a value
                    if (restHasVal(col, next_row)){
                        int below_next_now = getNextValRow(col, next_row);

                        // use the row number to get the tile that has value below next_tile and store this tile as below_next_tile
                        Tile below_next_tile = board.tile(col, below_next_now);

                        // check if the value of next_tile and below_next_tile are the same
                        // if same, move the two tiles into the check_tile position (because we know check_tile is null)
                        // these two tiles can be merged and update the score
                        if (checkVal(next_tile, below_next_tile)){
                            board.move(col, rowNum, next_tile);
                            board.move(col, rowNum, below_next_tile);
                            movable = true;
                            score += (next_tile.value() * 2);
                        }

                        // if the value of next_tile and below_next_tile are not the same, then move next_tile to
                        // the check_tile position (because check_tile is null)
                        else {
                            board.move(col,rowNum, next_tile);
                            movable = true;
                        }
                    }

                    // if check_tile is null, next_tile is not null, and below next_tile there is no tile that has value,
                    // move next_tile to the check_tile position and break out of the for loop
                    else {
                        board.move(col,rowNum, next_tile);
                        movable = true;
                        break;
                    }

                    // if check tile is null and tiles below check_tile are all null, break out of the for loop
                } else {
                    break;
                }

            }

        }
        return movable;
    }


    /**
     * helper method takes in the column and row number of a tile
     * and check whether in its column there is a tile that has value below the current tile
     * @param col is the column number of the tile
     * @param row is the row number of the tile
     * @return true if below the current tile, there is a tile that has value
     */
    private boolean restHasVal (int col, int row){

        boolean hasVal = false;

        // column is fixed, just iterate on the now number and see
        for (int rowNum = row - 1; rowNum >= 0; rowNum--){
            if (board.tile(col, rowNum) != null){
                hasVal = true;
                break;
            }
        }
        return hasVal;
    }

    /**
     * helper mehods
     * @param col
     * @param row
     * @return
     */
    private int getNextValRow (int col, int row){

        int rowNum = row - 1;

        while (rowNum > 0){
            if (board.tile(col,rowNum) != null){
                break;
            }
            rowNum --;
        }

        return rowNum;

    }

    /**
     * helper method see if the tiles have the same value
     * @param a is one tile
     * @param b is the other tile
     * @return true if tile a and tile b has the same value
     */
    private boolean checkVal (Tile a, Tile b){
        return a.value() == b.value();
    }


    private boolean mergeForCol(int col) {
        boolean change = false;
        int l = board.size() - 1;
        int r = board.size() - 2;
        Tile first = board.tile(col, l);

        while(l >= 0 && r >= 0) {

            if(first == null) {
                first = board.tile(col, r);
            } else {
                Tile second = board.tile(col, r);
                if(second != null) {
                    if(checkVal(first, second)) {
                        score += first.value() * 2;
                        board.move(col, l, first);
                        board.move(col, l, second);
                        l--;
                        first = board.tile(col, l);
                        change = true;
                    } else {
                        // shift first
                        first = second;
                    }
                }
            }
            r--;
        }
        if(first != null) {
            board.move(col, l, first);
            change = true;
        }

        return change;

    }




    /** Checks if the game is over and sets the gameOver variable
     *  appropriately.
     */
    private void checkGameOver() {
        gameOver = checkGameOver(board);
    }

    /** Determine whether game is over. */
    private static boolean checkGameOver(Board b) {
        return maxTileExists(b) || !atLeastOneMoveExists(b);
    }

    /** Returns true if at least one space on the Board is empty.
     *  Empty spaces are stored as null.
     * */
    public static boolean emptySpaceExists(Board b) {

        // assume the board is full initially and set empty as false
        boolean empty = false;

        // check if there is a tile that is null, if so change empty to true and break out of the for loop
        for (Tile tile : b){
            if (tile == null){
                empty = true;
                break;
            }
        }

        return empty;
    }

    /**
     * Returns true if any tile is equal to the maximum valid value.
     * Maximum valid value is given by MAX_PIECE. Note that
     * given a Tile object t, we get its value with t.value().
     */
    public static boolean maxTileExists(Board b) {

        // assume the board does not have the tile with the max value (i.e. 2048) initially and set max as false
        boolean max = false;

        // iterate over each tile on board and check whether the tile's value is 2048, if so set max to true
        // to avoid null pointer exception check whether the tile is null first
        for (Tile tile : b){
            if (tile == null){
                continue;
            } else  if (tile.value() == MAX_PIECE){
                max = true;
                break;
            }
        }
        return max;
    }

    /**
     * Returns true if there are any valid moves on the board.
     * There are two ways that there can be valid moves:
     * 1. There is at least one empty space on the board.
     * 2. There are two adjacent tiles with the same value.
     */
    public static boolean atLeastOneMoveExists(Board b) {

        // assume no tile can be moved
        boolean move = false;

        // if there is empty space on the board, the tile can be moved
        if (emptySpaceExists(b) == true){
            move = true;
        }

        else {
            // iterate on each tile, and if the current has the same value as its up adjacent tiles, change move to true and break the for loop
            for (int col = 0; col <= b.size() - 1; col++){
                for (int row = 0; row < b.size() - 1; row++){
                    boolean currentAndUp = b.tile(col, row).value() == b.tile(col, row + 1).value();
                    if (currentAndUp == true){
                        move = true;
                        break;
                    }
                }
                if (move == true) break;
            }
            // after running the current and adjacent up scenario, if move is true, don't need to run the current and adjacent right scenario
            // however if the current and adjacent up does not have the same value (thus not up movable)
            // proceed to check if current and its right adjacent tile has the same value
            if (move == false){
                for (int row = 0; row <= b.size() - 1; row++){
                    for (int col = 0; col < b.size() - 1; col++){
                        boolean currentAndRight = b.tile(col, row).value() == b.tile(col + 1, row).value();
                        if (currentAndRight == true){
                            move = true;
                            break;
                        }
                    }
                    if (move == true) break;
                }
            }

        }
        return move;
    }


    @Override
     /** Returns the model as a string, used for debugging. */
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    @Override
    /** Returns whether two models are equal. */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    @Override
    /** Returns hash code of Model’s string. */
    public int hashCode() {
        return toString().hashCode();
    }
}
