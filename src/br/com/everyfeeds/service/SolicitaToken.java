package br.com.everyfeeds.service;

import java.io.IOException;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
	public MainService service;
	public Token token;
	public String scopes;
	public Context context;
	public String ERRO_EVERYFEEDS = "Erro EveryFeeds";

	public SolicitaToken(Principal principalActivity,
			GoogleApiClient mGoogleApiClient, Token token, String scopes,
			Context context,MainService service) {
		this.principalActivity = principalActivity;
		this.mGoogleApiClient = mGoogleApiClient;
		this.token = token;
		this.scopes = scopes;
		this.context = context;
		this.service=service;
	}

	public void requisitaToken() {
		Bundle appActivities = new Bundle();

		String code = null;
		try {
			if (principalActivity != null) {
				code = GoogleAuthUtil.getToken(
						principalActivity, // Context context
						Plus.AccountApi.getAccountName(mGoogleApiClient),
						scopes);
			} else {
				code = GoogleAuthUtil.getToken(
						context, // Context context
						Plus.AccountApi.getAccountName(mGoogleApiClient),
						scopes);
			}
			// definindo o valor do token
			token.setToken(code);
		} catch (IOException transientEx) {
			return;
		} catch (UserRecoverableAuthException e) {
			// and the second call to GoogleAuthUtil.getToken will succeed.
			if (principalActivity != null) {
				principalActivity.startActivityForResult(e.getIntent(),
						AUTH_CODE_REQUEST_CODE);
			} else {
				Log.e(ERRO_EVERYFEEDS, e.getMessage());
			}
			return;
		} catch (GoogleAuthException authEx) {
			if (principalActivity != null) {
				principalActivity.showMessage(authEx.getMessage());
			} else {
				Log.e(ERRO_EVERYFEEDS, authEx.getMessage());
			}
			return;
		} catch (Exception e) {
			if (principalActivity != null) {
				principalActivity.showMessage(e.getMessage());
			} else {
				Log.e(ERRO_EVERYFEEDS, e.getMessage());
			}
			throw new RuntimeException(e);
		}
	}

	@Override
	protected Void doInBackground(Void... params) {
		try {
			requisitaToken();

		} catch (Exception e) {
			if (principalActivity != null) {
				principalActivity.showMessage(e.getMessage());
			} else {
				Log.e(ERRO_EVERYFEEDS, e.getMessage());
			}
		}

		return null;
	}
	
	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		if(principalActivity != null){
			principalActivity.iniciaSolicitacoes();
		}else{
			service.executaSubscriptionsBasic();
		}
	}
}
