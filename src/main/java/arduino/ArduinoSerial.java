package arduino;

import java.awt.image.BufferedImage;

/**
*
*
*
*/

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Enumeration;
import java.util.List;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import imageSearching.DetectImage;


public class ArduinoSerial implements SerialPortEventListener {
	SerialPort serialPort;
        /** The port we're normally going to use. */
	private static final String PORT_NAMES[] = { 
			"/dev/tty.usbmodem1411", // Mac OS X
                        "/dev/ttyACM0", // Raspberry Pi
			"/dev/ttyUSB0", // Linux
			"COM3", // Windows
	};
	/**
	* A BufferedReader which will be fed by a InputStreamReader 
	* converting the bytes into characters 
	* making the displayed results codepage independent
	*/
	private BufferedReader input;
	/** The output stream to the port */
	private OutputStream output;
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;
	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 9600;
	
	private Webcam webcam;
	private DetectImage detect;
	private Voice voice;
	private InfoOptimization op;

	public void initialize() {
                // the next line is for Raspberry Pi and 
                // gets us into the while loop and was suggested here was suggested http://www.raspberrypi.org/phpBB3/viewtopic.php?f=81&t=32186
//                System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0");

		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		//First, Find an instance of serial port as set in PORT_NAMES.
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			for (String portName : PORT_NAMES) {
				if (currPortId.getName().equals(portName)) {
					portId = currPortId;
					break;
				}
			}
		}
		if (portId == null) {
			System.out.println("Could not find COM port.");
			return;
		}
		
		try {
			detect=new DetectImage(DetectImage.getVisionService());
			webcam=Webcam.getDefault();
			VoiceManager vm = VoiceManager.getInstance();
	        voice = vm.getVoice("kevin16");
	        op=new InfoOptimization();
	 
	        voice.allocate();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (GeneralSecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		

		try {
			webcam.open();
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(),
					TIME_OUT);

			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			// open the streams
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();

			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}

	/**
	 * This should be called when you stop using the port.
	 * This will prevent port locking on platforms like Linux.
	 */
	public synchronized void close() {
		if (serialPort != null) {
			//serialPort.removeEventListener();
			serialPort.close();
		}
		webcam.close();
	}

	/**
	 * Handle an event on the serial port. Read the data and print it.
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				String inputLine=input.readLine();
				double disM=Integer.parseInt(inputLine.trim())*1.0/100.0;
				BufferedImage image = webcam.getImage();
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write( image, "jpg", baos );
				
				 List<EntityAnnotation> labels = detect.identifyLabel(baos.toByteArray());
				
				
				 if(labels!=null){
					 StringBuilder sb=new StringBuilder();
					 if(disM<=2){
						 sb.append("Caution!There is a "+op.optimize(labels.get(0).getDescription())+" ");
						 if(labels.size()==2){
							 sb.append("or "+op.optimize(labels.get(1).getDescription())+" ");
						 }
						 
						 sb.append(disM+" meters in front of you.");
	 
					 }
					 else if(disM<=4){
						 sb.append("Attention!There is a "+op.optimize(labels.get(0).getDescription())+" ");
						 if(labels.size()==2){
							 sb.append("or "+op.optimize(labels.get(1).getDescription())+" ");
						 }
						 
						 sb.append(disM+" meters in front of you.");
						
					 }
					 else
						 sb.append("All clear");
					 
					 System.out.println(sb.toString());
					 voice.speak(sb.toString());
					 
				 }
				 else{
					 voice.speak("All clear");
					 System.out.println("All Clear");
				 }

			} catch (Exception e) {
				System.err.println(e.toString());
			}
		}
		// Ignore all the other eventTypes, but you should consider the other ones.
	}

	public static void main(String[] args) throws Exception {
		ArduinoSerial main = new ArduinoSerial();
		main.initialize();
		Thread t=new Thread() {
			public void run() {
				//the following line will keep this app alive for 1000 seconds,
				//waiting for events to occur and responding to them (printing incoming messages to console).
				try {Thread.sleep(10000);} catch (InterruptedException ie) {}
			}
		};
		t.start();
		System.out.println("Started");
	}
}