package gr.forth.ics.urbanNet.security;

import gr.forth.ics.urbanNet.main.UrbanNetApp;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.util.Log;

/**
 * Handles all the operations of saving and retrieving certificates in the internal private memory of urbanNet application.
 * @author syntych
 */
public class CertificateManager {
    Context context;
    private String randomPass;

    public CertificateManager(Context context) {
	this.context = context;
	this.randomPass = null;
	// if (!certExists("server")) {
	Log.d(this.getClass().getName(), "Copying the server certificate in the internal memory.");
	try {
	    InputStream in;
	    in = this.context.getResources().openRawResource(UrbanNetApp.SERVERS[UrbanNetApp.SRV_INX].getBksRId());
	    byte[] buffer = new byte[in.available()];
	    in.read(buffer);
	    in.close();
	    this.saveKeystore("server", buffer);
	}
	catch (IOException e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	// }
    }

    /**
     * Loads the server keystore
     * @return the server keystore as an {@link InputStream}
     */
    public InputStream getServerKeystore() {
	return getKeystore("server");
    }

    /**
     * Loads the client keystore
     * @return the client keystore as an {@link InputStream}
     */
    public InputStream getClientKeystore() {
	return getKeystore("client");
    }

    /**
     * Loads a keystore from a file
     * @param filename The file in which the keystore is
     * @return the keystore as an {@link InputStream}
     */
    public InputStream getKeystore(String filename) {
	try {
	    return context.openFileInput(filename);
	}
	catch (FileNotFoundException e) {
	    e.printStackTrace();
	    return null;
	}
    }

    /**
     * Saves the client keystore in a file
     * @param keystore
     * @return true if saved successfully.
     * @throws IOException
     */
    public void saveClientKeystore(byte[] keystore) throws IOException {
	this.saveKeystore("client", keystore);
    }

    /**
     * Saves a keystore in a file
     * @param filename The file to write in.
     * @param keystore The keystore to save
     * @throws IOException
     */
    public void saveKeystore(String filename, byte[] keystore) throws IOException {
	FileOutputStream out = context.openFileOutput(filename, Context.MODE_PRIVATE);
	out.write(keystore);
	out.close();
    }

    /**
     * Save the pass (16-Byte random string) to a file
     * @param pass the pass
     * @throws IOException
     */
    public void saveRandomPassword(String pass) throws IOException {
	this.randomPass = pass;
	FileOutputStream file = context.openFileOutput("randomPass", Context.MODE_PRIVATE);
	file.write(pass.getBytes(), 0, 32);
	file.close();
    }

    /**
     * Load the pass from the file.
     * @return the pass
     * @throws IOException
     */
    public String loadRandomPassword() throws IOException {
	if (this.randomPass != null) return this.randomPass;
	String pass;
	InputStream file;
	byte[] bytes = new byte[32];
	// try {
	file = context.openFileInput("randomPass");
	file.read(bytes);
	pass = new String(bytes);
	/*} catch (Exception e) { e.printStackTrace(); return null; } */
	if (pass.equals(""))
	// return null;
	throw new IOException("pass is empty String");
	return pass;
    }

    /**
     * Check if a certificate with a specific filename exists in the private storage.
     * @param filename The certificate filename.
     * @return true if the certificate exists.
     */
    public Boolean certExists(String filename) {
	String[] list = context.fileList();
	for (int i = 0; i < list.length; i++) {
	    if (list[i].equals(filename)) return true;
	}
	return false;
    }

    /**
     * Check if the client certificate exists in the private storage.
     * @return true if the client certificate exists.
     */
    public Boolean userCertExists() {
	return this.certExists("client");
    }

    public Context getContext() {
	return context;
    }

    /**
     * <h1>Danger!</h1>
     * <p>
     * This method will delete both the client keystore and the random password!
     * </p>
     * @return True if both files was successfully deleted; else false. to package only.
     */
    public boolean deleteClientKeystoreAndRandomPassword() {
	return (context.deleteFile("client") && context.deleteFile("randomPass"));
    }

}
