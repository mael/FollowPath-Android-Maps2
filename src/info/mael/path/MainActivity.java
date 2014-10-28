/*
 * Author: Mael T.
 */
package info.mael.path;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

@SuppressLint("NewApi")
public class MainActivity extends FragmentActivity implements LocationListener,
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener, OnMapClickListener {

	private static String TAG = "MainActivity";

	private LocationRequest mLocationRequest;
	private LocationClient mLocationClient;
	private boolean mUpdatesRequested = false;
	private static final String APPTAG = "LocationSample";
	private  final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	// Update interval in milliseconds
	private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

	//MAP
	private static GoogleMap map;

	//Define static path points
	private LatLng start = new LatLng(42.799711, -1.633764);
	private LatLng one = new LatLng(42.799823, -1.633863);
	private LatLng two = new LatLng(42.799924, -1.633965);
	private LatLng three= new LatLng(42.800006, -1.633812);
	private LatLng four = new LatLng(42.800087, -1.633921);
	private LatLng five = new LatLng(42.800099, -1.634077);
	private LatLng end = new LatLng(42.799959, -1.634120);

	private LatLng  BASE;
	private LatLng NEXT;

	private LatLng actual;
	private LatLng prev;

	private ArrayList<LatLng> route = new ArrayList<LatLng>();

	private int NextPoint=0;
	private boolean first = true;
	private Marker prev_marker;
	private Switch gps_btn;
	private Button clear_btn;


	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		getActionBar().hide();
		setContentView(R.layout.activity_main);

		mLocationRequest = LocationRequest.create();
		mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		mLocationClient = new LocationClient(this, this, this);

		//map
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);


		map.setMyLocationEnabled(true);
		map.getUiSettings().setZoomControlsEnabled(false);
		map.getUiSettings().setCompassEnabled(true);
		map.setOnMapClickListener(this);


		loadRoute();
		printRouteBase();
		BASE = start;
		NEXT = start;

		gps_btn = (Switch) findViewById(R.id.sw_gps);
		gps_btn.setChecked(false);
		gps_btn.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {

				if (isChecked) {
					Log.d(TAG, "Start GPS");
					startLocation();
				} else {
					stopUpdates();
					Log.d(TAG, "Stop GPS");
				}
			}
		});

		clear_btn = (Button) findViewById(R.id.clear_map);

		clear_btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				clearMap(v);
				Toast.makeText(getApplicationContext(), "Clear map",Toast.LENGTH_SHORT).show();

			}
		});

	}
	private void loadRoute(){
		route.add(start);
		route.add(one);
		route.add(two);
		route.add(three);
		route.add(four);
		route.add(five);
		route.add(end);	
	}

	private void printRouteBase() {	
		map.addMarker(new MarkerOptions().position(start).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));	

		for (int i = 0; i < route.size(); i++) {	
			if(i != 0){
				map.addMarker(new MarkerOptions().position(route.get(i)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));	
				map.addPolyline(new PolylineOptions().add(route.get(i-1),  route.get(i)).width(5).color(Color.GREEN));
			}
		}
	}

	private void printRouteReal(LatLng pre, LatLng act) {
		map.addPolyline(new PolylineOptions().add(pre, act).width(5).color(Color.BLUE));
	}


	@Override
	public void onLocationChanged(Location location) {
		if(first){
			first = false; 
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(
					new LatLng(location.getLatitude() ,
							location.getLongitude()), 19));
			map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude() ,
					location.getLongitude())).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));	

		}

		actual = new LatLng(location.getLatitude(), location.getLongitude());

		if(prev!=null){
			printRouteReal(prev, actual);
			prev_marker.remove();

		}
		prev_marker = map.addMarker(new MarkerOptions().position(actual)
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
		prev = actual;		
		recalculateRoute();
	}	


	private void recalculateRoute(){

		double direction;
		double desfase;
		double ang_route;
		double ang_real;

		ang_route = Bearing(BASE,NEXT);
		Log.i(TAG, "ang_route: "+ang_route);

		ang_real = Bearing(NEXT,actual);
		Log.i(TAG, "ang_real: "+ang_real);


		if (ang_route > 180f){
			desfase = 360f-ang_route;
			Log.i(TAG, "D > 180: "+desfase);
		}
		else{
			desfase = 0-ang_route;
			Log.i(TAG, "D < 180: "+desfase);
		}	

		direction = desfase + ang_real;

		//Descartar los cuadrantes superiores 1º y 4º
		if(direction>90f && direction<270f){
			//Cuadrante 2
			if(direction<180f){
				double abs = 180f - direction;
				Toast.makeText(getApplicationContext(), "Turn Left: "+ abs ,Toast.LENGTH_SHORT).show();
			}
			//Cuadrante 3
			else{
				double abs = direction-180f;
				Toast.makeText(getApplicationContext(), "Turn Right: "+ abs ,Toast.LENGTH_SHORT).show();
			}
		}else{
			NextPoint++;
			Toast toast = Toast.makeText(getApplicationContext(),  "NEW point reference: "+ NextPoint, Toast.LENGTH_SHORT);		
			toast.setGravity(Gravity.TOP, 0, 0);
			toast.show();


			if(NextPoint < route.size()){
				BASE = NEXT;
				NEXT= route.get(NextPoint);
				Log.e(TAG, "New reference: "+ NextPoint);

				//Recalcular
				ang_route = Bearing(BASE,NEXT);
				Log.i(TAG, "ang_route: "+ang_route);

				ang_real = Bearing(NEXT,actual);
				Log.i(TAG, "ang_real: "+ang_real);


				if (ang_route > 180f){
					desfase = 360f-ang_route;
					Log.i(TAG, "D > 180: "+desfase);
				}
				else{
					desfase = 0-ang_route;
					Log.i(TAG, "D < 180: "+desfase);
				}	

				direction = desfase + ang_real;

				//Descartar los cuadrantes superiores 1º y 4º
				if(direction>90f && direction<270f){
					//Cuadrante 2
					if(direction<180f){
						double abs = 180f - direction;
						Toast.makeText(getApplicationContext(), "Turn Left: "+ abs ,Toast.LENGTH_SHORT).show();
					}
					//Cuadrante 3
					else{
						double abs = direction-180f;
						Toast.makeText(getApplicationContext(), "Turn Right: "+ abs ,Toast.LENGTH_SHORT).show();
					}
					Log.e(TAG,"NEXT: " + NextPoint);
				}
			}
			else{
				Toast.makeText(getApplicationContext(), "Final route!",Toast.LENGTH_SHORT).show();

			}
		}
	}


	@Override
	public void onStop() {
		if (mLocationClient.isConnected()) {
			stopPeriodicUpdates();
		}
		mLocationClient.disconnect();

		super.onStop();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStart() {
		super.onStart();
		mLocationClient.connect();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	public void getLocation(View v) {
		if (servicesConnected() && mUpdatesRequested) {
			Location currentLocation = mLocationClient.getLastLocation();
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(
					new LatLng(currentLocation.getLatitude() ,
							currentLocation.getLongitude()), 20));
		}
	}

	private void startLocation(){

		startPeriodicUpdates();

	}

	public void stopUpdates() {
		mUpdatesRequested = false;
		if (servicesConnected()) {
			stopPeriodicUpdates();
		}
	}

	@Override
	public void onConnected(Bundle bundle) {
		if (mUpdatesRequested) {
			startPeriodicUpdates();
		}
	}

	//Function Bearing
	public double Bearing(LatLng origen, LatLng destiny)  
	{  
		double lat1 = DegToRad(origen.latitude);
		double long1 =  DegToRad(origen.longitude);
		double lat2 =  DegToRad(destiny.latitude);
		double long2 =  DegToRad(destiny.longitude); 

		double deltaLong = long2 - long1;  

		double y = Math.sin(deltaLong) * Math.cos(lat2);  
		double x = Math.cos(lat1) * Math.sin(lat2) -  Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLong);  
		double bearing = Math.atan2(y, x);  
		return ConvertToBearing(RadToDeg(bearing));
	}  
	public double RadToDeg(double radians)  
	{  
		return radians * (180 / Math.PI);  
	}  

	public double DegToRad(double degrees)  
	{  
		return degrees * (Math.PI / 180);  
	}  

	public static double ConvertToBearing(double deg)  
	{  
		return (deg + 360) % 360;  
	} 

	//End Bearing

	private void startPeriodicUpdates() {
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
	}

	private void stopPeriodicUpdates() {
		mLocationClient.removeLocationUpdates(this);
	}

	//Go to
	public void animateCamera(View view) {
		if (map.getMyLocation() != null)
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(
					new LatLng( map.getMyLocation().getLatitude(),
							map.getMyLocation().getLongitude()), 15));
	}


	private void clearMap(View view) {
		map.clear();
		NextPoint = 0;
		BASE = start;
		NEXT= start;
		prev = null;
		printRouteBase();

	}

	@Override
	public void onMapClick(LatLng puntoPulsado) {		
		map.addMarker(new MarkerOptions().position(puntoPulsado).icon(BitmapDescriptorFactory
				.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

		if(first){
			first = false; 
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(
					puntoPulsado, 19));
			map.addMarker(new MarkerOptions().position(puntoPulsado).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));	

		}

		actual = puntoPulsado;

		if(prev!=null){
			printRouteReal(prev, actual);
			prev_marker.remove();

		}
		prev_marker = map.addMarker(new MarkerOptions().position(actual)
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
		prev = actual;		
		recalculateRoute();

	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (connectionResult.hasResolution()) {
			try {
				connectionResult.startResolutionForResult(
						this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);

			} catch (IntentSender.SendIntentException e) {
				e.printStackTrace();
			}
		} else {
			showErrorDialog(connectionResult.getErrorCode());
		}
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case CONNECTION_FAILURE_RESOLUTION_REQUEST :

			switch (resultCode) {
			case Activity.RESULT_OK:
				Log.d(APPTAG, getString(R.string.resolved));
				break;
			default:
				Log.d(APPTAG, getString(R.string.no_resolution));
				break;
			}
		default:
			Log.d(APPTAG,getString(R.string.unknown_activity_request_code, requestCode));

			break;
		}
	}

	private boolean servicesConnected() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (ConnectionResult.SUCCESS == resultCode) {
			Log.d(APPTAG, getString(R.string.play_services_available));
			return true;
		} else {
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
			if (dialog != null) {
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(dialog);
				errorFragment.show(getSupportFragmentManager(), APPTAG);
			}
			return false;
		}
	}

	private void showErrorDialog(int errorCode) {

		Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
				errorCode,
				this,
				CONNECTION_FAILURE_RESOLUTION_REQUEST);

		if (errorDialog != null) {
			ErrorDialogFragment errorFragment = new ErrorDialogFragment();
			errorFragment.setDialog(errorDialog);
			errorFragment.show(getSupportFragmentManager(), APPTAG);
		}
	}


	public static class ErrorDialogFragment extends DialogFragment {

		private Dialog mDialog;

		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}


	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}
}
