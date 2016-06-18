package com.ibm.api.psd2.api.controllers;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.api.psd2.api.beans.BankBean;
import com.ibm.api.psd2.api.dao.BankDao;

@RestController
public class BankController extends APIController
{
	private static final Logger logger = LogManager.getLogger(PaymentsController.class);

	@Autowired
	BankDao bdao;

	@PreAuthorize("#oauth2.hasScope('write')")
	@RequestMapping(method = RequestMethod.GET, value = "/banks", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<ArrayList<BankBean>> getBanks()
	{
		ResponseEntity<ArrayList<BankBean>> response;
		try
		{
			ArrayList<BankBean> b = bdao.getBanks();
			response = ResponseEntity.ok(b);
		}
		catch (Exception ex)
		{
			logger.error(ex);
			response = ResponseEntity.badRequest().body(null);
		}
		return response;
	}

	@PreAuthorize("#oauth2.hasScope('write')")
	@RequestMapping(method = RequestMethod.GET, value = "/banks/{bankId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<BankBean> getBankById(
			@PathVariable("bankId") String bankId)
	{
		ResponseEntity<BankBean> response;
		try
		{
			BankBean b = bdao.getBankDetails(bankId);
			response = ResponseEntity.ok(b);
		}
		catch (Exception ex)
		{
			logger.error(ex);
			response = ResponseEntity.badRequest().body(null);
		}
		return response;
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "admin/bank/", produces = MediaType.ALL_VALUE)
	public @ResponseBody ResponseEntity<String> createBank(@RequestBody BankBean bankBeanReq)
	{
		ResponseEntity<String> response;
		try
		{
			bdao.insertBank(bankBeanReq);
			response = ResponseEntity.ok("SUCCESS");
		}
		catch (Exception e)
		{
			logger.error(e);
			response = ResponseEntity.badRequest().body(e.getMessage());
		}
		return response;
	}

}
