package br.com.everyfeeds.service;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.os.AsyncTask;
import android.util.Log;
import br.com.everyfeeds.Principal;
import br.com.everyfeeds.entity.Canal;
import br.com.everyfeeds.entity.Token;
import br.com.everyfeeds.entity.Usuario;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.ActivityListResponse;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionListResponse;

public class SolicitaCanaisConta extends AsyncTask<Void, Void, Void> {

	private static YouTube youTube;
	private Token token;
	private Principal principalActivity;
	private Usuario dadosUsuario;
	private List<Subscription> subscriptionList = new ArrayList<Subscription>();
	private List<Canal> feedsAtuais = new ArrayList<Canal>();
	private List<Canal> feedsAntigos = new ArrayList<Canal>();
	private MainService service;
	private Calendar dataUltimaConsulta;
	private String scopes = "oauth2:" + YouTubeScopes.YOUTUBE;

	private String ERRO_EVERYFEEDS = "Erro EveryFeeds";

	public SolicitaCanaisConta(Token token, Usuario dadosUsuario,
			Principal principalActivity, MainService service,
			Calendar dataUltimaConsulta) {
		this.principalActivity = principalActivity;
		this.token = token;
		this.dadosUsuario = dadosUsuario;
		this.service = service;
		this.dataUltimaConsulta = dataUltimaConsulta;
	}

	@Override
	protected Void doInBackground(Void... params) {
		Calendar dataHoje = Calendar.getInstance();
		try {
			if (service == null) {
				solicitaSubscriptions();
				solicitaInformacaoYouTubeFull(true);
				solicitaInformacaoYouTubeFull(false);
			} else {
					solicitaSubscriptions();
				solicitaInformacaoYouTubeBasic();
			}
		} catch (Exception e) {
			Log.e(ERRO_EVERYFEEDS, e.getMessage());
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		if ((feedsAtuais.size()!= 0) || (feedsAntigos.size()!=0)) {
			if (service == null) {
				if(feedsAtuais.size()!= 0){
					Collections.sort(feedsAtuais);
				}else if(feedsAntigos.size()!=0){
					Collections.sort(feedsAntigos);
				}
				dadosUsuario.setCanaisSemana(feedsAtuais);
				dadosUsuario.setCanaisOutros(feedsAntigos);
				
			} else {
				if(feedsAtuais.size()!= 0){
					Collections.sort(feedsAtuais);
				}
				service.verificaFeeds(feedsAtuais);
			}
		} 
		return;

	}

	

	private void solicitaSubscriptions() throws IOException {
		GoogleCredential credential = new GoogleCredential()
				.setAccessToken(token.getToken());
		try{
		youTube = new YouTube.Builder(new NetHttpTransport(),
				new JacksonFactory(), credential).setApplicationName(
				"EveryFeeds").build();
		}catch(Exception e){
			Log.e(ERRO_EVERYFEEDS, e.getMessage());
		}
		YouTube.Subscriptions.List subscriptionRequest = youTube
				.subscriptions().list("snippet");
		subscriptionRequest.setMine(true);
		subscriptionRequest.setMaxResults(50L);
		
		SubscriptionListResponse subscriptionResult = null;
		try {
			subscriptionResult = subscriptionRequest.execute();
		} catch (Exception e) {
			Log.e(ERRO_EVERYFEEDS, e.getMessage());
		}
		subscriptionList = subscriptionResult.getItems();
		if (subscriptionResult.getPageInfo().getTotalResults() > 50) {
			while (subscriptionResult.getNextPageToken() != null) {
				subscriptionRequest = youTube.subscriptions().list("snippet");
				subscriptionRequest.setMine(true);
				subscriptionRequest.setPageToken(subscriptionResult
						.getNextPageToken());
				try {
					subscriptionResult = subscriptionRequest.execute();
				} catch (Exception e) {
					Log.e(ERRO_EVERYFEEDS, e.getMessage());
				}
				subscriptionList.addAll(subscriptionResult.getItems());
			}
		}
	}

	private void solicitaInformacaoYouTubeFull(boolean feedsSemana) throws IOException,
			ParseException {
		List<Canal> listaCanais = new ArrayList<Canal>();
		if (subscriptionList != null) {

			for (Subscription subscription : subscriptionList) {

				YouTube.Activities.List activityRequest = youTube.activities()
						.list("snippet");
				activityRequest.setChannelId(subscription.getSnippet()
						.getResourceId().getChannelId());
				if(feedsSemana){
					activityRequest.setPublishedAfter(getDataAtualConvertida());
				}
				
				ActivityListResponse activityResponse = activityRequest
						.execute();
				if(!activityResponse.getItems().isEmpty()){					
					Calendar dataUltimaAtividade = converteData(activityResponse
							.getItems().get(0).getSnippet().getPublishedAt()
							.toString());
					Canal dadosCanais = new Canal(subscription.getSnippet()
							.getResourceId().getChannelId(), subscription
							.getSnippet().getTitle(), subscription.getSnippet()
							.getDescription(), subscription.getSnippet()
							.getThumbnails().getDefault().getUrl().toString(),
							dataUltimaAtividade);
					
					if(feedsSemana){
						feedsAtuais.add(dadosCanais);
					}else{
						if(!isDuplicidade(dadosCanais)){
							feedsAntigos.add(dadosCanais);
						}
					}
				}
			}
		}

	}

	private void solicitaInformacaoYouTubeBasic() throws IOException,
			ParseException {
		List<Canal> listaCanais = new ArrayList<Canal>();

		if (subscriptionList != null) {

			for (Subscription subscription : subscriptionList) {

				YouTube.Activities.List activityRequest = youTube.activities()
						.list("snippet");
				activityRequest.setChannelId(subscription.getSnippet()
						.getResourceId().getChannelId());
				
				activityRequest.setPublishedAfter(getDataAtualConvertida());

				ActivityListResponse activityResponse = activityRequest
						.execute();
				if (activityResponse.getItems().size() != 0) {
					// só serão adicionados feeds que foram atualizados no
					// inicio da semana
					Calendar dataUltimaAtividade = converteData(activityResponse
							.getItems().get(0).getSnippet().getPublishedAt()
							.toString());
					Canal dadosCanais = new Canal();
					dadosCanais.setId(subscription.getSnippet().getResourceId()
							.getChannelId());
					dadosCanais.setDataUltimaAtualizacao(dataUltimaAtividade);
					feedsAtuais.add(dadosCanais);
				}
			}
		}

	}
	private DateTime getDataAtualConvertida(){
		Calendar dataInicioSemana = null;
		if (dataUltimaConsulta == null) {
			dataInicioSemana = Calendar.getInstance(Locale.ENGLISH);
			dataInicioSemana.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
			dataInicioSemana.set(Calendar.HOUR_OF_DAY, 00);
			dataInicioSemana.set(Calendar.MINUTE, 00);
			dataInicioSemana.set(Calendar.SECOND, 00);
		} else {
			dataInicioSemana = dataUltimaConsulta;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		sdf.format(dataInicioSemana.getTime());
		DateTime data = new DateTime(sdf.getCalendar().getTime());
		
		return data;
		
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

	

	private boolean isDuplicidade(Canal dadosCanal){
		for(Canal dadosCanalAtual:feedsAtuais){
			if(dadosCanalAtual.equals(dadosCanal)){
				return true;
			}
		}
		return false;
	}

}
