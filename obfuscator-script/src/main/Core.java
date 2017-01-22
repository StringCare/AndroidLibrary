package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.ArrayList;

public class Core {
	
	static BufferedWriter writer = null;
	static String key = null;
	static String variant = null;
	static String module = null;
	static String file = null;
	static String error = null;
	static boolean variantLocated = false;
	static boolean moduleLocated = false;
	static String stringPath = null;
	
	final static boolean DEBUG = true;
	final static String TAG = "obfuscator-script";
	final static String SEPARATOR = "-----------------------------------------------------------------------------";
	final static String VERSION = "0.5";
	final static int maxToShow = 15;
	final static String FOLDER = "string_obfuscation";
	final static String PARAMS_ORDER = "params [module] [variant] [optional:sha1]";
	        

	public static void main(String[] args) {
				
		if (args.length < 2) {
			print(PARAMS_ORDER);
			System.exit(0);
			return;
		}
				
		for (int i = 0; i < args.length; i++) {
			if (i == 0)
				module = args[i];
			else if (i == 1)
				variant = args[i];
			else if (i == 2)
				key = args[i];
		}
		
		
		print(SEPARATOR);
		
		getKey();
		
		if (key == null) {
			if (error != null)
				print(error);
			
			print("SHA1 fingerprint not detected; try " + PARAMS_ORDER);
		} else {
			mainProcess();
		}

	    print(SEPARATOR);
	    print("v " + VERSION);
	}

	public static String getString(BufferedReader br) {
		StringBuilder builder = new StringBuilder();
		
		try {
			String aux = "";
			
			while ((aux = br.readLine()) != null) {
				builder.append(aux);
			}
		} catch (IOException e) {
			if (DEBUG) e.printStackTrace();
		}
		
		return builder.toString();
	}
	
	public static void mainProcess() {
		String xml = "";
		
		if (variant != null)
			stringPath = module + File.separator + "build" + File.separator + "intermediates" +
					File.separator + "res" + File.separator + "merged" +
					File.separator +  variant + File.separator + "values" +
					File.separator + "values.xml";
		
		else {
			print("variant not detected; try " + PARAMS_ORDER);
			System.exit(0);
		}
		
		if (module == null) {
			print("module not detected; try " + PARAMS_ORDER);
			print(SEPARATOR);
			System.exit(0);
		} else if (!checkModule()) {
			print("module " + module + " not detected; wrong module name");
			print(SEPARATOR);
			System.exit(0);
		}
				
        String inputFilePath = getCurrentPath() + stringPath;
       
        print("looking for string file on -> " + inputFilePath);
        
		String message = "";
		
		File file = new File(inputFilePath);
	    try {
	    	FileInputStream stream = new FileInputStream(file);
	    	message = getString(new BufferedReader(new InputStreamReader(stream,"UTF-8")));
		} catch (UnsupportedEncodingException e1) {
			if (DEBUG) e1.printStackTrace();
		} catch (NullPointerException e1) {
			if (DEBUG) e1.printStackTrace();
		} catch (FileNotFoundException e) {
			if (DEBUG) e.printStackTrace();
		}
	    
	    xml = find(message);
	    
	    File fingerFolder = new File(FOLDER);
	    
	    if (!fingerFolder.exists()) {
	    	
	    	try {
				Files.createDirectory(fingerFolder.toPath());
			} catch (IOException e) {
				if (DEBUG) e.printStackTrace();
			}
	    }
	    
	    writeFile(file, xml);
	}
	
	public static void writeFile(File file, String xml) {
		
		try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath()),"UTF-8"));
            writer.write(xml);
        } catch (Exception e) {
        	if (DEBUG) e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            	// 
            }
        }
	}
	
	public static boolean isEncrypted(String value, String key) {
		boolean encrypted = true;
		
		try {
			if (AES.decrypt(value, key).equals(value))	// not encrypted value
				encrypted = false;
			else
				encrypted = true;
		} catch (Exception e) {
			encrypted = false;
		}
		
		return encrypted;
	}

	public static String find(String xmlO) {
		String toFind1 = "hidden=\"true\"";
		
		String xml1 = xmlO;
		while (xml1.indexOf(toFind1) > 0) {
			String toAnalyze = xml1.substring(xml1.indexOf(toFind1), (int)(xml1.length()));
			
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
				
				
				toShow = toShow.length() > maxToShow ? toShow.substring(0, maxToShow) + ".." : toShow;
				encrypted = encrypted.length() > maxToShow ? encrypted.substring(0, maxToShow) + ".." : encrypted;
				print("[" + toShow + "] - [" + encrypted + "]" + (hasExtra ? extra : ""));
			} catch (Exception e) {
				print("error on " + result);
				e.printStackTrace();
			}
			
			
			xml1 = toAnalyze.substring(toAnalyze.indexOf(result + "</string>"), (int)(toAnalyze.length()));

			if (xml1.indexOf(toFind1)  <= 0) break;
		}
		
		return xmlO;
	}
	
	public static String extrac(String val) {
		
		val = val.substring(val.indexOf('>') + 1, val.length());
		val = val.substring(0, val.indexOf("</string>"));
		
		return val;
	}
	
	public static void getKey() {
		try {
			
			String cmd = "";
			if (System.getProperty("os.name").toLowerCase().contains("windows")) {
				cmd = "gradlew.bat";
			} else {
				cmd = "gradlew";
				Runtime.getRuntime().exec("chmod +x ../" + cmd);
			}
						
			InputStream is = Runtime.getRuntime().exec("../" + cmd + " signingReport").getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader buff = new BufferedReader (isr);

			String line;
			while ((line = buff.readLine()) != null) {
				parseTrace(line);
				
				if (key != null) {
					print("SHA1 fingerprint: " + key);
					break;
				}
					
			}
			
		} catch (IOException e) {
			if (DEBUG) e.printStackTrace();
		}
	}

	/**
	 * returns true if key has been located
	 * @param moduleName
	 * @param variant
	 * @param line
	 * @return boolean
	 */
	public static void parseTrace(String line) {
		
		boolean mustPrint = false;
		
		if (line.toLowerCase().contains("downloading")) {
			mustPrint = true;
		} else if (line.toLowerCase().contains("unzipping")) {
			mustPrint = true;			
		} else if (line.toLowerCase().contains("permissions")) {
			mustPrint = true;			
		} else if (line.toLowerCase().contains("sha") && moduleLocated && variantLocated) {
			key = line.split(" ")[1];
		} else if (line.toLowerCase().contains("error")) {
			error = line.split(": ")[1];
		} else if (line.toLowerCase().contains("variant") && moduleLocated) {
			String locV = line.split(" ")[1];
			if (locV.equals(variant)) {
				print(locV + " variant");
				variantLocated = true;
			}
		} else if (line.toLowerCase().contains(":" + module)) {
			moduleLocated = true;		
		}
		
		if (mustPrint)
			print(line);
	}
	
	/**
	 * prints messages (for gradle console)
	 * @param message
	 */
	private static void print(String message) {
		String var = ":undefined:";
		
		if (variant != null)
			var = ":" + module + ":";
		
		var += TAG;
		
		System.out.println(var + " - " + message);
	}
	
	private static String getCurrentPath() {
		String path = new File(".").getAbsolutePath().replace(".", "").replace(module + File.separator,"");
		return path;
	}
	
	private static boolean checkModule() {
		
		boolean ok = false;
		
		File thisRoot = new File(getCurrentPath());
		
		print(getCurrentPath());
				
		for (final File fileEntry : thisRoot.listFiles()) {
	        if (fileEntry.isDirectory() && isModule(fileEntry)
	         		&& fileEntry.getName().toLowerCase().equals(module.toLowerCase())) {
	        	ok = true;
	        	break;
	        }
	    }
		
		return ok; 
	}
	
	private static boolean isModule(File folder) {
		
		boolean hasGradleFile 	= false;
		boolean hasSrcFolder 	= false;
		
		for (final File file : folder.listFiles()) {
	        if (file.getName().equals("build.gradle") && !file.isDirectory())
	        	hasGradleFile = true;
	        else if (file.getName().equals("src") && file.isDirectory())
	        	hasSrcFolder = true;
	    }
		
		return hasGradleFile && hasSrcFolder;
	}
}
