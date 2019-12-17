package philkes.cubo.bluetooth;

import android.content.Context;
import android.graphics.Movie;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import philkes.cubo.R;

public class DeviceAdapter extends ArrayAdapter<String> {

    private List<String> deviceList= new ArrayList<>();

    public DeviceAdapter(@NonNull Context context, List<String> devices) {
        super(context, 0,devices);
        deviceList=devices;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(getContext()).inflate(R.layout.device_list_item,parent,false);

        String device = deviceList.get(position);

        TextView name = (TextView) listItem.findViewById(R.id.txtDevice);
        name.setText(device);

        return listItem;
    }
}
