package com.ibm.api.oauth2server.db;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ibm.api.oauth2server.beans.ClientInfo;
import com.ibm.api.oauth2server.beans.UserInfo;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import static com.mongodb.client.model.Filters.eq;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;

@Service
public class ClientDetailsServiceImpl implements ClientDetailsService
{
	private static final Logger logger = LogManager.getLogger(ClientDetailsServiceImpl.class);
	
	@Autowired
	private MongoConnection conn;

	@Autowired
	private MongoDocumentParser mdp;

	@Value("${mongodb.collection.clients}")
	private String collection;
	
	@Override
	public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException
	{
		ClientInfo clientInfo = null;

		try
		{
			clientInfo = getClientInfo(clientId);
		}
		catch (Exception e)
		{
			logger.error(e);
			throw new ClientRegistrationException(e.getMessage(), e);
		}

		BaseClientDetails bcd = new BaseClientDetails();
		bcd.setClientId(clientInfo.getClientId());
		bcd.setClientSecret(clientInfo.getClientSecret());
		bcd.setAccessTokenValiditySeconds(clientInfo.getAccessTokenValidity());
		bcd.setRefreshTokenValiditySeconds(clientInfo.getRefreshTokenValidity());
		bcd.setAuthorizedGrantTypes(StringUtils.commaDelimitedListToSet(clientInfo.getGrantTypes()));
		bcd.setScope(StringUtils.commaDelimitedListToSet(clientInfo.getScope()));
		bcd.setAuthorities(AuthorityUtils.commaSeparatedStringToAuthorityList(clientInfo.getAuthorities()));
		return bcd;
	}
	
	private ClientInfo getClientInfo(String clientId) throws Exception
	{
		MongoCollection<Document> coll = conn.getDB().getCollection(collection);
		FindIterable<Document> iterable = coll.find(eq("clientId", clientId));

		ClientInfo clientInfo= null;

		for (Document document : iterable)
		{
			if (document != null)
			{
				logger.info("message = " + document.toJson());
				clientInfo = mdp.parse(document, new ClientInfo());
			}
		}
		return clientInfo;
	}	
	
	public void createClient(ClientInfo c) throws Exception
	{
		MongoCollection<Document> coll = conn.getDB().getCollection(collection);
		coll.insertOne(mdp.format(c));
	}

}
