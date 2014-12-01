package br.com.everyfeeds.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class Usuario implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2048797189632377531L;
	private String nome;
	private String email;
	private String profilePlus;
	private Bitmap imagemProfile;
	private List<Canal> canaisSemana;
	private List<Canal> canaisOutros;
	private int mData;

	public Usuario() {
	}

	public Usuario(String nome, String email, String profilePlus,
			Bitmap imagemProfile) {
		this.nome = nome;
		this.email = email;
		this.profilePlus = profilePlus;
		this.imagemProfile = imagemProfile;
		canaisSemana = new ArrayList<Canal>();
		canaisOutros = new ArrayList<Canal>();

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

	public List<Canal> getCanaisSemana() {
		return canaisSemana;
	}

	public void setCanaisSemana(List<Canal> canaisSemana) {
		this.canaisSemana = canaisSemana;
	}

	public List<Canal> getCanaisOutros() {
		return canaisOutros;
	}

	public void setCanaisOutros(List<Canal> canaisOutros) {
		this.canaisOutros = canaisOutros;
	}

}
