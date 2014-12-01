package br.com.everyfeeds.entity;

import java.io.Serializable;


public class Token implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8676090044613199250L;
	/**
	 * 
	 */
	private String Token;
	private int mData;
	
	public Token(){}
	
	public String getToken() {
		return Token;
	}

	public void setToken(String token) {
		Token = token;
	}

	
}
