package com.example.mynfcdemo;

import java.nio.charset.Charset;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements OnClickListener{

	NfcAdapter mAdapter;
	TextView promt;
	NdefMessage mNdefPushMessage;
	PendingIntent mPendingIntent;
	String[][] techListsArray;
	String ndefReadTag;
	private String[][] mTechLists;
	private IntentFilter[] mFilters;
	private int numberOfBlockToRead = 0;
	private byte[] bNumberOfBlock = null;
	private DataDevice ma = (DataDevice) getApplication();
	private String sNDEFMessage = "";
	private byte[] fullNdefMessage = null;
	private long cpt = 0;
	private String info = "";
	private Button nfcWrite;
	private Intent writeIntent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		promt = (TextView) findViewById(R.id.promt);
		nfcWrite = (Button) findViewById(R.id.NFCWriteButton);
//		nfcWrite.setVisibility(View.INVISIBLE);
		nfcWrite.setOnClickListener(this);
		// 获取默认的NFC控制器
		mAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mAdapter == null) {
			promt.setText("设备不支持NFC！");
			finish();
			return;
		}
		if (!mAdapter.isEnabled()) {
			promt.setText("请在系统设置中先启用NFC功能！");
			finish();
			return;
		}
		mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		NdefRecord[] arrayOfNdefRecord = new NdefRecord[1];
		arrayOfNdefRecord[0] = newTextRecord("Message from NFC Reader :-)",
				Locale.CHINA, true);
		this.mNdefPushMessage = new NdefMessage(arrayOfNdefRecord);
		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
		ndef.addCategory("*/*");
		mFilters = new IntentFilter[] { ndef, };
		mTechLists = new String[][] { new String[] { android.nfc.tech.NfcV.class
				.getName() },new String[] { MifareClassic.class
				.getName() }  };
		ma = (DataDevice) getApplication();
	}

	private NdefRecord newTextRecord(String payload, Locale locale,
			boolean encodeInUtf8) {
		byte[] langBytes = locale.getLanguage().getBytes(
				Charset.forName("US-ASCII"));

		Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset
				.forName("UTF-16");

		byte[] textBytes = payload.getBytes(utfEncoding);

		int utfBit = encodeInUtf8 ? 0 : (1 << 7);

		char status = (char) (utfBit + langBytes.length);

		byte[] data = new byte[1 + langBytes.length + textBytes.length];

		data[0] = (byte) status;

		System.arraycopy(langBytes, 0, data, 1, langBytes.length);

		System.arraycopy(textBytes, 0, data, 1 + langBytes.length,
				textBytes.length);

		NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,

		NdefRecord.RTD_TEXT, new byte[0], data);

		return record;
	}

	@Override
	public void onNewIntent(Intent paramIntent) {
		super.onNewIntent(paramIntent);
		// setIntent(paramIntent);
		nfcWrite.setVisibility(View.INVISIBLE);
		resolveIntent(paramIntent);

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (this.mAdapter == null)
			return;
		if (!this.mAdapter.isEnabled()) {
			promt.setText("请在系统设置中先启用NFC功能！");
		}
		// this.mAdapter.enableForegroundDispatch(this, this.mPendingIntent,
		// null, null);
		mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters,
				mTechLists);
		this.mAdapter.enableForegroundNdefPush(this, this.mNdefPushMessage);
	}

	protected void resolveIntent(Intent intent) {
		// 得到是否检测到TAG触发
		if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())
				|| NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())
				|| NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
			// 处理该intent
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			// 获取id数组
			DataDevice ma = (DataDevice) getApplication();
			ma.setCurrentTag(tag);
			byte[] bytesId = tag.getId();
			String temp = null;
			temp =  toHexString(bytesId);
			if(temp.length() == 8){
				info = temp;
				String msg = "ID:" + temp;
				
				if(null!=processIntent(intent)){
					msg = msg + "\n" + "Message: " + processIntent(intent);
				}
				promt.setText(msg);
				nfcWrite.setVisibility(View.VISIBLE);
				sNDEFMessage = processIntent(intent);
				writeIntent = new Intent(MainActivity.this,WriteTag.class);
			}else if(temp.length() == 16){
				char[] chare = temp.toCharArray();
				StringBuilder sBuilder = new StringBuilder();
				for(int i = 7;i>=0;i--){
					
					sBuilder.append(chare[2*i]);
					sBuilder.append(chare[2*i + 1]);
					
				}
				info = sBuilder.toString();
//				promt.setText("ID:" + info);.
				 byte[] GetSystemInfoAnswer = NFCCommand.SendGetSystemInfoCommandCustom(tag,(DataDevice)getApplication());
					
					if(!DecodeGetSystemInfoResponse(GetSystemInfoAnswer))
			    	{
			        	return;
			    	}
					else{
					new StartReadTask().execute();
					}
			}else{
				Toast.makeText(this, "未知的标签类型", Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (this.mAdapter == null)
			return;
		this.mAdapter.disableForegroundDispatch(this);
		this.mAdapter.disableForegroundNdefPush(this);
	}

	@SuppressLint("NewApi")
	private String processIntent(Intent intent) {
		Parcelable[] rawmsgs = intent
				.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		NdefMessage msg = (NdefMessage) rawmsgs[0];
		NdefRecord[] records = msg.getRecords();
		String resultStr = new String(records[0].getPayload());
		return resultStr;
	}
	// 字符序列转换为16进制字符串
	public static String toHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	private class StartReadTask extends AsyncTask<Void, Void, Void> {
//		private final ProgressDialog dialog = new ProgressDialog(
//				MainActivity.this);
//
//		// can use UI thread here
//		protected void onPreExecute() {
//			this.dialog.setMessage("Please, keep your phone close to the tag.");
//			this.dialog.show();
//		}

		@Override
		protected Void doInBackground(Void... params) {
			DataDevice dataDevice = (DataDevice) getApplication();
			fullNdefMessage = null;
			byte[] resultBlock0 = new byte[4];
			byte[] resultBlock1 = new byte[8];
			cpt = 0;

			resultBlock0 = null;
			while ((resultBlock0 == null || resultBlock0[0] == 1) && cpt < 1500) {
				resultBlock0 = NFCCommand.SendReadSingleBlockCommand(
						dataDevice.getCurrentTag(), new byte[] { 0x00, 0x00 },
						dataDevice);
				cpt++;
				Log.v("CPT ", " CPT Read Block 0 ===> " + String.valueOf(cpt));
			}

			// if CC0 = E1h & CC1 = right version
			if (resultBlock0[0] == (byte) 0x00
					&& resultBlock0[1] == (byte) 0xE1
					&& resultBlock0[2] == (byte) 0x40) {
				// NDEF TAG Format valid
				cpt = 0;
				resultBlock1 = null;

				while ((resultBlock1 == null || resultBlock1[0] == 1)
						&& cpt < 10) {
					resultBlock1 = NFCCommand.SendReadMultipleBlockCommand(
							dataDevice.getCurrentTag(),
							new byte[] { 0x00, 0x01 }, (byte) 0x02, dataDevice);
				}
				if (resultBlock1[1] == (byte) 0x03
						&& resultBlock1[6] == (byte) 0x54) // Text message
				{
					if (resultBlock1[5] < 0)
						numberOfBlockToRead = ((resultBlock1[5] + 256 + 14) / 4);
					else
						numberOfBlockToRead = ((resultBlock1[5] + 14) / 4);
				} else if (resultBlock1[1] == (byte) 0x03
						&& resultBlock1[6] == (byte) 0x55) // URL message
				{
					if (resultBlock1[1] < 0)
						numberOfBlockToRead = ((resultBlock1[2] + 256 + 12) / 4);
					else
						numberOfBlockToRead = ((resultBlock1[2] + 12) / 4);
				}
			} else {
				// Not NDEF TAG Format
				numberOfBlockToRead = 0;
			}

			bNumberOfBlock = Helper
					.ConvertIntTo2bytesHexaFormat(numberOfBlockToRead);

			cpt = 0;
			if (numberOfBlockToRead < 32) {
				while ((fullNdefMessage == null || fullNdefMessage[0] == 1)
						&& cpt < 10 && numberOfBlockToRead != 0) {
					fullNdefMessage = NFCCommand
							.SendReadMultipleBlockCommandCustom(
									dataDevice.getCurrentTag(), new byte[] {
											0x00, 0x00 }, bNumberOfBlock[1],
									dataDevice);
					cpt++;
				}
			} else {
				while ((fullNdefMessage == null || fullNdefMessage[0] == 1)
						&& cpt < 10 && numberOfBlockToRead != 0) {
					fullNdefMessage = NFCCommand
							.SendReadMultipleBlockCommandCustom2(
									dataDevice.getCurrentTag(), new byte[] {
											0x00, 0x00 }, bNumberOfBlock,
									dataDevice);
					cpt++;
					Log.i("CPT ", "***** " + String.valueOf(cpt));
				}
			}

			return null;
		}

		protected void onPostExecute(final Void unused) {
//			if (this.dialog.isShowing())
//				this.dialog.dismiss();

			if (fullNdefMessage == null) {
				Toast toast = Toast.makeText(getApplicationContext(),
						"No NDEF message", Toast.LENGTH_SHORT);
				toast.show();
				return;
			} else if (fullNdefMessage.length > 1) {
				byte[] NdefMessage = new byte[fullNdefMessage.length - 1];
				for (int i = 1; i < fullNdefMessage.length; i++) {
					NdefMessage[i - 1] = fullNdefMessage[i];
				}
				sNDEFMessage = NDEFMessages
						.ConvertNDEF_ByteArrayToString(NdefMessage);

				if (sNDEFMessage != "No Ndef Message Found") {
					// Intent i = new Intent(NDEFMenu.this, NDEFRead.class);
					// i.putExtra(NDEFRead.EXTRA_1, sNDEFMessage);
					// startActivity(i);
					info = "ID: "+ info + "\n" + "Message: " +sNDEFMessage;
//					promt.setText(sNDEFMessage);
				} else {
					Toast toast = Toast.makeText(getApplicationContext(),
							"No Tag ?", Toast.LENGTH_SHORT);
					toast.show();
				}
			} else {
				sNDEFMessage = "No NDEF message";
			}
			nfcWrite.setVisibility(View.VISIBLE);
			writeIntent = new Intent(MainActivity.this,NDEFWrite.class);
			promt.setText(info);
		}
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch(arg0.getId()){
			case R.id.NFCWriteButton:
//				Intent intent = new Intent(MainActivity.this,NDEFWrite.class);
				writeIntent.putExtra("message", sNDEFMessage);
				startActivity(writeIntent);
				break;
		}
	}
	
	public boolean DecodeInventoryResponse (byte[] InventoryResponse)
	 {
		 //if the tag has returned a god response 
		 if(InventoryResponse[0] == (byte) 0x00)
		 {
			 DataDevice ma = (DataDevice)getApplication();
			 String uidToString = "";
			 byte[] uid = new byte[8];
			 // change uid format from byteArray to a String
			 for (int i = 1; i <= 8; i++) 
			 {
				 uid[i - 1] = InventoryResponse[10 - i];
				 uidToString += Helper.ConvertHexByteToString(uid[i - 1]);				
			 }
			 
			 //***** TECHNO ******
			 ma.setUid(uidToString);
			 if(uid[0] == (byte) 0xE0)
				 ma.setTechno("ISO 15693");
			 else if (uid[0] == (byte) 0xE0)
				 ma.setTechno("ISO 14443");
			 else
				 ma.setTechno("unknow");
			 
			 //***** MANUFACTURER ****
			 if(uid[1]== (byte) 0x02)
				 ma.setManufacturer("STMicroelectronics");
			 else if(uid[1]== (byte) 0x04)
				 ma.setManufacturer("NXP");
			 else if(uid[1]== (byte) 0x07)
				 ma.setManufacturer("Texas Instrument");
			 else
				 ma.setManufacturer("unknow");
			 
			 //**** PRODUCT NAME *****
			 if(uid[2] >= (byte) 0x20 && uid[2] <= (byte) 0x23)
				 ma.setProductName("LRI2K");
			 else if(uid[2] >= (byte) 0x28 && uid[2] <= (byte) 0x2B)
				 ma.setProductName("LRIS2K");
			 else if(uid[2] >= (byte) 0x2C && uid[2] <= (byte) 0x2F)
				 ma.setProductName("M24LR64-R");
			 else if(uid[2] >= (byte) 0x40 && uid[2] <= (byte) 0x43)
				 ma.setProductName("LRI1K");
			 else if(uid[2] >= (byte) 0x44 && uid[2] <= (byte) 0x47)
				 ma.setProductName("LRIS64K");
			 else if(uid[2] == (byte) 0x4C)
				 ma.setProductName(" M24LR16E-R");
			 else
				 ma.setProductName("unknow");
			 
			 return true;
		 }
		 
		//if the tag has returned an error code 
		 else
		 {
			 return false;
		 }
	 }
	 
	 
	//***********************************************************************/
	 //* the function Decode the tag answer for the GetSystemInfo command
	 //* the function fills the values (dsfid / afi / memory size / icRef /..) 
	 //* in the myApplication class. return true if everything is ok.
	 //***********************************************************************/
	 public boolean DecodeGetSystemInfoResponse (byte[] GetSystemInfoResponse)
	 {
		 //if the tag has returned a god response 
		 if(GetSystemInfoResponse[0] == (byte) 0x00 && GetSystemInfoResponse.length >= 12)
		 { 
			 DataDevice ma = (DataDevice)getApplication();
			 String uidToString = "";
			 byte[] uid = new byte[8];
			 // change uid format from byteArray to a String
			 for (int i = 1; i <= 8; i++) 
			 {
				 uid[i - 1] = GetSystemInfoResponse[10 - i];
				 uidToString += Helper.ConvertHexByteToString(uid[i - 1]);
			 }
			 
			 //***** TECHNO ******
			 ma.setUid(uidToString);
			 if(uid[0] == (byte) 0xE0)
				 ma.setTechno("ISO 15693");
			 else if (uid[0] == (byte) 0xE0)
				 ma.setTechno("ISO 14443");
			 else
				 ma.setTechno("unknow");
			 
			 //***** MANUFACTURER ****
			 if(uid[1]== (byte) 0x02)
				 ma.setManufacturer("STMicroelectronics");
			 else if(uid[1]== (byte) 0x04)
				 ma.setManufacturer("NXP");
			 else if(uid[1]== (byte) 0x07)
				 ma.setManufacturer("Texas Instrument");
			 else
				 ma.setManufacturer("unknow");
			 
			 //**** PRODUCT NAME *****
			 if(uid[2] >= (byte) 0x20 && uid[2] <= (byte) 0x23)
				 ma.setProductName("LRI2K");
			 else if(uid[2] >= (byte) 0x28 && uid[2] <= (byte) 0x2B)
				 ma.setProductName("LRIS2K");
			 else if(uid[2] >= (byte) 0x2C && uid[2] <= (byte) 0x2F)
				 ma.setProductName("M24LR64-R");
			 else if(uid[2] >= (byte) 0x40 && uid[2] <= (byte) 0x43)
				 ma.setProductName("LRI1K");
			 else if(uid[2] >= (byte) 0x44 && uid[2] <= (byte) 0x47)
				 ma.setProductName("LRIS64K");
			 else if(uid[2] == (byte) 0x4C)
				 ma.setProductName("M24LR16E-R");
			 else
				 ma.setProductName("unknow");
			 
			 //*** DSFID ***
			 ma.setDsfid(Helper.ConvertHexByteToString(GetSystemInfoResponse[10]));
			 
			//*** AFI ***
			 ma.setAfi(Helper.ConvertHexByteToString(GetSystemInfoResponse[11]));
			 
			//*** MEMORY SIZE ***
			 if(ma.isBasedOnTwoBytesAddress())
			 {
				 String temp = new String();
				 temp += Helper.ConvertHexByteToString(GetSystemInfoResponse[13]);
				 temp += Helper.ConvertHexByteToString(GetSystemInfoResponse[12]);
				 ma.setMemorySize(temp);
			 }
			 else 
				 ma.setMemorySize(Helper.ConvertHexByteToString(GetSystemInfoResponse[12]));
			 
			//*** BLOCK SIZE ***
			 if(ma.isBasedOnTwoBytesAddress())
				 ma.setBlockSize(Helper.ConvertHexByteToString(GetSystemInfoResponse[14]));
			 else
				 ma.setBlockSize(Helper.ConvertHexByteToString(GetSystemInfoResponse[13]));

			//*** IC REFERENCE ***
			 if(ma.isBasedOnTwoBytesAddress())
			 {
				 ma.setIcReference(Helper.ConvertHexByteToString(GetSystemInfoResponse[15]));
				 if(GetSystemInfoResponse[15] == (byte)0x5A)
					 ma.setProductName("M24LR04E-R");
			 }
				 
			 else
			 {
				 ma.setIcReference(Helper.ConvertHexByteToString(GetSystemInfoResponse[14]));
				 if(GetSystemInfoResponse[14] == (byte)0x5A)
					 ma.setProductName("M24LR04E-R");
			 }
				 
			 return true;
		 }
		 
		//if the tag has returned an error code 
		 else
			 return false;
	 }
}
