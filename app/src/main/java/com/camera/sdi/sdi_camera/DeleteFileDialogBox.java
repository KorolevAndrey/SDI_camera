package com.camera.sdi.sdi_camera;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

/**
 * Created by sdi on 23.07.14.
 */
public class DeleteFileDialogBox extends Dialog implements View.OnClickListener {
    Context context;

    File fileToDelete = null;

    Button btn_yes, btn_no;

    public DeleteFileDialogBox(Context context, File fileToDelete) {
        super(context);

        setContentView(R.layout.dialog_delete_file);

        this.fileToDelete = fileToDelete;
        this.context      = context;

        btn_no = (Button) findViewById(R.id.id_btn_delete_photo_no);
        btn_yes= (Button) findViewById(R.id.id_btn_delete_photo_yes);

        btn_no.setOnClickListener(this);
        btn_yes.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.id_btn_delete_photo_yes:
                fileToDelete.delete();
                Toast.makeText(context, "deleted " + fileToDelete.getName(), Toast.LENGTH_SHORT).show();
                dismiss();
                break;

            case R.id.id_btn_delete_photo_no:
                cancel();
                break;
        }
    }
}
