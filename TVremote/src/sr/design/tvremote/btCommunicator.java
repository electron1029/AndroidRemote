package sr.design.tvremote;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import sr.design.tvremote.StartingActivity.btHandler;

import android.bluetooth.BluetoothSocket;

public class btCommunicator extends Thread
{
	private final String commError = "An error occured while trying to communicate with remote device.";
	private final BluetoothSocket btSocket;
	private final btHandler handler;
	private final StartingActivity activ;
	
	private InputStream btInStream;
	private OutputStream btOutStream;
	private boolean waitForAck; //TODO
	private boolean closing;
	
	public boolean isDoneSending = true;

	public btCommunicator(BluetoothSocket socket, btHandler handler, StartingActivity activ)
	{
		btSocket = socket;
		this.handler = handler;
		this.activ = activ;
		
		closing = false;
		
		// Get the input and output streams
		try
		{
			btInStream = socket.getInputStream();
			btOutStream = socket.getOutputStream();
		} catch (IOException e)
		{
			activ.errorReport(commError, true);
		}
	}

	@Override
	public void run()
	{
		byte[] buffer = new byte[1024]; // buffer store for the stream
		int bytes; // bytes returned from read()

		
		// Keep listening to the InputStream until an exception occurs
		while (true)
		{
			try
			{
				// Read from the InputStream
				bytes = btInStream.read(buffer);
				// Send the obtained bytes to the UI activity
				handler.obtainMessage(StartingActivity.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
			} catch (IOException e)
			{
				if(closing) return; //This is expected when we are aborting connection
				handler.obtainMessage(StartingActivity.ERROR, -1, -1, commError).sendToTarget();
			}
		}
	}

	/** Sends bytes through BT socket
	 * to the devices that we are connected to.
	 */
	public void write(byte[] bytes)
	{
		isDoneSending = false;
		try
		{
			int x = bytes.length;
			for(int i=0; i < bytes.length; i=i+64)
			{
				byte[] part = Arrays.copyOfRange(bytes, i, Math.min(i+64, bytes.length));
				btOutStream.write(part);
				//String s = new String(part);
				//System.out.println("***" + s);
				//System.out.println(i);
				try
				{
					Thread.sleep(85);
				} catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		} catch (IOException e)
		{
			if(closing) return; //This is expected when we are aborting connection
			activ.errorReport(commError, false);
			isDoneSending = true;
		}
		try{
			Thread.sleep(150);
		} catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		isDoneSending = true;
	}

	/**
	 * Shut down connection
	 */
	public void cancel()
	{
		try
		{
			closing = true;
			btSocket.close();
		} catch (IOException e)
		{
			System.out.println("Error in closing socket");
			e.printStackTrace();
			//User doesn't need to know about this ;)
		}
	}
}
