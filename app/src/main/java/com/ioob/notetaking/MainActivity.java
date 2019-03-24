package com.ioob.notetaking;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity{
    Cursor todoCursor;
    ListView noteList;
    TextView emptyText;
    NoteAdapter adapter;
    SQLiteDatabase db;

    EditText text;
    ArrayList<String> lists;
    private GestureDetector gestureDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        noteList = (ListView) findViewById(R.id.note_list);
        emptyText = (TextView) findViewById(R.id.empty);
        noteList.setEmptyView(emptyText);

        View mainView = findViewById(R.id.mainConstraintLayout);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.planets_array, R.layout.sort_spinner);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);


        loadNotesFromDatabase();
        spinner.setOnItemSelectedListener(new CustomOnItemSelectedListener(adapter));

        gestureDetector = new GestureDetector(MainActivity.this, mGestureListener);

        mainView.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return gestureDetector.onTouchEvent(event);
            }
        });
        noteList.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return gestureDetector.onTouchEvent(event);
            }
        });

        mainView.setClickable(true);



    }
    public void loadNotesFromDatabase() {
        // Create a new instance of the NoteTakingDatabase
        NoteTakingDatabase handler = new NoteTakingDatabase(getApplicationContext());
        // Get the writable database
        db = handler.getWritableDatabase();
        //Get all notes from the database
        todoCursor = db.rawQuery("SELECT * FROM notes", null);

        // Create an instance of the NoteAdapter with our cursor
         adapter = new NoteAdapter(this, todoCursor, 0);

        // Set the NoteAdapter to the ListView (display all notes from DB)
        noteList.setAdapter(adapter);

        noteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent openNote = new Intent(MainActivity.this, NoteActivity.class);
                openNote.putExtra("noteId", id);
                startActivity(openNote);
            }
        });

    }

    public void refreshList(){
        todoCursor = db.rawQuery("SELECT * FROM notes", null);
        adapter.changeCursor(todoCursor );
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close database cursor
        if (todoCursor != null) {
            todoCursor.close();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_note:
                // TODO something
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("NOTESTATUS","Notes being loaded");
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    loadNotesFromDatabase();
                }
                else {
                    // TODO tell the user we need permission for our app to work
                    Log.i("NOTESTATUS", "Notes not being loaded");
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            0);

                }
                break;
        }
    }
    private class CustomOnItemSelectedListener implements AdapterView.OnItemSelectedListener{
        NoteAdapter adapter;
        Boolean isloaded = false;
        public CustomOnItemSelectedListener(NoteAdapter adapter){
            this.adapter = adapter;
        }


        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Log.i("ISLOADED", String.valueOf(isloaded));
            if (isloaded) {
                String value = parent.getItemAtPosition(position).toString();
                if (value.equals("Sort By Title")) {
                    todoCursor = db.rawQuery("SELECT * FROM notes ORDER BY noteText", null);

                }
                else if(value.equals("Sort By Oldest")){
                    todoCursor = db.rawQuery("SELECT * FROM notes ORDER BY datetime(noteDate)", null);

                }
                else if (value.equals("Sort By Newest")){
                    todoCursor = db.rawQuery("SELECT * FROM notes ORDER BY datetime(noteDate) DESC", null);
                }
                else if (value.equals("Sort By Category")){
                    Log.i("ISLOADED", "CATEGORIES");
                    todoCursor = db.rawQuery("SELECT * FROM notes ORDER BY noteCategory ", null);
                }


                else{
                    todoCursor = db.rawQuery("SELECT * FROM notes ORDER BY noteText DESC", null);
                }
                Log.i("ISLOADED", value);
                adapter.changeCursor(todoCursor);
                adapter.notifyDataSetChanged();

            }
            else{
                isloaded=true;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {


        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (e1.getY()<e2.getY()){
                   refreshList();
                    Toast.makeText(MainActivity.this, "NOTES REFRESHED", Toast.LENGTH_SHORT).show();

                }

            } catch (Exception e) {

            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    };



    public boolean userHasPermission() {
        return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }


}
