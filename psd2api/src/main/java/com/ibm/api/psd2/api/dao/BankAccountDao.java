package com.ibm.api.psd2.api.dao;

import java.util.ArrayList;

import com.ibm.api.psd2.api.beans.account.BankAccountDetailsBean;

public interface BankAccountDao
{
	public BankAccountDetailsBean getBankAccountDetails(String bankId, String accountId) throws Exception;
	public BankAccountDetailsBean getBankAccountDetails(String bankId, String accountId, String username) throws Exception;
	public void createBankAccountDetails(BankAccountDetailsBean b) throws Exception;
	public ArrayList<BankAccountDetailsBean> getBankAccounts(String username, String bank_id) throws Exception; 

}
