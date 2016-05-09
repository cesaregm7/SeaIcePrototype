package com.seaice.csar.seaiceprototype;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

public class MapsActivity extends AppCompatActivity implements GoogleMap.OnInfoWindowClickListener ,OnMapReadyCallback {

    private GoogleMap mMap;
    ProtocolParser miProtocolo = new ProtocolParser();
    ArrayList<ProtocolParser.Information> informacion;
    Dictionary dicCoordenadas = new Hashtable();
    Dictionary dicInfo = new Hashtable();
    String prueba = "0^0^I saw a bear near this area^1^2.456,3.5546~1^0^Thin ice^1^2.456,3.5546~2^0^Blizzard^1^2.456,3.5546";
    boolean iRuta = false;
    Context ctx;
    ArrayList<Integer> keyList = new ArrayList<>();
    ArrayList<Integer> idsMandar = new ArrayList<>();
    ArrayList<Double> latsMandar = new ArrayList<>();
    ArrayList<Double> lngsMandar = new ArrayList<>();
    private static MapsActivity mapsAct;

    int indiceActual = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ctx = this;

        final LinearLayout ll = new LinearLayout(this);
        final ImageButton closeRoute = new ImageButton(this);
        closeRoute.setImageResource(R.drawable.save);
        closeRoute.setBackgroundColor(0);
        closeRoute.setPadding(0,0,50,50);
        closeRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                iRuta = false;
                ll.removeAllViews();

                //MANDAR DATA AL SERVER
                String idsS = "";
                if (idsMandar.size() != 0) {

                    for (Integer id : idsMandar) {
                        idsS += id + ",";
                    }
                    idsS = idsS.substring(0, idsS.length() - 1);
                    final String idSFinal = idsS;

                    String latS = "";
                    for (Double lat : latsMandar) {
                        latS += lat + ",";
                    }
                    latS = latS.substring(0, latS.length() - 1);
                    final String latSFinal =latS;

                    String lngS = "";
                    for (Double lng : lngsMandar) {
                        lngS += lng + ",";
                    }
                    lngS = lngS.substring(0, lngS.length() - 1);
                    final String lngSFinal = lngS;

                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            HttpManager hm = new HttpManager();
                            hm.execute(idSFinal, latSFinal, lngSFinal);
                        }
                    });

                    thread.run();

                }
            }
        });
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mMap = mapFragment.getMap();


        double[] punto1 = {64.2008, -149.4937};
        double[] punto2 = {50.5889, -82.3308};
        double[] punto3 = {38.89, -77.03};
        String[] coso1 = {"","","",""};
        String[] coso2 = {"","","",""};
        String[] coso3 = {"","","",""};

        dicCoordenadas.put(0, punto1);
        dicCoordenadas.put(1, punto2);
        dicCoordenadas.put(2, punto3);
        dicInfo.put(0, coso1);
        dicInfo.put(1, coso2);
        dicInfo.put(2, coso3);
        keyList.add(0);
        keyList.add(1);
        keyList.add(2);
        FabSpeedDial fabSpeedDial = (FabSpeedDial) findViewById(R.id.fab_speed_dial);
        assert fabSpeedDial != null;
        fabSpeedDial.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                String botonSeleccionado = (String) menuItem.getTitle();
                if (botonSeleccionado.equals("Refresh")) {
                    //DETERMINAR SI HAY WIFI
                    if(isNetworkAvailable())
                    {
                        //
                    }
                    else
                    {
                        sendRequest();
                    }
                    //RECIBIR REQUEST
                    //putDataMap(prueba);




                } else if (botonSeleccionado.equals("Route")) {
                    iRuta = true;
                    ll.addView(closeRoute);
                    idsMandar.clear();
                    lngsMandar.clear();
                    latsMandar.clear();


                } else if (botonSeleccionado.equals("Report")) {
                    DialogReport dr = new DialogReport();
                    dr.show(getFragmentManager(), "Report");

                }
                return false;
            }
        });
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.marker_information, null);

                LatLng latLng = marker.getPosition();
                double[] coordTemp;
                int indiceMarker = -1;
                for (int i = 0; i < keyList.size(); i++) {
                    coordTemp = (double[]) dicCoordenadas.get(keyList.get(i));
                    if (latLng.latitude == coordTemp[0] && latLng.longitude == coordTemp[1]) {
                        indiceMarker = keyList.get(i);
                    }
                }

                if (!(indiceMarker == -1)) {
                    String[] infoTemp = (String[]) dicInfo.get(indiceMarker);
                    if (!infoTemp[0].equals("")) {
                        TextView temperatura = (TextView) v.findViewById(R.id.temperaturaMarker);
                        temperatura.setText("Weather: " + infoTemp[0]);
                    }
                    if (!infoTemp[1].equals("")) {
                        TextView viento = (TextView) v.findViewById(R.id.vientoMarker);
                        viento.setText("Wind: " + infoTemp[1]);
                        System.out.println("entra a Viento");
                    }
                    if (!infoTemp[2].equals("")) {
                        TextView hielo = (TextView) v.findViewById(R.id.hieloMarker);
                        hielo.setText("Ice: " + infoTemp[2]);
                    }
                    if (!infoTemp[3].equals("")) {
                        TextView texto = (TextView) v.findViewById(R.id.textoMarker);
                        texto.setText("Details: " + infoTemp[3]);
                        System.out.println("entra a Texto");
                    }
                }


                /*TextView latitud = (TextView) v.findViewById(R.id.latitudMarker);
                latitud.setText("Latitud: " + latLng.latitude);

                TextView longitud = (TextView) v.findViewById(R.id.longitudMarker);
                longitud.setText("Longitud: " + latLng.longitude);*/

                return v;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (iRuta == true) {
                    //mMap.clear();
                    MarkerOptions options = new MarkerOptions();

                    options.position(latLng);
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    Marker marker = mMap.addMarker(options);
                    marker.showInfoWindow();
                    indiceActual += 1;
                    keyList.add(indiceActual);
                    dicCoordenadas.put(indiceActual, new double[]{latLng.latitude, latLng.longitude});
                    dicInfo.put(indiceActual, new String[]{"", "", "", ""});
                    //---------------Agregar Puntos a Listas-------------------
                    idsMandar.add(indiceActual);
                    latsMandar.add(latLng.latitude);
                    lngsMandar.add(latLng.longitude);
                }

            }
        });
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mapFragment.getMapAsync(this);

        final Switch travelingMode = (Switch) findViewById(R.id.switch1);
        travelingMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(travelingMode.isChecked()){
                    travelingMode.setText("Traveling: On");
                }
                else{
                    travelingMode.setText("Traveling: Off");
                }
            }
        });
        ll.setGravity((Gravity.RIGHT | Gravity.BOTTOM));
        this.addContentView(ll,
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));

        mapsAct = this;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng alaska = new LatLng(64.2008, -149.4937);
        LatLng canada = new LatLng(50.5889, -82.3308);
        LatLng usa = new LatLng(38.89, -77.03);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.addMarker(new MarkerOptions()
                .position(alaska)
                .title("Alaska"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(alaska));

        mMap.addMarker(new MarkerOptions()
                .position(canada)
                .title("Canada"));

        mMap.addMarker(new MarkerOptions()
                .position(usa)
                .title("United States"));
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    public void putDataMap(String info){

        info= info.replace('¿', '^');

        double [] coordenadas;
        int index;
        String[] dataCoordenada;
        informacion = miProtocolo.parse(info);
        //System.out.println(informacion.size()+"");
        for(int i=0;i<informacion.size();i++){
            System.out.println(informacion.get(i).type);
            index = informacion.get(i).id;
            coordenadas = (double[]) dicCoordenadas.get(index);
            if(informacion.get(i).type==0){
                ((String[])dicInfo.get(index))[3] = informacion.get(i).text;
            }
            else if(informacion.get(i).type==1){
                ((String[])dicInfo.get(index))[1] = informacion.get(i).number+" "+informacion.get(i).direction;
            }
            else if(informacion.get(i).type==2){
                ((String[])dicInfo.get(index))[0] = informacion.get(i).number+"";
            }
            else{
                ((String[])dicInfo.get(index))[2] = informacion.get(i).number+"";
            }
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
    protected void sendRequest(){
        String requestString = "get:";
        for (int localIdC : keyList){
            requestString+=Integer.toString(localIdC)+",";
        }
        requestString = requestString.substring(0, requestString.length() - 1);

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage("+50258228830", null, requestString, null, null);
            Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
        }

        catch (Exception e) {
            Toast.makeText(getApplicationContext(), "SMS failed, please try again.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }

    public static MapsActivity getInstance()
    {
        return mapsAct;
    }
}
