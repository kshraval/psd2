package com.ibm.api.psd2.api.dao;

import java.util.ArrayList;

import com.ibm.api.psd2.api.beans.BankBean;

public interface BankDao
{
	public BankBean getBankDetails(String bankId) throws Exception;
	public ArrayList<BankBean> getBanks() throws Exception;
	public void insertBank(BankBean bankBean) throws Exception;
}
