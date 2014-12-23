package sr.design.tvremote;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class StartingActivity extends Activity
{
	public static String activeRemote = "";
	
	// BT Variables:
	private BluetoothAdapter btAdapter;
	private BroadcastReceiver btReceiver;
	private BluetoothDevice btDevice;
	private btClient client;
	private String deviceName = "HC-06";
	btHandler handler;
	btCommunicator btComm;

	// Communication constants:
	private final int REQUEST_ENABLE_BT = 1;
	public static final int ERROR = 3;
	public static final int MESSAGE_READ = 4;
	public static final int DIALOG_DISMIS = 5;

	// State variables
	boolean connect_fail = false;
	private boolean connected = false;
	private boolean isInBasic = false;
	private boolean isInAdvanced = false; 
	private boolean isInVCR = false;
	ProgressDialog progressBar;
	
	// Dialogs
	AlertDialog questionDialog;
	Dialog message;


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_starting);
		progressBar = new ProgressDialog(this);

		// Show Action bar:
		ActionBar actionBar = getActionBar();
		actionBar.show();

		// this code checks when the activity restarts on screen orientation change
		// or on minimization and subsequent maximization of application
		// to see what the last application state was and changes back to the correct
		// layout accordingly
		if(savedInstanceState != null)
		{
			if (savedInstanceState.getBoolean("isInAdvanced"))
			{
				changeToAdvanced();
			} else if (savedInstanceState.getBoolean("isInVCR"))
			{
				changeToVCR();
			} else
			{
				changeToBasic();
			}
		} else
		{
			changeToBasic();
		}
	}

	protected void onSaveInstanceState (Bundle outState)
	{
		outState.putBoolean("isInBasic", isInBasic);
		outState.putBoolean("isInAdvanced", isInAdvanced);
		outState.putBoolean("isInVCR", isInVCR);
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		setupBT();
		initializeDatabase();	// for basic commands
		initializeAdvancedDatabase();	// separated this in case something goes wrong and we need to take something out
		initializeVCR(); 	// separated again just because
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_starting, menu);
		return true;
	}

	/**
	 * This gets called when user clicks on an item in the action bar. We will
	 * use this to change from basic options to advanced options
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_advanced:
				changeToAdvanced();
				break;
			case R.id.action_basic:
				changeToBasic();
				break;
			case R.id.action_vcr:
				changeToVCR();
				break;
			default:
				break;
		}
		return true;
	}

	private void changeToBasic()
	{
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.main_frame, new BasicOptionsFragment());
		ft.commit();
		isInBasic = true;
		isInAdvanced = false;
		isInVCR = false;
	}

	private void changeToAdvanced()
	{
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.main_frame, new AdvancedOptionsFragment());
		ft.commit();
		isInBasic = false;
		isInAdvanced = true;
		isInVCR = false;
	}
	
	private void changeToVCR()
	{
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.main_frame, new VCRFragment());
		ft.commit();
		isInBasic = false;
		isInAdvanced = false;
		isInVCR = true;
	}

	private void setupBT()
	{
		//Reset critical signal
		connect_fail = false;
		connected = false;
		btDevice = null;
		
		// If we take too long connecting, throw a timeout after 15sec:
		Thread t = new Thread()
		{
			@Override
			public void run()
			{
				new CountDownTimer(15000, 5000)
				{
					@Override
					public void onTick(long mLeft)
					{
						if (connected || connect_fail)
						{
							System.out.println("Timer cancelled " + mLeft);
							this.cancel();
						}
					}

					@Override
					public void onFinish()
					{
						if (!connected && !connect_fail)
						{
							cleanUp();
							errorReport("Unable to connect. Please make " + 
							"sure the base station is turned on and try again.", true);
						}
					}
				}.start();
			}
		};
		t.run();

		// Tell user we are trying to connect BT:
		progressBar.setMessage("Establishing Bluetooth Connection...");
		progressBar.setCancelable(false);
		progressBar.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				questionUser(false, true, "Yes", "No", "Canceling will exit the application.", "Are you sure you want to exit?");
			}
		});
		progressBar.show();

		btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter == null)
		{
			errorReport("Your device does not support Bluetooth communication.", false);
			return;
		}

		if (!btAdapter.isEnabled())
		{
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		System.out.println("at end of OnCreate: " + btAdapter.isEnabled());
		getBTDevice();
	}

	private void getBTDevice()
	{
		Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
		if (pairedDevices.size() > 0)
		{
			System.out.println("Got some devices");
			// Loop through paired devices
			for (BluetoothDevice device : pairedDevices)
			{
				// Print the name and address
				System.out.println(device.getName() + " " + device.getAddress());
				if (device.getName().contains(deviceName))
				{
					System.out.println("Accepted device in pairs");
					startClientThread(device);
					return; // No need to discover other devices anymore
				}
			}
		} else
			System.out.println("No devices");

		// Discover other devices
		// Create a BroadcastReceiver for ACTION_FOUND
		BroadcastReceiver receiver = new BroadcastReceiver()
		{
			@Override
			public void onReceive(Context context, Intent intent)
			{
				System.out.println("In on Receive");
				String action = intent.getAction();
				// When discovery finds a device
				if (BluetoothDevice.ACTION_FOUND.equals(action))
				{
					// Get the BluetoothDevice object from the Intent
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

					if (device == null)
						return;
					// Print what we found
					System.out.println("New device: " + device.getName() + " " + device.getAddress());

					if (device.getName().contains(deviceName))
					{
						System.out.println("Accepted device in onReceive");
						startClientThread(device);
					}
				}
			}
		};
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		btReceiver = receiver;
		registerReceiver(btReceiver, filter);

		// Start Discovery
		btAdapter.startDiscovery();
	}

	private void startClientThread(BluetoothDevice device)
	{
		if(btDevice != null) return; //already accepted a device!
		btDevice = device;
		handler = new btHandler();
		try
		{
			client = new btClient(btDevice, btAdapter, this);
		} catch (IOException e)
		{
			errorReport("An error occured while establishing a connection", true);
		}
		client.execute("");
		System.out.println("Got past starting thread");
	}

	public void manageConnectedSocket(BluetoothSocket socket)
	{
		System.out.println("In manageConnectedSocket");
		btComm = new btCommunicator(socket, handler, this);

		// At this point, we know that all is well with BT.
		// Let the user know of the success and set state variable to true:
		connected = true;
		Toast.makeText(this, "Connection Established!", Toast.LENGTH_LONG).show();
		btComm.start();
	}

	private void cleanUp()
	{
		System.out.println("cleanUp called");
		//Close any leaking dialogs:
		if(message != null && message.isShowing())
			message.dismiss();
		if(questionDialog != null && questionDialog.isShowing())
			questionDialog.dismiss();
		if(progressBar != null && progressBar.isShowing())
			progressBar.dismiss();
		try
		{
			if (btReceiver != null)
				unregisterReceiver(btReceiver);
		}catch (Exception e)
		{
			//do nothing. This means receiver was not registered.
		}
		if (client != null)
			client.cancel(true);
		if (btComm != null)
			btComm.cancel();
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		cleanUp();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		System.out.println("in onActivResult");
		if (resultCode == Activity.RESULT_OK)
		{
			// infoTV.setText("All is well with BT. requestCode = " +
			// requestCode);
			getBTDevice();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/*
	 * populates the database with the correct basic data
	 */
	public void initializeDatabase()
	{
		// don't change this...this is hardcoded for the tv we're using
		activeRemote = "Toshiba VC-N2S";
		
		cmdDataBase dataBase = new cmdDataBase(this);
		Resources res = getResources();
		
		// opens and reads the basic file
		InputStream in_s;
		InputStreamReader inputreader;
		String[] irCodes;

		dataBase.open();
		dataBase.clearDatabase();

		//TODO: doing this retard way
		
		/*********POWER**************/
		in_s = res.openRawResource(R.raw.toshibapower);
		inputreader = new InputStreamReader(in_s);
		irCodes = irCodeReader.readFile(inputreader);
		
		//reading in from external file!
		dataBase.addCommand(activeRemote, res.getString(R.string.string_power), irCodes[0]);
		
		/**************0**********/
		in_s = res.openRawResource(R.raw.toshibazero);
		inputreader = new InputStreamReader(in_s);
		irCodes = irCodeReader.readFile(inputreader);
		dataBase.addCommand(activeRemote, res.getString(R.string.string_0), irCodes[0]);
		
		/***********1*************/
		in_s = res.openRawResource(R.raw.toshibaone);
		inputreader = new InputStreamReader(in_s);
		irCodes = irCodeReader.readFile(inputreader);
		dataBase.addCommand(activeRemote, res.getString(R.string.string_1), irCodes[0]);
		
		/**********2***********/
		in_s = res.openRawResource(R.raw.toshibatwo);
		inputreader = new InputStreamReader(in_s);
		irCodes = irCodeReader.readFile(inputreader);
		dataBase.addCommand(activeRemote, res.getString(R.string.string_2), irCodes[0]);
		
		/**********3**********/
		in_s = res.openRawResource(R.raw.toshibathree);
		inputreader = new InputStreamReader(in_s);
		irCodes = irCodeReader.readFile(inputreader);
		dataBase.addCommand(activeRemote, res.getString(R.string.string_3), irCodes[0]);
		
		/**********4*********/
		in_s = res.openRawResource(R.raw.toshibafour);
		inputreader = new InputStreamReader(in_s);
		irCodes = irCodeReader.readFile(inputreader);
		dataBase.addCommand(activeRemote, res.getString(R.string.string_4), irCodes[0]);
		
		/**********5*********/
		in_s = res.openRawResource(R.raw.toshibafive);
		inputreader = new InputStreamReader(in_s);
		irCodes = irCodeReader.readFile(inputreader);
		dataBase.addCommand(activeRemote, res.getString(R.string.string_5), irCodes[0]);
		
		/**********6*********/
		in_s = res.openRawResource(R.raw.toshibasix);
		inputreader = new InputStreamReader(in_s);
		irCodes = irCodeReader.readFile(inputreader);
		dataBase.addCommand(activeRemote, res.getString(R.string.string_6), irCodes[0]);
		
		/**********7*********/
		in_s = res.openRawResource(R.raw.toshibaseven);
		inputreader = new InputStreamReader(in_s);
		irCodes = irCodeReader.readFile(inputreader);
		dataBase.addCommand(activeRemote, res.getString(R.string.string_7), irCodes[0]);
		
		/**********8*********/
		in_s = res.openRawResource(R.raw.toshibaeight);
		inputreader = new InputStreamReader(in_s);
		irCodes = irCodeReader.readFile(inputreader);
		dataBase.addCommand(activeRemote, res.getString(R.string.string_8), irCodes[0]);
		
		/**********9*********/
		in_s = res.openRawResource(R.raw.toshibanine);
		inputreader = new InputStreamReader(in_s);
		irCodes = irCodeReader.readFile(inputreader);
		dataBase.addCommand(activeRemote, res.getString(R.string.string_9), irCodes[0]);
		
		/********CH UP*********/
		in_s = res.openRawResource(R.raw.toshibachannelup);
		inputreader = new InputStreamReader(in_s);
		irCodes = irCodeReader.readFile(inputreader);
		dataBase.addCommand(activeRemote, res.getString(R.string.string_channel_up), irCodes[0]);
		
		/********CH DN*********/
		in_s = res.openRawResource(R.raw.toshibachanneldown);
		inputreader = new InputStreamReader(in_s);
		irCodes = irCodeReader.readFile(inputreader);
		dataBase.addCommand(activeRemote, res.getString(R.string.string_channel_down), irCodes[0]);
		
		/*******VOL UP*********/
		in_s = res.openRawResource(R.raw.toshibavolumeup);
		inputreader = new InputStreamReader(in_s);
		irCodes = irCodeReader.readFile(inputreader);
		dataBase.addCommand(activeRemote, res.getString(R.string.string_volume_up), irCodes[0]);
		
		/*******VOL DN*********/
		in_s = res.openRawResource(R.raw.toshibavolumedown);
		inputreader = new InputStreamReader(in_s);
		irCodes = irCodeReader.readFile(inputreader);
		dataBase.addCommand(activeRemote, res.getString(R.string.string_volume_down), irCodes[0]);
		
		dataBase.close();
	}
	
	// to add advanced commands to our database.
	// separated in case we have some issue or something things are more flexible
	public void initializeAdvancedDatabase()
	{
		// don't change this...this is hardcoded for the tv we're using
		activeRemote = "Toshiba VC-N2S";
		
		cmdDataBase dataBase = new cmdDataBase(this);
		Resources res = getResources();
		
		InputStream in_s;	// text file order = menu, source, mute
		InputStreamReader inputreader;
		String[] irCodes;

		dataBase.open();
		
		/******MENU********/
		in_s = res.openRawResource(R.raw.toshibamenu);	// text file order = menu, source, mute
		inputreader = new InputStreamReader(in_s);
		irCodes = irCodeReader.readFile(inputreader);
		dataBase.addCommand(activeRemote, res.getString(R.string.string_menu), irCodes[0]);

		/******SOURCE********/
		in_s = res.openRawResource(R.raw.toshibasource);	// text file order = menu, source, mute
		inputreader = new InputStreamReader(in_s);
		irCodes = irCodeReader.readFile(inputreader);
		dataBase.addCommand(activeRemote, res.getString(R.string.string_source), irCodes[0]);
		
		/******MUTE********/
		in_s = res.openRawResource(R.raw.toshibamute);	// text file order = menu, source, mute
		inputreader = new InputStreamReader(in_s);
		irCodes = irCodeReader.readFile(inputreader);
		dataBase.addCommand(activeRemote, res.getString(R.string.string_mute), irCodes[0]);

		dataBase.close();
	}

	// add VCR commands to the database
	private void initializeVCR()
	{
		// don't change this...this is hardcoded for the tv we're using
		activeRemote = "Toshiba VC-N2S";
		
		cmdDataBase dataBase = new cmdDataBase(this);
		Resources res = getResources();
		
		InputStream in_s;	// text file order = menu, source, mute
		InputStreamReader inputreader;
		String[] irCodes;

		dataBase.open();
		
		/******PLAY********/
		in_s = res.openRawResource(R.raw.toshibaplay);	// text file order = menu, source, mute
		inputreader = new InputStreamReader(in_s);
		irCodes = irCodeReader.readFile(inputreader);
		dataBase.addCommand(activeRemote, res.getString(R.string.string_play), irCodes[0]);

		/******STOP********/
		in_s = res.openRawResource(R.raw.toshibastop);	// text file order = menu, source, mute
		inputreader = new InputStreamReader(in_s);
		irCodes = irCodeReader.readFile(inputreader);
		dataBase.addCommand(activeRemote, res.getString(R.string.string_stop), irCodes[0]);
		
		/******REWIND********/
		in_s = res.openRawResource(R.raw.toshibarewind);	// text file order = menu, source, mute
		inputreader = new InputStreamReader(in_s);
		irCodes = irCodeReader.readFile(inputreader);
		dataBase.addCommand(activeRemote, res.getString(R.string.string_rewind), irCodes[0]);

		/******FORWARD********/
		in_s = res.openRawResource(R.raw.toshibafastforward);	// text file order = menu, source, mute
		inputreader = new InputStreamReader(in_s);
		irCodes = irCodeReader.readFile(inputreader);
		dataBase.addCommand(activeRemote, res.getString(R.string.string_forward), irCodes[0]);

		/******EJECT********/
		in_s = res.openRawResource(R.raw.toshibaeject);	// text file order = menu, source, mute
		inputreader = new InputStreamReader(in_s);
		irCodes = irCodeReader.readFile(inputreader);
		dataBase.addCommand(activeRemote, res.getString(R.string.string_eject), irCodes[0]);

		/******PAUSE********/
		in_s = res.openRawResource(R.raw.toshibapause);	// text file order = menu, source, mute
		inputreader = new InputStreamReader(in_s);
		irCodes = irCodeReader.readFile(inputreader);
		dataBase.addCommand(activeRemote, res.getString(R.string.string_pause), irCodes[0]);

		
		dataBase.close();	
	}

	/** Ask user a question **/
	private void questionUser(final boolean retry, final boolean reOpenProgressBar, String exitButtonTitle, String button2Title, String message,
			String alertTitle)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setCancelable(false);
		builder.setPositiveButton(exitButtonTitle, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				finish(); // Exit app
				return;
			}
		});
		builder.setNegativeButton(button2Title, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				dialog.cancel();
				// Retry
				if (retry)
					setupBT();
				// Re-show the previous dialog
				else if (reOpenProgressBar && !connected && !progressBar.isShowing())
					progressBar.show();
				return;
			}
		});
		questionDialog = builder.create();
		questionDialog.setTitle(alertTitle);
		questionDialog.show();
	}

	/**
	 * Method to be called whenever an error occurred to let user know what went
	 * wrong
	 * 
	 * @param error
	 * @param retryConnect
	 *            - if true, app will try to re-establish BT connection. If
	 *            false, no retry option will be given to user
	 */
	protected void errorReport(String error, boolean retryConnect)
	{
		String title = "Something went wrong:";

		if (retryConnect)
		{
			if (connected)
				return; // Sometimes we get false signal. TODO: test this
			questionUser(true, false, "Exit", "Retry", error, title);
			return;
		}
		message = new Dialog(this);
		message.setTitle(title);
		TextView tv = new TextView(this);
		tv.setTextSize(26);
		tv.setText(error);
		tv.setPadding(10, 10, 10, 10);
		message.setContentView(tv);
		message.show();

	}

	/***
	 * This class handles messages that the GUI Thread receives from other
	 * threads
	 */
	class btHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			String response;

			switch (msg.what)
			{
			// TODO: What will Arduino actually send us?
				case MESSAGE_READ:
					response = new String((byte[]) msg.obj);
					// System.out.println("Response: " + response);
					// if(!Boolean.parseBoolean(response))
					// errorReport("Unable to communicate with remote device.");
					break;
				case ERROR:
					response = new String((byte[]) msg.obj);
					errorReport(response, false);
					break;
				case DIALOG_DISMIS:
					// System.out.println("dismissing");
					// progressBar.dismiss();
					// System.out.println("dismissed");
					break;
			}
		}
	}

}
