package com.example.congresstracker.other;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.congresstracker.models.Bill;

public class BillTrackDatabaseHelper extends SQLiteOpenHelper {


    private static final String DATABASE_FILE = "MyTrackedBillsDb.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "tracked_bills";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_BILL_ID = "billid";
    public static final String COLUMN_BILL_TITLE = "title";
    public static final String COLUMN_LAST_DATE = "lastdate";
    public static final String COLUMN_IS_ACTIVE = "active";
    public static final String COLUMN_LAST_VOTE = "lastvote";


    // using SQL syntax to create a string that will be passed to create a database
    private static final String CREATE_TABLE_ONE = "CREATE TABLE IF NOT EXISTS " +
            TABLE_NAME +
            " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_BILL_ID + " TEXT, " +
            COLUMN_BILL_TITLE + " TEXT, " +
            COLUMN_LAST_DATE + " DATETIME, " +
            COLUMN_IS_ACTIVE + " BOOLEAN, " +
            COLUMN_LAST_VOTE + " TEXT" +
            ")";


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ONE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    //Singleton style instance
    private static BillTrackDatabaseHelper mInstance = null;

    public static BillTrackDatabaseHelper getInstance(Context context){

        if(mInstance == null){
            mInstance = new BillTrackDatabaseHelper(context);
        }
        return mInstance;
    }

    public final SQLiteDatabase mDatabase;

    private BillTrackDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_FILE, null, DATABASE_VERSION);

        mDatabase = getWritableDatabase();
    }

    public void trackBill(Bill bill){
        ContentValues cv= new ContentValues();
        cv.put(COLUMN_BILL_ID, bill.getBillNum());
        cv.put(COLUMN_BILL_TITLE, bill.getTitle());
        cv.put(COLUMN_LAST_DATE, bill.getLatestActionDate());
        cv.put(COLUMN_IS_ACTIVE, bill.isActive());
        cv.put(COLUMN_LAST_VOTE, bill.getLastVote());
        mDatabase.insert(TABLE_NAME, null, cv);
    }

    public Cursor getBillById(String id){

        String selection = COLUMN_BILL_ID + " = '" + id + "'";
        return mDatabase.query(TABLE_NAME,null,
                selection,null,null,
                null,null);
    }

    public Cursor getAllBills(){

        return mDatabase.query(TABLE_NAME,null,
                null,null,
        null,null,null);
    }
}
