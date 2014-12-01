package br.com.everyfeeds.entity;

import java.io.Serializable;
import java.util.Calendar;

import android.graphics.Bitmap;

public class Canal implements Comparable<Canal>,Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = -731179335982522224L;

	private String id;

	private String titulo;

	private String descricao;

	private String thumbnails;

	private Bitmap imagemCanal;

	private Calendar dataUltimaAtualizacao;
	private int mData;

	public Canal() {
	}

	public Canal(String id, String titulo, String descricao, String thumbnails,
			Calendar dataUltimaAtualizacao) {
		super();
		this.id = id;
		this.titulo = titulo;
		this.thumbnails = thumbnails;
		this.dataUltimaAtualizacao = dataUltimaAtualizacao;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String title) {
		this.titulo = title;
	}

	public String getThumbnails() {
		return thumbnails;
	}

	public void setThumbnails(String thumbnails) {
		this.thumbnails = thumbnails;
	}

	public Calendar getDataUltimaAtualizacao() {
		return dataUltimaAtualizacao;
	}

	public void setDataUltimaAtualizacao(Calendar dataUltimaAtualizacao) {
		this.dataUltimaAtualizacao = dataUltimaAtualizacao;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Bitmap getImagemCanal() {
		return imagemCanal;
	}

	public void setImagemCanal(Bitmap imagemCanal) {
		this.imagemCanal = imagemCanal;
	}

	@Override
	public int compareTo(Canal dadosCanal) {
		if (getDataUltimaAtualizacao() == null
				|| dadosCanal.getDataUltimaAtualizacao() == null) {
			return 0;
		}
		return getDataUltimaAtualizacao().compareTo(
				dadosCanal.getDataUltimaAtualizacao());
	}

	@Override
	public boolean equals(Object o) {
		if ((o instanceof Canal) && (((Canal) o).getId().equals(this.getId()))) {
			return true;
		} else {
			return false;
		}
	}

	
}
