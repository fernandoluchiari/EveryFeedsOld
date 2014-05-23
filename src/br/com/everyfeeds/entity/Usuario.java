package br.com.everyfeeds.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;

public class Usuario implements Serializable{
	private String nome;
	private String email;
	private String profilePlus;
	private Bitmap imagemProfile;
	private List<Canal> canais;
	
	
	public Usuario(){}
		
	public Usuario(String nome, String email, String profilePlus,Bitmap imagemProfile) {
		this.nome = nome;
		this.email = email;
		this.profilePlus = profilePlus;
		this.imagemProfile = imagemProfile;
		canais = new ArrayList<Canal>();
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getProfilePlus() {
		return profilePlus;
	}
	public void setProfilePlus(String profilePlus) {
		this.profilePlus = profilePlus;
	}

	public Bitmap getImagemProfile() {
		return imagemProfile;
	}

	public void setImagemProfile(Bitmap imagemProfile) {
		this.imagemProfile = imagemProfile;
	}

	public List<Canal> getCanais() {
		return canais;
	}

	public void setCanais(List<Canal> canais) {
		this.canais = canais;
	}
	
	
	
}
