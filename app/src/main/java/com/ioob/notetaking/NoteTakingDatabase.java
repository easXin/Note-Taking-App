package com.ioob.notetaking;


import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.view.GestureDetector;

public class NoteTakingDatabase extends SQLiteOpenHelper {

    private String DATABASE_NAME = "notes";

    NoteTakingDatabase(Context context) {
        super(context, "NOTES_DATABASE", null, 3);
    }

    // This is where we need to write create table statements.
    // This is called when database is created.
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE notes(_id INTEGER PRIMARY KEY, noteImage TEXT, noteText TEXT, noteDescription TEXT, noteCategory TEXT, noteDate TEXT)");
    }
    void storeNote(SQLiteDatabase db, String path, String text, String description, String category, String date) {
        ContentValues values = new ContentValues();
        values.put("noteImage", path);
        values.put("noteText", text);
        values.put("noteDescription", description);
        values.put("noteCategory", category);
        values.put("noteDate", date);

        db.insert(DATABASE_NAME, null, values);
    }

    void updateNote(SQLiteDatabase db, Integer id, String path, String text, String description, String category) {
        ContentValues values = new ContentValues();
        values.put("noteImage", path);
        values.put("noteText", text);
        values.put("noteDescription", description);
        values.put("noteCategory", category);

        db.update(DATABASE_NAME, values, "_id=?", new String[]{String.valueOf(id)});
    }

    void deleteNote(SQLiteDatabase db, Integer id) {
        SQLiteDatabase retVal = this.getWritableDatabase();
        retVal.delete(DATABASE_NAME, "_id=?", new String[]{String.valueOf(id)});

    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }

    }
}
