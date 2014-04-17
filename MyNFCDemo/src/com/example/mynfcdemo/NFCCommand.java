// THE PRESENT FIRMWARE WHICH IS FOR GUIDANCE ONLY AIMS AT PROVIDING CUSTOMERS 
// WITH CODING INFORMATION REGARDING THEIR PRODUCTS IN ORDER FOR THEM TO SAVE 
// TIME. AS A RESULT, STMICROELECTRONICS SHALL NOT BE HELD LIABLE FOR ANY 
// DIRECT, INDIRECT OR CONSEQUENTIAL DAMAGES WITH RESPECT TO ANY CLAIMS 
// ARISING FROM THE CONTENT OF SUCH FIRMWARE AND/OR THE USE MADE BY CUSTOMERS 
// OF THE CODING INFORMATION CONTAINED HEREIN IN CONNECTION WITH THEIR PRODUCTS.

package com.example.mynfcdemo;

import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.util.Log;


public class NFCCommand {

	 //***********************************************************************/
	 //* the function send an Inventory command (0x26 0x01 0x00) 
	 //* the argument myTag is the intent triggered with the TAG_DISCOVERED
	 //***********************************************************************/
	 public static byte[] SendInventoryCommand (Tag myTag)
	 {
		 byte[] UIDFrame = new byte[] { (byte) 0x26, (byte) 0x01, (byte) 0x00 };
		 byte[] response = new byte[] { (byte) 0x01 };
		 
		 int errorOccured = 1;
		 while(errorOccured != 0)
		 {
			 try 
			 {
				 NfcV nfcvTag = NfcV.get(myTag);
				 nfcvTag.close();
				 nfcvTag.connect();
				 response = nfcvTag.transceive(UIDFrame);
				 nfcvTag.close();
				 if (response[0] == (byte) 0x00) 
				 {
					 Log.i("NFCCOmmand", "SENDED Frame : " + Helper.ConvertHexByteToString((byte) 0x26) + " " + Helper.ConvertHexByteToString((byte) 0x01) + " " + Helper.ConvertHexByteToString((byte) 0x00) );
					 errorOccured = 0;				
				 }
			 }
			 catch (Exception e) 
			 {
				 errorOccured ++;
				 Log.i("Polling**ERROR***", "SendInventoryCommand" + Integer.toString(errorOccured));
				 if(errorOccured >= 2)
				 {
					 Log.i("Exception","Inventory Exception " + e.getMessage());
					 return response;	
				 }
			}
		 }
		 Log.i("NFCCOmmand", "Response " + Helper.ConvertHexByteToString((byte)response[0]));
		 return response;
	 }
	
	 
	//***********************************************************************/
	 //* the function send an Get System Info command (0x02 0x2B) 
	 //* the argument myTag is the intent triggered with the TAG_DISCOVERED
	 //***********************************************************************/
	 public static byte[] SendGetSystemInfoCommandCustom (Tag myTag, DataDevice ma)
	 {
		
		 byte[] response = new byte[] { (byte) 0x01 };
		 byte[] GetSystemInfoFrame = new byte[2]; 
		 
		 // to know if tag's addresses are coded on 1 or 2 byte we consider 2  
		 // then we wait the response if it's not good we trying with 1
		 ma.setBasedOnTwoBytesAddress(true);	 
		 
		 GetSystemInfoFrame = new byte[] { (byte) 0x0A, (byte) 0x2B };
		 
		 for(int h=0; h<=1;h++)
		 {
			 try 
			 {
				 NfcV nfcvTag = NfcV.get(myTag);
				 nfcvTag.close();
				 nfcvTag.connect();
				 response = nfcvTag.transceive(GetSystemInfoFrame);
				 nfcvTag.close();
				 if (response[0] == (byte) 0x00) 
				 {
					 Log.i("NFCCOmmand", "SENDED Frame : " + Helper.ConvertHexByteArrayToString(GetSystemInfoFrame));
					 h = 2;// to get out of the loop
				 }
			 }
			 catch (Exception e) 
			 {
				Log.i("Exception","Get System Info Exception " + e.getMessage());
				ma.setBasedOnTwoBytesAddress(false);
			}

			 Log.i("NFCCOmmand", "Response Get System Info " + Helper.ConvertHexByteArrayToString(response));
			 GetSystemInfoFrame = new byte[] { (byte) 0x02, (byte) 0x2B };
		 }
		 return response;
	 }
	 
	 
	 
	//***********************************************************************/
	 //* the function send an ReadSingle command (0x0A 0x20) || (0x02 0x20) 
	 //* the argument myTag is the intent triggered with the TAG_DISCOVERED
	 //* example : StartAddress {0x00, 0x02}  NbOfBlockToRead : {0x04}
	 //* the function will return 04 blocks read from address 0002
	 //* According to the ISO-15693 maximum block read is 32 for the same sector
	 //***********************************************************************/
	 public static byte[] SendReadSingleBlockCommand (Tag myTag, byte[] StartAddress,  DataDevice ma)
	 {
		 byte[] response = new byte[] {(byte) 0x01}; 
		 byte[] ReadSingleBlockFrame;
		 
		 if(ma.isBasedOnTwoBytesAddress())
			 ReadSingleBlockFrame = new byte[]{(byte) 0x0A, (byte) 0x20, StartAddress[1], StartAddress[0]};
		 else
			 ReadSingleBlockFrame = new byte[]{(byte) 0x02, (byte) 0x20, StartAddress[1]};

		 int errorOccured = 1;
		 while(errorOccured != 0)
		 {
			 try
			 {
				 NfcV nfcvTag = NfcV.get(myTag);
				 nfcvTag.close();
				 nfcvTag.connect();
				 response = nfcvTag.transceive(ReadSingleBlockFrame);
				 if(response[0] == (byte) 0x00)
				 {
					 errorOccured = 0;
					 Log.i("NFCCOmmand", "SENDED Frame : " + Helper.ConvertHexByteArrayToString(ReadSingleBlockFrame));
				 }
			 }
			 catch(Exception e)
			 {
				 errorOccured++;
				 Log.i("NFCCOmmand", "SendReadSingleBlockCommand errorOccured here " + errorOccured);
				 if(errorOccured == 2)
				 {
					 Log.i("Exception","Exception " + e.getMessage());
					 return response;
				 }
			 }
		 }
		 Log.i("NFCCOmmand", "Response Read Sigle Block" + Helper.ConvertHexByteArrayToString(response));
		 return response;
	 }
	 
	 
	 //***********************************************************************/
	 //* the function send an ReadSingle Custom command (0x0A 0x20) || (0x02 0x20) 
	 //* the argument myTag is the intent triggered with the TAG_DISCOVERED
	 //* example : StartAddress {0x00, 0x02}  NbOfBlockToRead : {0x04}
	 //* the function will return 04 blocks read from address 0002
	 //* According to the ISO-15693 maximum block read is 32 for the same sector
	 //***********************************************************************/
	 public static byte[] SendReadMultipleBlockCommandCustom (Tag myTag, byte[] StartAddress, byte NbOfBlockToRead,   DataDevice ma)
	 {
		long cpt =0;
		 int NbBytesToRead = (NbOfBlockToRead*4)+1;
		byte[] FinalResponse = new byte[NbBytesToRead];
			 
		for(int i =0;i<=(NbOfBlockToRead*4)-4; i = i+4)
		{
			byte[] temp = new byte[5];
			int incrementAddressStart0 = (StartAddress[0]+i/256)  ;								//Most Important Byte
			int incrementAddressStart1 = (StartAddress[1]+i/4) - (incrementAddressStart0*255);	//Less Important Byte
			
			temp = null;
			while (temp == null || temp[0] == 1 && cpt <= 2)
			{
				temp = SendReadSingleBlockCommand (myTag, new byte[]{(byte)incrementAddressStart0,(byte)incrementAddressStart1},ma);
				cpt ++;
			}
			cpt =0;
			
			if(i==0)
			{
				for(int j=0;j<=4;j++)
				{
					FinalResponse[j] = temp[j];
				}
			}
			else 
			{
				for(int j=1;j<=4;j++)
				{
					FinalResponse[i+j] = temp[j];
				}
			}
		}
		 return FinalResponse;
	 }
	 
	
	//***********************************************************************/
	 //* the function send an ReadSingle Custom command (0x0A 0x20) || (0x02 0x20) 
	 //* the argument myTag is the intent triggered with the TAG_DISCOVERED
	 //* example : StartAddress {0x00, 0x02}  NbOfBlockToRead : {0x04}
	 //* the function will return 04 blocks read from address 0002
	 //* According to the ISO-15693 maximum block read is 32 for the same sector
	 //***********************************************************************/
	
	 public static byte[] SendReadMultipleBlockCommandCustom2 (Tag myTag, byte[] StartAddress, byte[] bNbOfBlockToRead,   DataDevice ma)
	 {
		 
		 int iNbOfBlockToRead = Helper.Convert2bytesHexaFormatToInt(bNbOfBlockToRead);
		 int iNumberOfSectorToRead;
		 int iStartAddress = Helper.Convert2bytesHexaFormatToInt(StartAddress);
		 int iAddressStartRead = (iStartAddress/32)*32;
		 if(iNbOfBlockToRead%32 == 0)
		 {
			 iNumberOfSectorToRead = (iNbOfBlockToRead/32);
		 }
		 else
		 {
			 iNumberOfSectorToRead = (iNbOfBlockToRead/32)+1;
		 }
		 byte[] bAddressStartRead = Helper.ConvertIntTo2bytesHexaFormat(iAddressStartRead);
		 
		 byte[] AllReadDatas = new byte[((iNumberOfSectorToRead*128)+1)];
		 byte[] FinalResponse = new byte[(iNbOfBlockToRead*4)+1] ;

		 String sMemorySize = ma.getMemorySize();
		 sMemorySize = Helper.StringForceDigit(sMemorySize,4);
		 byte[] bLastMemoryAddress = Helper.ConvertStringToHexBytes(sMemorySize);
		 
		 //Loop needed for number of sector o read
		 for(int i=0; i<iNumberOfSectorToRead;i++)
		 {
			 byte[] temp = new byte[33]; 
			 
			 int incrementAddressStart0 = (bAddressStartRead[0]+i/8)  ;									//Most Important Byte
			 int incrementAddressStart1 = (bAddressStartRead[1]+i*32) - (incrementAddressStart0*256);	//Less Important Byte
			 
			 
			 if(bAddressStartRead[0]<0)
			 	 incrementAddressStart0 = ((bAddressStartRead[0]+256)+i/8);	
			 
			 if(bAddressStartRead[1]<0)
				 incrementAddressStart1 = ((bAddressStartRead[1]+256)+i*32) - (incrementAddressStart0*256);
			
			 
			 if(incrementAddressStart1 > bLastMemoryAddress[1] && incrementAddressStart0 > bLastMemoryAddress[0])
			 {
				 
			 
			 }
			 else
			 {
				temp = null;	
				temp = SendReadMultipleBlockCommand (myTag, new byte[]{(byte)incrementAddressStart0,(byte)incrementAddressStart1},(byte)0x1F,ma);
	
				// if any error occurs during 
				if(temp[0] == (byte)0x01)
				{
					return temp;
				}
				else
				{
					// to construct a response with first byte = 0x00
					if(i==0)
					{
						for(int j=0;j<=128;j++)
						{
							AllReadDatas[j] = temp[j];
						}
					}
					else 
					{
						for(int j=1;j<=128;j++)
						{
							AllReadDatas[(i*128)+j] = temp[j];
						}
					}
				}
			 }
		 }
		 
		 int iNbBlockToCopyInFinalReponse = Helper.Convert2bytesHexaFormatToInt(bNbOfBlockToRead);		 
		 int iNumberOfBlockToIgnoreInAllReadData = 4*(Helper.Convert2bytesHexaFormatToInt(StartAddress)%32);
		 
		 for(int h=1; h <= iNbBlockToCopyInFinalReponse*4 ; h++)
		 {
			 FinalResponse[h] = AllReadDatas[h + iNumberOfBlockToIgnoreInAllReadData];
		 }
		 
		 return FinalResponse;
	 }
	 
	 
	 
	 
	//***********************************************************************/
	 //* the function send an ReadMultiple command (0x0A 0x23) || (0x02 0x23) 
	 //* the argument myTag is the intent triggered with the TAG_DISCOVERED
	 //* example : StartAddress {0x00, 0x02}  NbOfBlockToRead : {0x04}
	 //* the function will return 04 blocks read from address 0002
	 //* According to the ISO-15693 maximum block read is 32 for the same sector
	 //***********************************************************************/
	 public static byte[] SendReadMultipleBlockCommand (Tag myTag, byte[] StartAddress, byte NbOfBlockToRead,  DataDevice ma)
	 {
		 byte[] response = new byte[] {(byte) 0x01}; 
		 byte[] ReadMultipleBlockFrame;
		 
		 if(ma.isBasedOnTwoBytesAddress())
			 ReadMultipleBlockFrame = new byte[]{(byte) 0x0A, (byte) 0x23, StartAddress[1], StartAddress[0], NbOfBlockToRead};
		 else
			 ReadMultipleBlockFrame = new byte[]{(byte) 0x02, (byte) 0x23, StartAddress[1], NbOfBlockToRead};

		 Log.i("NFCCOmmand", "SENDED Frame : " + Helper.ConvertHexByteArrayToString(ReadMultipleBlockFrame));
		 
		 int errorOccured = 1;
		 while(errorOccured != 0)
		 {
			 try
			 {
				 NfcV nfcvTag = NfcV.get(myTag);
				 nfcvTag.close();
				 nfcvTag.connect();
				 response = nfcvTag.transceive(ReadMultipleBlockFrame);
				 if(response[0] == (byte) 0x00)
				 {
					 errorOccured = 0;
					 Log.i("NFCCOmmand", "SENDED Frame : " + Helper.ConvertHexByteArrayToString(ReadMultipleBlockFrame));

				 }
			 }
			 catch(Exception e)
			 {
				 errorOccured++;
				 Log.i("NFCCOmmand", "SendReadMultipleBlockCommand errorOccured " + errorOccured);
				 if(errorOccured == 3)
				 {
					 Log.i("Exception","Exception " + e.getMessage());
					 Log.i("NFCCOmmand", "Error when try to read from address  " +  (byte)StartAddress[0] +" "+ (byte)StartAddress[1]);
					 return response;
				 }
			 }
		 }
		 Log.i("NFCCOmmand", "Response Read Multiple Block" + Helper.ConvertHexByteArrayToString(response));	
		 return response;
	 }
	 
	 

		//***********************************************************************/
		 //* the function send an WriteSingle command (0x0A 0x21) || (0x02 0x21) 
		 //* the argument myTag is the intent triggered with the TAG_DISCOVERED
		 //* example : StartAddress {0x00, 0x02}  DataToWrite : {0x04 0x14 0xFF 0xB2}
		 //* the function will write {0x04 0x14 0xFF 0xB2} at the address 0002
		 //***********************************************************************/
		 public static byte[] SendWriteSingleBlockCommand (Tag myTag, byte[] StartAddress, byte[] DataToWrite, DataDevice ma)
		 {
			 byte[] response = new byte[] {(byte) 0x01}; 
			 byte[] WriteSingleBlockFrame;
			 
			 if(ma.isBasedOnTwoBytesAddress())
				 WriteSingleBlockFrame = new byte[]{(byte) 0x0A, (byte) 0x21, StartAddress[1], StartAddress[0], DataToWrite[0], DataToWrite[1], DataToWrite[2], DataToWrite[3]};
			 else
				 WriteSingleBlockFrame = new byte[]{(byte) 0x02, (byte) 0x21, StartAddress[1], DataToWrite[0], DataToWrite[1], DataToWrite[2], DataToWrite[3]};
			 
			 int errorOccured = 1;
			 while(errorOccured != 0)
			 {
				 try
				 {
					 NfcV nfcvTag = NfcV.get(myTag);
					 nfcvTag.close();
					 nfcvTag.connect();
					 response = nfcvTag.transceive(WriteSingleBlockFrame);
					 if(response[0] == (byte) 0x00)
					 {
						 errorOccured = 0;
						 
						 Log.i("*******", "**SUCCESS** Write Data " + DataToWrite[0] +" "+ DataToWrite[1] +" "+ DataToWrite[2] +" "+ DataToWrite[3] + " at address " +  (byte)StartAddress[0] +" "+ (byte)StartAddress[1]);
					 }
				 }
				 catch(Exception e)
				 {
					 errorOccured++;
					 Log.i("NFCCOmmand", "READ SINGLE BLOCK error " + errorOccured);
					 if(errorOccured == 2)
					 {
						 Log.i("Exception","Exception " + e.getMessage());
						 Log.i("WRITE", "**ERROR WRITE SINGLE** at address " +  Helper.ConvertHexByteArrayToString(StartAddress));
						 return response;
					 }
				 }
			 }
			 return response;
		 }
		 
		 
		//***********************************************************************/
		//* the function send an Write command (0x0A 0x21) || (0x02 0x21) 
		 //* the argument myTag is the intent triggered with the TAG_DISCOVERED
		 //* example : StartAddress {0x00, 0x02}  DataToWrite : {0x04 0x14 0xFF 0xB2}
		 //* the function will write {0x04 0x14 0xFF 0xB2} at the address 0002
		 //***********************************************************************/
		 public static byte[] SendWriteMultipleBlockCommand (Tag myTag, byte[] StartAddress, byte[] DataToWrite, DataDevice ma)
		 {
			 byte[] response = new byte[] {(byte) 0x01}; 
			 long cpt = 0;
			 int NBByteToWrite = DataToWrite.length;
			 while (NBByteToWrite % 4 !=0)
				 NBByteToWrite ++;
			 
			 byte[] fullByteArrayToWrite = new byte[NBByteToWrite];
			 for(int j=0;j<NBByteToWrite;j++)
			 {
				 if(j<DataToWrite.length)
				 {
					 fullByteArrayToWrite[j]=DataToWrite[j];
				 }
				 else
				 {
					 fullByteArrayToWrite[j] = (byte)0x00;
				 }
			 }
				 
			for(int i =0;i<NBByteToWrite; i = i+4)
			{
				
				int incrementAddressStart0 = (StartAddress[0]+i/256)  ;								//Most Important Byte
				int incrementAddressStart1 = (StartAddress[1]+i/4) - (incrementAddressStart0*255);	//Less Important Byte
				response[0] = (byte)0x01;
				
				while((response[0] == (byte)0x01) && cpt <= 2)
				{
					response = SendWriteSingleBlockCommand(myTag,new byte[]{(byte)incrementAddressStart0,(byte)incrementAddressStart1},new byte[]{(byte)fullByteArrayToWrite[i],(byte)fullByteArrayToWrite[i+1],(byte)fullByteArrayToWrite[i+2],(byte)fullByteArrayToWrite[i+3]},ma);
					cpt ++;
				}
				if (response[0] == (byte)0x01)
					return response;
				cpt = 0;
			}
			 return response;
		 }
	 
}
