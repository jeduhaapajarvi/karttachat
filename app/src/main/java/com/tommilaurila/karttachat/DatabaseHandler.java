package com.tommilaurila.karttachat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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
    private static final String TABLE_GROUP_USERS = "group_users";
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
    private static final String KEY_LOCATIONGROUPID = "groupid";
    //GROUPS table column names
    private static final String KEY_GROUPCREATOR = "groupcreator";
    private static final String KEY_GROUPNAME = "groupname";
    private static final String KEY_GROUPPASSWORD = "grouppasswird";
    private static final String KEY_GROUPCREATIONTIME = "groupcreationtime";
    //GROUP_USERS table column names
    private static final String KEY_GROUP_USERS_USERID = "userid";
    private static final String KEY_GROUP_USERS_GROUPID = "groupid";

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
                + KEY_USERLEVEL + " INTEGER, "
                + KEY_USERCREATIONTIME + " TEXT, "
                + KEY_USERLASTSEEN + " TEXT, "
                + KEY_USERGROUPID + " INTEGER, "
                + KEY_USERSERVERTIME + " TEXT "
                + ")";
        //Build locations table statement
        String CREATE_LOCATIONS_TABLE = "CREATE TABLE " + TABLE_LOCATIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY NOT NULL, "
                + KEY_LOCATIONLAT + " DOUBLE, "
                + KEY_LOCATIONLNG + " DOUBLE, "
                + KEY_LOCATIONTIMESTAMP + " TEXT, "
                + KEY_LOCATIONUSERID + " TEXT, "
                + KEY_LOCATIONGROUPID + " TEXT "
                + ")";
        String CREATE_GROUPS_TABLE = "CREATE TABLE " + TABLE_GROUPS + "("
                + KEY_ID + " INTEGER PRIMARY KEY NOT NULL, "
                + KEY_GROUPCREATOR + " INTEGER, "
                + KEY_GROUPNAME + " TEXT, "
                + KEY_GROUPPASSWORD + " TEXT, "
                + KEY_GROUPCREATIONTIME + " TEXT "
                + ")";
        String CREATE_GROUP_USERS_TABLE = "CREATE TABLE " + TABLE_GROUP_USERS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_GROUP_USERS_GROUPID + " INTEGER, "
                + KEY_GROUP_USERS_USERID + " INTEGER"
                + ")";
        Log.d("oma", "Create users table: " + CREATE_USERS_TABLE);
        Log.d("oma", "Create locations table: " + CREATE_LOCATIONS_TABLE);
        Log.d("oma", "Create groups table: " + CREATE_GROUPS_TABLE);

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_LOCATIONS_TABLE);
        db.execSQL(CREATE_GROUPS_TABLE);
        db.execSQL(CREATE_GROUP_USERS_TABLE);
        Log.d("oma", "TABLE: " + db.toString());
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUP_USERS);

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

        //put all of the user information into the values string
        ContentValues values = new ContentValues();
        values.put(KEY_USERNAME, user.getUserName()); // Username
        values.put(KEY_USERPASSWORD ,user.getPassword());// Users password
        values.put(KEY_USERLEVEL ,user.getLevel());// Users level (admin, normal, etc.)
        values.put(KEY_USERCREATIONTIME ,user.getCreationTime());// Time the user was created
        values.put(KEY_USERLASTSEEN ,user.getLastSeen());// Time the user has been updated
        values.put(KEY_USERGROUPID ,user.getGroup_id());// Users current groupId
        values.put(KEY_USERSERVERTIME ,user.getServerTime());// Users last server time?

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

        User returnUser = new User();

        //Initializing database connection
        SQLiteDatabase db = this.getWritableDatabase();

        /*Log.d("oma", "UserId databasehandlerissa: " + id);*/

        Cursor cursor = db.query(TABLE_USERS, new String[]{KEY_ID,
                        KEY_USERNAME,
                        KEY_USERPASSWORD,
                        KEY_USERLEVEL,
                        KEY_USERCREATIONTIME,
                        KEY_USERLASTSEEN,
                        KEY_USERGROUPID,
                        KEY_USERSERVERTIME
                }, KEY_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();

            returnUser.setUser_id(cursor.getInt(0));
            returnUser.setUserName(cursor.getString(1));
            returnUser.setPassword(cursor.getString(2));
            returnUser.setLevel(cursor.getInt(3));
            returnUser.setCreationTime(cursor.getString(4));
            returnUser.setGroup_id(cursor.getInt(5));
            returnUser.setServerTime(cursor.getString(6));

            cursor.close();
        }

        // return user
        return returnUser;

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

                user.setUser_id(cursor.getInt(0));
                user.setUserName(cursor.getString(1));
                user.setPassword(cursor.getString(2));
                user.setLevel(cursor.getInt(3));
                user.setCreationTime(cursor.getString(4));
                user.setGroup_id(cursor.getInt(5));
                user.setServerTime(cursor.getString(6));
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
        values.put(KEY_USERNAME, user.getUserName()); // Username
        values.put(KEY_USERPASSWORD ,user.getPassword());// Users password
        values.put(KEY_USERLEVEL ,user.getLevel());// Users level (admin, normal, etc.)
        values.put(KEY_USERCREATIONTIME ,user.getCreationTime());// Time the user was created
        values.put(KEY_USERLASTSEEN ,user.getLastSeen());// Time the user has been updated
        values.put(KEY_USERGROUPID ,user.getGroup_id());// Users current groupId
        values.put(KEY_USERSERVERTIME ,user.getServerTime());// Users last server time?


        // updating row
        return db.update(TABLE_USERS, values, KEY_ID + "=?",
                new String[]{String.valueOf(user.getUser_id())});

    }

    //Deleting single user
    public void deleteUser(User user){

        //Initializing database connection
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USERS, KEY_ID + " = ?",
                new String[]{String.valueOf(user.getUser_id())});
        db.close();

        //Log.d("oma", "All users cleared");

    }

    /*---/USER---*/

    /*---LOCATIONS---*/

    //Create a new location and assign it for a single user
    public long newLocation(Location location){
        SQLiteDatabase db = this.getWritableDatabase();
        long newLocationId;

        ContentValues values = new ContentValues();
        values.put(KEY_ID , location.getLocation_id());
        values.put(KEY_LOCATIONLAT , location.getLat());
        values.put(KEY_LOCATIONLNG , location.getLng());
        values.put(KEY_LOCATIONTIMESTAMP , location.getTimestamp());
        values.put(KEY_LOCATIONUSERID , location.getUser_id());

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
        Location returnLoc = new Location();

        if(c != null){
            c.moveToFirst();

            returnLoc.setLocation_id(c.getInt(c.getColumnIndex(KEY_ID)));

            db.close();
        }
        return returnLoc;
    }

    //Get all locations by user id
    public List<Location> getLocationByUser(User user){
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_LOCATIONS + " WHERE "
                + KEY_LOCATIONUSERID + " = " + user.getUser_id();

        Cursor c = db.rawQuery(selectQuery, null);
        List<Location> locationList = new ArrayList<>();

        if (c.moveToFirst()) {
            do {
                Log.d("oma", "LocFromDB cursor: " + c.getString(2) + " " + c.getString(3));

                Location location = new Location();

                location.setLocation_id(c.getInt(0));
                location.setLat(c.getDouble(1));
                location.setLng(c.getDouble(2));
                location.setTimestamp(c.getString(3));
                location.setUser_id(c.getInt(4));
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
                + KEY_LOCATIONUSERID + " = " + user.getUser_id() + " ORDER BY " + KEY_ID + " DESC limit 1";

        //Log.d("oma", selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);
        Location returnLoc = new Location();

        if(c != null){
            c.moveToFirst();

            returnLoc.setLocation_id(c.getInt(0));
            returnLoc.setLat(c.getDouble(1));
            returnLoc.setLng(c.getDouble(2));
            returnLoc.setTimestamp(c.getString(3));
            returnLoc.setUser_id(c.getInt(4));

            c.close();
        }
        return returnLoc;
    }

    public void deleteLocationsOfUser(User user){

        //Initializing database connection
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LOCATIONS, KEY_LOCATIONUSERID + " = ?",
                new String[]{String.valueOf(user.getUser_id())});
        db.close();

    }
    /*---/LOCATIONS---*/

    /*---GROUPS---*/
    public void newGroup(Group group){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, group.getGroup_id());
        values.put(KEY_GROUPCREATOR, group.getCreator());
        values.put(KEY_GROUPNAME, group.getGroupName());
        values.put(KEY_GROUPPASSWORD, group.getGroupPassword());
        values.put(KEY_GROUPCREATIONTIME, group.getCreationTime());


        db.insert(TABLE_GROUPS, null, values);
        db.close();
    }

    public void joinGroup(Group group, User user){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_GROUP_USERS_GROUPID, group.getGroup_id());
        values.put(KEY_GROUP_USERS_USERID, user.getUser_id());

        db.insert(TABLE_GROUP_USERS, null, values);
        db.close();
    }

    public List<Group> getAllGroups(){
        SQLiteDatabase db = this.getReadableDatabase();

        List<Group> groupList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_GROUPS;
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Group group = new Group();

                group.setGroup_id(cursor.getInt(0));
                group.setCreator(cursor.getInt(1));
                group.setGroupName(cursor.getString(2));
                group.setCreationTime(cursor.getString(4));
                // Adding user to list
                groupList.add(group);
            } while (cursor.moveToNext());
        }

        cursor.close();
        // return user list
        return groupList;
    }

    public void clearGroups(){
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPS);

        String CREATE_GROUPS_TABLE = "CREATE TABLE " + TABLE_GROUPS + "("
                + KEY_ID + " INTEGER PRIMARY KEY NOT NULL, "
                + KEY_GROUPCREATOR + " TEXT, "
                + KEY_GROUPNAME + " TEXT, "
                + KEY_GROUPPASSWORD + " TEXT "
                + KEY_GROUPCREATIONTIME + " TEXT "
                + ")";

        db.execSQL(CREATE_GROUPS_TABLE);
    }

    public ArrayList<User> getUsersOfGroup(Group group){
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<User> userList = new ArrayList<>();

        String selectQuery = "SELECT a." + KEY_GROUP_USERS_USERID
                + " FROM " + TABLE_GROUP_USERS
                + " WHERE " + KEY_GROUP_USERS_GROUPID
                + " = " + group.getGroup_id()
                + " LEFT JOIN " + TABLE_USERS
                + " USING(" + KEY_ID;

        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                User user = new User();

                user.setUser_id(c.getInt(0));
                user.setUserName(c.getString(1));
                user.setPassword(c.getString(2));
                user.setLevel(c.getInt(3));
                user.setCreationTime(c.getString(4));
                user.setGroup_id(c.getInt(5));
                user.setServerTime(c.getString(6));
                // Adding user to list
                userList.add(user);

            } while (c.moveToNext());
        }

        return userList;
    }


    /*---/GROUPS---*/
}
