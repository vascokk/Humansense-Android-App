package ca.mcgill.hs.plugin;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class SensorLogger extends InputPlugin implements SensorEventListener{
	
	private final SensorManager sensorManager;
	private static boolean logging = false;
	private static float temperature = 0.0f;
	private static float[] magfield = { 0.0f, 0.0f, 0.0f };
	private static boolean magfieldUpdated = false;
	private static float[] orientation = { 0.0f, 0.0f, 0.0f };
	
	public SensorLogger(SensorManager sensorManager){
		this.sensorManager = sensorManager;
	}
	
	@Override
	public void startPlugin() {
		Log.i("SensorLogger", "Registered Sensor Listener");
		sensorManager.registerListener(this, 
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_FASTEST);
		sensorManager.registerListener(this, 
				sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_UI);
		sensorManager.registerListener(this, 
				sensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE),
				SensorManager.SENSOR_DELAY_UI);
		logging = true;
	}
	
	public void onSensorChanged(SensorEvent event) {
		if (logging) {
			final Sensor sensor = event.sensor;
	        final int type = sensor.getType();	
		        switch (type) {
		        	case Sensor.TYPE_MAGNETIC_FIELD:
		        		magfield = event.values.clone();
		        		magfieldUpdated = true;
		        		break;
		        	case Sensor.TYPE_TEMPERATURE:
		        		temperature = event.values[0];
		        		break;
		        	case Sensor.TYPE_ACCELEROMETER:
						if (magfieldUpdated) {
							magfieldUpdated = false;
							final int matrix_size = 16;
							float[] R = new float[matrix_size];
							float[] I = new float[matrix_size];
							float[] outR = new float[matrix_size];
							
							SensorManager.getRotationMatrix(R, I, event.values, magfield);

			                SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
			                SensorManager.getOrientation(outR, orientation);							// Update the orientation information.
						}
						logAccelerometerData(event.values, event.timestamp/1000000);
		        	}
				
			}
		}

	private void logAccelerometerData(final float[] values, final long timestamp) {
		final float x = values[0];
		final float y = values[1];
		final float z = values[2];
		final float m = (float) Math.sqrt(x * x + y * y + z * z) - SensorManager.STANDARD_GRAVITY;
		
		write(new SensorLoggerPacket(timestamp, x, y, z, m, temperature, magfield, orientation));
	}


	@Override
	public void stopPlugin() {
		Log.i("SensorLogger", "Unregistered Sensor Listener.");
		sensorManager.unregisterListener(this, 
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
		sensorManager.unregisterListener(this, 
				sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
		sensorManager.unregisterListener(this, 
				sensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE));
		logging = false;
	}


	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {		
	}
	
	// ***********************************************************************************
	// PUBLIC INNER CLASS -- SensorLoggerPacket
	// ***********************************************************************************
	
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
		
		public DataPacket clone(){
			return new SensorLoggerPacket(timestamp, x, y, z, m, temperature, magfield, orientation);
		}

	}

}
