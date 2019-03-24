package com.ioob.notetaking;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class NoteActivity extends AppCompatActivity  {


    String imagePath = "";
    Button saveNote;
    Button deleteNote;
    TextView noteTitle;
    ImageView noteImage;
    TextView noteDescription;
    SQLiteDatabase db;
    Spinner category;

    boolean isUpdate = false;
    int noteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        // Create a new instance of the NoteTakingDatabase
        NoteTakingDatabase handler = new NoteTakingDatabase(getApplicationContext());
        // Get the writable database
        db = handler.getReadableDatabase();

        saveNote = (Button) findViewById(R.id.create_note);
        deleteNote = (Button) findViewById(R.id.delete_note);
        noteTitle = (TextView) findViewById(R.id.note_title);
        noteImage = (ImageView) findViewById(R.id.note_image);
        noteDescription = (TextView) findViewById(R.id.note_description);
        final ArrayAdapter<CharSequence> sadapter = ArrayAdapter.createFromResource(this, R.array.user_inserted_categories, R.layout.sort_spinner);
        category = (Spinner) findViewById(R.id.categories);
        sadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category.setAdapter(sadapter);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isUpdate = true;
            noteId = (int) extras.getLong("noteId");
            boolean noteExist = noteExist(noteId);
            if(noteExist){
                setNote(noteId);
                saveNote.setText("Update Note");
                deleteNote.setText("Delete");
            }
            else{
                Toast.makeText(this,"Note may have been deleted.\nRefresh the page",Toast.LENGTH_LONG).show();
                finish();
            }

        }


        noteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Use the EasyImage library to open up a chooser to pick an image.
                EasyImage.openChooserWithGallery(NoteActivity.this, "Upload an Image", 0);
            }
        });

        saveNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean formIsBlank = noteTitle.getText().toString().trim().isEmpty();
                String description = noteDescription.getText().toString().trim();
                if(formIsBlank){
                    noteTitle.setError("Note Title Cannot be blank");
                }
                else{
                    //No errors in the form
                    if (!isUpdate) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String date = sdf.format(new Date());
                        storeNote(imagePath, noteTitle.getText().toString(), "Description", category.getSelectedItem().toString(), date);

                    } else {
                        updateNote(noteId, imagePath, noteTitle.getText().toString(), description, category.getSelectedItem().toString());
                    }

                    finish();
                }

            }
        });

        deleteNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteNote(noteId);
                finish();
            }
        });

    }
    private void deleteNote(int noteId) {
        // Create a new instance of the NoteTakingDatabase
        NoteTakingDatabase handler = new NoteTakingDatabase(getApplicationContext());
        // Get the writable database
        SQLiteDatabase db = handler.getWritableDatabase();
        // Store the note in the database
        handler.deleteNote(db, noteId);

        Toast.makeText(this,"NOTE DELETED",Toast.LENGTH_SHORT).show();
    }

    private void updateNote(int noteId, String imagePath, String title, String description, String category) {
        // Create a new instance of the NoteTakingDatabase
        NoteTakingDatabase handler = new NoteTakingDatabase(getApplicationContext());
        // Get the writable database
        SQLiteDatabase db = handler.getWritableDatabase();
        // Store the note in the database
        handler.updateNote(db, noteId, imagePath, title, description, category);

        Toast.makeText(this,"note has been updated",Toast.LENGTH_LONG).show();
    }

    //Checks whether the note exist in the database
    private boolean noteExist(int noteId){
        Cursor cursor = db.rawQuery("SELECT * FROM notes WHERE _id = " + noteId, null);

        if(cursor.moveToFirst()){
            return true;
        }
        else{
            return false;
        }
    }

    private void setNote(Integer noteId) {
        // Get note by id
        Cursor cursor = db.rawQuery("SELECT * FROM notes WHERE _id = " + noteId, null);
        cursor.moveToFirst();

        // Set note details to view
        String path = cursor.getString(cursor.getColumnIndexOrThrow("noteImage"));
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        noteImage.setImageBitmap(bitmap);

        // Get the note text from the database as a String
        String noteText = cursor.getString(cursor.getColumnIndexOrThrow("noteText"));
        noteTitle.setText(noteText);

        String description = cursor.getString(cursor.getColumnIndexOrThrow("noteDescription"));
        noteDescription.setText(description);

        String noteCategory = cursor.getString(cursor.getColumnIndexOrThrow("noteCategory"));


        cursor.close();
    }

    public void storeNote(String path, String title, String description, String category, String date) {
        // Create a new instance of the NoteTakingDatabase
        NoteTakingDatabase handler = new NoteTakingDatabase(getApplicationContext());
        // Get the writable database
        SQLiteDatabase db = handler.getWritableDatabase();
        // Store the note in the database

        handler.storeNote(db, path, title, description, category, date);

        Toast.makeText(this, "Note has been created!", Toast.LENGTH_LONG).show();

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                // TODO error stuff
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                imagePath = imageFile.getAbsolutePath();
                Bitmap imageBitmap = BitmapFactory.decodeFile(imagePath);
                noteImage.setImageBitmap(imageBitmap);
            }
        });
    }



}
