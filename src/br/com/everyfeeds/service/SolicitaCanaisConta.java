package br.com.everyfeeds.service;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import br.com.everyfeeds.Principal;
import br.com.everyfeeds.R;
import br.com.everyfeeds.R.string;
import br.com.everyfeeds.entity.Canal;
import br.com.everyfeeds.entity.Token;
import br.com.everyfeeds.entity.Usuario;

import com.google.android.gms.common.api.GoogleApiClient;
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
	long inicio = 0;

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
		
		try {
			solicitaSubscriptions();
			if (service == null) {
				inicio = System.currentTimeMillis();
				solicitaInformacaoYouTubeFull(true);
				solicitaInformacaoYouTubeFull(false);
			} else {
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
				gerarTabelaFeeds();
			} else {
				if(feedsAtuais.size()!= 0){
					Collections.sort(feedsAtuais);
				}
				service.verificaFeeds(feedsAtuais);
			}
		} else {
			if (service == null) {
				principalActivity.showBarraAguarde(false);
			}
			Log.e(ERRO_EVERYFEEDS,
					principalActivity.getString(string.msg_erro_conexao));
		}
		return;

	}

	private void gerarTabelaFeeds() {
		TableLayout tabelaFeedsPrincipal = (TableLayout) principalActivity
				.findViewById(R.id.tabelaFeedCorrente);

		TableLayout tabelaOutrosFeeds = (TableLayout) principalActivity
				.findViewById(R.id.tabelaOutrosFeeds);

		if (feedsAntigos.isEmpty() && feedsAtuais.isEmpty()) {
			principalActivity
					.showMessage(principalActivity.getString(string.msg_sem_feed));
		} else {
			for (Canal dadosCanais : feedsAtuais) {
				TableRow linhaTabela = new TableRow(principalActivity);
				linhaTabela.setPadding(0, 0, 0, 5);
				linhaTabela.setGravity(Gravity.CENTER);
				final ImageView imagemFeed = new ImageView(principalActivity);
				imagemFeed.setImageBitmap(dadosCanais.getImagemCanal());
				final String idChannel = dadosCanais.getId();
				imagemFeed.setOnClickListener(new OnClickListener() {
				    @Override
					public void onClick(View v) {
				    	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + idChannel));
				    	principalActivity.startActivity(intent);
					}
				});	
				
				/**imagemFeed.setOnTouchListener(new OnTouchListener() {

			        @Override
			        public boolean onTouch(View v, MotionEvent event) {
			            switch(event.getAction()){
			            case MotionEvent.ACTION_DOWN:
			                                     imagemFeed.setVisibility(75);
			                break;
			            case MotionEvent.ACTION_UP:
			                                     imagemFeed.setVisibility(100);
			                break;
			            }
			            return true;
			        }

					
			    });**/
				
				linhaTabela.addView(imagemFeed);

				TextView descricaoFeed = new TextView(principalActivity);
				descricaoFeed.setPadding(5, 0, 0, 0);
				descricaoFeed.setText(dadosCanais.getTitulo());

				linhaTabela.addView(descricaoFeed);
				tabelaFeedsPrincipal.addView(linhaTabela);
			}
			int indice = 0;
			TableRow linhaTabela = null;
			for (Canal dadosCanais : feedsAntigos) {

				if (indice % 2 == 0) {
					linhaTabela = new TableRow(principalActivity);
					linhaTabela.setPadding(0, 0, 0, 5);
					linhaTabela.setGravity(Gravity.CENTER);
				}
				ImageView imagemFeed = new ImageView(principalActivity);
				imagemFeed.setImageBitmap(dadosCanais.getImagemCanal());
				imagemFeed.setPadding(0, 0, 5, 0);
				linhaTabela.addView(imagemFeed);

				if ((indice == feedsAntigos.size() - 1) || indice % 2 != 0) {
					tabelaOutrosFeeds.addView(linhaTabela);
				}
				indice++;
			}

			principalActivity.atualizaComponentes(true);
		}
		principalActivity.showBarraAguarde(false);
		long mili = System.currentTimeMillis() - inicio;
		int segundos = (int) Math.round(mili / 1000.0);
		principalActivity.showMessage("Os feeds demoraram " + segundos
				+ " segundos para carregar!");
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
		
		SubscriptionListResponse subscriptionResult = null;
		try {
			subscriptionResult = subscriptionRequest.execute();
		} catch (Exception e) {
			Log.e(ERRO_EVERYFEEDS, e.getMessage());
		}
		subscriptionList = subscriptionResult.getItems();
		if (subscriptionResult.getPageInfo().getTotalResults() > 5) {
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
					dadosCanais.setImagemCanal(getImagemFeed(dadosCanais
							.getThumbnails()));
					if(feedsSemana){
						feedsAtuais.add(dadosCanais);
					}else{
						feedsAntigos.add(dadosCanais);
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
					// só irão ser adicionados feeds que foram atualizados no
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

	private Bitmap getImagemFeed(String url) {
		String urldisplay = url;
		Bitmap mIcon11 = null;
		try {
			InputStream in = new java.net.URL(urldisplay).openStream();
			mIcon11 = BitmapFactory.decodeStream(in);
		} catch (Exception e) {
			Log.e(ERRO_EVERYFEEDS, e.getMessage());
			e.printStackTrace();

		}
		return mIcon11;
	}

	

}
