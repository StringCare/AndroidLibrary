package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;

public class Core {
	
	static BufferedWriter writer = null;

	public static void main(String[] args) {

		String xml = "";
		System.out.println(":obfuscator-script: -----------------------------------------------------------------------------");
		System.out.println(":obfuscator-script: v0.3 --- bugs or improvements to https://github.com/efraespada/AndroidStringObfuscator/issues");
		System.out.println(":obfuscator-script: -----------------------------------------------------------------------------");

		if (args.length != 2) {
			System.out.println(":obfuscator-script: -> params [xml_file_name] [SHA1_fingerprint_app]");
			System.exit(0);
		}
		String key = "";
		String file = "";
		for (int i = 0; i < args.length; i++) {
			if (i == 0) file = args[i];
			if (i == 1) key = args[i];
		}
		
		File jarFile = new File(".");
		
        String inputFilePath = jarFile.getAbsolutePath().replace(".", "") + file;
		System.out.println(":obfuscator-script: -> looking for string file on -> " + inputFilePath);
		String message = "";
	    try {
	    	FileInputStream stream = new FileInputStream(new File(inputFilePath));
	    	message = getString(new BufferedReader(new InputStreamReader(stream,"UTF-8")));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (NullPointerException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	    
	    xml = find(message, key);
	   
	    String folderName = key.replaceAll(":", "");
	    
	    File fingerFolder = new File(folderName);
	    if (!fingerFolder.exists()) {
	    	try {
				Files.createDirectory(fingerFolder.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    
	    File xmlFile = new File(fingerFolder, "strings.xml");
	    writeFile(xmlFile, xml);
		System.out.println(":obfuscator-script: -----------------------------------------------------------------------------");
	}

	public static String getString(BufferedReader br) {
		StringBuilder builder = new StringBuilder();
		try {
			String aux = "";
			while ((aux = br.readLine()) != null) {
				builder.append(aux);
			}
		} catch (IOException e) {
		  e.printStackTrace();
		}
		return builder.toString();
	}
	
	public static void writeFile(File file, String xml) {
		try {
            writer = new BufferedWriter(new FileWriter(file, false));
            writer.write(xml);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            }
        }
	}
	
	public static boolean isEncrypted(String value, String key) {
		boolean encrypted = true;
		try {
			if (AES.decrypt(value, key).equals(value)) { // not encrypted value
				encrypted = false;
			} else encrypted = true;
		} catch (Exception e) {
			encrypted = false;
		}
		return encrypted;
	}

	public static String find(String xmlO, String key) {
		String toFind1 = "hidden=\"true\"";
		
		String jj = xmlO;
		while (jj.indexOf(toFind1) > 0) {
			String toAnalyze = jj.substring(jj.indexOf(toFind1), (int)(jj.length()));
			
			String result = extrac(toAnalyze);
			
			try {
				String encrypted = "";
				String toShow = "";
				
				String extra = " value_already_encrypted";
				boolean hasExtra = false;
				
				if (isEncrypted(result, key)) {
					encrypted = result;
					toShow = AES.decrypt(result, key);
					hasExtra = true;
				} else {
					encrypted = AES.encrypt(result, key);
					toShow = result;				
					xmlO = xmlO.replaceAll(">" + result + "<", ">" + encrypted + "<");
				}
				
				
				toShow = toShow.length() > 8 ? toShow.substring(0, 8) + ".." : toShow;
				encrypted = encrypted.length() > 8 ? encrypted.substring(0, 8) + ".." : encrypted;
				System.out.println(":obfuscator-script: -> [" + toShow + "] - [" + encrypted + "]" + (hasExtra ? extra : ""));
			} catch (Exception e) {
				System.out.println("error on " + result);
				e.printStackTrace();
			}
			
			
			jj = toAnalyze.substring(toAnalyze.indexOf(result + "</string>"), (int)(toAnalyze.length()));

			if (jj.indexOf(toFind1)  <= 0) break;
		}
		
		return xmlO;
	}
	
	public static String extrac(String val) {
		val = val.substring(val.indexOf('>') + 1, val.length());
		val = val.substring(0, val.indexOf("</string>"));
		return val;
	}
}
