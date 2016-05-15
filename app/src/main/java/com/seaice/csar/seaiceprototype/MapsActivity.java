package com.seaice.csar.seaiceprototype;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

public class MapsActivity extends AppCompatActivity implements GoogleMap.OnInfoWindowClickListener ,OnMapReadyCallback {

    private GoogleMap mMap;
    ProtocolParser miProtocolo = new ProtocolParser();
    ArrayList<ProtocolParser.Information> informacion;
    Dictionary dicCoordRut = new Hashtable();
    Dictionary dicInfRut = new Hashtable();
    Dictionary dicMarkRut= new Hashtable();
    Dictionary dicCoordRep = new Hashtable();
    Dictionary dicInfRep = new Hashtable();
    Dictionary dicMarkRep = new Hashtable();
    boolean iRuta = false;
    boolean iBorrar = false;
    boolean iReportar = false;
    Context ctx;
    ArrayList<Integer> keyListRut = new ArrayList<>();
    ArrayList<Integer> keyListRep = new ArrayList<>();
    ArrayList<Integer> idsMandar = new ArrayList<>();
    ArrayList<Double> latsMandar = new ArrayList<>();
    ArrayList<Double> lngsMandar = new ArrayList<>();
    private static MapsActivity mapsAct;
    public LocationDbHelper myLocationDbHelper = new LocationDbHelper(this);

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
        closeRoute.setPadding(0, 0, 50, 50);
        closeRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //----------------------------Cerrar crear ruta--------------------------
                if(iRuta){
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
                                HttpInsert hm = new HttpInsert();
                                hm.execute(idSFinal, latSFinal, lngSFinal);
                            }
                        });

                        thread.run();

                    }
                }
                //-----------------------Cerrar borrar puntos-------------------------
                if(iBorrar){
                    iBorrar = false;
                    ll.removeAllViews();
                }

            }
        });
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mMap = mapFragment.getMap();


        /*double[] punto1 = {64.2008, -149.4937};
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
        keyList.add(2);*/
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
                        sendRequestNetwork();
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
                    //-------Nuevo------
                    iReportar = true;

                }else if (botonSeleccionado.equals("Delete")) {
                    iBorrar = true;
                    ll.addView(closeRoute);

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

                /*---------------Marcadores de Ruta--------------------*/
                LatLng latLng = marker.getPosition();
                double[] coordTemp;
                int indiceMarker = -1;
                for (int i = 0; i < keyListRut.size(); i++) {
                    coordTemp = (double[]) dicCoordRut.get(keyListRut.get(i));
                    if (latLng.latitude == coordTemp[0] && latLng.longitude == coordTemp[1]) {
                        indiceMarker = keyListRut.get(i);
                    }
                }

                if (!(indiceMarker == -1)) {
                    String[] infoTemp = (String[]) dicInfRut.get(indiceMarker);
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

                /*----------------Marcadores de Reporte FALTA-----------------------*/
                for (int i = 0; i < keyListRep.size(); i++) {
                    coordTemp = (double[]) dicCoordRep.get(keyListRep.get(i));
                    if (latLng.latitude == coordTemp[0] && latLng.longitude == coordTemp[1]) {
                        indiceMarker = keyListRep.get(i);
                    }
                }

                /*TextView latitud = (TextView) v.findViewById(R.id.latitudMarker);
                latitud.setText("Latitud: " + latLng.latitude);

                TextView longitud = (TextView) v.findViewById(R.id.longitudMarker);
                longitud.setText("Longitud: " + latLng.longitude);

                ImageView imagen = (ImageView) v.findViewById(R.id.imageMarker);

                TextView titulo = (TextView) v.findViewById(R.id.titleMarker);

                TextView descripcion = (TextView) v.findViewById(R.id.textoMarker);*/

                return v;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (iRuta) {
                    //mMap.clear();
                    MarkerOptions options = new MarkerOptions();

                    indiceActual = (int) myLocationDbHelper.insertFullLocation(latLng.latitude, latLng.longitude);
                    myLocationDbHelper.updateInfo(indiceActual," ~ ~ ~ ");
                    options.position(latLng);
                    options.title(indiceActual+"");
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    Marker marker = mMap.addMarker(options);
                    marker.showInfoWindow();

                    keyListRut.add(indiceActual);
                    dicCoordRut.put(indiceActual, new double[]{latLng.latitude, latLng.longitude});
                    dicInfRut.put(indiceActual, new String[]{"", "", "", ""});
                    dicMarkRut.put(indiceActual, marker);
                    //---------------Agregar Puntos a Listas-------------------
                    idsMandar.add(indiceActual);
                    latsMandar.add(latLng.latitude);
                    lngsMandar.add(latLng.longitude);
                }
                if (iReportar) {
                    //------------------FALTA
                    MarkerOptions options = new MarkerOptions();

                    options.position(latLng);
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    //options.
                    Marker marker = mMap.addMarker(options);
                    marker.showInfoWindow();
                    DialogReport dr = new DialogReport(marker, latLng.latitude, latLng.longitude);
                    dr.show(getFragmentManager(), "Report");

                    //indiceActual = (int) myLocationDbHelper.insertFullReport(latLng.latitude, latLng.longitude, dr.hfu.Title, dr.hfu.Description, dr.finalFile.getAbsolutePath());




                }

            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                //LatLng latLng = marker.getPosition();

                if(iBorrar)
                {
                    final int id = Integer.parseInt(marker.getTitle());
                    marker.remove();

                    int indexTemp = keyListRut.indexOf(id);
                    if(indexTemp==-1){
                        indexTemp = keyListRep.indexOf(id);
                        keyListRep.remove(indexTemp);
                        dicCoordRep.remove(indexTemp);
                        dicInfRep.remove(indexTemp);
                        myLocationDbHelper.deleteReport(id);
                    }
                    else{
                        keyListRut.remove(indexTemp);
                        dicCoordRut.remove(id);
                        dicInfRut.remove(id);
                        dicMarkRut.remove(id);
                        myLocationDbHelper.deleteLocation(id);
                    }
                    //---------------------FALTA
                    new HttpDelete().execute(id + "");
/*
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            HttpDelete myHttpDelete = new HttpDelete();
                            myHttpDelete.execute(id + "");
                        }
                    });

                    thread.run();
*/

                    /*
                    double latTemp, longTemp;
                    latTemp = latLng.latitude;
                    longTemp = latLng.longitude;
                    for (int indiceMarcadores : keyList) {
                        Toast.makeText(MapsActivity.this, keyList + " :v", Toast.LENGTH_SHORT).show();
                        double[] coordTemp = (double[]) dicCoordenadas.get(indiceMarcadores);
                        if (coordTemp[0] == latTemp && coordTemp[1] == longTemp) {
                            Toast.makeText(MapsActivity.this, "Entro aqui :v", Toast.LENGTH_SHORT).show();
                            ((Marker) dicMarker.get(indiceMarcadores)).remove();
                            dicCoordenadas.remove(indiceMarcadores);
                            dicInfo.remove(indiceMarcadores);
                            dicMarker.remove(indiceMarcadores);
                            myLocationDbHelper.deleteLocation(indiceMarcadores);
                            HttpDelete myHttpDelete = new HttpDelete();
                            myHttpDelete.doInBackground(indiceMarcadores + "");
                        }
                    }
                    */

                }
                return false;
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

        Cursor markersC = myLocationDbHelper.readAllLocation();
        while(markersC.moveToNext())
        {
            int lat = markersC.getColumnIndex(myLocationDbHelper.COLUMN_NAME_LATITUD);
            int lng = markersC.getColumnIndex(myLocationDbHelper.COLUMN_NAME_LONGITUD);
            int id = markersC.getColumnIndex(myLocationDbHelper.COLUMN_NAME_ID);
            int miInfo = markersC.getColumnIndex(myLocationDbHelper.COLUMN_NAME_INFO);
            int miTipo = markersC.getColumnIndex(myLocationDbHelper.COLUMN_NAME_TIPO);




            if(markersC.getInt(miTipo)==-1){
                LatLng newMarker = new LatLng(markersC.getFloat(lat),markersC.getFloat(lng));

                Marker tempMarker = mMap.addMarker(new MarkerOptions().position(newMarker).title(markersC.getInt(id) + ""));
                keyListRut.add(markersC.getInt(id));
                dicMarkRut.put(markersC.getInt(id), tempMarker);
                dicCoordRut.put(markersC.getInt(id), new double[]{(double) markersC.getFloat(lat), (double) markersC.getFloat(lng)});
                dicInfRut.put(markersC.getInt(id), markersC.getString(miInfo).split("~"));
            }
            else{
                //----------------------FALTA---------
                LatLng newMarker = new LatLng(markersC.getFloat(lat),markersC.getFloat(lng));

                //Marker tempMarker = mMap.addMarker(new MarkerOptions().position(newMarker).title(markersC.getInt(id) + ""));
                keyListRep.add(markersC.getInt(id));
                //dicMarkRep.put(markersC.getInt(id), tempMarker);
                dicCoordRut.put(markersC.getInt(id), new double[]{(double) markersC.getFloat(lat), (double) markersC.getFloat(lng)});
                //dicInfRut.put(markersC.getInt(id), markersC.getString(miInfo).split("~"));
            }




        }
        markersC.close();

        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(64.2008, -149.4937)));

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
            coordenadas = (double[]) dicCoordRut.get(index);
            if(informacion.get(i).type==0){
                ((String[])dicInfRut.get(index))[3] = informacion.get(i).text;
            }
            else if(informacion.get(i).type==1){
                ((String[])dicInfRut.get(index))[1] = informacion.get(i).number+" "+informacion.get(i).direction;
            }
            else if(informacion.get(i).type==2){
                ((String[])dicInfRut.get(index))[0] = informacion.get(i).number+"";
            }
            else{
                ((String[])dicInfRut.get(index))[2] = informacion.get(i).number+"";
            }
        }

        for(int idTemp:keyListRut){
            String[] miInfoTemp = (String[])dicInfRut.get(idTemp);
            String stringInfo = miInfoTemp[0]+" ~"+miInfoTemp[1]+" ~"+miInfoTemp[2]+" ~ "+miInfoTemp[3];
            myLocationDbHelper.updateInfo(idTemp,stringInfo);
        }


        //String stringInfo = ((String[])dicInfo.get(index))[0]+"~"
        updateMarkers();
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private void sendRequestNetwork() {
        String telefono="+50230359588";
        String requestString = "get:";
        for (int localIdC : keyListRut){
            requestString+=Integer.toString(localIdC)+",";
        }
        requestString = requestString.substring(0, requestString.length() - 1);

        HttpManager httpManager = new HttpManager();
        httpManager.ma = this;
        httpManager.execute(telefono, requestString);
    }

    protected void postRequestNetwork(String response)
    {
        try {
            JSONObject jo = new JSONObject(response);
            JSONArray mensajes = new JSONArray(jo.getString("mensaje"));
            for(int i = 0; i < mensajes.length(); i++)
            {
                putDataMap(mensajes.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    protected void sendRequest(){
        String requestString = "get:";
        for (int localIdC : keyListRut){
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

    public void updateMarkers()
    {
        Enumeration<Marker> e = dicMarkRut.elements();
        while(e.hasMoreElements()){
            Marker marker = (Marker) e.nextElement();
            if(marker.isInfoWindowShown())
            {
                marker.hideInfoWindow();
                marker.showInfoWindow();
            }
        }
    }

    public static MapsActivity getInstance()
    {
        return mapsAct;
    }
}
