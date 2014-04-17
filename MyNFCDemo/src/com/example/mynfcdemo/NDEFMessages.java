// THE PRESENT FIRMWARE WHICH IS FOR GUIDANCE ONLY AIMS AT PROVIDING CUSTOMERS 
// WITH CODING INFORMATION REGARDING THEIR PRODUCTS IN ORDER FOR THEM TO SAVE 
// TIME. AS A RESULT, STMICROELECTRONICS SHALL NOT BE HELD LIABLE FOR ANY 
// DIRECT, INDIRECT OR CONSEQUENTIAL DAMAGES WITH RESPECT TO ANY CLAIMS 
// ARISING FROM THE CONTENT OF SUCH FIRMWARE AND/OR THE USE MADE BY CUSTOMERS 
// OF THE CODING INFORMATION CONTAINED HEREIN IN CONNECTION WITH THEIR PRODUCTS.


package com.example.mynfcdemo;

public class NDEFMessages {
	
	
	//***********************************************************************/
	 //* The function Convert a String to a Byte Array NDEF message.
	 //* The Byte Array NDEF message contains both [CC field and TLV field]
	 //* from magic number -> 0xE1 to Terminator TLV -> 0xFE
	 //* Example : "salut" -> { 0XE1 ...................... 0XFE }
	 //***********************************************************************/
	public static byte[] ConvertStringToNDEF_Text_ByteArray(String stringToConvert,DataDevice ma)
	{
		byte[] ConvertedString = new byte[stringToConvert.length()+14];
		char[]CharArray = stringToConvert.toCharArray();
		int LenghtOfV = CharArray.length + 7;
		int PayloadLenght = CharArray.length + 3;
		if (PayloadLenght>=255)
			PayloadLenght = 255;
		int maxMessageLenght = (Helper.ConvertStringToInt((ma.getMemorySize().replace(" ", ""))))*4; 
		byte CC2 = (byte)0x00;
		int blockSize = Helper.ConvertStringToHexByte(ma.getBlockSize());
		int memorySize = 0;
		
		if(ma.getMemorySize().length()>3)
			memorySize = Helper.ConvertStringToInt(ma.getMemorySize().replace(" ", ""));
		else	
			memorySize = Helper.ConvertStringToHexByte(ma.getMemorySize());
		
		CC2 = (byte) (((memorySize+1) * (blockSize+1))/8);
		
		if(CC2 >= 255 || CC2 <= 0)
			CC2 = (byte)0xFF;
		
		if(stringToConvert.length() <= maxMessageLenght)
		{	
			// ------------ CC --------------
			ConvertedString[0] = (byte)0xE1; //CC0
			ConvertedString[1] = (byte)0x40; //CC1
			ConvertedString[2] = CC2; 	 	 //CC2
			ConvertedString[3] = (byte)0x00; //CC3
			
			// ------------- T --------------
			ConvertedString[4] = (byte)0x03; // T
			
			// ------------- L --------------
			ConvertedString[5] = (byte)LenghtOfV;
			
			// ------------- V --------------
			ConvertedString[6] = (byte)0xD1; 			// Record Head defined in the table 14 of the documentation
			ConvertedString[7] = (byte)0x01; 			// Type Lenght 
			ConvertedString[8] = (byte)PayloadLenght;	// Payload Lenght for text Lenght size of the text + 2 + 1
			ConvertedString[9] = (byte)0x54; 			//Type (here ascii value of 0x54 is 'T' for Text)
			ConvertedString[10] = (byte)0x02; 			// Status Byte for langguage 0x02
			ConvertedString[11] = (byte)0x65; 			// language code 'e'
			ConvertedString[12] = (byte)0x6E; 			// language code 'n'
			
			for(int i=0; i<stringToConvert.length();i++)
			{
				ConvertedString[13+i] = (byte)CharArray[i]; // Payload
			}
			
			ConvertedString[stringToConvert.length()+13] = (byte)0xFE;// Terminator TLV
		}
		else
		{
			ConvertedString = null;
			return ConvertedString;
		}
		
		return ConvertedString;
	}
	
	//***********************************************************************/
	 //* The function Convert a String to a Byte Array NDEF url.
	 //* The Byte Array NDEF url contains both [CC field and TLV field]
	 //* from magic number -> 0xE1 to Terminator TLV -> 0xFE
	 //* Example : "http://www.st.com" -> { 0XE1 ...................... 0XFE }
	 //***********************************************************************/
	public static byte[] ConvertStringToNDEF_Url_ByteArray(String stringToConvert,DataDevice ma)
	{
		byte[] ConvertedString = new byte[stringToConvert.length()+12];
		char[]CharArray = stringToConvert.toCharArray();
		int LenghtOfV = CharArray.length + 5;
		int PayloadLenght = CharArray.length + 1;
		int maxMessageLenght = (Helper.ConvertStringToInt((ma.getMemorySize().replace(" ", ""))))*4; 
		
		int memorySize = Helper.ConvertStringToHexByte(ma.getMemorySize());
		int blockSize = Helper.ConvertStringToHexByte(ma.getBlockSize());
		byte CC2 = (byte) (((memorySize+1) * (blockSize+1))/8);
		
		if(stringToConvert.length() <= maxMessageLenght)
		{
			if(ma.getMemorySize().length()>3)
			{
				CC2 = (byte)0xFF;
			}
			
			// ------------ CC --------------
			ConvertedString[0] = (byte)0xE1; //CC0  magic Number 0xE1 
			ConvertedString[1] = (byte)0x40; //CC1  version & read/write access conditions
			ConvertedString[2] = CC2; 		 //CC2  
			ConvertedString[3] = (byte)0x00; //CC3
			
			// ------------- T --------------
			ConvertedString[4] = (byte)0x03; // T
			
			// ------------- L --------------
			ConvertedString[5] = (byte)LenghtOfV;
			
			// ------------- V --------------
			ConvertedString[6] = (byte)0xD1; 			// Record Head defined in the table 14 of the documentation
			ConvertedString[7] = (byte)0x01; 			// Type Lenght 
			ConvertedString[8] = (byte)PayloadLenght;	// Payload Lenght for text Lenght size of the text + 1
			ConvertedString[9] = (byte)0x55; 			//Type (here ascii value of 0x55 is 'U' for URL)
			ConvertedString[10] = (byte)0x01; 			// Status Byte for langguage 0x02
			
				
			for(int i=0; i<stringToConvert.length();i++)
			{
				ConvertedString[11+i] = (byte)CharArray[i]; // Payload
			}
				
			ConvertedString[stringToConvert.length()+11] = (byte)0xFE;// Terminator TLV
		}
		
		else
		{
			return ConvertStringToNDEF_Url_ByteArray("message too long",ma);
		}
		
		return ConvertedString;
	}
	
	//***********************************************************************/
	 //* The function Convert a Byte Array NDEF Text to a String .
	 //* Example :  { 0XE1 ...................... 0XFE } -> "hello"
	 //***********************************************************************/
	public static String ConvertNDEF_ByteArrayToString(byte[] ByteArrayToConvert)
	{
		String ConvertedByteArray = "";
		if(ByteArrayToConvert.length > 9)
		{
			if(ByteArrayToConvert[9]==(byte)0x54)//TEXT
			{
				int TextMessageLenght = 0;
				if(ByteArrayToConvert[8]<0)
					TextMessageLenght = ByteArrayToConvert[8]-3 + 256;
				else
					TextMessageLenght = ByteArrayToConvert[8]-3;
				
				for(int i=0; i<TextMessageLenght;i++)
				{
					if(13+i<ByteArrayToConvert.length)
						ConvertedByteArray += (char)ByteArrayToConvert[13+i];
				}
			}
			else if (ByteArrayToConvert[9]==(byte)0x55)//URL
			{
				int TextMessageLenght = 0 ;
				
				if(ByteArrayToConvert[8]<0)
					TextMessageLenght = ByteArrayToConvert[8]-1+256;
				else
					TextMessageLenght = ByteArrayToConvert[8]-1;
				
				
				for(int i=0; i<TextMessageLenght;i++)
				{
					if(13+i<ByteArrayToConvert.length)
						ConvertedByteArray += (char)ByteArrayToConvert[11+i];
				}
				
				ConvertedByteArray = "http://www."+ConvertedByteArray;
			}
			else
			{
				ConvertedByteArray = "unknow data";
			}
		
		}
		else 
		{
			ConvertedByteArray = "No Ndef Message Found";
		}
		
		return ConvertedByteArray;

	}
}
