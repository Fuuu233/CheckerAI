import java.util.List;
import java.util.Vector;


/**
 * A state of the checker game is characterized by a board containing
 * symbols X and O, the next player to move
 * 
 * @author Shang Da
 * 
 */
public class CheckerState implements Cloneable {
    public static final int
		       EMPTY = 0,
		       RED = 1,
		       RED_KING = 2,
		       BLACK = 3,
		       BLACK_KING = 4;
	//
	public int[][] board;
	private int playerToMove = RED;
	private int aiPlayer;
	private boolean keepJump = false;
	public CheckerState() {
        // Constructor.  Create the board and set it up for a new game.
	     board = new int[8][8];
	     aiPlayer = BLACK;//black default
	     setUpGame();
    }
	public CheckerState(int aiPlayer) {
        // Constructor.  Create the board and set it up for a new game.
	     board = new int[8][8];
	     this.aiPlayer = aiPlayer;//black default
	     setUpGame();
    }
	public CheckerState(int [][]board, int playerToMove, boolean keepJump, int aiPlayer) {
        // Constructor.  Create the board and set it up for a new game.
	     this.board = board;
	     this.playerToMove = playerToMove;
	     this.keepJump = keepJump;
	     this.aiPlayer = aiPlayer;
    }
	public void setAI(int aiPlayer) {
		this.aiPlayer = aiPlayer;
	}
	public void setUpGame() {
        // Set up the board with checkers in position for the beginning
        // of a game.  Note that checkers can only be found in squares
        // that satisfy  row % 2 == col % 2.  At the start of the game,
        // all such squares in the first three rows contain black squares
        // and all such squares in the last three rows contain red squares.
	    for (int row = 0; row < 8; row++) {
	       for (int col = 0; col < 8; col++) {
	          if ( row % 2 == col % 2 ) {
	             if (row < 3)
	                board[row][col] = BLACK;
	             else if (row > 4)
	                board[row][col] = RED;
	             else
	                board[row][col] = EMPTY;
	          }
	          else {
	             board[row][col] = EMPTY;
	          }
	       }
	    }
	 }  // end setUpGame()
      
	public int getPlayerToMove() {
		return playerToMove;
	}

	public double getUtility() {
		double heuristic = 0;
		double diff_men = 0;
		double diff_kings = 0;
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				switch(board[i][j]) {
					case RED:
						diff_men--;
						break;
					case RED_KING:
						diff_kings--;
						break;
					case BLACK:
						diff_men++;
						break;
					case BLACK_KING:	
						diff_kings++;
						break;
				}
			}
		}
		heuristic = (double)(aiPlayer - 2) * (diff_men + 2 * diff_kings);
		return heuristic;
	}
	
	public boolean canJump() {//if it can jump then must jump
		return getLegalMoves()[0].isJump();
	}
	
	public boolean keepJump() {//if has jump after a jump ,then continue jumping
		return keepJump;
	}

	public int pieceAt(int row, int col) {
		return board[row][col];
	}
	
	public List<CheckersMove> getLegalActions(){
		List<CheckersMove> retLst = new Vector<CheckersMove>();
		CheckersMove[] legalMoves = getLegalMoves();
		if(legalMoves==null) return null;
		for(int i = 0; i < legalMoves.length; i++ ) {
			retLst.add(legalMoves[i]);
		}
		return retLst;
	}
	

    public void makeMove(CheckersMove move) {
          // Make the specified move.  It is assumed that move
          // is non-null and that the move it represents is legal.
       makeMove(move.fromRow, move.fromCol, move.toRow, move.toCol);
       if(move.isJump()) {//If this move is jump, and if it can keep jumping, we maintain current player
    	   CheckersMove[] legalJumps = getLegalJumps(move.toRow,move.toCol);
    	   if(legalJumps !=null) {
    		   playerToMove = (playerToMove == RED)? RED:BLACK;
    		   keepJump = true;
    		   return;
    	   }
       }
       playerToMove = (playerToMove == RED)? BLACK:RED;
       keepJump = false;
    }
    
    public void makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        // Make the move from (fromRow,fromCol) to (toRow,toCol).  It is
        // assumed that this move is legal.  If the move is a jump, the
        // jumped piece is removed from the board.  If a piece moves
        // the last row on the opponent's side of the board, the 
        // piece becomes a king.
	     board[toRow][toCol] = board[fromRow][fromCol];
	     board[fromRow][fromCol] = EMPTY;
	     if (fromRow - toRow == 2 || fromRow - toRow == -2) {
	           // The move is a jump.  Remove the jumped piece from the board.
	        int jumpRow = (fromRow + toRow) / 2;  // Row of the jumped piece.
	        int jumpCol = (fromCol + toCol) / 2;  // Column of the jumped piece.
	        board[jumpRow][jumpCol] = EMPTY;
	     }
	     if (toRow == 0 && board[toRow][toCol] == RED)
	        board[toRow][toCol] = RED_KING;
	     if (toRow == 7 && board[toRow][toCol] == BLACK)
	        board[toRow][toCol] = BLACK_KING;
	  }
	  
	
	  public CheckersMove[] getLegalMoves() {
	         // Return an array containing all the legal CheckersMoves
	         // for the specfied player on the current board.  If the player
	         // has no legal moves, null is returned.  The value of player
	         // should be one of the constants RED or BLACK; if not, null
	         // is returned.  If the returned value is non-null, it consists
	         // entirely of jump moves or entirely of regular moves, since
	         // if the player can jump, only jumps are legal moves.
	
	     if (playerToMove != RED && playerToMove != BLACK)
	        return null;
	
	     int playerKing;  // The constant representing a King belonging to player.
	     if (playerToMove == RED)
	        playerKing = RED_KING;
	     else
	        playerKing = BLACK_KING;
	
	     Vector<CheckersMove> moves = new Vector<CheckersMove>();  // Moves will be stored in this vector.
	     
	     /*  First, check for any possible jumps.  Look at each square on the board.
	         If that square contains one of the player's pieces, look at a possible
	         jump in each of the four directions from that square.  If there is 
	         a legal jump in that direction, put it in the moves vector.
	     */
	
	     for (int row = 0; row < 8; row++) {
	        for (int col = 0; col < 8; col++) {
	           if (board[row][col] == playerToMove || board[row][col] == playerKing) {
	              if (canJump(playerToMove, row, col, row+1, col+1, row+2, col+2))
	                 moves.addElement(new CheckersMove(row, col, row+2, col+2));
	              if (canJump(playerToMove, row, col, row-1, col+1, row-2, col+2))
	                 moves.addElement(new CheckersMove(row, col, row-2, col+2));
	              if (canJump(playerToMove, row, col, row+1, col-1, row+2, col-2))
	                 moves.addElement(new CheckersMove(row, col, row+2, col-2));
	              if (canJump(playerToMove, row, col, row-1, col-1, row-2, col-2))
	                 moves.addElement(new CheckersMove(row, col, row-2, col-2));
	           }
	        }
	     }
	     
	     /*  If any jump moves were found, then the user must jump, so we don't 
	         add any regular moves.  However, if no jumps were found, check for
	         any legal regualar moves.  Look at each square on the board.
	         If that square contains one of the player's pieces, look at a possible
	         move in each of the four directions from that square.  If there is 
	         a legal move in that direction, put it in the moves vector.
	     */
	     
	     if (moves.size() == 0) {
	        for (int row = 0; row < 8; row++) {
	           for (int col = 0; col < 8; col++) {
	              if (board[row][col] == playerToMove || board[row][col] == playerKing) {
	                 if (canMove(playerToMove,row,col,row+1,col+1))
	                    moves.addElement(new CheckersMove(row,col,row+1,col+1));
	                 if (canMove(playerToMove,row,col,row-1,col+1))
	                    moves.addElement(new CheckersMove(row,col,row-1,col+1));
	                 if (canMove(playerToMove,row,col,row+1,col-1))
	                    moves.addElement(new CheckersMove(row,col,row+1,col-1));
	                 if (canMove(playerToMove,row,col,row-1,col-1))
	                    moves.addElement(new CheckersMove(row,col,row-1,col-1));
	              }
	           }
	        }
	     }
	     
	     /* If no legal moves have been found, return null.  Otherwise, create
	        an array just big enough to hold all the legal moves, copy the
	        legal moves from the vector into the array, and return the array. */
	     
	     if (moves.size() == 0)
	        return null;
	     else {
	        CheckersMove[] moveArray = new CheckersMove[moves.size()];
	        for (int i = 0; i < moves.size(); i++)
	           moveArray[i] = (CheckersMove)moves.elementAt(i);
	        return moveArray;
	     }
	
	  }  // end getLegalMoves
	  
	
	  public CheckersMove[] getLegalJumps(int row, int col) {
	        // Return a list of the legal jumps that the specified player can
	        // make starting from the specified row and column.  If no such
	        // jumps are possible, null is returned.  The logic is similar
	        // to the logic of the getLegalMoves() method.
	     if (playerToMove != RED && playerToMove != BLACK)
	        return null;
	     int playerKing;  // The constant representing a King belonging to player.
	     if (playerToMove == RED)
	        playerKing = RED_KING;
	     else
	        playerKing = BLACK_KING;
	     Vector<CheckersMove> moves = new Vector<CheckersMove>();  // The legal jumps will be stored in this vector.
	     if (board[row][col] == playerToMove || board[row][col] == playerKing) {
	        if (canJump(playerToMove, row, col, row+1, col+1, row+2, col+2))
	           moves.addElement(new CheckersMove(row, col, row+2, col+2));
	        if (canJump(playerToMove, row, col, row-1, col+1, row-2, col+2))
	           moves.addElement(new CheckersMove(row, col, row-2, col+2));
	        if (canJump(playerToMove, row, col, row+1, col-1, row+2, col-2))
	           moves.addElement(new CheckersMove(row, col, row+2, col-2));
	        if (canJump(playerToMove, row, col, row-1, col-1, row-2, col-2))
	           moves.addElement(new CheckersMove(row, col, row-2, col-2));
	     }
	     if (moves.size() == 0)
	        return null;
	     else {
	        CheckersMove[] moveArray = new CheckersMove[moves.size()];
	        for (int i = 0; i < moves.size(); i++)
	           moveArray[i] = (CheckersMove)moves.elementAt(i);
	        return moveArray;
	     }
	  }  // end getLegalMovesFrom()
	  
	
	  private boolean canJump(int player, int r1, int c1, int r2, int c2, int r3, int c3) {
	          // This is called by the two previous methods to check whether the
	          // player can legally jump from (r1,c1) to (r3,c3).  It is assumed
	          // that the player has a piece at (r1,c1), that (r3,c3) is a position
	          // that is 2 rows and 2 columns distant from (r1,c1) and that 
	          // (r2,c2) is the square between (r1,c1) and (r3,c3).
	          
	     if (r3 < 0 || r3 >= 8 || c3 < 0 || c3 >= 8)
	        return false;  // (r3,c3) is off the board.
	        
	     if (board[r3][c3] != EMPTY)
	        return false;  // (r3,c3) already contains a piece.
	        
	     if (player == RED) {
	        if (board[r1][c1] == RED && r3 > r1)
	           return false;  // Regular red piece can only move  up.
	        if (board[r2][c2] != BLACK && board[r2][c2] != BLACK_KING)
	           return false;  // There is no black piece to jump.
	        return true;  // The jump is legal.
	     }
	     else {
	        if (board[r1][c1] == BLACK && r3 < r1)
	           return false;  // Regular black piece can only move downn.
	        if (board[r2][c2] != RED && board[r2][c2] != RED_KING)
	           return false;  // There is no red piece to jump.
	        return true;  // The jump is legal.
	     }
	
	  }  // end canJump()
	  
	
	  private boolean canMove(int player, int r1, int c1, int r2, int c2) {
	        // This is called by the getLegalMoves() method to determine whether
	        // the player can legally move from (r1,c1) to (r2,c2).  It is
	        // assumed that (r1,r2) contains one of the player's pieces and
	        // that (r2,c2) is a neighboring square.
	        
	     if (r2 < 0 || r2 >= 8 || c2 < 0 || c2 >= 8)
	        return false;  // (r2,c2) is off the board.
	        
	     if (board[r2][c2] != EMPTY)
	        return false;  // (r2,c2) already contains a piece.
	
	     if (player == RED) {
	        if (board[r1][c1] == RED && r2 > r1)
	            return false;  // Regualr red piece can only move down.
	         return true;  // The move is legal.
	     }
	     else {
	        if (board[r1][c1] == BLACK && r2 < r1)
	            return false;  // Regular black piece can only move up.
	         return true;  // The move is legal.
	     }
	     
	  }  // end canMove()
  

    public void setPieceAt(int row, int col, int piece) {
           // Set the contents of the square in the specified row and column.
		   // piece must be one of the constants EMPTY, RED, BLACK, RED_KING,
		   // BLACK_KING.
        board[row][col] = piece;
    }
    
    public boolean isEmpty(int row, int col) {
    	return board[row][col] == EMPTY;
    }

	public int getNumberOfMarkedPositions() {
		int retVal = 0;
		
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				if (!(isEmpty(row, col))) {
					retVal++;
				}
			}
		}
		return retVal;
	}
	
	@Override
	public CheckerState clone() {
		int[][] newBoard = new int[8][8];
		for(int i = 0; i <8; i++) {
			for(int j = 0; j <8; j++) {
				newBoard[i][j] = board[i][j];
			}
		}
		int newPlayerToMove = playerToMove;
		boolean newKeepJump = keepJump;
		CheckerState copy = new CheckerState(newBoard, newPlayerToMove, newKeepJump, aiPlayer);
		return copy;
	}
	
	@Override
	public int hashCode() {
		// Need to ensure equal objects have equivalent hashcodes (Issue 77).
		return toString().hashCode();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				builder.append(pieceAt(row, col)).append(" ");
			}
			builder.append("\n");
		}
		return builder.toString();
	}

}

