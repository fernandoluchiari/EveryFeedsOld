package br.com.everyfeeds;

import java.util.Calendar;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.SendIntentException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import br.com.everyfeeds.R.string;
import br.com.everyfeeds.entity.Token;
import br.com.everyfeeds.entity.Usuario;
import br.com.everyfeeds.receiver.ServiceReceiver;
import br.com.everyfeeds.service.GeraComponentes;
import br.com.everyfeeds.service.MainService;
import br.com.everyfeeds.service.SolicitaCanaisUsuario;
import br.com.everyfeeds.service.SolicitaProfile;
import br.com.everyfeeds.service.SolicitaToken;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.google.api.services.youtube.YouTubeScopes;

public class Principal extends Activity implements OnClickListener,
		ConnectionCallbacks, OnConnectionFailedListener {

	private static final int RC_SIGN_IN = 0;

	private static final String TAG = "Principal";

	private GoogleApiClient mGoogleApiClient;
	private Token token = new Token();
	private Usuario dadosUsuario = new Usuario();
	private boolean mIntentInProgress;
	private SolicitaProfile threadProfile;
	private SolicitaToken threadToken;
	private GeraComponentes threadGeraComponentes;  

	private boolean isExecutando = false;

	private ConnectionResult mConnectionResult;

	// private SignInButton btnSignIn;
	private Button btnSignOut;
	private ImageView imgProfilePic;
	private TextView txtName;
	private String scopes = "oauth2:" + YouTubeScopes.YOUTUBE;
	private ProgressBar barraAguarde;
	private int[] imagensPerfil = { R.drawable.mario, R.drawable.boba,
			R.drawable.clone, R.drawable.magic, R.drawable.ving,
			R.drawable.wolv };
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	      Bundle bundle = intent.getExtras();
	      if (bundle != null) {
	        Usuario dadosFeeds = (Usuario)intent.getSerializableExtra("dadosUsuario");
	        dadosUsuario.setCanaisOutros(dadosFeeds.getCanaisOutros());
	        dadosUsuario.setCanaisSemana(dadosFeeds.getCanaisSemana());
	        geraTabela();
	      }
	    }
	  };

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.principal);

		btnSignOut = (Button) findViewById(R.id.btn_sign_out);
		imgProfilePic = (ImageView) findViewById(R.id.imgProfilePic);
		txtName = (TextView) findViewById(R.id.txtName);
		barraAguarde = (ProgressBar) findViewById(R.id.barraAguarde);
		barraAguarde.setVisibility(View.GONE);

		imgProfilePic
				.setImageResource(imagensPerfil[(int) (Math.random() * 5)]);

		// Button click listeners
		btnSignOut.setOnClickListener(this);

		// Initializing google plus api client

		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).addApi(Plus.API, new Plus.PlusOptions.Builder().build())
				.addScope(Plus.SCOPE_PLUS_LOGIN).build();
		atualizaComponentes(false);
		((TableLayout) this.findViewById(R.id.tabelaFeedCorrente))
				.removeAllViewsInLayout();

		iniciaAlarm(true);
	}
	
	@Override
	protected void onResume() {
		registerReceiver(receiver, new IntentFilter(SolicitaCanaisUsuario.NOTIFICATION));		
		super.onResume();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if (isDispositivoOnline()) {
			if (!mGoogleApiClient.isConnected()) {
				mGoogleApiClient.connect();
			}
		} else {
			showMessage(this.getString(string.msg_sem_conexao));
			Intent intent = new Intent(this, Inicial.class);
			startActivity(intent);
		}
	}

	protected void onStop() {
		super.onStop();
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
		unregisterReceiver(receiver);
	}
	

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (!result.hasResolution()) {
			GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
					0).show();
			return;
		} else if (isDispositivoOnline() && !mIntentInProgress) {
			mConnectionResult = result;
			resolveSignInError();
		} else if (isDispositivoOnline()) {
			showMessage(this.getString(string.msg_sem_conexao));
			Intent intent = new Intent(this, Inicial.class);
			startActivity(intent);
		}

	}

	@Override
	public void onConnected(Bundle connectionHint) {
		if (!isExecutando) {
			showBarraAguarde(true);
			isExecutando = true;
			threadToken = new SolicitaToken(this, mGoogleApiClient, token,
					scopes, null,null);
			threadProfile = new SolicitaProfile(this, mGoogleApiClient,
					dadosUsuario);
			threadToken.execute();
			threadProfile.execute();
			iniciaServico(true);
		}
	}

	public void iniciaSolicitacoes(){
		
		 Intent intent = new Intent(this, SolicitaCanaisUsuario.class);
		    intent.putExtra("token",token);
		    startService(intent);
	}
	
	public void geraTabela(){
		threadGeraComponentes = new GeraComponentes(this, dadosUsuario);
		threadGeraComponentes.execute();
	}
	
	
	
	public void iniciaServico(boolean ativar) {
		Intent it = new Intent("SERVICO_EVERY");
		if (ativar) {
			if(!isMyServiceRunning(MainService.class)){
				startService(it);
			}
		} else {
			stopService(it);
		}
	}
	
	private boolean isMyServiceRunning(Class<?> serviceClass) {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (serviceClass.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	@Override
	public void onConnectionSuspended(int cause) {
		mGoogleApiClient.connect();

	}

	protected void onActivityResult(int requestCode, int responseCode,
			Intent intent) {
		if (requestCode == RC_SIGN_IN) {
			mIntentInProgress = false;
			if (!mGoogleApiClient.isConnected()) {
				mGoogleApiClient.connect();
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_sign_out) {
			// Signout button clicked
			signOutFromGplus();

		}

	}

	/**
	 * Sign-out from google
	 * */
	private void signOutFromGplus() {
		if (isExecutando) {
			threadProfile.cancel(true);
			//threadYoutube.cancel(true);
			threadToken.cancel(true);
			if(threadGeraComponentes != null){
				threadGeraComponentes.cancel(true);
			}
			iniciaAlarm(false);
			iniciaServico(false);
		}
		if (mGoogleApiClient.isConnected()) {
			Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
			mGoogleApiClient.disconnect();
			mGoogleApiClient.connect();
		}
		Intent intent = new Intent(this, Inicial.class);
		startActivity(intent);
	}

	private void iniciaAlarm(boolean ativar) {
		if (ativar) {
			int intervaloAlarme = 1000 * 60 * 4;
			AlarmManager service = (AlarmManager) this
					.getSystemService(Context.ALARM_SERVICE);
			Intent i = new Intent(this, ServiceReceiver.class);
			final PendingIntent pending = PendingIntent.getBroadcast(this, 0,
					i, PendingIntent.FLAG_UPDATE_CURRENT);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, 4);
			service.setInexactRepeating(AlarmManager.RTC,
					cal.getTimeInMillis(), intervaloAlarme, pending);
		} else {
			Intent intent = new Intent(this, ServiceReceiver.class);
			PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent,
					0);
			AlarmManager alarmManager = (AlarmManager) this
					.getSystemService(Context.ALARM_SERVICE);
			alarmManager.cancel(sender);
		}
	}

	/**
	 * Method to resolve any signin errors
	 * */
	private void resolveSignInError() {

		if (mConnectionResult.hasResolution()) {
			try {
				mIntentInProgress = true;
				mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
			} catch (SendIntentException e) {
				mIntentInProgress = false;
				mGoogleApiClient.connect();
			}
		}
	}

	public void showMessage(final String texto) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Context contexto = getApplicationContext();

				int duracao = Toast.LENGTH_SHORT;

				Toast toast = Toast.makeText(contexto, texto, duracao);
				toast.show();

			}
		});
	}

	public void showBarraAguarde(final boolean visibility) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (visibility) {
					barraAguarde.setVisibility(View.VISIBLE);
				} else {
					barraAguarde.setVisibility(View.GONE);
				}
			}
		});
	}

	public void AtualizaDadosLogin(String name, String email, Bitmap profile) {
		getImgProfilePic().setImageBitmap(profile);
		getTxtName().setText(name);
	}

	public void atualizaComponentes(boolean visible) {
		TableLayout tabelaFeedsPrincipal = (TableLayout) findViewById(R.id.tabelaFeedCorrente);
		TableLayout tabelaOutrosFeeds = (TableLayout) findViewById(R.id.tabelaOutrosFeeds);
		if (visible) {
			tabelaFeedsPrincipal.setVisibility(View.VISIBLE);
			tabelaOutrosFeeds.setVisibility(View.VISIBLE);
		} else {
			tabelaFeedsPrincipal.setVisibility(View.GONE);
			tabelaOutrosFeeds.setVisibility(View.GONE);
		}
	}

	private boolean isDispositivoOnline() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void onBackPressed() {
		
	}

	public ImageView getImgProfilePic() {
		return imgProfilePic;
	}

	public TextView getTxtName() {
		return txtName;
	}

	public GoogleApiClient getmGoogleApiClient() {
		return mGoogleApiClient;
	}

}
