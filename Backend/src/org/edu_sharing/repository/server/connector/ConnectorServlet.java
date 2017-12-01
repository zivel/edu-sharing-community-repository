package org.edu_sharing.repository.server.connector;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.repository.client.tools.UrlTool;
import org.edu_sharing.repository.server.AuthenticationToolAPI;
import org.edu_sharing.repository.server.MCAlfrescoBaseClient;
import org.edu_sharing.repository.server.RepoFactory;
import org.edu_sharing.repository.server.tools.ApplicationInfo;
import org.edu_sharing.repository.server.tools.ApplicationInfoList;
import org.edu_sharing.repository.server.tools.security.Encryption;
import org.edu_sharing.service.Constants;
import org.edu_sharing.service.InsufficientPermissionException;
import org.edu_sharing.service.authentication.oauth2.TokenService;
import org.edu_sharing.service.authentication.oauth2.TokenService.Token;
import org.edu_sharing.service.connector.Connector;
import org.edu_sharing.service.connector.ConnectorFileType;
import org.edu_sharing.service.connector.ConnectorService;
import org.edu_sharing.service.connector.ConnectorServiceFactory;
import org.edu_sharing.service.editlock.EditLockService;
import org.edu_sharing.service.editlock.EditLockServiceFactory;
import org.edu_sharing.service.editlock.LockedException;
import org.edu_sharing.service.mime.MimeTypesV2;
import org.edu_sharing.service.nodeservice.NodeService;
import org.edu_sharing.service.nodeservice.NodeServiceFactory;
import org.edu_sharing.service.toolpermission.ToolPermissionServiceFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;


public class ConnectorServlet extends HttpServlet  {

	Logger logger = Logger.getLogger(ConnectorServlet.class);
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String connectorId = req.getParameter("connectorId");
		String nodeId = req.getParameter("nodeId");
		
		
		HashMap<String,String> auth = new AuthenticationToolAPI().validateAuthentication(req.getSession());
		
		if(auth == null){
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		
		
		
		if(!ToolPermissionServiceFactory.getInstance().hasToolPermissionForConnector(connectorId)){
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		
		ApplicationInfo homeRepo = ApplicationInfoList.getHomeRepository();
		
		boolean readOnly=true;
		try{
			MCAlfrescoBaseClient repoClient = null;
			repoClient = (MCAlfrescoBaseClient)RepoFactory.getInstance(homeRepo.getAppId(), req.getSession());
			readOnly=!repoClient.hasPermissions(nodeId, new String[]{CCConstants.PERMISSION_WRITE});
			if(!repoClient.hasPermissions(nodeId, new String[]{CCConstants.PERMISSION_READ})){
				resp.sendError(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
			
			
		}catch(Throwable e){
			logger.error(e.getMessage(),e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,e.getMessage());
			return;
		}
		
		Connector connector = null;
		for(Connector con : ConnectorServiceFactory.getConnectorService().getConnectorList().getConnectors()){
			if(con.getId().equals(connectorId)){
				connector = con;
			}
		}
		if(connector == null){
			logger.error("no valid connector");
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"no valid connector");
			return;
		}
		
		ApplicationInfo connectorAppInfo = null;
		for(Map.Entry<String, ApplicationInfo> entry : ApplicationInfoList.getApplicationInfos().entrySet()){
			ApplicationInfo appInfo = entry.getValue();
			if(ApplicationInfo.TYPE_CONNECTOR.equals(appInfo.getType())){
				connectorAppInfo = appInfo;
			}
		}
		
		if(connectorAppInfo == null){
			logger.error("no connector appinfo registered");
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"no connector appinfo registered");
			return;
		}
		
		NodeRef nodeRef = new NodeRef(Constants.storeRef,nodeId);
		NodeService nodeService=NodeServiceFactory.getLocalService();
		HashMap<String, Object> properties=null;
		try {
			properties = nodeService.getProperties(nodeRef.getStoreRef().getProtocol(), nodeRef.getStoreRef().getIdentifier(), nodeId);
		} catch (Throwable e1) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "node id is invalid or can not be accessed");
			return;
		}
		if(connector != null && ConnectorService.ID_TINYMCE.equals(connector.getId())){
			try{
				EditLockService editLockService = EditLockServiceFactory.getEditLockService();
				if(!readOnly)
					editLockService.lock(nodeRef);
			}catch( LockedException e){
				resp.sendError(HttpServletResponse.SC_FORBIDDEN, "node is locked by another user");
				return;
			}catch( InsufficientPermissionException e){
				resp.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
				return;
			}
		}
		
		try{
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("node",nodeId);
			jsonObject.put("endpoint",connector.getUrl());
			jsonObject.put("tool", connector.getId());
			String mimetype = MimeTypesV2.getMimeType(properties);
			jsonObject.put("mimetype",mimetype);
			for(ConnectorFileType filetype : connector.getFiletypes()){
				if(filetype.getMimetype().equals(mimetype))
					jsonObject.put("filetype", filetype.getFiletype());
			}
			jsonObject.put("ts", System.currentTimeMillis() / 1000);
			jsonObject.put("sessionId", req.getSession().getId());
			jsonObject.put("ticket", req.getSession().getAttribute(CCConstants.AUTH_TICKET));
			jsonObject.put("api_url",homeRepo.getWebServerUrl() + "/" + homeRepo.getWebappname() + "/rest");
			
			if(req.getSession().getAttribute(CCConstants.AUTH_SCOPE)==null){
				ApplicationContext eduApplicationContext = org.edu_sharing.spring.ApplicationContextFactory.getApplicationContext();
				TokenService tokenService = (TokenService) eduApplicationContext.getBean("oauthTokenService");
				Token token=tokenService.createToken(AuthenticationUtil.getFullyAuthenticatedUser(),(String)req.getSession().getAttribute(CCConstants.AUTH_TICKET));
				jsonObject.put("accessToken", token.getAccessToken());
				jsonObject.put("refreshToken", token.getRefreshToken());
				jsonObject.put("expiresIn", tokenService.getExpiresIn());
			}
			
			logger.debug("jsonObject:" + jsonObject);
				
			
			/**
			 * encrypt the values with AES to prevent the length limit of 245 bytes with RSA
			 */
			KeyGenerator keygen = KeyGenerator.getInstance("AES");
			//maybe use 256:
			//http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html
			keygen.init(128);
			SecretKey aesKey = keygen.generateKey();
			Encryption eAES = new Encryption("AES");
			byte[] encrypted = eAES.encrypt(jsonObject.toString(), aesKey);
			String url = UrlTool.setParam(connectorAppInfo.getContentUrl(), "e", URLEncoder.encode(Base64.encodeBase64String(encrypted)));
			
			/**
			 * encrypt the AES key with RSA public key
			 */
			Encryption eRSA = new Encryption("RSA");
			byte[] aesKeyEncrypted = eRSA.encrypt(aesKey.getEncoded(), eRSA.getPemPublicKey(connectorAppInfo.getPublicKey()));
			url = UrlTool.setParam(url, "k", URLEncoder.encode(Base64.encodeBase64String(aesKeyEncrypted)));
			logger.info("url:" + url + "  length:" + url.length());
			resp.sendRedirect(url);
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,e.getMessage());
			return;
		}
		
		
		
	}
}
