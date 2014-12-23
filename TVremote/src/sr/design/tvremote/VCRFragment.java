package sr.design.tvremote;

import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;

public class VCRFragment extends Fragment implements OnClickListener
{
	// Buttons and other layout:
	private Button play, stop, forward, rewind_back, eject, pause;

	@Override
	public View onCreateView(LayoutInflater infalter, ViewGroup container, Bundle b)
	{
		View view = infalter.inflate(R.layout.vcr_fragment, container, false);
		// Get layout stuff:
		play = (Button) view.findViewById(R.id.play);
		stop = (Button) view.findViewById(R.id.stop);
		forward = (Button) view.findViewById(R.id.forward);
		rewind_back = (Button) view.findViewById(R.id.rewind);
		eject = (Button) view.findViewById(R.id.button_eject);
		pause = (Button) view.findViewById(R.id.button_pause);

		// Set click listeners for buttons
		play.setOnClickListener(this);
		stop.setOnClickListener(this);
		forward.setOnClickListener(this);
		rewind_back.setOnClickListener(this);
		eject.setOnClickListener(this);
		pause.setOnClickListener(this);

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
		
		if(v.getId() ==  R.id.play)
		{
			cmd = res.getString(R.string.string_play);
		} else if (v.getId() == R.id.stop)
		{
			cmd = res.getString(R.string.string_stop);
		} else if (v.getId() == R.id.forward)
		{
			cmd = res.getString(R.string.string_forward);
		} else if (v.getId() == R.id.rewind)
		{
			cmd = res.getString(R.string.string_rewind);
		}  else if (v.getId() == R.id.button_eject)
		{
			cmd = res.getString(R.string.string_eject);
		}  else if (v.getId() == R.id.button_pause)
		{
			cmd = res.getString(R.string.string_pause);
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
