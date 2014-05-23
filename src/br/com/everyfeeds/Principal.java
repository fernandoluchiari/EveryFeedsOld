package br.com.everyfeeds;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import br.com.everyfeeds.entity.Token;
import br.com.everyfeeds.entity.Usuario;
import br.com.everyfeeds.service.IService;
import br.com.everyfeeds.service.MainService;
import br.com.everyfeeds.service.MainService.Controller;
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
		ConnectionCallbacks, OnConnectionFailedListener, ServiceConnection {

	private static final int RC_SIGN_IN = 0;

	private static final String TAG = "Principal";

	private GoogleApiClient mGoogleApiClient;
	private Token token = new Token();
	private Usuario dadosUsuario = new Usuario();
	private boolean mIntentInProgress;
	private SolicitaProfile threadProfile;
	private SolicitaToken threadToken;
	
	private ServiceConnection connection;
	private MainService servico;
	
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

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.principal);

		// btnSignIn = (SignInButton) findViewById(R.id.btn_sign_in);
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
				.addOnConnectionFailedListener(this).addApi(Plus.API, null)
				.addScope(Plus.SCOPE_PLUS_LOGIN).build();
		atualizaComponentes(false);
		((TableLayout) this.findViewById(R.id.tabelaFeedCorrente))
				.removeAllViewsInLayout();	
		
		connection = this;
		bindService(new Intent("SERVICO_EVERY"), connection, 0); // 
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (isDispositivoOnline()) {
			mGoogleApiClient.connect();
		} else {
			showMessage("Verifique sua conexão com a internet e tente novamente mais tarde!");
			Intent intent = new Intent(this, Inicial.class);
			startActivity(intent);
		}
	}

	protected void onStop() {
		super.onStop();
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
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
			showMessage("Verifique sua conexão com a internet e tente novamente mais tarde!");
			Intent intent = new Intent(this, Inicial.class);
			startActivity(intent);
		}

	}

	@Override
	public void onConnected(Bundle connectionHint) {
		if (!isExecutando) {
			isExecutando = true;
			threadProfile = new SolicitaProfile(this, mGoogleApiClient,
					dadosUsuario);
			threadToken = new SolicitaToken(this, mGoogleApiClient, token,
					scopes);
			threadProfile.execute();
			threadToken.execute();				
		}
	}
	
	public void iniciaServico(){
		Intent it = new Intent("ServicoEvery");
		IService c = (IService) servico;
		c.setGoogleApiClient(mGoogleApiClient);
		c.setUsuario(dadosUsuario);
		c.setToken(token);
		startService(it);
		
		Intent intent = new Intent();
		intent.setAction("br.com.everyfeeds.receiver.ScheduleReceiver");
		sendBroadcast(intent);
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
		threadProfile.cancel(true);
		threadToken.cancel(true);
		if (mGoogleApiClient.isConnected()) {
			Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
			mGoogleApiClient.disconnect();
			mGoogleApiClient.connect();
		}
		Intent intent = new Intent(this, Inicial.class);
		startActivity(intent);
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

	public ImageView getImgProfilePic() {
		return imgProfilePic;
	}

	public TextView getTxtName() {
		return txtName;
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		Controller controller = (Controller)service;
		servico = controller.getServiceListener();
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		// TODO Auto-generated method stub
		
	}

	
}
