// THE PRESENT FIRMWARE WHICH IS FOR GUIDANCE ONLY AIMS AT PROVIDING CUSTOMERS 
// WITH CODING INFORMATION REGARDING THEIR PRODUCTS IN ORDER FOR THEM TO SAVE 
// TIME. AS A RESULT, STMICROELECTRONICS SHALL NOT BE HELD LIABLE FOR ANY 
// DIRECT, INDIRECT OR CONSEQUENTIAL DAMAGES WITH RESPECT TO ANY CLAIMS 
// ARISING FROM THE CONTENT OF SUCH FIRMWARE AND/OR THE USE MADE BY CUSTOMERS 
// OF THE CODING INFORMATION CONTAINED HEREIN IN CONNECTION WITH THEIR PRODUCTS.

package com.example.mynfcdemo;

import android.app.Application;
import android.nfc.Tag;

public class DataDevice extends Application
{
	private Tag currentTag;
	private String uid;
	private String techno;
	private String manufacturer;
	private String productName;
	private String dsfid;
	private String afi;
	private String memorySize;
	private String blockSize;
	private String icReference;
	private boolean basedOnTwoBytesAddress;

	public void setCurrentTag(Tag currentTag) {
		this.currentTag = currentTag;
	}

	public Tag getCurrentTag() {
		return currentTag;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getUid() {
		return uid;
	}

	public void setTechno(String techno) {
		this.techno = techno;
	}

	public String getTechno() {
		return techno;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductName() {
		return productName;
	}

	public void setDsfid(String dsfid) {
		this.dsfid = dsfid;
	}

	public String getDsfid() {
		return dsfid;
	}

	public void setAfi(String afi) {
		this.afi = afi;
	}

	public String getAfi() {
		return afi;
	}

	public void setMemorySize(String memorySize) {
		this.memorySize = memorySize;
	}

	public String getMemorySize() {
		return memorySize;
	}

	public void setBlockSize(String blockSize) {
		this.blockSize = blockSize;
	}

	public String getBlockSize() {
		return blockSize;
	}

	public void setIcReference(String icReference) {
		this.icReference = icReference;
	}

	public String getIcReference() {
		return icReference;
	}

	public void setBasedOnTwoBytesAddress(boolean basedOnTwoBytesAddress) {
		this.basedOnTwoBytesAddress = basedOnTwoBytesAddress;
	}

	public boolean isBasedOnTwoBytesAddress() {
		return basedOnTwoBytesAddress;
	}
}
