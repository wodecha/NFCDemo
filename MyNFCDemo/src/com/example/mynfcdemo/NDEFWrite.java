// THE PRESENT FIRMWARE WHICH IS FOR GUIDANCE ONLY AIMS AT PROVIDING CUSTOMERS 
// WITH CODING INFORMATION REGARDING THEIR PRODUCTS IN ORDER FOR THEM TO SAVE 
// TIME. AS A RESULT, STMICROELECTRONICS SHALL NOT BE HELD LIABLE FOR ANY 
// DIRECT, INDIRECT OR CONSEQUENTIAL DAMAGES WITH RESPECT TO ANY CLAIMS 
// ARISING FROM THE CONTENT OF SUCH FIRMWARE AND/OR THE USE MADE BY CUSTOMERS 
// OF THE CODING INFORMATION CONTAINED HEREIN IN CONNECTION WITH THEIR PRODUCTS.

package com.example.mynfcdemo;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class NDEFWrite extends Activity{
	
	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;
	private DataDevice ma = (DataDevice)getApplication();
	private byte[] NdefTextMessageToWrite;
	private byte[] NdefUrlMessageToWrite;
	private byte[] WriteStatus;
	private String NDEFTextMessage;
	private String NDEFUrlMessage;
	private long cpt = 0;
	
	Button launchWrite;
	Button clearEdit;
	RadioButton rbOptionText;
	RadioButton rbOptionUrl;
	EditText ndefTextEdit;
    String messageString = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ndef_write);
		
		mAdapter = NfcAdapter.getDefaultAdapter(this);
		mPendingIntent = PendingIntent.getActivity(this, 0,new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		mFilters = new IntentFilter[] {ndef,};
		mTechLists = new String[][] { new String[] { android.nfc.tech.NfcV.class.getName() } };
		ma = (DataDevice)getApplication();
		Intent intent = getIntent();
		messageString = intent.getStringExtra("message");
		initListener();
	}
	
	@Override
    protected void onNewIntent(Intent intent) 
    {
    	// TODO Auto-generated method stub
    	super.onNewIntent(intent);
    	String action = intent.getAction();
    	if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action))
    	{
	    	Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
	    	DataDevice dataDevice = (DataDevice)getApplication();
	    	dataDevice.setCurrentTag(tagFromIntent);
    	}
    }

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
	}

	@Override
    protected void onPause() {
		cpt = 500;
		super.onPause();
        
    }
	
	private void initListener()
	{
		launchWrite = (Button) findViewById(R.id.ndefWriteEdit);
		clearEdit = (Button) findViewById(R.id.ndefClearEdit);
		ndefTextEdit = (EditText) findViewById(R.id.etNdefWrite);
		rbOptionText = (RadioButton) findViewById(R.id.option1);
		rbOptionUrl = (RadioButton) findViewById(R.id.option2);
		if(!messageString.contains("http://www.")){
			ndefTextEdit.setText(messageString);
			}else{
				ndefTextEdit.setText("");
			}
		clearEdit.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				ndefTextEdit.setText("");
			}
		});
		
		rbOptionText.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				ndefTextEdit.setLines(6);
				if(!messageString.contains("http://www.")){
				ndefTextEdit.setText(messageString);
				}else{
					ndefTextEdit.setText("");
				}
			}
		});
		
		rbOptionUrl.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				ndefTextEdit.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT );
				if(!messageString.contains("http://www.")){
					ndefTextEdit.setText("http://www.");
					}else{
						ndefTextEdit.setText(messageString);
					}
//				ndefTextEdit.setText("http://www.");
				ndefTextEdit.setSelection(ndefTextEdit.getText().length());
			}
		});
		
		launchWrite.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				if (rbOptionText.isChecked() == true && ndefTextEdit.getText().length() > 0)
				{
					new StartWriteTask().execute();
					finishActivity(1);
				}	
				else if (rbOptionUrl.isChecked() == true && ndefTextEdit.getText().length() > 0)
				{
					String tmp;
					
					tmp = ndefTextEdit.getText().toString();
					if (tmp.contains("http://www."))
					{
						tmp = tmp.substring(11);
						ndefTextEdit.setText(tmp);
						Log.i("tmp == " + tmp, " ");
					}
					new StartWriteTask2().execute();
					finishActivity(1);
				}
			}
		});
	}
	
	private class StartWriteTask extends AsyncTask<Void, Void, Void> {
	      private final ProgressDialog dialog = new ProgressDialog(NDEFWrite.this);
	      // can use UI thread here
//	      protected void onPreExecute() 
//	      {
//	         this.dialog.setMessage("Programming...");
//	         if (!this.dialog.isShowing())
//	         this.dialog.show();
//	      }
	      // automatically done on worker thread (separate from UI thread)
	     
	      @Override
	      protected Void doInBackground(Void... params)
			{
				// TODO Auto-generated method stub
				DataDevice dataDevice = (DataDevice)getApplication();
				ma = (DataDevice)getApplication();
		        NDEFTextMessage = ndefTextEdit.getText().toString();
		        NdefTextMessageToWrite = NDEFMessages.ConvertStringToNDEF_Text_ByteArray(NDEFTextMessage, dataDevice);
		        WriteStatus = null;
		        if(NdefTextMessageToWrite != null)
		        {
		        	cpt = 0;
					
					while ((WriteStatus == null || WriteStatus[0] == 1) && cpt <200)
					{
						Log.i("NDEFWrite", "Dan le WRITE MULTIPLE le cpt est ?-----> " + String.valueOf(cpt));
						WriteStatus = NFCCommand.SendWriteMultipleBlockCommand(ma.getCurrentTag(), new byte[]{0x00,0x00}, NdefTextMessageToWrite, dataDevice);
						cpt ++;
					}
		        }
		        else
		        {
		        	WriteStatus= new byte[] {(byte)0x02};//error code for message too long for memory
		        }
		       
				return null;

			}
	      // can use UI thread here
	      protected void onPostExecute(final Void unused)
	      {
	         if (this.dialog.isShowing())
	            this.dialog.dismiss();
	         if(WriteStatus[0] == (byte)0x00)
	         {
	        	 Toast.makeText(ma.getApplicationContext(), "写入成功", Toast.LENGTH_SHORT).show();
	        	 finish();
	         }	 
	         else if(WriteStatus[0] == (byte)0x02)
	         {
	        	 Toast.makeText(ma.getApplicationContext(), "请不要输入过大的数据", Toast.LENGTH_SHORT).show();
	         }
	         
	         else
	         {
				Toast.makeText(ma.getApplicationContext(), "写入失败,请写入您刚扫描的标签.", Toast.LENGTH_SHORT).show();
	         } 
	      }
	   }
	
	private class StartWriteTask2 extends AsyncTask<Void, Void, Void> {
	      private final ProgressDialog dialog = new ProgressDialog(NDEFWrite.this);
	      // can use UI thread here
	      protected void onPreExecute() 
	      {
	         this.dialog.setMessage("Programming...");
	         this.dialog.show();
	      }
	      // automatically done on worker thread (separate from UI thread)
	      
	      @Override
	      protected Void doInBackground(Void... params)
			{
				// TODO Auto-generated method stub
				DataDevice dataDevice = (DataDevice)getApplication();
				ma = (DataDevice)getApplication();
		        NDEFUrlMessage = ndefTextEdit.getText().toString();
		        NdefUrlMessageToWrite = NDEFMessages.ConvertStringToNDEF_Url_ByteArray(NDEFUrlMessage, dataDevice);
				
		        cpt = 0;
				WriteStatus = null;
				
				while ((WriteStatus == null || WriteStatus[0] == 1) && cpt<=200)
				{
					WriteStatus = NFCCommand.SendWriteMultipleBlockCommand(ma.getCurrentTag(), new byte[]{0x00,0x00}, NdefUrlMessageToWrite, dataDevice);
					cpt ++;
				}
				return null;
			}
	      // can use UI thread here
	      protected void onPostExecute(final Void unused)
	      {
	         if (this.dialog.isShowing())
	            this.dialog.dismiss();
	         if(WriteStatus[0] == (byte)0x00)
	         {
	        	 Toast.makeText(ma.getApplicationContext(), "写入成功", Toast.LENGTH_SHORT).show();
	        	 finish();
	         }	 
	         else if(WriteStatus[0] == (byte)0x02)
	         {
	        	 Toast.makeText(ma.getApplicationContext(), "请不要输入过大的数据", Toast.LENGTH_SHORT).show();
	         }
	         
	         else
	         {
				Toast.makeText(ma.getApplicationContext(), "写入失败,请写入您刚扫描的标签.", Toast.LENGTH_LONG).show();
	         } 
	         finish();
	      }
	   }
}
