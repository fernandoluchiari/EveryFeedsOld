package br.com.everyfeeds;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.google.android.gms.common.SignInButton;

public class Inicial extends Activity implements OnClickListener {

	private SignInButton btnSignIn;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.inicial);

		btnSignIn = (SignInButton) findViewById(R.id.sign_in_button);
		btnSignIn.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sign_in_button:
			signInWithGplus();
			break;
		}

	}

	/**
	 * Sign-in into google
	 * */
	private void signInWithGplus() {
		if (isDispositivoOnline()) {
			Intent intent = new Intent(this, Principal.class);
			startActivity(intent);
		} else {
			Toast.makeText(
					getApplicationContext(),
					"Verifique sua conexão com a internet e tente novamente mais tarde!",
					Toast.LENGTH_SHORT).show();
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

}
