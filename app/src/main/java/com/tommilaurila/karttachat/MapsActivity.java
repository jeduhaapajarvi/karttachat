package com.tommilaurila.karttachat;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
//import android.location.LocationListener;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.NumberPicker;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends android.support.v7.app.AppCompatActivity
    implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        PaivitysvaliDialogFragment.PaivitysvaliDialogListener
{

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private Marker omaSijainti;
    private Timer lahetysAjastin = new Timer();
    private String kayttajaNimi;
    private String kayttajaId;
    private String ryhmaNimi;
    private String ryhmaId;
    private String ryhmaLuoja;
    private String chatViesti = "testi";
    private ArrayList<Kayttaja> kayttajat = new ArrayList<>();
    private boolean seurantaPaalla = false;
    private int paivitysVali = 60;

    final String LOPETA_SEURANTA = "com.tommilaurila.tie13karttademo.lopetaseuranta";
    final String NOTIF_CLICK = "com.tommilaurila.tie13karttademo.notifclick";


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_maps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_paivitysvali) {
            DialogFragment pvFragment = new PaivitysvaliDialogFragment();
            Bundle b = new Bundle();
            b.putInt("sek", paivitysVali);
            pvFragment.setArguments(b);
            pvFragment.show(getSupportFragmentManager(), "paivitysvali");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void luePaivitysvali() {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_paivitysvali), Context.MODE_PRIVATE);
        paivitysVali = sharedPref.getInt("paivitysvali", 60);
    }


    public void onPaivitysvaliPositiveClick(DialogFragment dialog) {
        Dialog dialogView = dialog.getDialog();
        NumberPicker np = (NumberPicker)dialogView.findViewById(R.id.numberPicker);

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_paivitysvali), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("paivitysvali", np.getValue());
        editor.apply();

        paivitysVali = np.getValue();
    }


    public void onPaivitysvaliNegativeClick(DialogFragment dialog) {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Log.d("oma","ollaan onCreatessa (MapsActivity)");

        setTitle("Kartta");

        luePaivitysvali();

        setUpMapIfNeeded();
        buildGoogleApiClient();
        createLocationRequest();

        // luetaan mainactivitystä lähetetyt käyttäjä- ja ryhmänimet
        Intent intent = getIntent();

        // jos aktiviteetti avattiin notifikaatiosta, katsotaan painettiinko
        // lopeta seuranta-nappia
        if(intent.getAction() != null) {
            if(intent.getAction().equals(LOPETA_SEURANTA)) {
                seurantaPaalla = false;
                Log.d("oma", "Painettiin lopeta seuranta notifikaatiosta");
                lueKayttajaTiedot();

                poistuRyhmastaTask prt = new poistuRyhmastaTask();
                String poistoPolku = getString(R.string.polku_poistu_ryhmasta) + "?kid=" + kayttajaId;
                Log.d("oma", "poistopolku on " + poistoPolku);
                prt.execute(poistoPolku);

                Intent palaaRyhmaAktiviteettiinIntent = new Intent(this, MainActivity.class);
                startActivity(palaaRyhmaAktiviteettiinIntent);
            }
            else if(intent.getAction().equals(NOTIF_CLICK)) {
                // luetaan nykyiset käyttäjä- ja ryhmätiedot muistista
                lueKayttajaTiedot();

                seurantaPaalla = true;
                Log.d("oma", "notif click");
            }
        }
        else {
            seurantaPaalla = true;

            kayttajaId = intent.getStringExtra("xtraKayttajaId");
            kayttajaNimi = intent.getStringExtra("xtraKayttajaNimi");
            ryhmaId = intent.getStringExtra("xtraRyhmaId");
            ryhmaNimi = intent.getStringExtra("xtraRyhmaNimi");
            ryhmaLuoja = intent.getStringExtra("xtraRyhmaLuoja");

            kirjoitaKayttajaTiedot(kayttajaId, kayttajaNimi, ryhmaId, ryhmaNimi, ryhmaLuoja);
        }

        Kayttaja mina = new Kayttaja();
        mina.setNimimerkki(kayttajaNimi);

        // luodaan uusi sijaintiolio, johon koordinaatit sijoitetaan
        // TODO: pyydetään oma alkusijainti laitteen paikannuspalvelulta
        Sijainti olenTassa = new Sijainti(63.731818, 25.333794);

        // asetetaan sijaintiolio mina-olion ominaisuudeksi
        mina.setSijainti(olenTassa);


        // luodaan ajastin, joka lähettää omaa sijaintia serverille säännöllisesti
        kaynnistaAjastin();

        setTitle("Ryhmä: " + ryhmaNimi);
    }// onCreate


    private void lueKayttajaTiedot() {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_maps_userinfo), Context.MODE_PRIVATE);
        String k_id = sharedPref.getString("k_id", "");
        if(k_id.length() > 0) kayttajaId = k_id;
        String k_nimi = sharedPref.getString("k_nimi", "");
        if(k_nimi.length() > 0) kayttajaNimi = k_nimi;
        String r_id = sharedPref.getString("r_id", "");
        if(r_id.length() > 0) ryhmaId = r_id;
        String r_nimi = sharedPref.getString("r_nimi", "");
        if(r_nimi.length() > 0) ryhmaNimi = r_nimi;
        String r_luoja = sharedPref.getString("r_luoja", "");
        if(r_luoja.length() > 0) ryhmaLuoja = r_luoja;
    }


    private void kirjoitaKayttajaTiedot(String k_id, String k_nimi, String r_id, String r_nimi, String r_luoja) {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_maps_userinfo), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("k_id", k_id);
        editor.putString("k_nimi", k_nimi);
        editor.putString("r_id", r_id);
        editor.putString("r_nimi", r_nimi);
        editor.putString("r_luoja", r_luoja);
        editor.apply();
    }


    private void kaynnistaAjastin() {
        lahetysAjastin.schedule(new TimerTask(){
            @Override
            public void run() {
                if(seurantaPaalla) {
                    naytaNotification(ryhmaNimi);

                    if(mLastLocation != null) {

                        LatLng munPaikka = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                        // oma käyttäjänimi, oma ryhmänimi, oma sijainti, chat-viesti
                        Log.d("oma", "Sijaintipäivitys::: " +
                                "oma id:" + kayttajaId +
                                " ryhmä-id:" + ryhmaId +
                                " mun paikka: " + munPaikka +
                                " chatviesti: " + chatViesti);
                        Log.d("oma", "päivitysväli on " + paivitysVali + " sek.");
                        lahetaOmaSijainti(kayttajaId, ryhmaId, munPaikka, chatViesti);
                    }//if mLoc != null
                }//if seurantaPaalla
            }//run
        }, 1000, paivitysVali * 1000); // starts your code after 1000 milliseconds, then repeat it every 30 sec. (30000 milliseconds)
    }


    private void naytaNotification(String rNimi) {
        //TODO: tämä on vain hatusta vedetty id notifikaatiolle
        int mId = 111;

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MapsActivity.class);
        resultIntent.setAction(NOTIF_CLICK);

        Intent lopetaSeurantaIntent = new Intent(this, MapsActivity.class);
        lopetaSeurantaIntent.setAction(LOPETA_SEURANTA);

        PendingIntent pIntent = PendingIntent.getActivity(this, 0, lopetaSeurantaIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MapsActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.Action pauseAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_disable_tracking,
                        getString(R.string.stop_tracking),
                        pIntent).build();

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_action_settings_input_antenna)
                        .setContentTitle(getString(R.string.title_activity_main))
                        .setContentText(getString(R.string.your_group) + " " + rNimi)
                        .addAction(pauseAction);

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(mId, mBuilder.build());
    }


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        Log.d("oma", "ollaan onStartissa (MapsActivity)");
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("oma", "ollaan onResumessa (MapsActivity)");
        setUpMapIfNeeded();
        seurantaPaalla = true;
        if(lahetysAjastin == null) {
            lahetysAjastin = new Timer();
            kaynnistaAjastin();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("oma", "ollaan onstopissa (MapsActivity)");
        seurantaPaalla = false;
        // notifikaation keksitty id on 111
        cancelNotification(this, 111);
    }


    public void cancelNotification(Context ctx, int notifyId) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
        nMgr.cancel(notifyId);
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    protected void createLocationRequest() {
        Log.d("oma", "create location request");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000); // 10 sek.
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        Log.d("oma", "start location updates");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        TileProvider wmsTileProvider = TileProviderFactory.getOsgeoWmsTileProvider();
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(wmsTileProvider));

        LatLng sijainti = new LatLng(63.756853, 25.322207);
        Log.d("oma", "max zoom " + mMap.getMaxZoomLevel());

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sijainti, 8.0f));
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.d("oma", "Ollaan onConnected!");
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("oma", "paikka vaihtui");
        mLastLocation = location;
    }


    private void updateUI() {
        Log.d("oma", "Lat on: " + String.valueOf(mLastLocation.getLatitude()));
        Log.d("oma", "Long on: " + String.valueOf(mLastLocation.getLongitude()));

        float kuvakeVari;

        // TODO: tässä päivitetään kayttajat-taulukossa olevien käyttäjien sijainnit
        for(int i=0; i<kayttajat.size(); i++) {
            Kayttaja kaveri = kayttajat.get(i);

            String kid = kaveri.getKayttaja_id() + "";

            // minä näyn punaisena
            if(kid.equals(kayttajaId)) {
                kuvakeVari = BitmapDescriptorFactory.HUE_RED;
            }

            // ryhmän perustaja (opettaja) näkyy keltaisena
            else if(kid.equals(ryhmaLuoja)) {
                kuvakeVari = BitmapDescriptorFactory.HUE_YELLOW;
            }
            // muut näkyvät sinisenä
            else {
                kuvakeVari = BitmapDescriptorFactory.HUE_AZURE;
            }

            Marker m = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(kaveri.getSijainti().getLat(), kaveri.getSijainti().getLng()))
                    .title(kaveri.getNimimerkki())
                    .alpha(markerinAlfa(kaveri)) // haetaan alfa iän mukaan
                    .icon(BitmapDescriptorFactory.defaultMarker(kuvakeVari))
                            .snippet("nähty " + kaveri.getViimeksi_nahty()));
            kaveri.setMerkki(m);
        }
    }


    private float markerinAlfa(Kayttaja k) {
        float palautusAlfa = -1f;

        try {
            // serveriltä tullut nykyinen aika
            Calendar serveriAika = Calendar.getInstance();
            String[] sosat = k.getServeriAika().split(" ");
            String[] spvmOsat = sosat[0].split("-");
            String[] saikaOsat = sosat[1].split(":");
            serveriAika.set(Integer.parseInt(spvmOsat[0]),// vuosi
                    Integer.parseInt(spvmOsat[1]) -1,    // kuukausi, alkaa nollasta
                    Integer.parseInt(spvmOsat[2]),       // päivä
                    Integer.parseInt(saikaOsat[0]),      // tunti
                    Integer.parseInt(saikaOsat[1]),      // minuutti
                    Integer.parseInt(saikaOsat[2]));     // sekunti

            // käyttäjän viimeksi-nähty-aika
            Calendar viim_nahty = Calendar.getInstance();
            String[] osat = k.getViimeksi_nahty().split(" ");
            String[] pvmOsat = osat[0].split("-");
            String[] aikaOsat = osat[1].split(":");
            viim_nahty.set(Integer.parseInt(pvmOsat[0]),// vuosi
                    Integer.parseInt(pvmOsat[1]) -1,    // kuukausi, alkaa nollasta
                    Integer.parseInt(pvmOsat[2]),       // päivä
                    Integer.parseInt(aikaOsat[0]),      // tunti
                    Integer.parseInt(aikaOsat[1]),      // minuutti
                    Integer.parseInt(aikaOsat[2]));     // sekunti

            long ika_ms = serveriAika.getTimeInMillis() - viim_nahty.getTimeInMillis();

            // muutetaan millisekuntiaika int-luvuksi helpomman käsittelyn takia
            // tarkistetaan kuitenkin, että long-arvo voidaan muuttaa int-luvuksi
            int ikaMin;

            if(ika_ms < Integer.MAX_VALUE && ika_ms > Integer.MIN_VALUE)
                ikaMin = (int)(ika_ms / 1000) / 60;
            else if(ika_ms > Integer.MAX_VALUE)
                ikaMin = Integer.MAX_VALUE;
            else ikaMin = 0;

            // asetetaan alfa-arvo markerin iän perusteella
            if(ikaMin < 16) palautusAlfa = 1.0f;
            else if(ikaMin < 31) palautusAlfa = 0.8f;
            else palautusAlfa = 0.5f;

            Log.d("oma", "serveriaika: " + "ikä min: " + ikaMin + " alfa: " + palautusAlfa);
        } catch (Exception e) {
            Log.d("oma", "pvm:n parsintavirhe: " + e.toString());
        }

        return palautusAlfa;
    }


    // UC7: oman sijainnin lähetys palvelimelle
    private void lahetaOmaSijainti(String kayttajaNimi, String ryhmaNimi,
                                   LatLng sijainti, String chatViesti) {
        // tässä lähetetään kaikki tiedot: oma käyttäjänimi, nykyinen ryhmänimi,
        // oma sijainti ja mahdollinen chat-viesti www-palvelimelle
        // haetaan ryhmät www-palvelimelta asynctaskin avulla
        if(kayttajaNimi != null && ryhmaNimi != null && sijainti != null) {
            IlmoitaSijaintiTask ist = new IlmoitaSijaintiTask();
            ist.execute(new String[]{getString(R.string.polku_ilmoita_sijainti),
                    kayttajaNimi,
                    ryhmaNimi,
                    sijainti.latitude + "",
                    sijainti.longitude + "",
                    chatViesti});
        }
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    private class poistuRyhmastaTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String result = "";

            ApuHttp postiMies = new ApuHttp();
            return postiMies.getSivu(urls[0]);
        }// doInBackground

        @Override
        protected void onPostExecute(String result) {
            //Log.d("oma", "käyttäjä poistettiin ryhmästä");
        }
    }//poistuRyhmästäTask


    private class IlmoitaSijaintiTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String result = "";

            // "http://172.19.129.105/r0/sijainti/ilmoita"
            // parametrit: url, uid, gid, lat, lng, msg
            HashMap<String, String> postiTiedot = new HashMap<>();
            postiTiedot.put("uid", urls[1]);
            postiTiedot.put("gid", urls[2]);
            postiTiedot.put("lat", urls[3]);
            postiTiedot.put("lng", urls[4]);
            postiTiedot.put("msg", urls[5]);

            // lähetetään omat tiedot ja vastaanotetaan samalla
            // muiden ryhmäläisten tiedot JSON-taulukkona
            ApuHttp postiMies = new ApuHttp();
            return postiMies.postData(urls[0], postiTiedot);
        }// doInBackground

        @Override
        protected void onPostExecute(String result) {
            Log.d("oma", "Serveri vastasi (sijaintiupdate): " + result);

            //yritetään parsia ryhmät ulos JSON-objektista
            // serveriltä saapuu JSON-array [ {...} ]
            try {
                JSONArray ryhmalista = new JSONArray(result);
                Log.d("oma", "JSONArrayn pituus on " + ryhmalista.length());

                // poistetaan kaikki vanhat markkerit kartalta
                for(int i=0; i<kayttajat.size(); i++) {
                    kayttajat.get(i).getMerkki().remove();
                    kayttajat.get(i).setMerkki(null);
                }

                // tyhjennetään käyttäjät-taulukko vanhoista merkinnöistä
                kayttajat.clear();

                // käydään JSONtaulukko läpi ja luodaan ryhma-olio, jokaisesta
                // JSON-objektista
                for(int i=0; i<ryhmalista.length(); i++) {
                    Kayttaja k = new Kayttaja();

                    // haetaan JSONarraysta i:nnes objekti
                    JSONObject job = ryhmalista.getJSONObject(i);

                    // täytetään käyttäjäolion kentät JSONobjektista
                    Sijainti s = new Sijainti();
                    s.setSijainti_id(job.getInt("sijainti_id"));
                    s.setLat(job.getDouble("lat"));
                    s.setLng(job.getDouble("lng"));
                    s.setAikaleima(job.getString("aikaleima"));
                    s.setKayttaja_id(job.getInt("kayttaja_id"));

                    Kayttaja kaveri = new Kayttaja();
                    kaveri.setKayttaja_id(job.getInt("kayttaja_id"));
                    kaveri.setNimimerkki(job.getString("nimimerkki"));
                    kaveri.setViimeksi_nahty(job.getString("viimeksi_nahty"));
                    kaveri.setRyhma_id(job.getInt("ryhma_id"));
                    kaveri.setServeriAika(job.getString("serveriaika"));
                    kaveri.setSijainti(s);

                    // lisätään luotu käyttäjäolio aktiviteetin kayttajat-taulukkoon
                    kayttajat.add(kaveri);
                }//for

                // päivitetään markkerit kartalle
                updateUI();
            }//try
            catch (Exception e) {
                Log.d("oma", "tuli virhe "+ e.toString());
            }
        }
    }//IlmoitaSijaintiTask

}// class mapsactivity
