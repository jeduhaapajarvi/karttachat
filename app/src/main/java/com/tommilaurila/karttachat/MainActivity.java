package com.tommilaurila.karttachat;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity
    implements LisaaRyhmaDialogFragment.LisaaRyhmaDialogListener,
        LisaaKayttajaDialogFragment.LisaaKayttajaDialogListener,
        GlobalVariables.GlobalVariablesListener {

    // taulukko, joka sisältää ryhmäolioita
    public ArrayList<Group> ryhmat = new ArrayList<>();
    ListView lvRyhmaLista;
    RyhmalistaAdapter arrayAdapter;
    String kayttaja;
    String ryhmaId;

    GlobalVariables gv = new GlobalVariables(this);

    final String LOPETA_SEURANTA = "com.tommilaurila.tie13karttademo.lopetaseuranta";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        if(intent != null &&
                intent.getAction() != null &&
                intent.getAction().equals(LOPETA_SEURANTA)) {
            cancelNotification(this, 111);
        }

        // haetaan ryhmät www-palvelimelta asynctaskin avulla
        //currentGroups = gv.getAllGroups();

        // etsitään listview-komponentti layoutista
        lvRyhmaLista = (ListView)findViewById(R.id.lvRyhmaLista);

        ArrayList<Group> groupList = gv.getAllGroups();

        // luodaan sovitin ryhmätaulukon ja listviewin välille
        arrayAdapter = new RyhmalistaAdapter(this,R.layout.listarivi_ryhmat, groupList);

        // liitetään luotu sovitin listviewin kanssa yhteen
        lvRyhmaLista.setAdapter(arrayAdapter);

        lvRyhmaLista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // Toast.makeText(getBaseContext(), item, Toast.LENGTH_LONG).show();
                // avataan kartta-aktiviteetti
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);

                // lähetetään käyttäjänimi sekä valittu ryhmä kartta-aktiviteetille

                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_nimi),Context.MODE_PRIVATE);
                //oletus-id on -1, joka tarkoittaa, että käyttäjä-id:tä ei voitu lukea
                String k_id = sharedPref.getString("kayttajaid", "-1");

                // TODO: muuta kovakoodatut nimet
                intent.putExtra("xtraKayttajaId", k_id);
                intent.putExtra("xtraKayttajaNimi", kayttaja);
                // lisätään tyhjät lainaukset loppuun -> string-muoto
                intent.putExtra("xtraRyhmaId", ryhmat.get(position).getGroup_id() + "");
                intent.putExtra("xtraRyhmaNimi", ryhmat.get(position).getGroupName());
                intent.putExtra("xtraRyhmaLuoja", ryhmat.get(position).getCreator() + "");

                startActivity(intent);
            }
        });

        // yritetään lukea käyttäjätunnus puhelimen muistista
        // jos sitä ei löydy, näytetään rekisteröintidialogi

        //gv.currentUser.getUserName();

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_nimi), Context.MODE_PRIVATE);
        kayttaja = sharedPref.getString("kayttajatunnus", "");
        Log.d("oma", "luettiin muistista käyttäjä " + kayttaja);
        setTitle(getString(R.string.title_activity_main) + " (" + kayttaja + ")");

        // jos puhelimen muistissa ei ole käyttäjätunnusta, näytetään rekisteröintidialogi
        if(kayttaja.length() < 1) {
            DialogFragment kayttajaFragment = new LisaaKayttajaDialogFragment();
            kayttajaFragment.show(getSupportFragmentManager(), "lisaakayttaja");
        }
    }


    public void cancelNotification(Context ctx, int notifyId) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
        nMgr.cancel(notifyId);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if(id == R.id.action_rekisterointi) {
            // tässä käynnistetään rekisteröintitoiminto
            DialogFragment kayttajaFragment = new LisaaKayttajaDialogFragment();
            kayttajaFragment.show(getSupportFragmentManager(), "lisaakayttaja");
            return true;
        }
        // kun painetaan lisää ryhmä -kuvaketta action barissa
        else if(id == R.id.action_add_group) {
            DialogFragment newFragment = new LisaaRyhmaDialogFragment();
            newFragment.show(getSupportFragmentManager(), "lisaaryhma");
            return true;
        }


        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // otetaan vastaan dialogi (joka lähetti itsensä tänne)
        // ja luetaan dialogin syöttökenttien sisältö
        Dialog dialogView = dialog.getDialog();
        EditText etRyhmaNimi = (EditText)dialogView.findViewById(R.id.ryhmanimi);
        EditText etRyhmaSalasana = (EditText)dialogView.findViewById(R.id.ryhmasalasana);

        // luetaan oma käyttäjä-id puhelimen muistista
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_nimi), Context.MODE_PRIVATE);
        String kayttajaid = sharedPref.getString("kayttajaid", "");

        // lähetetään käyttäjän kirjoittaman ryhmän nimi ja salasana serverille
        new lisaaRyhmaTask().execute(kayttajaid,
                etRyhmaNimi.getText().toString(),
                etRyhmaSalasana.getText().toString());

        // haetaan ryhmät www-palvelimelta asynctaskin avulla
        // nyt uusi ryhmä pitäisi olla mukana
        gv.getAllGroups();
        //haeRyhmatTask task = new haeRyhmatTask();
        //task.execute(new String[]{getString(R.string.polku_hae_ryhmat)});

        // lisätään syötetty ryhmä currentGroups-taulukkoon
        // TODO: muuta tämä niin, että se toimii ryhmäolioiden avulla
        // currentGroups.add(etRyhmaNimi.getText().toString());
        // arrayAdapter.notifyDataSetChanged();

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
    }

    @Override
    public void onGroupListUpdate(){
        Log.d("oma", "Grouplistupdate callback!");
        ryhmat = gv.currentGroups;
        arrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCurrentUserUpdate(){
        setTitle(getString(R.string.title_activity_main) + " (" + gv.currentUser.getUserName() + ")");
    }

    @Override
    public void onMapGroupUpdate(Group group){
        // FIXME: 12.02.2016 figure out a way (probably instancing) to not need to handle all GVListener callbacks
    }

    @Override
    public void onKayttajaRegisterClick(DialogFragment dialog) {
        // otetaan vastaan dialogi (joka lähetti itsensä tänne)
        // ja luetaan dialogin syöttökenttien sisältö
        Dialog dialogView = dialog.getDialog();
        EditText etKayttajaNimi = (EditText)dialogView.findViewById(R.id.kayttajanimi);
        EditText etKayttajaSalasana = (EditText)dialogView.findViewById(R.id.kayttajasalasana);

        gv.registerUser(etKayttajaNimi.getText().toString(), etKayttajaSalasana.getText().toString());
   }

    @Override
    public void onKayttajaLoginClick(DialogFragment dialog) {
        //Interface callback for logging in
        Dialog dialogView = dialog.getDialog();
        EditText etKayttajaNimi = (EditText)dialogView.findViewById(R.id.kayttajanimi);
        EditText etKayttajaSalasana = (EditText)dialogView.findViewById(R.id.kayttajasalasana);

        gv.loginUser(etKayttajaNimi.getText().toString(), etKayttajaSalasana.getText().toString());
    }

    public void onDebugClick(View v){
        arrayAdapter.notifyDataSetChanged();
        Log.d("oma", "BUTTON onDebugClick");
        ArrayList<Group> groupList = new ArrayList<>(gv.getAllGroups());

    }

    //sisäluokka, joka hoitaa yhteydenottoja serverin suuntaan
    // AsyncTask<params, progress, result>
    private class rekisteroidyTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... tunnukset) {
            Log.d("oma", "annoit tunnuksen: " + tunnukset[0]);
            Log.d("oma", "annoit salasanan: " + tunnukset[1]);

            // TODO: mitä jos postData palauttaa null?
            // String k_id = postData(tunnukset[0], tunnukset[1]);

            // luodaan hashmap-olio postitettavista tiedoista
            HashMap<String, String> postTiedot = new HashMap<>();
            postTiedot.put("kt", tunnukset[0]);
            postTiedot.put("ss", tunnukset[1]);

            ApuHttp postittaja = new ApuHttp();
            String k_id = postittaja.postData(getString(R.string.polku_lisaa_kayttaja), postTiedot);

            return k_id;
        }// DoInBackground

        @Override
        protected void onPostExecute(String result) {
            // jos result on null, käyttäjä-id:tä ei palautunut serveriltä
            if(result != null && result.length() > 0) {
                // tallennetaan käyttäjä-id puhelimen muistiin
                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_nimi), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("kayttajaid", result);
                editor.apply();

                kayttaja = sharedPref.getString("kayttajatunnus", "");
                setTitle(getString(R.string.title_activity_main) + " (" + kayttaja + ")");
            }// if
            else {
                // TODO: muuta valikkoteksti strings-tiedostoon
                Toast.makeText(getApplicationContext(),
                        "Käyttäjä-id:n haku epäonnistui",
                        Toast.LENGTH_LONG).show();
            }// else

        }// onPostExec
    }//rek.task


    //sisäluokka, joka hoitaa yhteydenottoja serverin suuntaan
    // AsyncTask<params, progress, result>
    private class lisaaRyhmaTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... tunnukset) {

            // TODO: mitä jos postData palauttaa null?
            // tunnukset[0]=ryhmän luojan käyttäjä-id, rl
            // tunnukset[1]=ryhmän nimi, rn
            // tunnukset[2]=ryhmän salasana, rs
            HashMap<String, String> postiTiedot = new HashMap<>();
            postiTiedot.put("rl", tunnukset[0]);
            postiTiedot.put("rn", tunnukset[1]);
            postiTiedot.put("rs", tunnukset[2]);

            ApuHttp postiMies = new ApuHttp();
            String r_id = postiMies.postData(getString(R.string.polku_lisaa_ryhma), postiTiedot);

            return r_id;
        }// DoInBackground

        @Override
        protected void onPostExecute(String result) {
            // jos result on null, käyttäjä-id:tä ei palautunut serveriltä
            if(result != null && result.length() > 0) {
                // ryhmän lisäys onnistui
                Log.d("oma", "serveriltä palautui ryhmä-id: " + result);
                ryhmaId = result;
            }// if
            else {
                // TODO: muuta valikkoteksti strings-tiedostoon
                Toast.makeText(getApplicationContext(),
                        "Ryhmän lisäys epäonnistui",
                        Toast.LENGTH_LONG).show();
            }// else
        }// onPostExec
    }//lisaa ryhma task

}// ****************  mainactivity   ***************************
