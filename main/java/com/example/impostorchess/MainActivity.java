package com.example.impostorchess;



import android.content.Context;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    private final int UP_TO_PASS = 100000, LEN = 5;
    private String text;
    private final String ZERO = "0", F = "";
    private boolean chosen = false, done = false;
    private float x, y;
    CustomView cv;
    protected GestureDetector gestureDetector;
    protected DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            gestureDetector = new GestureDetector(this, this);
            return insets;
        });
        cv = findViewById(R.id.cv);
        Button create = findViewById(R.id.create_button), join = findViewById(R.id.join_button), copy = findViewById(R.id.copy_button), start = findViewById(R.id.start_button), random = findViewById(R.id.random_button);
        TextView pass = findViewById(R.id.pass), name = findViewById(R.id.name);
        EditText type = findViewById(R.id.type);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                create.setVisibility(View.GONE);
                join.setVisibility(View.GONE);
                name.setVisibility(View.GONE);
                cv.changePlayer();
                long p = System.currentTimeMillis() % UP_TO_PASS;
                text = String.valueOf(p);
                while (text.length() < LEN) {
                    text = ZERO + text;
                }
                cv.text = text;
                pass.setText(text);
                pass.setVisibility(View.VISIBLE);
                databaseReference = FirebaseDatabase.getInstance().getReference().child(text);
                databaseReference.setValue(text);
                copy.setVisibility(View.VISIBLE);
                copy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        copy.setVisibility(View.GONE);
                        pass.setVisibility(View.GONE);
                        ClipboardManager clipboard = (android.text.ClipboardManager) getBaseContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboard.setText(text);
                    }
                });
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String val = dataSnapshot.getValue(String.class);
                            if (val.equals(F)) {
                                cv.setVisibility(View.VISIBLE);
                                start.setVisibility(View.VISIBLE);
                                random.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }
        });
        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                create.setVisibility(View.GONE);
                join.setVisibility(View.GONE);
                type.setVisibility(View.VISIBLE);
                name.setVisibility(View.GONE);
                type.addTextChangedListener(new TextWatcher() {
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (s.length() == LEN) {
                            DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().child(String.valueOf(s));
                            dbr.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        cv.text = String.valueOf(s);
                                        type.setVisibility(View.GONE);
                                        cv.setVisibility(View.VISIBLE);
                                        start.setVisibility(View.VISIBLE);
                                        random.setVisibility(View.VISIBLE);
                                        dbr.setValue(F);
                                        dbr.removeEventListener(this);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {}
                            });
                        }
                    }
                });
            }
        });
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start.setVisibility(View.GONE);
                random.setVisibility(View.GONE);
                cv.setBoard();
                done = true;
            }
        });
        random.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cv.randomize();
            }
        });
    }

    @Override
    protected void onDestroy() {
        databaseReference.removeValue();
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public void onLongPress(@NonNull MotionEvent event) {
        if (chosen) {
            if (done) {
                cv.clearHighlights();
            }
            cv.swapPiecesOrMakeMove(x, y, event.getX(), event.getY());
        } else {
            x = event.getX();
            y = event.getY();
            if (done) {
                cv.highlightMoves(x, y);
            }
        }
        chosen = !chosen;
    }

    public boolean onDown(@NonNull MotionEvent event) {
        return false;
    }

    public void onShowPress(@NonNull MotionEvent e) {}

    public boolean onSingleTapUp(@NonNull MotionEvent e) {
        return false;
    }

    public boolean onScroll(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}