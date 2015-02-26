package ca.dreamteam.logrunner.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

import ca.dreamteam.logrunner.R;

/**
 * Created by gordon on 25/02/2015.
 */
public class LeDeviceListAdapter extends ArrayAdapter<BluetoothDevice> {

    private Set<String> mLeAddresses;

    private boolean mChanged;

    public LeDeviceListAdapter(Context context, int resource) {
        super(context, resource);

        mLeAddresses = new HashSet<String>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        TextView view;

        if(convertView != null && convertView instanceof TextView){
            view = (TextView) convertView;
        }else{
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);

            view = (TextView)inflater.inflate(R.layout.device_list_text_view, parent, false);
        }

        BluetoothDevice device = getItem(position);

        String name = device.getName();
        if(name == null || name.length() == 0){
            name = "UNKNOWN";
        }

        view.setText(name + " @ " + device.getAddress());
        return view;
    }

    @Override
    public void add(BluetoothDevice device){
        String leAddress = device.getAddress();

        if( ! mLeAddresses.contains(leAddress)){
            mLeAddresses.add(leAddress);
            mChanged = true;
            super.add(device);
        }
    }

    @Override
    public void notifyDataSetChanged(){
        // Only notify if something has actually changed
        if(mChanged){
            mChanged = false;
            super.notifyDataSetChanged();
        }
    }

    @Override
    public void clear(){
        mLeAddresses.clear();
        super.clear();
        mChanged = true;
    }


}
