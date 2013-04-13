package com.example.attendance;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

public class PhotoHandler implements PictureCallback {

	private final Context context;
	//For GPS Location
	protected LocationManager locationManager;
	Double lat = null;
	Double lng = null;
	GPSTracker gps;

	public PhotoHandler(Context context) {
		this.context = context;
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {

		File pictureFileDir = getDir();

		if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

			// Log.d(MakePhotoActivity.DEBUG_TAG, "Can't create directory to save image.");
			Toast.makeText(context, "Can't create directory to save image.",Toast.LENGTH_LONG).show();
			return;

		}

//		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
//		String date = dateFormat.format(new Date());
//		String photoFile = "Picture_" + date + ".jpg";
		
		String photoFile = "MyPicture.jpg";

		String filename = pictureFileDir.getPath() + File.separator + photoFile;

		File pictureFile = new File(filename);

		try 
		{
			FileOutputStream fos = new FileOutputStream(pictureFile);
			fos.write(data);
			fos.close();
			Toast.makeText(context, "New Image saved:" + filename,Toast.LENGTH_LONG).show();
			
			gps = new GPSTracker(context);
			if(gps.canGetLocation())
			{
				String MbCode 			   = "MbCode001";
			    String Paswrd   		   = "Password";

				double GpsLatitude  	   = gps.getGPSLatitude();
				double GpsLongitude 	   = gps.getGPSLongitude();
				double NetworkLatitude     = gps.getNetworkLatitude();
				double Networklongitude    = gps.getNetworkLongitude();
				
				String StrGpsLatitude  	   = GpsLatitude +"";
				String StrGpsLongitude 	   = GpsLongitude +"";
				String StrNetworkLatitude  = NetworkLatitude +"";
				String StrNetworklongitude = Networklongitude +"";

				String GpsAddress 		   = "";
				String NetworkAddress      = "";
				
				//String image = data.toString();
				String image = Base64.encodeToString(data, Base64.DEFAULT);
				//String image = MyBase64.encodeBytes(data);
				
				Toast.makeText(context, "Your Network Location is - \nLat: " + NetworkLatitude + "\nLong: " + Networklongitude +
														"\nYour GPS Location is - \nLat: " + GpsLatitude + "\nLong: " + GpsLongitude, 
														Toast.LENGTH_LONG).show();	
				
				if(NetworkLatitude != 0 && Networklongitude !=0)
				{
					NetworkAddress = showLoc(NetworkLatitude,Networklongitude);
				}
				
				if(GpsLatitude != 0 && GpsLongitude !=0)
				{
					GpsAddress = showLoc(GpsLatitude,GpsLongitude);
				}
				new MyAsyncTask().execute(MbCode, Paswrd, StrGpsLatitude, StrGpsLongitude, StrNetworkLatitude, StrNetworklongitude, GpsAddress, NetworkAddress, image);
			}
			else
			{
				//If GPS and Network is not available show alert pop up to turn on the GPS or Network
				gps.showSettingsAlert();
			}
		} 
		catch (Exception error) 
		{
			Toast.makeText(context, "Image could not be saved.",Toast.LENGTH_LONG).show();
		}
	}

	private File getDir() 
	{
		File sdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		return new File(sdDir, "CameraAPIDemo");
	}
	
	//==========================================================================================================================================
	//getting address from lat lng
	//==========================================================================================================================================	
		protected String showLoc(double lat, double lng) 
		{
			String add = null;
			try
			{
			Geocoder geocoder;
			List<Address> addresses;
			geocoder = new Geocoder(context, Locale.getDefault());
			addresses = geocoder.getFromLocation(lat, lng, 1);

			String address = addresses.get(0).getAddressLine(0);
			String city = addresses.get(0).getAddressLine(1);
			String country = addresses.get(0).getAddressLine(2);
			
			add = address+" , "+city+" , "+country;
			Toast.makeText( context,address+" , "+city+" , "+country,Toast.LENGTH_SHORT ).show();
			
			}
			catch (Throwable e) 
			{
				Log.d("address error: \n", ""+e);
				Toast.makeText( context,""+e,Toast.LENGTH_SHORT ).show();
			}
			return add;
		}
	//==========================================================================================================================================
	//END getting address from lat lng
	//==========================================================================================================================================	
		
	//===================================================================================================================================
	//sending EmailAddress and Password to server
	//===================================================================================================================================
	private class MyAsyncTask extends AsyncTask<String, Integer, Double>{

		String responseBody = null;
		@Override
		protected Double doInBackground(String... params) {
			postData(params[0],params[1],params[2],params[3],params[4],params[5],params[6],params[7],params[8]);
			return null;
		}

		protected void onPostExecute(Double result){
			//Toast.makeText(context, responseBody, Toast.LENGTH_LONG).show();

			if(responseBody!=null)
			{
				processResponce(responseBody);
			}
			else
			{
				Toast.makeText(context, "Empty Responce.", Toast.LENGTH_LONG).show();
			}

		}
		protected void onProgressUpdate(Integer... progress){
			
		}

		public void postData(String mb_code,String pwd,String nlat,String nlng,String glat,String glng,String nloc,String gloc,String Photo) {
			// Create a new HttpClient and Post Header
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("http://203.153.37.13:89/AttendenceService.asmx/SaveAttendence");

			try {
				// Data that I am sending
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("mb_code", mb_code));
				nameValuePairs.add(new BasicNameValuePair("pwd", pwd));
				nameValuePairs.add(new BasicNameValuePair("nlat", nlat));
				nameValuePairs.add(new BasicNameValuePair("nlng", nlng));
				nameValuePairs.add(new BasicNameValuePair("glat", glat));
				nameValuePairs.add(new BasicNameValuePair("glng", glng));
				nameValuePairs.add(new BasicNameValuePair("nloc", nloc));
				nameValuePairs.add(new BasicNameValuePair("gloc", gloc));
				nameValuePairs.add(new BasicNameValuePair("Photo", Photo));

				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				// Execute HTTP Post Request
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				responseBody = EntityUtils.toString(response.getEntity());

				Log.d("result", responseBody);
			} 
			catch (Throwable t ) {
				//Toast.makeText( getApplicationContext(),""+t,Toast.LENGTH_LONG).show();
				Log.d("Error Time of Login",t+"");
			} 
		}
	}
	//===================================================================================================================================
	//END sending EmailAddress and Password to server 
	//===================================================================================================================================


	//===================================================================================================================================
	//processing the XML got from server
	//===================================================================================================================================
	private void processResponce(String responceFromServer) 
	{
		try {
			//getting the xml Value as per child node form the saved xml
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputStream is = new ByteArrayInputStream(responceFromServer.getBytes("UTF-8"));
			Document doc = db.parse(is);

			NodeList root=doc.getElementsByTagName("root");
			String loginStatus = null;
			for (int i=0;i<root.getLength();i++) 
			{
				loginStatus = "" + ((Element)root.item(i)).getAttribute("status");
				//Toast.makeText( getApplicationContext(),loginStatus,Toast.LENGTH_SHORT).show();
			}

			//If Email and Pass match with server 
			if(loginStatus.equalsIgnoreCase("T"))
			{
				Toast.makeText( context,"Thanks for your attendance",Toast.LENGTH_LONG).show();
			}
			else 
			{
				Toast.makeText( context,"Attendance could not be successful please try again! ",Toast.LENGTH_LONG).show();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		} 
		
		if (AttendanceActivity.pgLogin != null) {
			if (AttendanceActivity.pgLogin.isShowing()) {
				AttendanceActivity.pgLogin.cancel();
				AttendanceActivity.pgLogin.dismiss();
			}
		}
	}
	//===================================================================================================================================
	//processing the XML got from server
	//===================================================================================================================================

} 