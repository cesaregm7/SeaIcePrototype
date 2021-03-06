package com.seaice.csar.seaiceprototype;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
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

import java.io.InputStream;
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

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

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

                } else if (botonSeleccionado.equals("Weather")) {
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
                if(marker.getTitle().equals("-1")){
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
                            temperatura.setText("Temp.: " + infoTemp[0] + " K");
                        }
                        if (!infoTemp[1].equals("")) {
                            TextView viento = (TextView) v.findViewById(R.id.vientoMarker);
                            viento.setText("Wind: " + infoTemp[1].split(" ")[0] + " m/s; "+ infoTemp[1].split(" ")[1]+" deg;");
                        }
                    }
                }

                if(marker.getTitle().equals("1")){
                    int indiceMarker = -1;

                /*----------------Marcadores de Reporte FALTA-----------------------*/
                    for (int i = 0; i < keyListRep.size(); i++) {
                        coordTemp = (double[]) dicCoordRep.get(keyListRep.get(i));
                        if (latLng.latitude == coordTemp[0] && latLng.longitude == coordTemp[1]) {
                            indiceMarker = keyListRep.get(i);
                        }
                    }

                    if (!(indiceMarker == -1)) {
                        System.out.println(indiceMarker);
                        String[] infoTemp = (String[]) dicInfRep.get(indiceMarker);
                        TextView titulo = (TextView) v.findViewById(R.id.temperaturaMarker);
                        titulo.setText("Titulo: " + infoTemp[0]);
                        TextView contenido = (TextView) v.findViewById(R.id.vientoMarker);
                        contenido.setText("Contenido: " + infoTemp[1]);
                        ImageView imagenMarker = (ImageView) v.findViewById(R.id.imageMarker);
                        //imagenMarker.
                        String urldisplay = "https://seaice-jayala.rhcloud.com/reportes/" + infoTemp[2];
                        System.out.println("url: "+urldisplay);
                        Bitmap mIcon11 = null;
                        try {
                            InputStream in = new java.net.URL(urldisplay).openStream();
                            mIcon11 = BitmapFactory.decodeStream(in);
                        } catch (Exception e) {
                            //Log.e("Error", e.getMessage());
                            e.printStackTrace();
                        }
                        imagenMarker.setImageBitmap(mIcon11);
                    }
                }

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
                    options.title("-1");
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
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                    options.title("1");
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
                    final int tempType = Integer.parseInt(marker.getTitle());
                    marker.remove();

                    LatLng latLng = marker.getPosition();
                    double[] coordTemp;
                    int indiceMarker = -1;

                    if(tempType==-1){
                        for (int i = 0; i < keyListRut.size(); i++) {
                            coordTemp = (double[]) dicCoordRut.get(keyListRut.get(i));
                            if (latLng.latitude == coordTemp[0] && latLng.longitude == coordTemp[1]) {
                                indiceMarker = keyListRut.get(i);
                            }
                        }
                        //if(indiceMarker!=-1){
                            keyListRut.remove(keyListRut.indexOf(indiceMarker));
                            dicCoordRut.remove(indiceMarker);
                            dicInfRut.remove(indiceMarker);
                            dicMarkRut.remove(indiceMarker);
                            myLocationDbHelper.deleteLocation(indiceMarker);
                            new HttpDelete().execute(indiceMarker + "", "ruta");
                       // }

                    }
                    if(tempType==1){
                        for (int i = 0; i < keyListRep.size(); i++) {
                            coordTemp = (double[]) dicCoordRep.get(keyListRep.get(i));
                            if (latLng.latitude == coordTemp[0] && latLng.longitude == coordTemp[1]) {
                                indiceMarker = keyListRep.get(i);
                            }
                        }
                       // if(indiceMarker!=-1){
                            keyListRep.remove(keyListRep.indexOf(indiceMarker));
                            dicCoordRep.remove(indiceMarker);
                            dicInfRep.remove(indiceMarker);
                            dicMarkRep.remove(indiceMarker);
                            myLocationDbHelper.deleteReport(indiceMarker);
                            new HttpDelete().execute(indiceMarker + "", "reporte");
                        //}
                    }


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

                Marker tempMarker = mMap.addMarker(new MarkerOptions().position(newMarker).title("-1"));
                keyListRut.add(markersC.getInt(id));
                dicMarkRut.put(markersC.getInt(id), tempMarker);
                dicCoordRut.put(markersC.getInt(id), new double[]{(double) markersC.getFloat(lat), (double) markersC.getFloat(lng)});
                dicInfRut.put(markersC.getInt(id), markersC.getString(miInfo).split("~"));
            }
            else{
                //----------------------FALTA---------

                Cursor markersC2 = myLocationDbHelper.readSingleReport(markersC.getInt(miTipo));
                markersC2.moveToFirst();

                int titulo = markersC2.getColumnIndex(myLocationDbHelper.COLUMN_NAME_TITULO);
                int contenido = markersC2.getColumnIndex(myLocationDbHelper.COLUMN_NAME_DESCRIPCION);
                int path = markersC2.getColumnIndex(myLocationDbHelper.COLUMN_NAME_PATH);
                int idServer = markersC.getColumnIndex(myLocationDbHelper.COLUMN_NAME_ID_SERVER);
                System.out.println(titulo+", "+contenido+", "+path+", "+id);

                LatLng newMarker = new LatLng(markersC.getFloat(lat),markersC.getFloat(lng));

                Marker tempMarker = mMap.addMarker(new MarkerOptions().position(newMarker).title("1").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                keyListRep.add(markersC.getInt(idServer));
                dicMarkRep.put(markersC.getInt(idServer), tempMarker);
                dicCoordRep.put(markersC.getInt(idServer), new double[]{(double) markersC.getFloat(lat), (double) markersC.getFloat(lng)});
                dicInfRep.put(markersC.getInt(idServer), new String[]{markersC2.getString(titulo),markersC2.getString(contenido),markersC2.getString(path)});
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
        updateMarkers();
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    public void sendRequestNetwork() {
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
            JSONArray responses = new JSONArray(response);
            JSONObject jo = responses.getJSONObject(0);
            JSONArray mensajes = new JSONArray(jo.getString("mensaje"));
            for(int i = 0; i < mensajes.length(); i++)
            {
                putDataMap(mensajes.getString(i));
            }
            JSONArray jsonReport = responses.getJSONArray(1);
            updateMarkerInfoHTTP(jsonReport);
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

        Enumeration<Marker> e1 = dicMarkRep.elements();
        while(e1.hasMoreElements()){
            Marker marker = (Marker) e1.nextElement();
            if(marker.isInfoWindowShown())
            {
                marker.hideInfoWindow();
                marker.showInfoWindow();
            }
        }
    }

    public void updateMarkerInfoHTTP(JSONArray markerInfo) throws JSONException {
        int idTemp;
        for(int i = 0; i < markerInfo.length(); i++)
        {
            JSONObject jo = markerInfo.getJSONObject(i);
            System.out.println("bla"+jo.getInt("reporteid"));
            idTemp = jo.getInt("reporteid");
            if(keyListRep.contains(idTemp)){
                dicInfRep.put(idTemp, new String[]{jo.getString("titulo"),jo.getString("detalle"),jo.getString("path")});

                myLocationDbHelper.updateReport(idTemp, jo.getString("titulo"), jo.getString("detalle"), jo.getString("path"));
            }
            else{
                System.out.println("creo marker cian"+jo.getInt("reporteid"));
                LatLng newMarker = new LatLng(jo.getDouble("lat"),jo.getDouble("lng"));

                Marker tempMarker = mMap.addMarker(new MarkerOptions().position(newMarker).title("1").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                keyListRep.add(jo.getInt("reporteid"));
                dicMarkRep.put(jo.getInt("reporteid"), tempMarker);
                dicCoordRep.put(jo.getInt("reporteid"), new double[]{jo.getDouble("lat"), jo.getDouble("lng")});
                dicInfRep.put(jo.getInt("reporteid"), new String[]{jo.getString("titulo"),jo.getString("detalle"),jo.getString("path")});

                myLocationDbHelper.insertFullReport(jo.getDouble("lat"), jo.getDouble("lng"),jo.getString("titulo"),jo.getString("detalle"),jo.getString("path"),jo.getInt("reporteid"));
            }
        }
        updateMarkers();
    }

    public static MapsActivity getInstance()
    {
        return mapsAct;
    }
}
