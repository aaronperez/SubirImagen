package com.aaron.subirimagenes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URL;


public class Main extends Activity {

    private RadioGroup rbGrupo;
    private EditText etURL,etComo;
    private ImageView ivImagen;
    private HiloFacil hf=null;
    private Bitmap bmImagen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initComponent();
        if (savedInstanceState != null) {
            bmImagen = savedInstanceState.getParcelable("bitmap");
            ivImagen.setImageBitmap(bmImagen);
        }
        else{
            ivImagen.setImageDrawable(getResources().getDrawable(R.drawable.fondo));
        }
    }

    public void initComponent(){
        etURL=(EditText)findViewById(R.id.etURL);
        etComo=(EditText)findViewById(R.id.etComo);
        ivImagen=(ImageView)findViewById(R.id.ivImagen);
        rbGrupo=(RadioGroup)findViewById(R.id.rgGrupo);
    }

    @Override
    protected void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
        b.putParcelable("bitmap", bmImagen);
    }

    //Lanza un hilo para no bloquear el teléfono durante la carga de la imagen
    public void descargar(View v) throws InterruptedException {
        if (valido()) {
            hf = new HiloFacil();
            hf.execute(new String[]{etURL.getText().toString()});
        }
    }

    //Usamos el nombre original para ponerlo por defecto
    public String nombreArchivo(String n) {
        String nombre = n;
        int x = 0;
        while (nombre.indexOf("/", x) != -1) {
            x = nombre.indexOf("/", x) + 1;
        }
        return nombre.substring(x);
    }

    //Comprueba si URL está vacía y si cumple con una extensión válida
    public Boolean valido() {
        String nombre = etURL.getText().toString();
        if (nombre.isEmpty()) {
            tostada(R.string.errorNombreVacio);
            return false;
        } else {
            if(etComo.getText().toString().isEmpty()){
                nombre = nombreArchivo(nombre);
                etComo.setText(nombre);
            }
            else{
                nombre=etComo.getText().toString();
            }
            if(!etComo.getText().toString().substring(etComo.getText().toString().length()-4,etComo.getText().toString().length()).equals(etURL.getText().toString().substring(etURL.getText().toString().length()-4,etURL.getText().toString().length()))){
                tostada(R.string.errorFormato);
                return false;
            }
            if (nombre.substring(nombre.length() - 4, nombre.length()).compareToIgnoreCase(".gif") == 0
                    || nombre.substring(nombre.length() - 4, nombre.length()).compareToIgnoreCase(".jpg") == 0
                    || nombre.substring(nombre.length() - 4, nombre.length()).compareToIgnoreCase(".png") == 0
                    || nombre.substring(nombre.length() - 4, nombre.length()).compareToIgnoreCase("jpeg") == 0) {
                return true;
            }
        }
        tostada(R.string.errorTipoArchivo);
        return false;
    }

    /* Mostramos un mensaje flotante a partir de un recurso string*/
    private void tostada(int s) {
        Toast.makeText(this, getText(s), Toast.LENGTH_SHORT).show();
    }

    class HiloFacil extends AsyncTask<String, Integer, Bitmap> {

        private ProgressDialog dialogo;

        //Lanzamos un dialogo que nos indica que está en carga
        @Override
        protected void onPreExecute() {
            dialogo = new ProgressDialog(Main.this);
            dialogo.setMessage(getString(R.string.descargando));
            dialogo.setCancelable(false);
            dialogo.show();
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... objects) {
            String url = objects[0];
            if (!url.substring(0, 7).equals("http://") && !url.substring(0, 8).equals("https://")) {
                url = "http://" + url;
            }
            try {
                URL u = new URL(url);
                Bitmap image = BitmapFactory.decodeStream(u.openConnection().getInputStream());
                return image;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bm) {
            super.onPostExecute(bm);
            bmImagen=bm;
            ivImagen.setImageBitmap(bm);
            dialogo.dismiss();
            FileOutputStream salida = null;
            try {
                RadioButton rb = (RadioButton) findViewById(rbGrupo.getCheckedRadioButtonId());
                switch (Integer.valueOf(rb.getTag().toString())) {
                    case 0:
                        salida = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + etComo.getText().toString());
                        break;
                    case 1:
                        salida = new FileOutputStream(getExternalFilesDir(Environment.DIRECTORY_DCIM) + "/" + etComo.getText().toString());
                        break;
                }
                bm.compress(Bitmap.CompressFormat.PNG, 50, salida);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
    }
}
