package com.tommilaurila.karttachat;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sakari.saastamoinen on 5.2.2016.
 */
public class GlobalVariables {

    private static final String USERLOGIN = "userlogin";
    private static final String USERREGISTER = "userregister";
    private static final String GROUPCREATE = "groupcreate";
    private static final String GROUPJOIN = "groupjoin";
    private static final String GROUPGETALL = "getallgroups";
    private static final String LOCATIONREPORT = "locationreport";

    //MainActivity ma = new MainActivity();
    Context context;
    User currentUser;
    public ArrayList<Group> currentGroups = new ArrayList<>();

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

    private void addGroup(Group group){
        DatabaseHandler db = new DatabaseHandler(context);

        //Add group to database and currentGroups array
        //!!!Might cause database inconsistencies!!!
        db.newGroup(group);
        currentGroups.add(group);
    }

    public ArrayList<Group> getAllGroups(){

        DatabaseHandler db = new DatabaseHandler(context);

        currentGroups = new ArrayList<>(db.getAllGroups());

        if(currentGroups.size() < 1){
            Log.d("oma", "Fetching groups from server.");
            new networkPostTask().execute(GROUPGETALL);
        }else {
            Log.d("oma", currentGroups.toString());
        }

        return currentGroups;
    }

    public List<User> getUsersOfGroup(Group group){
        DatabaseHandler db = new DatabaseHandler(context);

        return db.getUsersOfGroup(group);
    }

    /*---/GROUPS---*/

    /*---NETWORK---*/
    private class networkPostTask extends AsyncTask<String, Void, String[] >{

        @Override
        protected String[] doInBackground(String...  params){

            //Initialize ApuHttp object, returnString and url -Strings and
            // postInfo HashMap for later use
            ApuHttp httpHelper = new ApuHttp();
            String returnString[] = new String[2];
            String url;
            HashMap<String, String> postInfo = new HashMap<>();

            //Put job parameter into returnstring for onPostExecute
            returnString[0] = params[0];


            switch (params[0]){
                case USERLOGIN:
                    url =  context.getString(R.string.path_public_server) +
                            context.getString(R.string.path_user_login);
                    /*params:
                    * 1 username
                    * 2 password*/
                    postInfo.put("kt", params[1]);
                    postInfo.put("ss", params[2]);
                    break;
                case USERREGISTER:
                    url = context.getString(R.string.path_public_server) +
                            context.getString(R.string.path_add_user);
                    /*params:
                    * 1 username
                    * 2 password*/
                    postInfo.put("kt", params[1]);
                    postInfo.put("ss", params[2]);
                    break;
                case GROUPGETALL:
                    url = context.getString(R.string.path_public_server) +
                            context.getString(R.string.path_get_groups);
                    break;
                case GROUPCREATE:
                    url = context.getText(R.string.path_public_server) +
                            context.getString(R.string.path_add_group);
                    /*params:
                    * 1 creator user id
                    * 2 group name
                    * 3 group password*/
                    postInfo.put("rl", params[1]);
                    postInfo.put("rn", params[2]);
                    postInfo.put("rs", params[3]);
                    break;
                case GROUPJOIN:
                    url = context.getString(R.string.path_public_server) +
                            context.getString(R.string.path_join_group);
                    /*params:
                    * 1 joining user id
                    * 2 group name
                    * 3 group password*/
                    postInfo.put("uid", params[1]);
                    postInfo.put("rn", params[2]);
                    postInfo.put("rs", params[3]);
                    break;
                case LOCATIONREPORT:
                    url = context.getString(R.string.path_public_server) +
                            context.getString(R.string.path_post_location);
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
                    url = context.getString(R.string.path_public_server);
            }

            Log.d("oma", "URL: " + url);
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
                    case GROUPGETALL:
                        try {
                            JSONArray jsonArray = new JSONArray(result[1]);

                            // Go through the JSONarray, extract the JSONobjects from it
                            // and create Group objects from them and add them to group table
                            for(int i=0; i<jsonArray.length(); i++) {
                                Group group = new Group();

                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                group.setGroup_id(jsonObject.getInt("ryhma_id"));
                                group.setCreator(jsonObject.getInt("luoja"));
                                group.setGroupName(jsonObject.getString("nimi"));
                                group.setGroupPassword(jsonObject.getString("salasana"));
                                group.setCreationTime(jsonObject.getString("perustamisaika"));

                                addGroup(group);
                                currentGroups.add(group);

                                Log.d("oma", "Added group: " + group.toString());
                            }//for
                        }//try
                        catch (Exception e) {
                            Log.d("oma", "tuli virhe "+ e.toString());
                        }

                        //ma.arrayAdapter.notifyDataSetChanged();

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
                    case GROUPJOIN:
                        break;
                    case LOCATIONREPORT:
                        //try to parse a JSONarray from the response
                        try {
                            JSONArray jsonArray = new JSONArray(result[1]);

                            for (int i = 0; i < jsonArray.length(); i++){
                                User user = new User();
                                Location location = new Location();
                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                location.setLocation_id(jsonObject.getInt("sijainti_id"));
                                location.setLat(jsonObject.getDouble("lat"));
                                location.setLng(jsonObject.getDouble("lng"));
                                location.setTimestamp(jsonObject.getString("aikaleima"));
                                location.setUser_id(jsonObject.getInt("kayttaja_id"));
                                location.setGroup_id(jsonObject.getInt("ryhma_id"));

                                user.setUser_id(jsonObject.getInt("kayttaja_id"));
                                user.setUserName(jsonObject.getString("nimimerkki"));
                                user.setLastSeen(jsonObject.getString("viimeksi_nahty"));
                                user.setGroup_id(jsonObject.getInt("ryhma_id"));
                                user.setServerTime(jsonObject.getString("serveriaika"));
                            }
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
