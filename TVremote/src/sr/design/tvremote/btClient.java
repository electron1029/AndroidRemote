package sr.design.tvremote;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;

public class btClient extends AsyncTask<String, Void, String>
{
	private BluetoothSocket btSocket;
	private BluetoothAdapter btAdapter;
	private BluetoothDevice device;
	private final StartingActivity activ;

	// Note: if this random UUID doesn't word for Arduino, try
	// 00001101-0000-1000-8000-00805F9B34FB
	// http://stackoverflow.com/questions/5764958/android-bluetooth-how-to-initiate-pairing?rq=1
	// UUID of server and client must match:
	// UUID.fromString("05201992-1105-1954-0321-19705F9B34FB");
	//private final UUID btID = UUID.fromString("04c6093b-0000-1000-8000-00805f9b34fb");
	private final UUID btID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	public btClient(BluetoothDevice device, BluetoothAdapter btAdapter, StartingActivity activ) throws IOException
	{
		this.btAdapter = btAdapter;
		this.activ = activ;
		this.device = device;

		// Get a BluetoothSocket to connect with the given BluetoothDevice
		btSocket = device.createRfcommSocketToServiceRecord(btID);

	}

	@Override
	protected String doInBackground(String... arg0)
	{
		// Cancel discovery because it will slow down the connection
		btAdapter.cancelDiscovery();
		System.out.println("bt Client started");

		try
		{
			// Connect the device through the socket. This will block
			// until it succeeds or throws an exception
			System.out.println("At socket connect " + device.getName());
			btSocket.connect();
			System.out.println("Connect success");
		} catch (IOException connectException)
		{
			// Unable to connect; close the socket and get out
			connectException.printStackTrace();
			try
			{
				System.out.println("Unable to connect");
				btSocket.close();
			} catch (IOException closeException)
			{
				return "Unable to connect and unable to close socket.";
			}
			return "Unable to connect";
		}
		return "true"; // if all went well, we return true
	}

	@Override
	protected void onPostExecute(String result)
	{
		// If response was not True, something went wrong
		if (!Boolean.parseBoolean(result))
		{
			activ.connect_fail = true;
			activ.errorReport(result, true); //TODO: remember there are other falses to change.
		}

		// Otherwise, we can start managing connection:
		else activ.manageConnectedSocket(btSocket);

		activ.progressBar.dismiss();
	}
}
