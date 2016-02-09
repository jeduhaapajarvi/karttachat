package com.tommilaurila.karttachat;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import javax.sql.StatementEvent;

/**
 * Created by sakari.saastamoinen on 5.2.2016.
 */
public class GlobalVariables {

    private static final String USERLOGIN = "userlogin";
    private static final String USERREGISTER = "userregister";
    private static final String GROUPCREATE = "groupcreate";
    private static final String LOCATIONREPORT = "locationreport";

    Context context;
    User currentUser;

    public GlobalVariables(){

    }

    public GlobalVariables(Context contex){
        this.context = contex;
    }

    //TODO Add a method that checks from server if there is new information
    //TODO available

    //TODO [OPTIONAL] Have the (to-be) HttpHelper class update information
    //TODO every x seconds and queue all requests in one packet

    /*---USER---*/

    public void checkUser(String userName, String pWord){

        Log.d("oma", "Login as: " + userName + " " + pWord);

        boolean loginValid;

        int userId = -1/*plug HttpHelper login here*/;
        //TODO Add HtmlHelper-class that has a loginUser method that checks
        //TODO from the server if a user with a particular password is a valid user
        //TODO return as userId, if not valid, return -1

        DatabaseHandler db = new DatabaseHandler(context);

        List<User> userList = db.getAllUsers();

        for(int i = 0; i < userList.size(); i++){
            if(userName.equals(userList.get(i).getUserName())){
                userId = userList.get(i).getUser_id();
            }
        }
    }

    public int addNewUser(User user){

        //New DatabaseHandler
        DatabaseHandler db = new DatabaseHandler(context);

        //Place the value DatabaseHandler to-be returned newUserId
        int newUserId = db.addUser(user);

        return newUserId;
    }

    public User getUser(int id){

        DatabaseHandler db = new DatabaseHandler(context);

        User targetUser = db.getUser(id);

        return targetUser;
    }

    public List<User> getAllUsers(){

        DatabaseHandler db = new DatabaseHandler(context);

        return db.getAllUsers();

    }

    public int getUserCount(){

        DatabaseHandler db = new DatabaseHandler(context);

        return db.getUserCount();
    }

    public int updateUser(User user){

        DatabaseHandler db = new DatabaseHandler(context);

        return db.updateUser(user);

    }

    public void deleteUser(User user){

        DatabaseHandler db = new DatabaseHandler(context);

        db.deleteLocationsOfUser(user);
        db.deleteUser(user);
    }

    public void logUser(int id){
        User targetUser;
        DatabaseHandler db = new DatabaseHandler(context);

        targetUser = db.getUser(id);

        //Log.d("oma", "Käyttäjänimi: " + targetUser.getUserName());

        //TODO Add here a log of user's newest location from locations database
        //Log.d("oma", "Lat: " + targetUser._lat);
        //Log.d("oma", "Lng: " + targetUser._lng);
    }

    /*---/USER---*/

    /*---LOCATIONS---*/

    public long newLocation(Location location){

        DatabaseHandler db = new DatabaseHandler(context);

        long newLocId = db.newLocation(location);

        return newLocId;
    }

    public Location locationById(long locationId){

        DatabaseHandler db = new DatabaseHandler(context);

        Location returnLoc = db.getLocationByLId(locationId);

        return returnLoc;
    }

    public List<Location> locationsByUser(User user){

        DatabaseHandler db = new DatabaseHandler(context);

        List<Location> userLocationList = db.getLocationByUser(user);

        /* Add a dummy location to avoid crashes if user has no locations */
        if (userLocationList == null){
            userLocationList.add(new Location(10.00000f, 10.00000f));
        }
        Log.d("oma", "Got location list by user's id");
        Log.d("oma", userLocationList.get(0).getLat() + " " + userLocationList.get(0).getLng());

        return userLocationList;
    }

    public Location locationLastByUser(User user){

        DatabaseHandler db = new DatabaseHandler(context);

        Location lastUserLocation = db.getUserLastLoc(user);

        return lastUserLocation;
    }

    /*---/LOCATIONS---*/

    /*---GROUPS---*/

    public void newGroup(Group group){
        new networkPostTask().execute(GROUPCREATE,
                group.getCreator() + "",
                group.getGroupName(),
                group.getGroupPassword());
    }

    public void setGroupInactive(User user){

    }

    /*---/GROUPS---*/

    /*---NETWORK---*/
    private class networkPostTask extends AsyncTask<String, Void, String[] >{

        @Override
        protected String[] doInBackground(String...  params){

            //Initialize ApuHttp object, returnString and url -Strings and
            // postInfo HashMap for later use
            ApuHttp httpHelper = new ApuHttp();
            String returnString[] = new String[]{};
            String url;
            HashMap<String, String> postInfo = new HashMap<>();

            //Put job parameter into returnstring for onPostExecute
            returnString[0] = params[0];


            switch (params[0]){
                case USERLOGIN:
                    url = R.string.path_public_server + R.string.path_user_login + "";
                    /*params:
                    * 1 username
                    * 2 password*/
                    postInfo.put("kt", params[1]);
                    postInfo.put("ss", params[2]);
                    break;
                case USERREGISTER:
                    url = R.string.path_public_server + R.string.path_add_user + "";
                    /*params:
                    * 1 username
                    * 2 password*/
                    postInfo.put("kt", params[1]);
                    postInfo.put("ss", params[2]);
                    break;
                case GROUPCREATE:
                    url = R.string.path_public_server + R.string.path_add_group + "";
                    /*params:
                    * 1 creator user id
                    * 2 group name
                    * 3 group password*/
                    postInfo.put("rl", params[1]);
                    postInfo.put("rn", params[2]);
                    postInfo.put("rs", params[3]);
                    break;
                case LOCATIONREPORT:
                    url = R.string.path_public_server + R.string.path_post_location + "";
                    postInfo.put("uid", params[1]);
                    postInfo.put("gid", params[2]);
                    postInfo.put("lat", params[3]);
                    postInfo.put("lng", params[4]);
                    postInfo.put("msg", params[5]);
                    break;
                default:
                    //If networkPostTask gets invalid parameters, log it and set url to
                    //servers "bare" address to avoid nullPointerExceptions
                    Log.d("oma", "Invalid networkPostTask parameter[0] '" + params[0] + "' !");
                    url = R.string.path_public_server + "";
            }

            returnString[1] = httpHelper.postData(url, postInfo);
            return returnString;
        }

        @Override
        protected void onPostExecute(String[] result){
            if(result != null && result[1].length() > 0){
                switch (result[0]){
                    case USERREGISTER: case USERLOGIN:
                        try {
                            JSONObject jsonObject = new JSONObject(result[1]);

                            User user = new User();

                            user.setUser_id(jsonObject.getInt("kayttaja_id"));
                            user.setUserName(jsonObject.getString("nimimerkki"));
                            user.setLevel(jsonObject.getInt("taso"));
                            user.setCreationTime(jsonObject.getString("perustamisaika"));
                            user.setLastSeen(jsonObject.getString("viimeksi_nahty"));
                            user.setGroup_id(jsonObject.getInt("usergroupid"));
                            user.setServerTime(jsonObject.getString("userservertime"));

                            if(result[0].equals(USERLOGIN)){
                                updateUser(user);
                            }else {
                                addNewUser(user);
                            }

                            currentUser = user;

                        } catch (Exception e){
                            Log.d("oma", "onPostExecute USERREGISTER error: " + e);
                        }
                        break;
                    case GROUPCREATE:
                        try {
                            JSONObject jsonObject = new JSONObject(result[1]);
                            DatabaseHandler db = new DatabaseHandler(context);

                            Group group = new Group();

                            group.setGroup_id(jsonObject.getInt("groupid"));
                            group.setGroupName(jsonObject.getString("groupname"));
                            group.setCreator(jsonObject.getInt("creatorid"));
                            group.setCreationTime(jsonObject.getString("groupcreationtime"));

                            db.newGroup(group);
                            currentUser.setGroup_id(group.getGroup_id());

                        } catch (Exception e){
                            Log.d("oma", "onPostExecute GROUPCREATE error: " + e);
                        }
                        break;
                    case LOCATIONREPORT:
                        try {
                            JSONArray jsonArray = new JSONArray(result[1]);

                        } catch (Exception e){
                            Log.d("oma", "onPostExecute LOCATIONREPORT error: " + e);
                        }
                        break;
                }
            }
        }
    }
    /*---/NETWORK---*/
}
