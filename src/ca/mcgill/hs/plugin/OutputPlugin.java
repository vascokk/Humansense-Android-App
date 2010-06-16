package ca.mcgill.hs.plugin;

import java.util.LinkedList;

import android.content.Context;
import android.preference.Preference;
import android.util.Log;

/**
 * Abstract class to be extended by all OutputPlugins. Provides an interface for using OutputPlugins.
 * 
 * @author Cicerone Cojocaru, Jonathan Pitre
 *
 */
public abstract class OutputPlugin implements Plugin, Runnable {
	
	//Waiting list for incoming DataPackets kept in case more than one arrives before the previous one can be handled.
	private final LinkedList<DataPacket> dpList = new LinkedList<DataPacket>();
	
	/**
	 * Called when a DataPacket is sent from an InputPlugin. Adds the DataPacket that is now ready to dpList.
	 * @param dp the DataPacket that is ready to be received.
	 */
	public final void onDataReady(DataPacket dp){ dpList.addLast(dp); }
	
	/**
	 * Used by the ThreadPool to continuously retrieved DataPackets from dpList. The DataPacket are
	 * passed to onDataReceived() one at a time for as long as this plugin is running and dpList is not empty.
	 */
	public void run(){
		while (!dpList.isEmpty()){
			DataPacket dp = dpList.removeFirst();
			onDataReceived(dp);
		}
	}
							
	/**
	 * Starts the plugin and calls onPluginStart().
	 */
	public final void startPlugin() {
		onPluginStart();
	}
		
	/**
	 * Stops the plugin and calls onPluginStop().
	 */
	public final void stopPlugin(){
		onPluginStop();
	}
		
	/**
	 * Called when this OutputPlugin is started. This method is meant to be overridden.
	 */
	protected void onPluginStart(){}
	
	/**
	 * Called when this OutputPlugin is stopped. This method is meant to be overridden.
	 */
	protected void onPluginStop(){}
	
	/**
	 * Called when there is data available for this plugin.
	 * @param dp the DataPacket that this plugin is receiving.
	 */
	abstract void onDataReceived(DataPacket dp);
	
	//TODO Write these comments.
	public static Preference[] getPreferences(Context c){return null;}
	public static boolean hasPreferences(){return false;}

}
