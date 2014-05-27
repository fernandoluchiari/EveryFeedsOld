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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import br.com.everyfeeds.Principal;
import br.com.everyfeeds.R;
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
	private Principal principalActivity;
	private Usuario dadosUsuario;
	private List<Canal> feedsAtuais = new ArrayList<Canal>();
	private List<Canal> feedsAntigos = new ArrayList<Canal>();
	private MainService service;
	long inicio = 0;

	public SolicitaCanaisConta(Token token, Usuario dadosUsuario,
			Principal principalActivity, MainService service) {
		this.principalActivity = principalActivity;
		this.token = token;
		this.dadosUsuario = dadosUsuario;
		this.service = service;
	}

	@Override
	protected Void doInBackground(Void... params) {
		
			
		try {
			if(service == null){
				principalActivity.showBarraAguarde(true);
				 inicio = System.currentTimeMillis();
				solicitaInformacaoYouTubeFull();
			}else{
				solicitaInformacaoYouTubeBasic();
			}
		} catch (Exception e) {
			principalActivity.showMessage(e.getMessage());
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		if (dadosUsuario.getCanais() != null) {
			if (service == null) {
				organizaFeeds();
				gerarTabelaFeeds();
			}else{
				organizaFeeds();
			}
		} else {
			if(service == null){
				principalActivity
						.showMessage("Ocorreu um erro no seu acesso a internet,tente novamente mais tarde!");
				principalActivity.showBarraAguarde(false);
			}else{
				Log.e("Sem internet","Ocorreu um erro no seu acesso a internet,tente novamente mais tarde!");
			}
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
					.showMessage("Você não possui nenhum feed assinado!");
		} else {
			for (Canal dadosCanais : feedsAtuais) {
				TableRow linhaTabela = new TableRow(principalActivity);
				linhaTabela.setPadding(0, 0, 0, 5);
				linhaTabela.setGravity(Gravity.CENTER);
				ImageView imagemFeed = new ImageView(principalActivity);
				imagemFeed.setImageBitmap(dadosCanais.getImagemCanal());
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

	private void solicitaInformacaoYouTubeFull() throws IOException,
			ParseException {
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
			Log.e("erro ao obter lista das incricoes: ",
					e.getMessage());
		}
		if (subscriptionResult != null) {

			List<Subscription> subscriptionList = subscriptionResult.getItems();

			for (Subscription subscription : subscriptionList) {

				YouTube.Activities.List activityRequest = youTube.activities()
						.list("snippet");
				activityRequest.setChannelId(subscription.getSnippet()
						.getResourceId().getChannelId());
				ActivityListResponse activityResponse = activityRequest
						.execute();
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
				listaCanais.add(dadosCanais);
			}
			dadosUsuario.setCanais(listaCanais);
		}

	}

	private void solicitaInformacaoYouTubeBasic() throws IOException,
			ParseException {
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
			Log.e("erro ao obter lista das incricoes: ",
					 e.getMessage());
		}
		if (subscriptionResult != null) {

			List<Subscription> subscriptionList = subscriptionResult.getItems();

			for (Subscription subscription : subscriptionList) {

				YouTube.Activities.List activityRequest = youTube.activities()
						.list("snippet");
				activityRequest.setChannelId(subscription.getSnippet()
						.getResourceId().getChannelId());
				ActivityListResponse activityResponse = activityRequest
						.execute();
				Calendar dataUltimaAtividade = converteData(activityResponse
						.getItems().get(0).getSnippet().getPublishedAt()
						.toString());
				Canal dadosCanais = new Canal();
				dadosCanais.setId(subscription.getSnippet().getResourceId()
						.getChannelId());
				dadosCanais.setDataUltimaAtualizacao(dataUltimaAtividade);

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
			Log.e("Error", e.getMessage());
			e.printStackTrace();
			principalActivity.showMessage(e.getMessage());
		}
		return mIcon11;
	}

	private void organizaFeeds() {
		Calendar dataAtual = Calendar.getInstance(Locale.ENGLISH);
		Calendar dataInicioSemana = Calendar.getInstance(Locale.ENGLISH);
		dataInicioSemana.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		for (Canal dadosCanal : dadosUsuario.getCanais()) {
			Calendar dataUltimaAtualizacao = dadosCanal
					.getDataUltimaAtualizacao();
			if ((dataUltimaAtualizacao.getTime().before(dataAtual.getTime()) && dataUltimaAtualizacao
					.getTime().after(dataInicioSemana.getTime()))
					|| (dataUltimaAtualizacao.get(Calendar.DAY_OF_YEAR) == dataAtual
							.get(Calendar.DAY_OF_YEAR) || dataUltimaAtualizacao
							.get(Calendar.DAY_OF_YEAR) == dataInicioSemana
							.get(Calendar.DAY_OF_YEAR))) {
				feedsAtuais.add(dadosCanal);
			} else {
				feedsAntigos.add(dadosCanal);
			}
		}
		Collections.sort(feedsAtuais);
		Collections.sort(feedsAntigos);
		if(principalActivity != null){
			principalActivity.showMessage("Feeds atualizados essa semana: "
					+ feedsAtuais.size());
			principalActivity.showMessage("Feeds sem atualização nessa semana: "
					+ feedsAntigos.size());
		}
	}

}
