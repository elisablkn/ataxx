/* Skeleton code copyright (C) 2008, 2022 Paul N. Hilfinger and the
 * Regents of the University of California.  Do not distribute this or any
 * derivative work without permission. */

package ataxx;

import java.util.ArrayList;
import java.util.Random;

import static ataxx.PieceColor.*;
import static java.lang.Math.min;
import static java.lang.Math.max;

/** A Player that computes its own moves.
 *  @author Elizaveta Belkina
 */
class AI extends Player {

    /** Maximum minimax search depth before going to static evaluation. */
    private static final int MAX_DEPTH = 4;
    /** A position magnitude indicating a win (for red if positive, blue
     *  if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI for GAME that will play MYCOLOR. SEED is used to initialize
     *  a random-number generator for use in move computations.  Identical
     *  seeds produce identical behaviour. */
    AI(Game game, PieceColor myColor, long seed) {
        super(game, myColor);
        _random = new Random(seed);
    }

    @Override
    boolean isAuto() {
        return true;
    }

    @Override
    String getMove() {
        if (!getBoard().canMove(myColor())) {
            game().reportMove(Move.pass(), myColor());
            return "-";
        }
        Main.startTiming();
        Move move = findMove();
        Main.endTiming();
        game().reportMove(move, myColor());
        return move.toString();
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(getBoard());
        _lastFoundMove = null;
        if (myColor() == RED) {
            minMax(b, MAX_DEPTH, true, 1, -INFTY, INFTY);
        } else {
            minMax(b, MAX_DEPTH, true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to the findMove method
     *  above. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove. */
    private int minMax(Board board, int depth, boolean saveMove, int sense,
                       int alpha, int beta) {
        /* We use WINNING_VALUE + depth as the winning value so as to favor
         * wins that happen sooner rather than later (depth is larger the
         * fewer moves have been made. */
        if (depth == 0 || board.getWinner() != null) {
            return staticScore(board, WINNING_VALUE + depth);
        }
        Move best;
        best = null;
        int bestScore;
        bestScore = 0;
        int maxEval;
        int eval;
        int minEval;
        ArrayList<Move> allMoves = new ArrayList<>();
        getAllMoves(board, allMoves);
        if (allMoves.isEmpty()) {
            allMoves.add(Move.PASS);
        }
        if (sense == 1) {
            maxEval = -INFTY;
            for (int i = 0; i < allMoves.size(); i++) {
                Board test = new Board(board);
                test.makeMove(allMoves.get(i));
                eval = minMax(test, depth - 1, false, -1, alpha, beta);
                if (eval > maxEval) {
                    maxEval = eval;
                    best = allMoves.get(i);
                }
                alpha = max(alpha, eval);
                if (alpha >= beta) {
                    break;
                }
                bestScore = maxEval;
            }
        } else if (sense == -1) {
            minEval = INFTY;
            for (int i = 0; i < allMoves.size(); i++) {
                Board test = new Board(board);
                test.makeMove(allMoves.get(i));
                eval = minMax(test, depth - 1, false, 1, alpha, beta);
                if (eval < minEval) {
                    minEval = eval;
                    best = allMoves.get(i);
                }
                beta = min(beta, minEval);
                if (beta <= alpha) {
                    break;
                }
                bestScore = minEval;
            }
        }
        if (saveMove) {
            _lastFoundMove = best;
        }
        return bestScore;
    }

    /** Return a heuristic value for BOARD.  This value is +- WINNINGVALUE in
     *  won positions, and 0 for ties. */
    private int staticScore(Board board, int winningValue) {
        PieceColor winner = board.getWinner();
        if (winner != null) {
            return switch (winner) {
            case RED -> winningValue;
            case BLUE -> -winningValue;
            default -> 0;
            };
        }

        return board.redPieces() - board.bluePieces();
    }

    void getAllMoves(Board board, ArrayList<Move> moves) {
        for (int i = 0; i < COL.length; i++) {
            for (int j = 0; j < ROW.length; j++) {
                if (board.whoseMove() == board.get(COL[i], ROW[j])) {
                    for (int row = -2; row <= 2; row++) {
                        for (int col = -2; col <= 2; col++) {
                            if (board.get(board.neighbor(Board.index(COL[i],
                                            ROW[j]), col, row)) == EMPTY) {
                                char col0 = COL[i];
                                char row0 = ROW[j];
                                char col1 = (char) (COL[i] + col);
                                char row1 = (char) (ROW[j] + row);
                                if (col1 >= 'a' && col1 <= 'g'
                                        && row1 >= '1' && row1 <= '7') {
                                    moves.add(Move.
                                            move(col0, row0, col1, row1));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /** Pseudo-random number generator for move computation. */
    private Random _random = new Random();

    /** All possible characters for columns. */
    private static final char[] COL = {'a', 'b', 'c', 'd', 'e', 'f', 'g'};

    /** All possible characters for rows. */
    private static final char[] ROW = {'1', '2', '3', '4', '5', '6', '7'};
}
