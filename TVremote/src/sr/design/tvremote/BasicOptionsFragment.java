package sr.design.tvremote;

import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;

public class BasicOptionsFragment extends Fragment implements OnClickListener
{
	//Buttons and other layout:
	private Button power, button_0, button_1, button_2, button_3, button_4,
		button_5, button_6, button_7, button_8, button_9, channelUp, channelDown,
		volumeUp, volumeDown;
	private long mLastClickTime;

	@Override
	public View onCreateView(LayoutInflater infalter, ViewGroup container, Bundle b)
	{
		View view = infalter.inflate(R.layout.basic_fragment, container, false);
		
		// Get layout stuff:
		power = (Button) view.findViewById(R.id.power);
		button_0 = (Button) view.findViewById(R.id.button_0);
		button_1 = (Button) view.findViewById(R.id.button_1);
		button_2 = (Button) view.findViewById(R.id.button_2);
		button_3 = (Button) view.findViewById(R.id.button_3);
		button_4 = (Button) view.findViewById(R.id.button_4);
		button_5 = (Button) view.findViewById(R.id.button_5);
		button_6 = (Button) view.findViewById(R.id.button_6);
		button_7 = (Button) view.findViewById(R.id.button_7);
		button_8 = (Button) view.findViewById(R.id.button_8);
		button_9 = (Button) view.findViewById(R.id.button_9);
		channelUp = (Button) view.findViewById(R.id.channelUp);
		channelDown = (Button) view.findViewById(R.id.channelDown);
		volumeUp = (Button) view.findViewById(R.id.volumeUp);
		volumeDown = (Button) view.findViewById(R.id.volumeDown);

		// Set click listeners for buttons
		power.setOnClickListener(this);
		button_0.setOnClickListener(this);
		button_1.setOnClickListener(this);
		button_2.setOnClickListener(this);
		button_3.setOnClickListener(this);
		button_4.setOnClickListener(this);
		button_5.setOnClickListener(this);
		button_6.setOnClickListener(this);
		button_7.setOnClickListener(this);
		button_8.setOnClickListener(this);
		button_9.setOnClickListener(this);
		channelUp.setOnClickListener(this);
		channelDown.setOnClickListener(this);
		volumeUp.setOnClickListener(this);
		volumeDown.setOnClickListener(this);
		
		return view;
	}

	public void onClick(View v)
	{	
		String cmd = null;
		StartingActivity activ = (StartingActivity) getActivity();
		Resources res = activ.getResources();
		
		if(v.getId() ==  R.id.power)
		{
			cmd = res.getString(R.string.string_power);
		} else if (v.getId() == R.id.button_0)
		{
			cmd = res.getString(R.string.string_0);
		} else if (v.getId() == R.id.button_1)
		{
			cmd = res.getString(R.string.string_1);
		} else if (v.getId() == R.id.button_2)
		{
			cmd = res.getString(R.string.string_2);
		} else if (v.getId() == R.id.button_3)
		{
			cmd = res.getString(R.string.string_3);
		} else if (v.getId() == R.id.button_4)
		{
			cmd = res.getString(R.string.string_4);
		} else if (v.getId() == R.id.button_5)
		{
			cmd = res.getString(R.string.string_5);
		} else if (v.getId() == R.id.button_6)
		{
			cmd = res.getString(R.string.string_6);
		} else if (v.getId() == R.id.button_7)
		{
			cmd = res.getString(R.string.string_7);
		} else if (v.getId() == R.id.button_8)
		{
			cmd = res.getString(R.string.string_8);
		} else if (v.getId() == R.id.button_9)
		{
			cmd = res.getString(R.string.string_9);
		} else if (v.getId() == R.id.channelDown)
		{
			cmd = res.getString(R.string.string_channel_down);
		} else if (v.getId() == R.id.channelUp)
		{
			cmd = res.getString(R.string.string_channel_up);
		} else if (v.getId() == R.id.volumeDown)
		{
			cmd = res.getString(R.string.string_volume_down);
		} else if (v.getId() == R.id.volumeUp)
		{
			cmd = res.getString(R.string.string_volume_up);
		}

		cmdDataBase dataBase = new cmdDataBase(activ);
		dataBase.open();
	
		//Send the encoding of the command through BT:
		//System.out.println(dataBase.getEncoding(StartingActivity.activeRemote, cmd));
		if(activ.btComm != null)
		{
			activ.btComm.write(dataBase.getEncoding(StartingActivity.activeRemote, cmd).getBytes());
		} else
			activ.errorReport("Please wait while the application " +
					"establishes a Bluetooth connection.", false);

		dataBase.close();
	}
}
