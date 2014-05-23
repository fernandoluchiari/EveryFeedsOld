package br.com.everyfeeds.service;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import br.com.everyfeeds.entity.Canal;
import br.com.everyfeeds.entity.Token;
import br.com.everyfeeds.entity.Usuario;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ActivityListResponse;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionListResponse;

public class SolicitaCanaisConta extends AsyncTask<Void, Void, Void> {

	private static YouTube youTube;
	private Token token;
	private Usuario dadosUsuario;
	private static String TAG_ERRO_EVERY = "Erro";

	public SolicitaCanaisConta(Token token, Usuario dadosUsuario) {
		this.token = token;
		this.dadosUsuario = dadosUsuario;
	}

	@Override
	protected Void doInBackground(Void... params) {
		try {
			Log.i("Information","iniciando o processo de pesquisa de feeds...");
			solicitaInformacaoYouTube();
		} catch (Exception e) {
			Log.e(TAG_ERRO_EVERY,e.getMessage());
		}
		return null;
	}

	

	private void solicitaInformacaoYouTube() throws IOException, ParseException {
		List<Canal> listaCanais = new ArrayList<Canal>();
		GoogleCredential credential = new GoogleCredential()
				.setAccessToken(token.getToken());
		youTube = new YouTube.Builder(new NetHttpTransport(),
				new JacksonFactory(), credential).setApplicationName(
				"EveryFeeds").build();

		YouTube.Subscriptions.List subscriptionRequest = youTube
				.subscriptions().list("snippet");
		subscriptionRequest.setMine(true);
		subscriptionRequest.setMaxResults(20L);
		SubscriptionListResponse subscriptionResult = null;
		try {
			subscriptionResult = subscriptionRequest.execute();
		} catch (Exception e) {
			Log.e(TAG_ERRO_EVERY,"erro ao obter lista das incricoes: "+e.getMessage());
		}
		if(subscriptionResult != null ){
		
		List<Subscription> subscriptionList = subscriptionResult.getItems();
		
			
			for (Subscription subscription : subscriptionList) {
				
				YouTube.Activities.List activityRequest = youTube.activities().list("snippet");
				activityRequest.setChannelId(subscription.getSnippet().getResourceId()
						.getChannelId());
				ActivityListResponse activityResponse = activityRequest.execute();			
				Calendar dataUltimaAtividade = converteData(activityResponse.getItems().get(0).getSnippet().getPublishedAt().toString());
				Canal dadosCanais = new Canal(subscription.getSnippet().getResourceId()
						.getChannelId(), subscription.getSnippet().getTitle(),
						subscription.getSnippet().getDescription(), subscription
						.getSnippet().getThumbnails().getDefault().getUrl()
						.toString(), dataUltimaAtividade);
				dadosCanais.setImagemCanal(getImagemFeed(dadosCanais
						.getThumbnails()));
				listaCanais.add(dadosCanais);
			}
			dadosUsuario.setCanais(listaCanais);
		}

	}

	private Calendar converteData(String dataUltimaAtividade)
			throws ParseException {
		DateFormat dtf;
		dtf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		Date data = (Date) dtf.parse(dataUltimaAtividade);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(data);
		return calendar;
	}

	private Bitmap getImagemFeed(String url) {
		String urldisplay = url;
		Bitmap mIcon11 = null;
		try {
			InputStream in = new java.net.URL(urldisplay).openStream();
			mIcon11 = BitmapFactory.decodeStream(in);
		} catch (Exception e) {
			Log.e(TAG_ERRO_EVERY, e.getMessage());
			e.printStackTrace();
		}
		return mIcon11;
	}
}
