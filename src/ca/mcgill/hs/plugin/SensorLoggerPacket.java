package ca.mcgill.hs.plugin;

public class SensorLoggerPacket implements DataPacket{
	
	final long timestamp;
	final float x;
	final float y;
	final float z;
	final float m;
	final float temperature;
	final float[] magfield;
	final float[] orientation;
	
	public SensorLoggerPacket(final long timestamp, final float x, final float y, final float z, final float m, final float temperature,
			final float[] magfield, final float[] orientation){
		this.timestamp = timestamp;
		this.x = x;
		this.y = y;
		this.z = z;
		this.m = m;
		this.temperature = temperature;
		this.magfield = magfield;
		this.orientation = orientation;
	}
	
	@Override
	public String getInputPluginName() {
		return "SensorLogger";
	}

}
