package sr.design.tvremote;

import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AdvancedOptionsFragment extends Fragment implements OnClickListener
{

	// Buttons and other layout:
	private Button source, menu, mute;

	@Override
	public View onCreateView(LayoutInflater infalter, ViewGroup container, Bundle b)
	{
		View view = infalter.inflate(R.layout.adv_fragment, container, false);
		// Get layout stuff:
		source = (Button) view.findViewById(R.id.source);
		menu = (Button) view.findViewById(R.id.menu);
		mute = (Button) view.findViewById(R.id.mute);

		// Set click listeners for buttons
		source.setOnClickListener(this);
		menu.setOnClickListener(this);
		mute.setOnClickListener(this);

		return view;
	}

	//TODO: test database will eventually be removed and we can
	//remove this function and try using one onClick method
	//for both fragments
	public void onClick(View v)
	{
		String cmd = null;
		StartingActivity activ = (StartingActivity) getActivity();
		Resources res = activ.getResources();
		
		if(v.getId() ==  R.id.menu)
		{
			cmd = res.getString(R.string.string_menu);
		} else if (v.getId() == R.id.source)
		{
			cmd = res.getString(R.string.string_source);
		} else if (v.getId() == R.id.mute)
		{
			cmd = res.getString(R.string.string_mute);
		} 
		
		cmdDataBase dataBase = new cmdDataBase(activ);
		dataBase.open();

		// Send the encoding of the command through BT:
		if (activ.btComm != null)
			activ.btComm.write(dataBase.getEncoding(StartingActivity.activeRemote, cmd).getBytes());
		else
			activ.errorReport("Please wait while the application " +
					"establishes a Bluetooth connection.", false);

		dataBase.close();
	}
}
