package com.tommilaurila.karttachat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sakari.saastamoinen on 5.2.2016.
 */
public class DatabaseHandler extends SQLiteOpenHelper{

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 10;
    // Database Name
    private static final String DATABASE_NAME = "userManager";
    //table names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_LOCATIONS = "locations";
    private static final String TABLE_GROUPS = "groups";
    // Common columns names
    private static final String KEY_ID = "id";
    //USERS table column names
    private static final String KEY_USERNAME = "username";
    private static final String KEY_USERPASSWORD = "password";
    private static final String KEY_USERLEVEL = "level";
    private static final String KEY_USERCREATIONTIME = "creationtime";
    private static final String KEY_USERLASTSEEN = "lastseen";
    private static final String KEY_USERGROUPID = "groupid";
    private static final String KEY_USERSTATUS = "status";
    private static final String KEY_USERLOCATION = "location";
    private static final String KEY_USERMARKER = "marker";
    private static final String KEY_USERSERVERTIME = "servertime";
    //LOCATIONS table column names
    private static final String KEY_LOCATIONLAT = "lat";
    private static final String KEY_LOCATIONLNG = "lng";
    private static final String KEY_LOCATIONTIMESTAMP = "timestamp";
    private static final String KEY_LOCATIONUSERID = "userid";
    //GROUPS table column names
    private static final String KEY_GROUPCREATOR = "groupcreator";
    private static final String KEY_GROUPNAME = "groupname";
    private static final String KEY_GROUPPASSWORD = "grouppasswird";
    private static final String KEY_GROUPCREATIONTIME = "groupcreationtime";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        //Build users table statement
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + KEY_ID + " INTEGER PRIMARY KEY NOT NULL, "
                + KEY_USERNAME + " TEXT, "
                + KEY_USERPASSWORD + " TEXT, "
                + KEY_USERLEVEL + " TEXT, "
                + KEY_USERCREATIONTIME + " TEXT, "
                + KEY_USERLASTSEEN + " TEXT, "
                + KEY_USERGROUPID + " TEXT, "
                + KEY_USERSTATUS + " TEXT, "
                + KEY_USERLOCATION + " TEXT, "
                + KEY_USERMARKER + " TEXT, "
                + KEY_USERSERVERTIME + " TEXT, "+")";
        //Build locations table statement
        String CREATE_LOCATIONS_TABLE = "CREATE TABLE " + TABLE_LOCATIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                + KEY_LOCATIONLAT + " TEXT, "
                + KEY_LOCATIONLNG + " TEXT, "
                + KEY_LOCATIONTIMESTAMP + " TEXT, "
                + KEY_LOCATIONUSERID + " TEXT, "
                + ")";
        String CREATE_GROUPS_TABLE = "CREATE TABLE " + TABLE_GROUPS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                + KEY_GROUPCREATOR + " TEXT, "
                + KEY_GROUPNAME + " TEXT, "
                + KEY_GROUPPASSWORD + " TEXT "
                + KEY_GROUPCREATIONTIME + " TEXT "
                + ")";
        Log.d("oma", "Create users table: " + CREATE_USERS_TABLE);
        Log.d("oma", "Create users table: " + CREATE_LOCATIONS_TABLE);

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_LOCATIONS_TABLE);
        Log.d("oma", "TABLE: " + db.toString());
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPS);

        // Create tables again
        onCreate(db);
    }

    /*------------- All CRUD (Create, Read, Update, Delete) operations -------------*/

    /*---USER---*/

    // Adding new user
    public int addUser(User user){

        //Initializing database connection
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT ROWID from " + TABLE_USERS + " order by ROWID DESC limit 1";
        int newUserId = -1;

        ContentValues values = new ContentValues();
        values.put(KEY_USER, user.getUserName()); // Username

        Log.d("oma", "DBHandler adding user: " + values.toString());

        // Inserting Row
        db.insert(TABLE_USERS, null, values);
        db.close(); // Closing database connection

        SQLiteDatabase dbRead = this.getReadableDatabase();

        Cursor cursor = dbRead.rawQuery(query, null);
        if (cursor != null && cursor.moveToFirst()){
            newUserId = Integer.parseInt(cursor.getLong(0) + "");
        }

        Log.d("oma", "newUserId: " + newUserId + "");
        return newUserId;
    }

    // Getting single user
    public User getUser(int id){

        //Initializing database connection
        SQLiteDatabase db = this.getWritableDatabase();

        /*Log.d("oma", "UserId databasehandlerissa: " + id);*/

        Cursor cursor = db.query(TABLE_USERS, new String[]{KEY_ID,
                        KEY_USER}, KEY_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }

        User user = new User(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1));

        cursor.close();

        // return user
        return user;

    }

    // Getting all users
    public List<User> getAllUsers(){

        List<User> userList = new ArrayList<User>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_USERS;

        //Initializing database connection
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setId(Integer.parseInt(cursor.getString(0)));
                user.setUserName(cursor.getString(1));
                // Adding user to list
                userList.add(user);
            } while (cursor.moveToNext());
        }

        cursor.close();
        // return user list
        return userList;

    }

    // Getting usercount
    public int getUserCount(){

        String countQuery = "SELECT  * FROM " + TABLE_USERS;
        int userCount = 0;

        //Initializing database connection
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        userCount = cursor.getCount();

        // return count
        return userCount;
    }

    // Updating single user
    public int updateUser(User user){

        //Initializing database connection
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USER, user.getUserName());

        // updating row
        return db.update(TABLE_USERS, values, KEY_ID + "=?",
                new String[]{String.valueOf(user.getId())});

    }

    //Deleting single user
    public void deleteUser(User user){

        //Initializing database connection
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USERS, KEY_ID + " = ?",
                new String[]{String.valueOf(user.getId())});
        db.close();

        //Log.d("oma", "All users cleared");

    }

    /*---/USER---*/

    /*---LOCATIONS---*/

    //Create a new location and assign it for a single user
    public long newLocation(Location location, User user){
        SQLiteDatabase db = this.getWritableDatabase();
        long newLocationId;

        ContentValues values = new ContentValues();
        values.put(KEY_USERID, user.getId());
        values.put(KEY_LAT, location.getLat());
        values.put(KEY_LNG, location.getLng());

        newLocationId = db.insert(TABLE_LOCATIONS, null, values);
        db.close();

        return newLocationId;
    }

    //Get location by location id
    public Location getLocationByLId(long locationId){
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_LOCATIONS + " WHERE "
                + KEY_ID + " = " + locationId;

        Log.d("oma", selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if(c != null){
            c.moveToFirst();
        }

        Location returnLoc = new Location();
        returnLoc.setId(c.getInt(c.getColumnIndex(KEY_ID)));
        returnLoc.setUserId(c.getInt(c.getColumnIndex(KEY_USERID)));
        returnLoc.setLat(c.getInt(c.getColumnIndex(KEY_LAT)));
        returnLoc.setLng(c.getInt(c.getColumnIndex(KEY_LNG)));

        db.close();
        return returnLoc;
    }

    //Get all locations by user id
    public List<Location> getLocationByUser(User user){
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_LOCATIONS + " WHERE "
                + KEY_USERID + " = " + user.getId();

        Cursor c = db.rawQuery(selectQuery, null);
        List<Location> locationList = new ArrayList<>();

        if (c.moveToFirst()) {
            do {
                Log.d("oma", "LocFromDB cursor: " + c.getString(2) + " " + c.getString(3));

                Location location = new Location();
                location.setId(Integer.parseInt(c.getString(0)));
                location.setUserId(Integer.parseInt(c.getString(1)));
                location.setLat(Float.parseFloat(c.getString(2)));
                location.setLng(Float.parseFloat(c.getString(3)));
                // Adding location to list
                locationList.add(location);
            } while (c.moveToNext());
        }

        //Close the cursor
        c.close();

        return locationList;
    }

    //Get user's latest location
    public Location getUserLastLoc(User user){
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_LOCATIONS + " WHERE "
                + KEY_USERID + " = " + user.getId() + " ORDER BY " + KEY_ID + " DESC limit 1";

        //Log.d("oma", selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if(c != null){
            c.moveToFirst();
        }

        Location returnLoc = new Location();
        returnLoc.setId(c.getInt(c.getColumnIndex(KEY_ID)));
        returnLoc.setUserId(c.getInt(c.getColumnIndex(KEY_USERID)));
        returnLoc.setLat(c.getFloat(c.getColumnIndex(KEY_LAT)));
        returnLoc.setLng(c.getFloat(c.getColumnIndex(KEY_LNG)));

        c.close();
        return returnLoc;
    }

    public void deleteLocationsOfUser(User user){

        //Initializing database connection
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LOCATIONS, KEY_USERID + " = ?",
                new String[]{String.valueOf(user.getId())});
        db.close();

    }
    /*---/LOCATIONS---*/

    /*---GROUPS---*/
    public void newGroup(Group newGroup){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_GROUPNAME, newGroup.getGroupName());
        values.put(KEY_GROUPPWORD, newGroup.getGroupPassWord());
        values.put(KEY_GROUPCREATOR, newGroup.getCreator().getId());

        db.insert(TABLE_GROUPS, null, values);
        db.close();
    }

    public void joinGroup(Group group, User user){
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_GROUPS + " WHERE "
                + KEY_ID + " = " + group.getGroupId() + " ORDER BY " + KEY_ID + " DESC limit 1";
    }
    /*---/GROUPS---*/
}
