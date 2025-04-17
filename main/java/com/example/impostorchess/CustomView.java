package com.example.impostorchess;



import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Random;


public class CustomView extends View {

    enum Piece {
        WHITE_PAWN,
        WHITE_ROOK,
        WHITE_KNIGHT_L,
        WHITE_KNIGHT_R,
        WHITE_BISHOP,
        WHITE_KING,
        WHITE_QUEEN,
        BLACK_PAWN,
        BLACK_ROOK,
        BLACK_KNIGHT_L,
        BLACK_KNIGHT_R,
        BLACK_BISHOP,
        BLACK_KING,
        BLACK_QUEEN,
        NONE
    }

    public String text;
    private ArrayList<Bitmap> pieceImages = new ArrayList<>();
    public boolean player = true;
    private boolean currPlayer = true, modifiable = true, canStart = false;
    private Paint paint;
    private Bitmap board;
    final private int X1 = 2, Y1 = 1, Y2 = 3, X2 = 4, XYNUM = 5, NUM = 8, INC = 1, TOTAL = NUM * NUM, LAST = NUM - 1, DIV = 2, SCALE = 10, PWD = 7, PN = 14, SETB = 2, PLAYER0 = 6, ALL_PIECES = DIV * NUM, DIFF_PIECES = TOTAL - ALL_PIECES, REV = NUM / DIV;
    private int squareSize, gridX, gridY, boardX, boardY, startX, startY;
    private Piece[][] visibleBoard = {
            {Piece.WHITE_ROOK, Piece.WHITE_KNIGHT_L, Piece.WHITE_BISHOP, Piece.WHITE_KING, Piece.WHITE_QUEEN, Piece.WHITE_BISHOP, Piece.WHITE_KNIGHT_R, Piece.WHITE_ROOK},
            {Piece.WHITE_PAWN, Piece.WHITE_PAWN, Piece.WHITE_PAWN, Piece.WHITE_PAWN, Piece.WHITE_PAWN, Piece.WHITE_PAWN, Piece.WHITE_PAWN, Piece.WHITE_PAWN},
            {Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE},
            {Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE},
            {Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE},
            {Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE, Piece.NONE},
            {Piece.BLACK_PAWN, Piece.BLACK_PAWN, Piece.BLACK_PAWN, Piece.BLACK_PAWN, Piece.BLACK_PAWN, Piece.BLACK_PAWN, Piece.BLACK_PAWN, Piece.BLACK_PAWN},
            {Piece.BLACK_ROOK, Piece.BLACK_KNIGHT_L, Piece.BLACK_BISHOP, Piece.BLACK_KING, Piece.BLACK_QUEEN, Piece.BLACK_BISHOP, Piece.BLACK_KNIGHT_R, Piece.BLACK_ROOK},
    }, actualBoard = new Piece[NUM][NUM];
    final float MX = 1.5F, MY = 1.1F, MOD = 1.2F, BOARD_MOD = 0.7F;


    public void changePlayer() {
        for (int i = 0; i < REV; i++) {
            for (int j = 0; j < NUM; j++) {
                swapPiecesHelper(i, j, LAST - i, LAST - j);
            }
        }
    }

    private void addEvent(DatabaseReference databaseReference) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String[] vl = dataSnapshot.getValue(String.class).split("-");
                    if (vl.length != XYNUM) {
                        return;
                    }
                    int x1 = Integer.parseInt(vl[X1]), x2 = Integer.parseInt(vl[X2]), y1 = Integer.parseInt(vl[Y1]), y2 = Integer.parseInt(vl[Y2]);
                    updateBoards(y1, x1, y2, x2);
                    currPlayer = !currPlayer;
                    databaseReference.removeEventListener(this);
                }
            }

            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private String translatePieces() {
        String res = String.valueOf(player);
        for (int i = 0; i < NUM; i++) {
            for (int j = 0; j < NUM; j++) {
                res += "-" + visibleBoard[i][j].ordinal();
            }
        }
        return res;
    }

    private void setActualBoard(String val) {
        for (int i = 0; i < PLAYER0; i++) {
            for (int j = 0; j < NUM; j++) {
                actualBoard[i][j] = visibleBoard[i][j];
            }
        }
        String[] v = val.split("-");
        for (int i = 0; i < SETB; i++) {
            for (int j = 0; j < NUM; j++) {
                actualBoard[LAST - i][LAST - j] = Piece.values()[Integer.parseInt(v[NUM * i + j + INC])];
            }
        }
        canStart = true;
    }

    public void setBoard() {
        modifiable = false;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(text);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String val = dataSnapshot.getValue(String.class), t = translatePieces();
                    if (!val.equals("created")) {
                        setActualBoard(val);
                    }
                    databaseReference.setValue(t);
                    if (!player) {
                        addEvent(databaseReference);
                    }
                    databaseReference.removeEventListener(this);
                }
            }

            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void swapPiecesHelper(int i1, int j1, int i2, int j2) {
        Piece tmp = visibleBoard[i1][j1];
        visibleBoard[i1][j1] = visibleBoard[i2][j2];
        visibleBoard[i2][j2] = tmp;
    }

    public void randomize() {
        Random rand = new Random();
        for (int i = DIFF_PIECES; i < TOTAL; i++) {
            int t = rand.nextInt(ALL_PIECES) + DIFF_PIECES, ix = i % NUM, iy = i / NUM, tx = t % NUM, ty = t / NUM;
            swapPiecesHelper(iy, ix, ty, tx);
        }
    }

    public void swapPieces(float x1, float y1, float x2, float y2) {
        int i1 = (int) (y1 - gridY) / squareSize, i2 = (int) (y2 - gridY) / squareSize, j1 = (int) (x1 - gridX) / squareSize, j2 = (int) (x2 - gridX) / squareSize;
        if (i1 >= NUM || i2 >= NUM || i1 < 0 || i2 < 0 || j1 >= NUM || j2 >= NUM || j1 < 0 || j2 < 0) {
            return;
        }
        if (modifiable) {
            if (i1 < PLAYER0 || i2 < PLAYER0) {
                return;
            }
            swapPiecesHelper(i1, j1, i2, j2);
        } else {
            makeMove(i1, j1, i2, j2);
        }
    }

    private void updateBoards(int i1, int j1, int i2, int j2) {
        actualBoard[i2][j2] = actualBoard[i1][j1];
        actualBoard[i1][j1] = Piece.NONE;
        visibleBoard[i2][j2] = visibleBoard[i1][j1];
        visibleBoard[i1][j1] = Piece.NONE;
    }

    private void makeMove(int i1, int j1, int i2, int j2) {
        if (!isLegalMove(i1, j1, i2, j2) || currPlayer != player) {
            return;
        }
        updateBoards(i1, j1, i2, j2);
        String move = String.valueOf(player) + "-" + String.valueOf(LAST - i1) + "-" + String.valueOf(LAST - j1) + "-" + String.valueOf(LAST - i2) + "-" + String.valueOf(LAST - j2);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(text);
        databaseReference.setValue(move);
        currPlayer = !currPlayer;
        databaseReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String m = dataSnapshot.getValue(String.class);
                    if (!m.equals(move)) {
                        String[] vl = m.split("-");
                        if (vl.length != XYNUM) {
                            return;
                        }
                        int x1 = Integer.parseInt(vl[X1]), x2 = Integer.parseInt(vl[X2]), y1 = Integer.parseInt(vl[Y1]), y2 = Integer.parseInt(vl[Y2]);
                        updateBoards(y1, x1, y2, x2);
                        currPlayer = !currPlayer;
                        databaseReference.removeEventListener(this);
                    }
                }
            }

            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private boolean isLegalMove(int i1, int j1, int i2, int j2) {
        if (!canStart) {
            return false;
        }
        Piece start = actualBoard[i1][j1], end = actualBoard[i2][j2];
        int s = start.ordinal(), e = end.ordinal();
        if (start == Piece.NONE || (s < PWD && e < PWD) || (end != Piece.NONE && s >= PWD && e >= PWD) || (currPlayer && s >= PWD) || (!currPlayer && s < PWD)) {
            return false;
        }
        if (start == Piece.BLACK_PAWN || start == Piece.WHITE_PAWN) {
            return j1 == j2 && i2 == i1 - INC || (Math.abs(j1 - j2) == INC && i2 == i1 - INC && end != Piece.NONE);
        }
        if (start == Piece.WHITE_BISHOP || start == Piece.BLACK_BISHOP) {
            return bishopMove(i1, i2, j1, j2);
        }
        if (start == Piece.BLACK_ROOK || start == Piece.WHITE_ROOK) {
            return rookMove(i1, i2, j1, j2);
        }
        if (start == Piece.BLACK_KING || start == Piece.WHITE_KING) {
            return Math.abs(i1 - i2) <= INC && Math.abs(j1 - j2) <= INC;
        }
        if (start == Piece.WHITE_KNIGHT_L || start == Piece.WHITE_KNIGHT_R || start == Piece.BLACK_KNIGHT_L || start == Piece.BLACK_KNIGHT_R) {
            int i = Math.abs(i1 - i2), j = Math.abs(j1 - j2);
            return (i == SETB && j == INC) || (j == SETB && i == INC);
        }
        return bishopMove(i1, i2, j1, j2) || rookMove(i1, i2, j1, j2);
    }

    private boolean bishopMove(int i1, int j1, int i2, int j2) {
        if (Math.abs(i1 - i2) != Math.abs(j1 - j2)) {
            return false;
        }
        int is = i1 + INC, ie = i2, js = j1 + INC, je = j2;
        if (i1 > i2) {
            is = i2 + INC;
            ie = i1;
        }
        if (j1 > j2) {
            js = j2 + INC;
            je = j1;
        }
        for (int i = is, j = js; i < ie && j < je; i++) {
            if (actualBoard[i][j] != Piece.NONE) {
                return false;
            }
            j++;
        }
        return true;
    }

    private boolean rookMove(int i1, int j1, int i2, int j2) {
        if (i1 != i2 && j1 != j2) {
            return false;
        }
        if (i1 == i2) {
            int js = j1 + INC, je = j2;
            if (j1 > j2) {
                js = j2 + INC;
                je = j1;
            }
            for (int j = js; j < je; j++) {
                if (actualBoard[i1][j] != Piece.NONE) {
                    return false;
                }
            }
        } else {
            int is = i1 + INC, ie = i2;
            if (i1 > i2) {
                is = i2 + INC;
                ie = i1;
            }
            for (int i = is; i < ie; i++) {
                if (actualBoard[i][j1] != Piece.NONE) {
                    return false;
                }
            }
        }
        return true;
    }

    public CustomView(Context context) {
        super(context);
        init();
    }

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        float width = Resources.getSystem().getDisplayMetrics().widthPixels, height = Resources.getSystem().getDisplayMetrics().heightPixels, min = Math.min(width, height) * BOARD_MOD;
        squareSize = (int) min / SCALE;
        boardX = (int) (width - min) / DIV;
        boardY = (int) (height - min) / DIV;
        startX = (int) (boardX + ((float) squareSize) * MX);
        startY = (int) (boardY + ((float) squareSize) * MY);
        gridX = boardX + squareSize;
        gridY = boardY + squareSize;
        paint = new Paint();
        paint.setAntiAlias(false);
        paint.setFilterBitmap(false);
        Bitmap[] inter = {
                BitmapFactory.decodeResource(getResources(), R.drawable.white_pawn),
                BitmapFactory.decodeResource(getResources(), R.drawable.white_rook),
                BitmapFactory.decodeResource(getResources(), R.drawable.white_knight_l),
                BitmapFactory.decodeResource(getResources(), R.drawable.white_knight_r),
                BitmapFactory.decodeResource(getResources(), R.drawable.white_bishop),
                BitmapFactory.decodeResource(getResources(), R.drawable.white_king),
                BitmapFactory.decodeResource(getResources(), R.drawable.white_queen),
                BitmapFactory.decodeResource(getResources(), R.drawable.black_pawn),
                BitmapFactory.decodeResource(getResources(), R.drawable.black_rook),
                BitmapFactory.decodeResource(getResources(), R.drawable.black_knight_l),
                BitmapFactory.decodeResource(getResources(), R.drawable.black_knight_r),
                BitmapFactory.decodeResource(getResources(), R.drawable.black_bishop),
                BitmapFactory.decodeResource(getResources(), R.drawable.black_king),
                BitmapFactory.decodeResource(getResources(), R.drawable.black_queen)
        };
        float mod = (float) squareSize / inter[Piece.WHITE_QUEEN.ordinal()].getHeight() * MOD;
        for (int i = 0; i < PN; i++) {
            pieceImages.add(Bitmap.createScaledBitmap(inter[i], (int) (inter[i].getWidth() * mod), (int) (inter[i].getHeight() * mod), true));
        }
        int m = (int) min;
        board = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.board), m, m, true);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(board, boardX, boardY, paint);
        for (int i = 0; i < NUM; i++) {
            for (int j = 0; j < NUM; j++) {
                int piece = visibleBoard[i][j].ordinal();
                if (piece < PN) {
                    Bitmap p = pieceImages.get(piece);
                    int x = (int) (startX + j * squareSize - (float) p.getWidth() / DIV), y = (int) (startY + i * squareSize - (float) p.getHeight() / DIV);
                    canvas.drawBitmap(p, x, y, paint);
                }
            }
        }
        invalidate();
    }

}
