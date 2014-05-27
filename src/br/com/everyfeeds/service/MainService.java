package br.com.everyfeeds.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import br.com.everyfeeds.Principal;
import br.com.everyfeeds.R;
import br.com.everyfeeds.entity.Canal;
import br.com.everyfeeds.entity.Token;
import br.com.everyfeeds.entity.Usuario;

import com.google.android.gms.common.api.GoogleApiClient;

public class MainService extends Service implements IService {

	private NotificationManager myNotificationManager;
	private final Controller controller = new Controller();
	private GoogleApiClient mGoogleApiClient;
	private Token token = new Token();
	private Usuario dadosUsuario = new Usuario();
	private SolicitaCanaisConta threadCanaisConta;
	private boolean cancelada = false;
	private List<Canal> feedsAtuais =  new ArrayList<Canal>();
	private List<Canal> feedsAntigos=  new ArrayList<Canal>();
	
	@Override
	public IBinder onBind(Intent intent) {
		return controller;
	}
	
	public class Controller extends Binder {
		public MainService getServiceListener() {
			return (MainService.this);
		}
	}
	
	@Override
	public void onCreate() {
		Log.i("Every", "Servico iniciado..");
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(!cancelada){
			Log.i("Every", "Iniciando requisicao de dados...");
			executaTarefas();
		}
		return Service.START_NOT_STICKY;
	}

	private void executaTarefas() {		
		threadCanaisConta = new SolicitaCanaisConta(token, dadosUsuario,null,this);
		threadCanaisConta.execute();
		
	}
	
	@Override
	public void onDestroy() {
		cancelada = true;
		super.onDestroy();
	}


	protected void exibeNotificacao(String msg) {
		Intent resultIntent = new Intent(this, Principal.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0,
				resultIntent, 0);
		NotificationCompat.Builder notificacao = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher).setContentTitle(msg)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setAutoCancel(true);
		notificacao.setContentIntent(pIntent);
		myNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		myNotificationManager.notify(1, notificacao.build());
	}

	@Override
	public GoogleApiClient getGoogleApiClient() {
		return mGoogleApiClient;
	}

	@Override
	public Token getToken() {
		return token;
	}

	@Override
	public Usuario getUsuario() {
		return dadosUsuario;
	}

	@Override
	public void setGoogleApiClient(GoogleApiClient dadosGoogle) {
		mGoogleApiClient = dadosGoogle;

	}

	@Override
	public void setToken(Token dadosToken) {
		token = dadosToken;

	}

	@Override
	public void setUsuario(Usuario dadosUsuario) {
		this.dadosUsuario = dadosUsuario;
	}
	

	@Override
	public List<Canal> getFeedsAntigos() {
		return feedsAntigos;
	}

	@Override
	public List<Canal> getFeedsNovos() {
		return feedsAtuais;
	}
	public void organizaFeeds() {
		Calendar dataAtual = Calendar.getInstance(Locale.ENGLISH);
		Calendar dataInicioSemana = Calendar.getInstance(Locale.ENGLISH);
		dataInicioSemana.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		for (Canal dadosCanal : dadosUsuario.getCanais()) {
			Calendar dataUltimaAtualizacao = dadosCanal
					.getDataUltimaAtualizacao();
			if ((dataUltimaAtualizacao.getTime().before(dataAtual.getTime()) && dataUltimaAtualizacao
					.getTime().after(dataInicioSemana.getTime()))
					|| (dataUltimaAtualizacao.get(Calendar.DAY_OF_YEAR) == dataAtual.get(Calendar.DAY_OF_YEAR)
					|| dataUltimaAtualizacao.get(Calendar.DAY_OF_YEAR) ==dataInicioSemana.get(Calendar.DAY_OF_YEAR))) {
				feedsAtuais.add(dadosCanal);				
			} else {
				feedsAntigos.add(dadosCanal);
			}
		}
		Collections.sort(feedsAtuais);
		Collections.sort(feedsAntigos);
	}
	
}
