package com.ibm.api.psd2.api.controllers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.api.psd2.api.Constants;
import com.ibm.api.psd2.api.beans.account.BankAccountDetailsBean;
import com.ibm.api.psd2.api.beans.account.BankAccountDetailsViewBean;
import com.ibm.api.psd2.api.beans.account.BankAccountOverviewBean;
import com.ibm.api.psd2.api.beans.subscription.SubscriptionInfoBean;
import com.ibm.api.psd2.api.beans.subscription.ViewIdBean;
import com.ibm.api.psd2.api.beans.transactions.TransactionBean;
import com.ibm.api.psd2.api.dao.BankAccountDao;
import com.ibm.api.psd2.api.dao.SubscriptionDao;
import com.ibm.api.psd2.api.dao.TransactionDao;
import com.ibm.api.psd2.api.response.BankAccountOverviewVisitor;
import com.ibm.api.psd2.api.response.BankAccountOwnerViewVisitor;

@RestController
public class BankAccountController extends APIController
{
	private static final Logger logger = LogManager.getLogger(BankAccountController.class);

	@Autowired
	BankAccountDao bad;

	@Autowired
	SubscriptionDao sdao;

	@Autowired
	TransactionDao tdao;

	@Value("${version}")
	private String version;

	@PreAuthorize("#oauth2.hasScope('write')")
	@RequestMapping(method = RequestMethod.GET, value = "/my/banks/{bankId}/accounts", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<List<BankAccountOverviewBean>> getBankAccounts(
			@PathVariable("bankId") String bankId, Authentication auth)
	{
		ResponseEntity<List<BankAccountOverviewBean>> response;
		try
		{
			// @Todo: Check if the user is authorized to view this account or
			// not.

			String user = (String) auth.getPrincipal();
			ViewIdBean ownerView = new ViewIdBean();
			ownerView.setId(Constants.OWNER_VIEW);

			List<SubscriptionInfoBean> lstSib = sdao.getSubscriptionInfo(user, bankId);

			if (lstSib == null)
			{
				throw new IllegalAccessException("Not Subscribed");
			}

			// Get a list of accountid from subscription info list that has
			// owner view enabled

			List<String> accountIds = new ArrayList<>();
			for (Iterator<SubscriptionInfoBean> iterator = lstSib.iterator(); iterator.hasNext();)
			{
				SubscriptionInfoBean s = iterator.next();
				if (s.getViewIds().contains(ownerView))
				{
					accountIds.add(s.getAccountId());
				}
			}

			List<BankAccountDetailsBean> ba = bad.getBankAccounts(user, bankId);

			List<BankAccountOverviewBean> bol = null;
			BankAccountOverviewVisitor bav = new BankAccountOverviewVisitor();
			for (Iterator<BankAccountDetailsBean> iterator = ba.iterator(); iterator.hasNext();)
			{

				BankAccountDetailsBean b = iterator.next();

				if (!accountIds.isEmpty() && accountIds.contains(b.getId()))
				{
					b.registerVisitor(BankAccountOverviewBean.class.getName() + ":" + Constants.OWNER_VIEW, bav);

					if (bol == null)
					{
						bol = new ArrayList<>();
					}
					bol.add(b.getBankAccountOverview(Constants.OWNER_VIEW));
				}
			}

			response = ResponseEntity.ok(bol);
		} catch (Exception ex)
		{
			logger.error(ex);
			response = ResponseEntity.badRequest().body(null);
		}
		return response;
	}

	@PreAuthorize("#oauth2.hasScope('write')")
	@RequestMapping(method = RequestMethod.GET, value = "/banks/{bankId}/accounts/{accountId}/{viewId}/account", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<BankAccountDetailsViewBean> getAccountById(
			@PathVariable("bankId") String bankId, @PathVariable("accountId") String accountId,
			@PathVariable("viewId") String viewId, Authentication auth)
	{
		ResponseEntity<BankAccountDetailsViewBean> response;
		try
		{
			String user = (String) auth.getPrincipal();
			ViewIdBean specifiedView = new ViewIdBean();
			specifiedView.setId(viewId);

			SubscriptionInfoBean sib = sdao.getSubscriptionInfo(user, accountId, bankId);
			if (sib == null || !sib.getViewIds().contains(specifiedView))
			{
				throw new IllegalAccessException("Not Subscribed");
			}

			BankAccountDetailsBean b = bad.getBankAccountDetails(bankId, accountId, user);
			BankAccountDetailsViewBean bo = null;

			if (b == null)
			{
				throw new IllegalArgumentException("Account Not Found");
			}

			BankAccountOwnerViewVisitor bv = new BankAccountOwnerViewVisitor();
			if (Constants.OWNER_VIEW.equals(viewId))
			{
				b.registerVisitor(BankAccountDetailsViewBean.class.getName() + ":" + viewId, bv);
				bo = b.getBankAccountDetails(viewId);
				response = ResponseEntity.ok(bo);
			} else
			{
				throw new IllegalArgumentException(
						"View ID is incorrect. Currently supported ones are: " + Constants.OWNER_VIEW);
			}
		} catch (Exception ex)
		{
			logger.error(ex);
			response = ResponseEntity.badRequest().body(null);
		}
		return response;
	}

	@PreAuthorize("#oauth2.hasScope('write')")
	@RequestMapping(method = RequestMethod.GET, value = "/my/banks/{bankId}/accounts/{accountId}/account", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<BankAccountDetailsViewBean> getAccountById(
			@PathVariable("bankId") String bankId, @PathVariable("accountId") String accountId, Authentication auth)
	{
		ResponseEntity<BankAccountDetailsViewBean> response;
		try
		{
			String user = (String) auth.getPrincipal();
			ViewIdBean ownerView = new ViewIdBean();
			ownerView.setId(Constants.OWNER_VIEW);

			SubscriptionInfoBean sib = sdao.getSubscriptionInfo(user, accountId, bankId);
			if (sib == null || !sib.getViewIds().contains(ownerView))
			{
				throw new IllegalAccessException("Not Subscribed");
			}

			BankAccountDetailsBean b = bad.getBankAccountDetails(bankId, accountId, user);
			BankAccountDetailsViewBean bo = null;

			if (b == null)
			{
				throw new IllegalArgumentException("Account Not Found");
			}

			BankAccountOwnerViewVisitor bv = new BankAccountOwnerViewVisitor();
			b.registerVisitor(BankAccountDetailsViewBean.class.getName() + ":" + Constants.OWNER_VIEW, bv);
			bo = b.getBankAccountDetails(Constants.OWNER_VIEW);
			response = ResponseEntity.ok(bo);

		} catch (Exception ex)
		{
			logger.error(ex);
			response = ResponseEntity.badRequest().body(null);
		}
		return response;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/admin/account", produces = MediaType.ALL_VALUE)
	public @ResponseBody ResponseEntity<String> createAccount(@RequestBody BankAccountDetailsBean b)
	{
		ResponseEntity<String> response;
		try
		{
			// @Todo: Check if the user is authorized to view this account or
			// not.

			if (b == null)
			{
				throw new IllegalArgumentException("No Account Specified");
			}

			logger.info("BankAccountDetailsBean = " + b.toString());

			bad.createBankAccountDetails(b);
			response = ResponseEntity.ok("SUCCESS");
		} catch (Exception e)
		{
			logger.error(e);
			response = ResponseEntity.badRequest().body("ERROR: " + e.getMessage());
		}
		return response;
	}

	@PreAuthorize("#oauth2.hasScope('write')")
	@RequestMapping(method = RequestMethod.GET, value = "/banks/{bankId}/accounts/{accountId}/{viewId}/transactions/{txnId}/transaction", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<TransactionBean> getTransactionById(@PathVariable("bankId") String bankId,
			@PathVariable("accountId") String accountId, @PathVariable("txnId") String txnId, Authentication auth)
	{
		ResponseEntity<TransactionBean> response;
		try
		{
			String user = (String) auth.getPrincipal();
			ViewIdBean ownerView = new ViewIdBean();
			ownerView.setId(Constants.OWNER_VIEW);

			SubscriptionInfoBean sib = sdao.getSubscriptionInfo(user, accountId, bankId);
			if (sib == null || !sib.getViewIds().contains(ownerView))
			{
				throw new IllegalAccessException("Not Subscribed");
			}

			TransactionBean t = tdao.getTransactionById(bankId, accountId, txnId);

			response = ResponseEntity.ok(t);

		} catch (Exception ex)
		{
			logger.error(ex);
			response = ResponseEntity.badRequest().body(null);
		}
		return response;
	}

	@PreAuthorize("#oauth2.hasScope('write')")
	@RequestMapping(method = RequestMethod.GET, value = "/banks/{bankId}/accounts/{accountId}/{viewId}/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<List<TransactionBean>> getTransactions(@PathVariable("bankId") String bankId,
			@PathVariable("accountId") String accountId,
			@RequestHeader(value = "obp_sort_direction", required = false) String sortDirection,
			@RequestHeader(value = "obp_limit", required = false) Integer limit,
			@RequestHeader(value = "obp_from_date", required = false) String fromDate,
			@RequestHeader(value = "obp_to_date", required = false) String toDate,
			@RequestHeader(value = "obp_sort_by", required = false) String sortBy, 
			@RequestHeader(value = "obp_offset", required=false) Integer number, Authentication auth)
	{
		ResponseEntity<List<TransactionBean>> response;
		try
		{
			String user = (String) auth.getPrincipal();
			ViewIdBean ownerView = new ViewIdBean();
			ownerView.setId(Constants.OWNER_VIEW);

			SubscriptionInfoBean sib = sdao.getSubscriptionInfo(user, accountId, bankId);
			if (sib == null || !sib.getViewIds().contains(ownerView))
			{
				throw new IllegalAccessException("Not Subscribed");
			}

			if (tdao == null)
			{
				logger.error("tdao is null");
			}
			List<TransactionBean> t = tdao.getTransactions(bankId, accountId, sortDirection, limit, fromDate, toDate, sortBy, number);

			response = ResponseEntity.ok(t);

		} catch (Exception ex)
		{
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			logger.error(sw.toString());
			response = ResponseEntity.badRequest().body(null);
		}
		return response;
	}
}
