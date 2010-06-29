package ca.mcgill.hs.plugin;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.zip.GZIPOutputStream;
import java.util.Date;
import java.util.HashMap;

import ca.mcgill.hs.R;
import ca.mcgill.hs.plugin.BluetoothLogger.BluetoothPacket;
import ca.mcgill.hs.plugin.GPSLogger.GPSLoggerPacket;
import ca.mcgill.hs.plugin.GSMLogger.GSMLoggerPacket;
import ca.mcgill.hs.plugin.SensorLogger.SensorLoggerPacket;
import ca.mcgill.hs.plugin.WifiLogger.WifiLoggerPacket;
import ca.mcgill.hs.util.PreferenceFactory;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * An OutputPlugin which writes data to files.
 * 
 * @author Cicerone Cojocaru, Jonathan Pitre
 *
 */
public class FileOutput extends OutputPlugin{
	
	//HashMap used for keeping file handles. There is one file associated with each input plugin connected.
	private final HashMap<String, DataOutputStream> fileHandles = new HashMap<String, DataOutputStream>();
	
	//File Extensions to be added at the end of each file.
	private final String WIFI_EXT = "-wifiloc.log";
	private final String GPS_EXT = "-gpsloc.log";
	private final String SENS_EXT = "-raw.log";
	private final String GSM_EXT = "-gsmloc.log";
	private final String BT_EXT = "-bt.log";
	private final String DEF_EXT = ".log";
	
	// Size of BufferedOutputStream buffer
	private final int BUFFER_SIZE;
	private final static String BUFFER_SIZE_KEY = "fileOutputBufferSize";
	
	//Boolean ON-OFF switch *Temporary only*
	private final boolean PLUGIN_ACTIVE;
	private final static String PLUGIN_ACTIVE_KEY = "fileOutputEnabled";
	
	/**
	 * This is the basic constructor for the FileOutput plugin. It has to be instantiated
	 * before it is started, and needs to be passed a reference to a Context.
	 * 
	 * @param context - the context in which this plugin is created.
	 */
	public FileOutput(Context context){
		SharedPreferences prefs = 
    		PreferenceManager.getDefaultSharedPreferences(context);
		
		PLUGIN_ACTIVE = prefs.getBoolean(PLUGIN_ACTIVE_KEY, false);
		BUFFER_SIZE = Integer.parseInt(prefs.getString(BUFFER_SIZE_KEY, "4096"));
	}
	
	/**
	 * Closes all files currently open.
	 * 
	 * @override
	 */
	protected void onPluginStop(){
		for (String id : fileHandles.keySet()){
			try {
				fileHandles.get(id).close();
			} catch (IOException e) {
				Log.e("FileOutput", "Caught IOException");
				e.printStackTrace();
			}
		}
	}

	/**
	 * This method gets called whenever an InputPlugin registered to FileOutput has data available
	 * to output. This method creates a file handle (if it doesn't exist already) for the InputPlugin
	 * the received DataPacket comes from. This method calls the appropriate version of dataParse based
	 * on the DataPacket type.
	 * 
	 * @param dp the DataPacket recieved.
	 * 
	 * @override
	 */
	synchronized void onDataReceived(DataPacket dp) {
		if (!PLUGIN_ACTIVE) return;
		String id = dp.getInputPluginName();
		
		try {
			if (!fileHandles.containsKey(id)){
				final File j = new File(Environment.getExternalStorageDirectory(), "hsandroidapp/data");
				if (!j.isDirectory()) {
					if (!j.mkdirs()) {
						Log.e("Output Dir", "Could not create output directory!");
						return;
					}
				}
				//Generate file name based on the plugin it came from and the current time.
				Date d = new Date(System.currentTimeMillis());
				SimpleDateFormat dfm = new SimpleDateFormat("yy-MM-dd-HHmmss");
				File fh = new File(j, dfm.format(d) + getFileExtension(dp));
				if (!fh.exists()) fh.createNewFile();
				Log.i("File Output", "File to write: "+fh.getName());
				fileHandles.put(id, new DataOutputStream(
						new BufferedOutputStream(new GZIPOutputStream(
								new FileOutputStream(fh), BUFFER_SIZE
						))));
			}
			
			//Choose correct dataParse method based on the format of the data received.
			DataOutputStream dos = fileHandles.get(id);
			if (dp.getClass() == WifiLoggerPacket.class){
				dataParse((WifiLoggerPacket) dp, dos);
			} else if (dp.getClass() == GPSLoggerPacket.class) {
				dataParse((GPSLoggerPacket) dp, dos);
			} else if (dp.getClass() == SensorLoggerPacket.class){
				dataParse((SensorLoggerPacket) dp, dos);
			} else if (dp.getClass() == GSMLoggerPacket.class){
				dataParse((GSMLoggerPacket) dp, dos);
			} else if (dp.getClass() == BluetoothPacket.class){
				dataParse((BluetoothPacket) dp, dos);
			}
			
		} catch (IOException e) {
			Log.e("FileOutput", "Caught IOException");
			e.printStackTrace();
		}
	}
	
	/**
	 * Parses and writes given WifiLoggerPacket to given DataOutputStream.
	 * @param wlp the WifiLoggerPacket to parse and write out.
	 * @param dos the DataOutputStream to write to.
	 */
	private void dataParse(WifiLoggerPacket wlp, DataOutputStream dos){
		try {
			dos.writeInt(wlp.neighbors);
			dos.writeLong(wlp.timestamp);
			for (int i = wlp.neighbors - 1; i >= 0; i--){
				dos.writeInt(wlp.levels[i]);
				dos.writeUTF(wlp.SSIDs[i]);
				dos.writeUTF(wlp.BSSIDs[i]);
			}
		} catch (IOException e) {
			Log.e("FileOutput", "Caught IOException (WifiLoggerPacket parsing)");
			e.printStackTrace();
		}
	}
	
	/**
	 * Parses and writes given SensorLoggerPacket to given DataOutputStream.
	 * @param wlp the WifiLoggerPacket to parse and write out.
	 * @param dos the DataOutputStream to write to.
	 */
	private void dataParse(SensorLoggerPacket slp, DataOutputStream dos){
		try {
			dos.writeLong(slp.time);
			dos.writeFloat(slp.x);
			dos.writeFloat(slp.y);
			dos.writeFloat(slp.z);
			dos.writeFloat(slp.m);
			dos.writeFloat(slp.temperature);
			for (float f : slp.magfield) dos.writeFloat(f);
			for (float f : slp.orientation) dos.writeFloat(f);
		} catch (IOException e) {
			Log.e("FileOutput", "Caught IOException (WifiLoggerPacket parsing)");
			e.printStackTrace();
		}
	}
	
	/**
	 * Parses and writes given GPSLoggerPacket to given DataOutputStream.
	 * @param gpslp the GPSLoggerPacket to parse and write out.
	 * @param dos the DataOutputStream to write to.
	 */
	private void dataParse(GPSLoggerPacket gpslp, DataOutputStream dos){
		try {
			dos.writeLong(gpslp.time);
			dos.writeFloat(gpslp.accuracy);
			dos.writeFloat(gpslp.bearing);
			dos.writeFloat(gpslp.speed);
			dos.writeDouble(gpslp.altitude);
			dos.writeDouble(gpslp.latitude);
			dos.writeDouble(gpslp.longitude);
		} catch (IOException e) {
			Log.e("FileOutput", "Caught IOException (GPSLocationPacket parsing)");
			e.printStackTrace();
		}
	}
	
	/**
	 * Parses and writes given GSMLoggerPacket to given DataOutputStream.
	 * @param gsmlp the GSMLoggerPacket to parse and write out.
	 * @param dos the DataOutputStream to write to.
	 */
	private void dataParse(GSMLoggerPacket gsmlp, DataOutputStream dos){
		try {
			dos.writeLong(gsmlp.time);
			dos.writeInt(gsmlp.mcc);
			dos.writeInt(gsmlp.mnc);
			dos.writeInt(gsmlp.cid);
			dos.writeInt(gsmlp.lac);
			dos.writeInt(gsmlp.rssi);
			dos.writeInt(gsmlp.neighbors);
			for (int i = gsmlp.neighbors - 1; i >= 0; i--){
				dos.writeInt(gsmlp.cids[i]);
				dos.writeInt(gsmlp.lacs[i]);
				dos.writeInt(gsmlp.rssis[i]);
			}
		}catch (IOException e) {
			Log.e("FileOutput", "Caught IOException (GSMLoggerPacket parsing)");
			e.printStackTrace();
		}
	}
	
	/**
	 * Parses and writes given BluetoothPacket to given DataOutputStream.
	 * @param gsmlp the BluetoothPacket to parse and write out.
	 * @param dos the DataOutputStream to write to.
	 */
	private void dataParse(BluetoothPacket btp, DataOutputStream dos){
		try {
			dos.writeLong(btp.time);
			dos.writeInt(btp.neighbours);
			for (int i = 0; i<btp.neighbours; i++){
				dos.writeUTF(btp.names.get(i) == null ? "null" : btp.names.get(i));
				dos.writeUTF(btp.addresses.get(i) == null ? "null" : btp.addresses.get(i));
			}
		} catch (IOException e){
			Log.e("FileOutput", "Caught IOException (BluetoothPacket parsing)");
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the String corresponding to the file extension (of the DataPacket) that should be added to the 
	 * name of the file currently being created.
	 * @param dp the given DataPacket
	 * @return the String representing the extension to add to the filename.
	 */
	private String getFileExtension(DataPacket dp){
		if (dp.getClass() == WifiLoggerPacket.class){
			return WIFI_EXT;
		} else if (dp.getClass() == GPSLoggerPacket.class) {
			return GPS_EXT;
		} else if (dp.getClass() == SensorLoggerPacket.class){
			return SENS_EXT;
		} else if (dp.getClass() == GSMLoggerPacket.class){
			return GSM_EXT;
		} else if (dp.getClass() == BluetoothPacket.class){
			return BT_EXT;
		} else {
			return DEF_EXT;
		}
	}
	
	/**
	 * Returns whether or not this OutputPlugin has Preferences.
	 * 
	 * @return whether or not this OutputPlugin has preferences.
	 */
	public static boolean hasPreferences() {return true;}
	
	/**
	 * Returns the list of Preference objects for this OutputPlugin.
	 * 
	 * @param c the context for the generated Preferences.
	 * @return an array of the Preferences of this object.
	 * 
	 * @override
	 */
	public static Preference[] getPreferences(Context c){
		Preference[] prefs = new Preference[2];
		
		prefs[0] = PreferenceFactory.getCheckBoxPreference(c, PLUGIN_ACTIVE_KEY,
				(String)c.getResources().getText(R.string.fileoutput_pluginname_pref), 
				(String)c.getResources().getText(R.string.fileoutput_pluginsummary_pref),
				(String)c.getResources().getText(R.string.fileoutput_pluginenabled_pref), 
				(String)c.getResources().getText(R.string.fileoutput_plugindisabled_pref));
		prefs[1] = PreferenceFactory.getListPreference(c, 
				R.array.fileOutputPluginBufferSizeStrings,
				R.array.fileOutputPluginBufferSizeValues, 
				(String)c.getResources().getText(R.string.fileoutput_buffersizedefault_pref),
				BUFFER_SIZE_KEY,
				R.string.fileoutput_buffersize_pref, 
				R.string.fileoutput_buffersize_pref_summary);

		return prefs;
	}

}
