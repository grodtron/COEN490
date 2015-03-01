package ca.dreamteam.logrunner.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.dreamteam.logrunner.R;

public class LeServiceListAdapter extends BaseExpandableListAdapter {

    private List<BluetoothGattService> mBluetoothServices;

    public LeServiceListAdapter(){
        mBluetoothServices = Collections.EMPTY_LIST;
    }

    public void setServiceList(List<BluetoothGattService> services){
        mBluetoothServices = services;
        super.notifyDataSetChanged();
    }

    public void clear(){
        mBluetoothServices.clear();
        super.notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return mBluetoothServices.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return mBluetoothServices.get(i).getCharacteristics().size();
    }

    @Override
    public Object getGroup(int i) {
        return mBluetoothServices.get(i);
    }

    @Override
    public Object getChild(int i, int j) {
        return mBluetoothServices.get(i).getCharacteristics().get(j);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int j) {
        return (i << 32) | j;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        BluetoothGattService service = mBluetoothServices.get(i);

        View newView;

        if(view instanceof LinearLayout && view.getId() == R.id.bluetoothServiceListEntry){
            newView = view;
        }else{
            LayoutInflater inflater = (LayoutInflater) viewGroup.getContext().getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);

            newView = inflater.inflate(R.layout.service_list_entry, viewGroup, false);
        }

        ((TextView)newView.findViewById(R.id.serviceUuidText))
                .setText(service.getUuid().toString());

        return newView;
    }

    @Override
    public View getChildView(int i, int j, boolean b, View view, ViewGroup viewGroup) {
        BluetoothGattCharacteristic characteristic
                = mBluetoothServices.get(i).getCharacteristics().get(j);

        View newView;

        if(view instanceof LinearLayout && view.getId() == R.id.bluetoothCharacteristicListEntry){
            newView = view;
        }else{
            LayoutInflater inflater = (LayoutInflater) viewGroup.getContext().getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);

            newView =  inflater.inflate(R.layout.characteristic_list_entry, viewGroup, false);
        }

        TextView uuidText = (TextView) newView.findViewById(R.id.characteristicUuidText);
        uuidText.setText(characteristic.getUuid().toString());

        TextView propertyList = (TextView) newView.findViewById(R.id.characteristicQualitiesText);
        propertyList.setText(BluetoothLeUtil.getReadableCharacteristicProperties(characteristic));

        return newView;
    }

    @Override
    public boolean isChildSelectable(int i, int i2) {
        return true;
    }
}
