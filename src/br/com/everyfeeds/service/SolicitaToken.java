package br.com.everyfeeds.service;

import java.io.IOException;

import android.os.AsyncTask;
import android.os.Bundle;
import br.com.everyfeeds.Principal;
import br.com.everyfeeds.entity.Token;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

public class SolicitaToken extends AsyncTask<Void, Void, Void> {

	public static int AUTH_CODE_REQUEST_CODE = 2000;
	public Principal principalActivity;
	public GoogleApiClient mGoogleApiClient;
	public Token token;
	public String scopes;

	public SolicitaToken(Principal principalActivity,
			GoogleApiClient mGoogleApiClient, Token token, String scopes) {
		this.principalActivity = principalActivity;
		this.mGoogleApiClient = mGoogleApiClient;
		this.token = token;
		this.scopes = scopes;
	}

	public void requisitaToken() {
		Bundle appActivities = new Bundle();
	
		String code = null;
		try {
			code = GoogleAuthUtil.getToken(principalActivity, // Context context
					Plus.AccountApi.getAccountName(mGoogleApiClient),scopes);
			// definindo o valor do token
			token.setToken(code);
		} catch (IOException transientEx) {
			return;
		} catch (UserRecoverableAuthException e) {
			// and the second call to GoogleAuthUtil.getToken will succeed.
			principalActivity.startActivityForResult(e.getIntent(),
					AUTH_CODE_REQUEST_CODE);
			return;
		} catch (GoogleAuthException authEx) {
			principalActivity.showMessage(authEx.getMessage());
			return;
		} catch (Exception e) {
			principalActivity.showMessage(e.getMessage());
			throw new RuntimeException(e);
		}
	}

	@Override
	protected Void doInBackground(Void... params) {
		try {
			requisitaToken();
			principalActivity.iniciaServico();
		} catch (Exception e) {
			principalActivity.showMessage(e.getMessage());
		}

		return null;
	}

}
