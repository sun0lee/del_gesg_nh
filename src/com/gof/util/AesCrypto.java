package com.gof.util;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AesCrypto {

	private SecretKeySpec secretKey;
	private IvParameterSpec IV;
	
	private static String aes128 = "GofconsultinGesg";                   //AES-128 Key
	private static String aes256 = "GofconsultinGesgGofconsultinGesg";   //AES-256 Key	
	private static String iv     = "gofCon1!gofCon1!";	
	
	
	public static void main(String[] args) throws Exception {
	
		String orgStr = "gesg1234";
		
		AesCrypto ase_128_cbc = new AesCrypto();
		String aes128CbcEncode = ase_128_cbc.AesCBCEncode(orgStr);
		String aes128CbcDeocde = ase_128_cbc.AesCBCDecode(aes128CbcEncode);
		log.info("Aes128 Encode: {}, Decode: {}", aes128CbcEncode, aes128CbcDeocde);
		
		AesCrypto ase_256_cbc = new AesCrypto(aes256, iv);
		String aes256CbcEncode = ase_256_cbc.AesCBCEncode(orgStr);
		String aes256CbcDeocde = ase_256_cbc.AesCBCDecode(aes256CbcEncode);
		log.info("Aes256 Encode: {}, Decode: {}", aes256CbcEncode, aes256CbcDeocde);
	}
	
	
	public AesCrypto(String reqSecretKey, String iv) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		
		this.secretKey = new SecretKeySpec(reqSecretKey.getBytes("UTF-8"), "AES");
		this.IV = new IvParameterSpec(iv.getBytes());
	}	
	
	
	public AesCrypto(String reqSecretKey) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		
		this.secretKey = new SecretKeySpec(reqSecretKey.getBytes("UTF-8"), "AES");
		this.IV = new IvParameterSpec(reqSecretKey.substring(0,16).getBytes());		
	}		
	
	
	public AesCrypto() throws UnsupportedEncodingException, NoSuchAlgorithmException {
		
		this.secretKey = new SecretKeySpec(aes128.getBytes("UTF-8"), "AES");
		this.IV = new IvParameterSpec(iv.getBytes());		
	}
	
	
    //AES CBC PKCS5Padding Encoding(Hex | Base64)
	public String AesCBCEncode(String plainText) throws Exception {		
	
		Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		c.init(Cipher.ENCRYPT_MODE, secretKey, IV);		
		byte[] encrpytionByte = c.doFinal(plainText.getBytes("UTF-8"));
		
		return Hex.encodeHexString(encrpytionByte);        //Hex
//		return Base64.encodeBase64String(encrpytionByte);  //Base64
	}

	//AES CBC PKCS5Padding Decoding(Hex | Base64)
	public String AesCBCDecode(String encodeText) throws Exception {
		
		Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		c.init(Cipher.DECRYPT_MODE, secretKey, IV);
		
		byte[] decodeByte = Hex.decodeHex(encodeText.toCharArray());   //Hex	
//		byte[] decodeByte = Base64.decodeBase64(encodeText);           //Base64
		
		return new String(c.doFinal(decodeByte), "UTF-8");
	}	

}