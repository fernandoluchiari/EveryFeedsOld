package br.com.everyfeeds.service;

import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import br.com.everyfeeds.Principal;
import br.com.everyfeeds.entity.Usuario;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
/**
 * Background Async task to load user profile picture from url
 * */
public class SolicitaProfile extends AsyncTask<Void, Void, Void> {
    private GoogleApiClient mGoogleApiClient;
	private Usuario dadosUsuario;
	private Principal principalActivity;
	private static final String TAG = "Solicita profile ";
	private static final int PROFILE_PIC_SIZE = 200;
	
	
	
 
	public SolicitaProfile(Principal principalActivity,GoogleApiClient mGoogleApiClient,Usuario dadosUsuario) {
		this.principalActivity= principalActivity; 
        this.mGoogleApiClient = mGoogleApiClient;
        this.dadosUsuario = dadosUsuario;
    }
    
    private void getImagemProfile(String url){
    	 String urldisplay = url;
         Bitmap mIcon11 = null;
         try {
             InputStream in = new java.net.URL(urldisplay).openStream();
             mIcon11 = BitmapFactory.decodeStream(in);
         } catch (Exception e) {
             Log.e("Error", e.getMessage());
             e.printStackTrace();
         }
         dadosUsuario.setImagemProfile(mIcon11);
    }
 
    protected Void doInBackground(Void... arg0) {
    	getProfileInformation();
    	
    	return null;
    }
    
    @Override
    protected void onPostExecute(Void result) {
    	// TODO Auto-generated method stub
    	super.onPostExecute(result);
    	 principalActivity.AtualizaDadosLogin("Olá "+dadosUsuario.getNome(), dadosUsuario.getEmail(), dadosUsuario.getImagemProfile());
    }
    
    /**
	 * Fetching user's information name, email, profile pic
	 * */
	private void getProfileInformation() {
	    try {
	        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
	            Person currentPerson = Plus.PeopleApi
	                    .getCurrentPerson(mGoogleApiClient);
	            	            
	            String personPhotoUrl = currentPerson.getImage().getUrl();
	            dadosUsuario.setNome(currentPerson.getDisplayName().split(" ")[0]);
	            dadosUsuario.setProfilePlus(currentPerson.getUrl());
	            dadosUsuario.setEmail(Plus.AccountApi.getAccountName(mGoogleApiClient));
	 
	            // by default the profile url gives 50x50 px image only
	            // we can replace the value with whatever dimension we want by
	            // replacing sz=X
	            personPhotoUrl = personPhotoUrl.substring(0,
	                   personPhotoUrl.length() - 2)
	                    + PROFILE_PIC_SIZE;
	            getImagemProfile(personPhotoUrl);
	            
	 
	            Log.e(TAG, "Name: " + dadosUsuario.getNome() + ", plusProfile: "
	            		+ dadosUsuario.getProfilePlus() + ", email: " + dadosUsuario.getEmail()
	            		+ ", Image: " + personPhotoUrl);
	            
	           
	        }
	    } catch (Exception e) {
	    	principalActivity.showMessage(e.getMessage());
	    }
	}

	
    
}


