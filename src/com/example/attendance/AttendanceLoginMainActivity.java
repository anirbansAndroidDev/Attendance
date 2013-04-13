package com.example.attendance;


import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class AttendanceLoginMainActivity extends Activity {

	private ProgressDialog pgLogin;
	private static String loginStatus = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_attendance_main);
	}

	public void signin(View V) {
		EditText user_id = (EditText)findViewById(R.id.txtEntryUserName);
		EditText password = (EditText)findViewById(R.id.txtEntryPassword);
		
		if(haveNetworkConnection())
		{
			if(user_id.getText().toString().length()<1 || password.getText().toString().length()<1)
			{
				Toast.makeText(this, "Please provide all information.", Toast.LENGTH_LONG).show();
			}
			else 
			{
				// pb.setVisibility(View.VISIBLE);
				// new MyAsyncTask().execute(user_id.getText().toString(),password.getText().toString());	

//				pgLogin = new ProgressDialog(AttendanceLoginMainActivity.this);
//				pgLogin.setMessage("Please wait while progress login...");
//				pgLogin.setIndeterminate(true);
//				pgLogin.setCancelable(false);
//				pgLogin.setCanceledOnTouchOutside(false);
//
//				pgLogin.show();
				Intent i = new Intent(this, AttendanceActivity.class);
				startActivity(i);

			}
		}
		else
		{
			Toast.makeText(this, "Sorry! No internet connection.", Toast.LENGTH_LONG).show();
		}
	}
	
	public void cancel(View v) {
		finish();
	}
		//===================================================================================================================================
		//sending EmailAddress and Password to server
		//===================================================================================================================================
		private class MyAsyncTask extends AsyncTask<String, Integer, Double>{

			String responseBody;
			@Override
			protected Double doInBackground(String... params) {
				// TODO Auto-generated method stub
				postData(params[0],params[1]);
				return null;
			}

			protected void onPostExecute(Double result){
				//Toast.makeText(getApplicationContext(), responseBody, Toast.LENGTH_LONG).show();
				processResponce(responseBody);
			}

			protected void onProgressUpdate(Integer... progress){

			}

			public void postData(String emailId,String passwrd) {
				// Create a new HttpClient and Post Header
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost("http://icaerp.com/AndroidDataService/dataServiceAndroid.asmx/login");

				try {
					// Data that I am sending
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
					nameValuePairs.add(new BasicNameValuePair("mb_code", emailId));
					nameValuePairs.add(new BasicNameValuePair("pwd", passwrd));
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
				//saving the file as a xml
				FileOutputStream fOut = openFileOutput("loginData.xml",MODE_WORLD_READABLE);
				OutputStreamWriter osw = new OutputStreamWriter(fOut);
				osw.write(responceFromServer);
				osw.flush();
				osw.close();

				//reading the file as xml
				FileInputStream fIn = openFileInput("loginData.xml");
				InputStreamReader isr = new InputStreamReader(fIn);
				char[] inputBuffer = new char[responceFromServer.length()];
				isr.read(inputBuffer);
				String readString = new String(inputBuffer);

				//getting the xml Value as per child node form the saved xml
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				InputStream is = new ByteArrayInputStream(readString.getBytes("UTF-8"));
				Document doc = db.parse(is);

				NodeList root=doc.getElementsByTagName("root");

				for (int i=0;i<root.getLength();i++) 
				{
					loginStatus = "" + ((Element)root.item(i)).getAttribute("status");
					//Toast.makeText( getApplicationContext(),loginStatus,Toast.LENGTH_SHORT).show();
				}

				//If Email and Pass match with server
				if(loginStatus.equalsIgnoreCase("T"))
				{
					NodeList mb=doc.getElementsByTagName("mb");
					for (int i=0;i<mb.getLength();i++) 
					{
						//		    		setMbcode("" + ((Element)mb.item(i)).getAttribute("mbcode"));
						//		    		setMbname("" + ((Element)mb.item(i)).getAttribute("mbname"));
						//		    		branchid  = "" + ((Element)mb.item(i)).getAttribute("branchid");
						//		    		pwd       = "" + ((Element)mb.item(i)).getAttribute("pwd");
					}
					Toast.makeText( getApplicationContext(),"Login Successful.",Toast.LENGTH_SHORT).show();
					if (pgLogin.isShowing()) {
						pgLogin.cancel();
						pgLogin.dismiss();
					}
					
				}
				else if(loginStatus.equalsIgnoreCase("F"))
				{
					Toast.makeText( getApplicationContext(),"Your login info don't match.",Toast.LENGTH_SHORT).show();
				}
			} 
			catch (Throwable t) 
			{
				Log.d("Error On Saving and reading", t+"");
			}
		}
		//===================================================================================================================================
		//processing the XML got from server
		//===================================================================================================================================
		
		
		//===================================================================================================================================
		//check packet data and wifi
		//===================================================================================================================================
		private boolean haveNetworkConnection() 
		{
			boolean haveConnectedWifi = false;
			boolean haveConnectedMobile = false;

			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo[] netInfo = cm.getAllNetworkInfo();
			for (NetworkInfo ni : netInfo) 
			{
				if (ni.getTypeName().equalsIgnoreCase("WIFI"))
					if (ni.isConnected())
						haveConnectedWifi = true;
				if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
					if (ni.isConnected())
						haveConnectedMobile = true;
			}
			return haveConnectedWifi || haveConnectedMobile;
		}
		//====================================================================================================================================
		//checking packet data and wifi END
		//====================================================================================================================================
}
