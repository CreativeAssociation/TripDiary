package com.creative.tripdiary;


import com.facebook.*;
import com.facebook.model.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;


public class HomeActivity extends Activity{
	@Override
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  
	  setContentView(R.layout.activity_home);
	  
	  findViewById(R.id.btnSignIn).setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
        	  authLogin();
          }
      });
	  
	  findViewById(R.id.btnSkip).setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
        	  	Intent i = new Intent(HomeActivity.this, MainActivity.class);
   	       		startActivity(i);
          }
      });
	 }
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	    if (Session.getActiveSession() != null || Session.getActiveSession().isOpened()){
	       Intent i = new Intent(HomeActivity.this, MainActivity.class);
	       startActivity(i);
	    }
	}
	
	public void authLogin(){
		// start Facebook Login
		  Session.openActiveSession(this, true, new Session.StatusCallback() {

		    // callback when session changes state
		    @Override
		    public void call(Session session, SessionState state, Exception exception) {
		    	if (session.isOpened()) {
		    		// make request to the /me API
		    		Request.newMeRequest(session, new Request.GraphUserCallback() {

		    		  // callback after Graph API response with user object
		    		  @Override
		    		  public void onCompleted(GraphUser user, Response response) {
		    			  if (user != null) {
		    				  Log.d("FB_ID", user.getId());
		    				  Intent i = new Intent(HomeActivity.this, MainActivity.class);
		    			      i.putExtra("FB_ID", user.getId());
		    				  //i.putExtra("FB_ID", "public");
		    				  startActivity(i);
		    			  }
		    		  }
		    		}).executeAsync();  
		    	}
		    }
		  });

	}
	
}
