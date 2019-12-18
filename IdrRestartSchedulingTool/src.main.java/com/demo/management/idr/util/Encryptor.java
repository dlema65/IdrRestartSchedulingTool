/**
 * Licensed Materials - Property of IBM 
 * 
 * (c) Copyright IBM Corp. 2019 All rights reserved.
 * 
 * The following sample of source code ("Sample") is owned by International 
 * Business Machines Corporation or one of its subsidiaries ("IBM") and is 
 * copyrighted and licensed, not sold. You may use, copy, modify, and 
 * distribute the Sample in any form without payment to IBM.
 * 
 * The Sample code is provided to you on an "AS IS" basis, without warranty of 
 * any kind. IBM HEREBY EXPRESSLY DISCLAIMS ALL WARRANTIES, EITHER EXPRESS OR 
 * IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. Some jurisdictions do 
 * not allow for the exclusion or limitation of implied warranties, so the above 
 * limitations or exclusions may not apply to you. IBM shall not be liable for 
 * any damages you suffer as a result of using, copying, modifying or 
 * distributing the Sample, even if IBM has been advised of the possibility of 
 * such damages.
 */
package com.demo.management.idr.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Encryptor {
	
	// Definición de logger
	private static final Logger logger = LogManager.getLogger(Encryptor.class.getName());
	
	private byte[] key;
	private String initVector;
	
	public Encryptor() throws IOException {
		logger.traceEntry();
		
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());

		// notice that default key file for encryption is located in resources/key.dat
		String keyFile = prefs.get("keyFile", 
				getClass().getClassLoader().getResource("key.dat").getFile());
		logger.trace("Archivo con clave de encriptación es {}", keyFile);
		
		String keyString = new String(Files.readAllBytes(Paths.get(keyFile)), StandardCharsets.UTF_8);
		key = DatatypeConverter.parseHexBinary(keyString);
        initVector = "RandomInitVector"; // 16 bytes IV
        logger.traceExit();
	}
	
    public String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());

            return logger.traceExit(Base64.encodeBase64String(encrypted));
        } catch (Exception ex) {
            logger.error(ex);
        }

        return null;
    }

    public String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));

            return new String(original);
        } catch (Exception ex) {
            logger.error(ex);
        }

        return null;
    }

    /**
     * Only for testing purposes.  Receives a value as first argument and returns its encrypted value
     * 
     * @param args
     */
    public static void main(String[] args) {
    	try {
    		Encryptor encryptor = new Encryptor();
    		
    		String cyphered = encryptor.encrypt(args[0]);
    		String decyphered = encryptor.decrypt(cyphered);

			logger.info("Cyphered value: {}", cyphered);
			logger.info("Decyphered: {}", decyphered);
    	} catch (IOException e) {
    		logger.error(e);
    	}
    }
}
