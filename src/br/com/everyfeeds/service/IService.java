package br.com.everyfeeds.service;

import java.util.List;

import br.com.everyfeeds.entity.Canal;
import br.com.everyfeeds.entity.Token;
import br.com.everyfeeds.entity.Usuario;

import com.google.android.gms.common.api.GoogleApiClient;

public interface IService {

	public GoogleApiClient getGoogleApiClient();

	public Token getToken();

	public Usuario getUsuario();

	public void setGoogleApiClient(GoogleApiClient dadosGoogle);

	public void setToken(Token dadosToken);

	public void setUsuario(Usuario dadosUsuario);

	public List<Canal> getFeedsAntigos();

	public List<Canal> getFeedsNovos();

}
