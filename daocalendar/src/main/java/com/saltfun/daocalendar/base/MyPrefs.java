package com.saltfun.daocalendar.base;

import java.io.*;
import java.util.Hashtable;

public class MyPrefs {

	private Hashtable hash = new Hashtable();
	private final File f;
	private final String filename;

	public MyPrefs(Class clss) {
		// use class name as filename to check binary file
		filename = (clss == null) ? "null.prefs" : Resource.DATA_PREFIX + ".prefs";

		// Get the file
		f = new File(filename);
		if (f.exists()) {
			// if binary exist, read binary into hash
			//System.out.println(filename + " Found");
			try {
				FileInputStream fi = new FileInputStream(filename);
				ObjectInputStream s = new ObjectInputStream(fi);  //first run warning todo

				try {
					hash = (Hashtable) s.readObject();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				s.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			// if binary not exist, create the file and return prefs with a null
			// hash
			//System.out.println(filename + " Not Found");
			FileOutputStream fos;
			ObjectOutputStream oos;

			try {
				fos = new FileOutputStream(filename);
				oos = new ObjectOutputStream(fos);
				oos.writeObject(hash);
				oos.close();
				//System.out.println("Create " + filename);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public String get(String key, String invalid_string) {
		return (hash.get(key) == null) ? invalid_string : hash.get(key).toString();
	}

	public int getInt(String key, int invalid_value) {
		return (hash.get(key) == null) ? invalid_value : (int) hash.get(key);
	}

	public double getDouble(String key, double invalid_value) {
		return (hash.get(key) == null) ? invalid_value : (double) hash.get(key);
	}

	public void put(String key, String str) {
		hash.put(key, str);
	}

	public void putInt(String key, int val) {
		hash.put(key, val);
	}

	public void putDouble(String key, double val) {
		hash.put(key, val);
	}

	public void remove(String key) {
		hash.remove(key);
	}

	public void save() {
		FileOutputStream fos;
		ObjectOutputStream oos;

		try {
			fos = new FileOutputStream(filename);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(hash);
			oos.close();
			//System.out.println("Save preference to " + filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void deleteFile() {
		f.delete();
	}
	
	public void clear(){
		f.delete();
	}
}
