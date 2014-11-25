package br.com.everyfeeds.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import br.com.everyfeeds.Principal;
import br.com.everyfeeds.R;
import br.com.everyfeeds.entity.Canal;
import br.com.everyfeeds.entity.Token;
import br.com.everyfeeds.entity.Usuario;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.google.api.services.youtube.YouTubeScopes;

public class MainService extends Service implements 
		ConnectionCallbacks, OnConnectionFailedListener {

	private NotificationManager myNotificationManager;
	private GoogleApiClient mGoogleApiClient;
	private Token token = new Token();
	private Usuario dadosUsuario = new Usuario();

	private SolicitaCanaisConta threadCanaisConta;
	private SolicitaToken threadToken;
	private String scopes = "oauth2:" + YouTubeScopes.YOUTUBE;

	private boolean cancelada = false;
	private Calendar dataUltimaConsulta = null;
	private List<Canal> feedsAtuais = new ArrayList<Canal>();
	private List<Canal> feedsAntigos = new ArrayList<Canal>();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}



	@Override
	public void onCreate() {
		Log.i("EveryFeeds-Service", "Servico criado...");

		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).addApi(Plus.API, new Plus.PlusOptions.Builder().build())
				.addScope(Plus.SCOPE_PLUS_LOGIN).build();
		

		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (!mGoogleApiClient.isConnected()) {
			mGoogleApiClient.connect();
		}
		if (!cancelada) {
			Log.i("EveryFeeds-Service", "Servico iniciado..");
			Log.i("EveryFeeds-Service", "Iniciando requisicao de dados...");
			executaTarefas();
		}
		return (super.onStartCommand(intent, flags, startId));
	}

	private void executaTarefas() {
		threadToken = new SolicitaToken(null, mGoogleApiClient, token, scopes,
				getApplicationContext());
		threadCanaisConta = new SolicitaCanaisConta(token, dadosUsuario, null,this,dataUltimaConsulta);
		threadToken.execute();
		threadCanaisConta.execute();
		dataUltimaConsulta = Calendar.getInstance(Locale.ENGLISH);

	}

	@Override
	public void onDestroy() {
		Log.i("EveryFeeds-Service", "service canceladoo");
		cancelada = true;
		if (mGoogleApiClient.isConnected()) {
			Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
			mGoogleApiClient.disconnect();
			mGoogleApiClient.connect();
		}
		super.onDestroy();
	}

	protected void exibeNotificacao(String msg) {
		Intent resultIntent = new Intent(this, Principal.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0,
				resultIntent, 0);
		NotificationCompat.Builder notificacao = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher)
				.setContentText(msg)
				.setContentTitle("EveryFeeds")
				.setTicker("Têm vídeo novo!")
				.setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_VIBRATE| Notification.DEFAULT_SOUND)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setAutoCancel(true);
		notificacao.setContentIntent(pIntent);
		myNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		myNotificationManager.notify(1, notificacao.build());
	}

	public void verificaFeeds(List<Canal> feedsAtuais) {
		if(!isForeground("br.com.everyfeeds")){
			if (feedsAtuais.size() != 0) {
				if(feedsAtuais.size() == 1){
					exibeNotificacao("Existe " + feedsAtuais.size()
							+ " vídeo novo em seus feeds!");
				}else{
					exibeNotificacao("Existem " + feedsAtuais.size()
							+ " vídeos novos em seus feeds!");
				}
			}
		}
	}

	
	public boolean isForeground(String myPackage){
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		 List< ActivityManager.RunningTaskInfo > runningTaskInfo = manager.getRunningTasks(1); 

		     ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
		   if(componentInfo.getPackageName().equals(myPackage)) return true;
		return false;
		}
	
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		mGoogleApiClient.connect();
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.i("EveryFeeds-Service", "conectou ao serviço google");

	}

	@Override
	public void onConnectionSuspended(int cause) {
		Log.i("EveryFeeds-Service", "suspendeu o serviço google");

	}

}
