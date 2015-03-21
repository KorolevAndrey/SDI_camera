package com.camera.sdi.sdi_camera.ListAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.camera.sdi.sdi_camera.Models.ImageGalleryItem;
import com.camera.sdi.sdi_camera.R;
import com.etsy.android.grid.util.DynamicHeightImageView;

/**
 * Created by sdi on 21.03.15.
 */
public class ImageGalleryAdapter extends ArrayAdapter<ImageGalleryItem> {

    private static class ViewHolder {
        public DynamicHeightImageView imageView;
        public CheckBox checkBox;
        public TextView textViewPath;
    }

    private LayoutInflater mLayoutInflater = null;

    public ImageGalleryAdapter(Context context, int resource) {
        super(context, resource);
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = mLayoutInflater.inflate(R.layout.image_gallery_item, null);

            ViewHolder vh   = new ViewHolder();
            vh.imageView    = (DynamicHeightImageView) convertView.findViewById(R.id.id_dhiv_image);
            vh.checkBox     = (CheckBox) convertView.findViewById(R.id.id_cb_select_image);
            vh.textViewPath = (TextView) convertView.findViewById(R.id.id_tv_description);
            convertView.setTag(vh);
        }

        final ImageGalleryItem item = getItem(position);
        ViewHolder vh = (ViewHolder) convertView.getTag();

        vh.imageView.setImageBitmap(item.getImage());

        vh.checkBox.setChecked(item.isChecked());
        vh.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                item.setCheckedState(isChecked);
            }
        });

        vh.textViewPath.setText(item.isDirectory() ? getItem(position).getFile().getName() : "");

        return convertView;
    }
}
