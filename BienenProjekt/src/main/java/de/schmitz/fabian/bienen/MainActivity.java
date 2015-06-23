package de.schmitz.fabian.bienen;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;




public class MainActivity extends Activity {
	//region Variablen
	private static final int SKALIERTE_GROESSE = 320;
	private static final String URI_SCHLUESSEL = "dieUri";
	private Button zaehlButton;
	private Button kameraButton;
	private Button btnScale;
	private Button btnConfirm;
	private Button btnReset;
	private TextView bienenzahl;
	private ImageView imageView,ivP1 ,ivP2 ,ivP3 ,ivP4;
	private float diffX,diffY;
	private float xcenter1, xcenter2, xcenter3, xcenter4;
	private float ycenter1, ycenter2, ycenter3, ycenter4;
	private boolean rechneVerschiebung;
	private int grenzwert = 115;
	private int bienenProProzentFlaeche = 10;
	private int anzahlBienenInsgesamt;


	MittelpunkteDerKreise Zentren = new MittelpunkteDerKreise(1,1,1,1,1,1,1,1);
	//endregion

	private static final int IMAGE_CAPTURE = 1000;

	private static final String TAG = "BienenZaehlApp";
//region Reset und Kamera
	private OnClickListener resetClickListener = new OnClickListener()
	{
		public void onClick(View v)
		{
			anzahlBienenInsgesamt = 0;
			bienenzahl.setText("Bienen Insgesamt zurückgesetzt");
		}

	};
	private OnClickListener kameraClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			startCamera();
		}
	};
	//endregion
//region Zählbutton
	private OnClickListener zaehlClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View v) {
			Drawable drawable = imageView.getDrawable();
			if (drawable instanceof BitmapDrawable) {
				BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
				Bitmap bitmap = bitmapDrawable.getBitmap();
				BienenBitmap bienenBitmap = erzeugeBienenBitmap(bitmap);

				berechneBienenInsgesamt(bienenBitmap.getBienenAnzahl());
				imageView.setImageBitmap(bienenBitmap.getBlackAndWhite());
				bienenzahl.setText("Anzahl Bienen: "
						+ bienenBitmap.getBienenAnzahl() +
						" Anzahl Bienen insgesamt" + anzahlBienenInsgesamt);

			}
		}

		public BienenBitmap erzeugeBienenBitmap(Bitmap original) {
			int width, height;
			height = original.getHeight();
			width = original.getWidth();
			Bitmap bwbitmap = Bitmap.createBitmap(width, height,
					Bitmap.Config.ARGB_8888);
			int helligkeitRot;
			int helligkeitGruen;
			int helligkeitBlau;
			int helligkeitTransparenz = 0xFF000000;
			int helligkeitGesamt;
			int countBlackPixel = 0;
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					int colour = original.getPixel(x, y);
					helligkeitRot = colour & 0x00ff0000;
					helligkeitBlau = helligkeitRot >> 16;

					if (helligkeitBlau < grenzwert) {
						helligkeitBlau = 0;
						countBlackPixel++;

					} else {
						helligkeitBlau = 255;
					}

					helligkeitRot = helligkeitBlau << 16;
					helligkeitGruen = helligkeitBlau << 8;
					helligkeitGesamt = helligkeitRot | helligkeitGruen
							| helligkeitBlau | helligkeitTransparenz;
					bwbitmap.setPixel(x, y, helligkeitGesamt);
				}
			}
			return new BienenBitmap(bwbitmap, berechneBienenZahl(
					countBlackPixel, width * height));
		}
	};
	//endregion
	// region Scale and Confirm
	private OnClickListener btnConfirmClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			btnScale.setVisibility(View.VISIBLE);
			btnConfirm.setVisibility(View.INVISIBLE);
			ivP1.setVisibility(View.INVISIBLE);
			ivP2.setVisibility(View.INVISIBLE);
			ivP3.setVisibility(View.INVISIBLE);
			ivP4.setVisibility(View.INVISIBLE);

		}
	};
	//scale
	private OnClickListener btnScaleClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
				btnScale.setVisibility(View.INVISIBLE);
				btnConfirm.setVisibility(View.VISIBLE);
				ivP1.setVisibility(View.VISIBLE);
				ivP2.setVisibility(View.VISIBLE);
				ivP3.setVisibility(View.VISIBLE);
				ivP4.setVisibility(View.VISIBLE);
			if(rechneVerschiebung)
			{
				diffX = ivP1.getWidth()/2;
				diffY = (ivP4.getY()-ivP1.getY());
				rechneVerschiebung = false;
			}
		}
	};
	//endregion
//region Punkte schieben
	//Punkt1 wird verschoben
	private OnTouchListener ivP1TouchListener = new OnTouchListener()
	{
		public boolean onTouch(View v , MotionEvent ev)
		{

			if (ev.getAction()==MotionEvent.ACTION_MOVE)
			{
					ivP1.setX(ev.getRawX()-diffX);
					ivP1.setY(ev.getRawY()-diffY);
			}
			return true;
		}
	};



	//Punkt2 wird verschoben
	private OnTouchListener ivP2TouchListener = new OnTouchListener()
	{
		public boolean onTouch(View v , MotionEvent ev)
		{

			if (ev.getAction()==MotionEvent.ACTION_MOVE)
			{
				ivP2.setX(ev.getRawX()-diffX);
				ivP2.setY(ev.getRawY()-diffY);
			}
			return true;
		}
	};




	//Punkt3 wird verschoben
	private OnTouchListener ivP3TouchListener = new OnTouchListener()
	{
		public boolean onTouch(View v , MotionEvent ev)
		{

			if (ev.getAction()==MotionEvent.ACTION_MOVE)
			{
				ivP3.setX(ev.getRawX()-diffX);
				ivP3.setY(ev.getRawY()-diffY);
			}
			return true;
		}
	};



	//Punkt4 wird verschoben
	private OnTouchListener ivP4TouchListener = new OnTouchListener()
	{
		public boolean onTouch(View v , MotionEvent ev)
		{

			if (ev.getAction()==MotionEvent.ACTION_MOVE)
			{
				ivP4.setX(ev.getRawX()-diffX);
				ivP4.setY(ev.getRawY()-diffY);
			}
			return true;
		}
	};

//endregion
//region Create
	private Uri imageUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		setContentView(R.layout.activity_main);
		imageView = (ImageView) findViewById(R.id.imageView);
		ivP1 = (ImageView) findViewById(R.id.ivP1);
		ivP2 = (ImageView) findViewById(R.id.ivP2);
		ivP3 = (ImageView) findViewById(R.id.ivP3);
		ivP4 = (ImageView) findViewById(R.id.ivP4);
		zaehlButton = (Button) findViewById(R.id.button1);
		zaehlButton.setOnClickListener(zaehlClickListener);
		btnScale = (Button) findViewById(R.id.btnScale);
		btnScale.setOnClickListener(btnScaleClickListener);
		btnConfirm = (Button) findViewById(R.id.btnConfirm);
		btnConfirm.setOnClickListener(btnConfirmClickListener);
		bienenzahl = (TextView) findViewById(R.id.Bienenanzahl);
		kameraButton = (Button) findViewById(R.id.kameraButton);
		kameraButton.setOnClickListener(kameraClickListener);
		btnReset = (Button) findViewById(R.id.btnReset);
		btnReset.setOnClickListener(resetClickListener);

		ivP1.setOnTouchListener(ivP1TouchListener);
		ivP2.setOnTouchListener(ivP2TouchListener);
		ivP3.setOnTouchListener(ivP3TouchListener);
		ivP4.setOnTouchListener(ivP4TouchListener);
		rechneVerschiebung = true;


	}
//endregion
//region Zwischenspeicher

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.d(TAG, "Sichere URI Wert: " + imageUri);
		outState.putParcelable(URI_SCHLUESSEL, imageUri);
	}



	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		imageUri = (Uri) savedInstanceState.get(URI_SCHLUESSEL);
		Log.d(TAG, "Restaurierter URI Wert: " + imageUri);
	}
//endregion
//region Rechne Bienen

	public int berechneBienenZahl(int schwarz, int gesamtPixel) {
		float bruchteilSchwarz = (float) schwarz / (float) gesamtPixel;
		float prozentSchwarz = 100 * bruchteilSchwarz;
		return (int) prozentSchwarz * bienenProProzentFlaeche;
	}



	public void berechneBienenInsgesamt (int Bienenaktuell)
	{
		anzahlBienenInsgesamt = anzahlBienenInsgesamt + Bienenaktuell;
	}
	private void startCamera() {
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.TITLE, "Bienenknipser");
		values.put(MediaStore.Images.Media.DESCRIPTION, "Descriptionxxxxxx");
		values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Toast.makeText(this, "Keine SD Karte gesteckt!", Toast.LENGTH_SHORT)
					.show();
			return;
		}

		imageUri = getContentResolver().insert(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		startActivityForResult(intent, IMAGE_CAPTURE);
	}
	//endregion
//region Mittelpunkte geben
	private void kreisMittelpunktBerechnen ()
	{
		ycenter1 = ivP1.getY()+ivP1.getHeight()/2;
		ycenter2 = ivP2.getY()+ivP2.getHeight()/2;
		ycenter3 = ivP3.getY()+ivP3.getHeight()/2;
		ycenter4 = ivP4.getY()+ivP4.getHeight()/2;

		xcenter1 = ivP1.getX()+ivP1.getWidth()/2;
		xcenter2 = ivP2.getX()+ivP2.getWidth()/2;
		xcenter3 = ivP3.getX()+ivP3.getWidth()/2;
		xcenter4 = ivP4.getX()+ivP4.getWidth()/2;

		Zentren.setYcenter1(ycenter1);
		Zentren.setYcenter2(ycenter2);
		Zentren.setYcenter3(ycenter3);
		Zentren.setYcenter4(ycenter4);
		Zentren.setXcenter1(xcenter1);
		Zentren.setXcenter2(xcenter2);
		Zentren.setXcenter3(xcenter3);
		Zentren.setXcenter4(xcenter4);



	}
	//endregion
	// region Punkte sortieren
	public float[][] punkteSortieren() {
		float x1 = Zentren.getXcenter1();
		float y1 = Zentren.getYcenter1();

		float x2 = Zentren.getXcenter2();
		float y2 = Zentren.getYcenter2();

		float x3 = Zentren.getXcenter3();
		float y3 = Zentren.getYcenter3();

		float x4 = Zentren.getXcenter4();
		float y4 = Zentren.getYcenter4();

		float[] xsort = new float[4];
		xsort[0] = x1;
		xsort[1] = x2;
		xsort[2] = x3;
		xsort[3] = x4;
		float[] ysort = new float[4];
		ysort[0] = y1;
		ysort[1] = y2;
		ysort[2] = y3;
		ysort[3] = y4;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3 - i; j++)
			{
				if (xsort[j]>xsort[j+1])
				{
					float tempx = xsort[j];
					xsort[j] = xsort[j+1];
					xsort[j+1] = tempx;
					float tempy = ysort[j];
					ysort[j] = ysort[j+1];
					ysort[j+1] = tempy;
				}

			}
		}

		float[][] punkteArray = new float[4][3];
		//region Einortnen der Punkte

		//region linke Hälfte der Punkte
		if (ysort[0]<ysort[1])
		{
			punkteArray[0][0] = 1;
			punkteArray[0][1] = xsort[0];
			punkteArray[0][2] = ysort[0];

			punkteArray[3][0] = 4;
			punkteArray[3][1] = xsort[1];
			punkteArray[3][2] = ysort[1];

		}
		else
		{
			punkteArray[0][0] = 1;
			punkteArray[0][1] = xsort[1];
			punkteArray[0][2] = ysort[1];

			punkteArray[3][0] = 4;
			punkteArray[3][1] = xsort[0];
			punkteArray[3][2] = ysort[0];
		}
		//endregion
		//region rechte Hälfte der Punkte
		if (ysort[2]<ysort[3])
		{
			punkteArray[1][0] = 2;
			punkteArray[1][1] = xsort[2];
			punkteArray[1][2] = ysort[2];

			punkteArray[2][0] = 3;
			punkteArray[2][1] = xsort[3];
			punkteArray[2][2] = ysort[3];

		}
		else
		{
			punkteArray[1][0] = 2;
			punkteArray[1][1] = xsort[3];
			punkteArray[1][2] = ysort[3];

			punkteArray[2][0] = 3;
			punkteArray[2][1] = xsort[2];
			punkteArray[2][2] = ysort[2];
		}
		//endregion
		//endregion

		return punkteArray;
	}
	//endregion

 	


	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == IMAGE_CAPTURE) {
			if (resultCode == RESULT_OK) {
				try {
					Bitmap bitmapVonKameraBild = MediaStore.Images.Media
							.getBitmap(getContentResolver(), imageUri);

					// Gr��e des aufgenommenen Bildes
					float w1 = bitmapVonKameraBild.getWidth();
					float h1 = bitmapVonKameraBild.getHeight();
					// auf eine H�he von 300 Pixel skalieren
					//int h2 = SKALIERTE_GROESSE;
					//int w2 = (int) (w1 / h1 * (float) h2);
					int h2 = 200;
					int w2 = 320;
					Bitmap skaliert = Bitmap.createScaledBitmap(
							bitmapVonKameraBild, w2, h2, false);

					imageView.setImageBitmap(skaliert);
				} catch (Exception e) {
					Log.e(TAG, "setBitmap()", e);
				}
			} else {
				int rowsDeleted = getContentResolver().delete(imageUri, null,
						null);
				Log.d(TAG, rowsDeleted + " rows deleted");
			}
		}
	}
}
