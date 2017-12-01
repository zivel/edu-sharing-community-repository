/**
 *
 *  
 * 
 * 
 *	
 *
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 *
 */
package org.edu_sharing.repository.server.authentication;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.repository.client.tools.UrlTool;
import org.edu_sharing.repository.server.AuthenticationToolAPI;
import org.edu_sharing.repository.server.tools.ApplicationInfo;
import org.edu_sharing.repository.server.tools.ApplicationInfoList;
import org.edu_sharing.repository.server.tools.security.ShibbolethSessions;
import org.edu_sharing.repository.server.tools.security.ShibbolethSessions.SessionInfo;
import org.edu_sharing.service.authentication.EduAuthentication;
import org.edu_sharing.service.authentication.SSOAuthorityMapper;
import org.springframework.context.ApplicationContext;

public class ShibbolethServlet extends HttpServlet {
	
	Logger logger = Logger.getLogger(ShibbolethServlet.class);
	
	Boolean useHeaders = null;
	
	@Override
	public void init() throws ServletException {
		super.init();
		useHeaders = new Boolean(getServletConfig().getInitParameter("useHeaders"));
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		logger.info("req.getRemoteUser():"+req.getRemoteUser());
		
		ApplicationContext eduApplicationContext = org.edu_sharing.spring.ApplicationContextFactory.getApplicationContext();
		
		SSOAuthorityMapper ssoMapper = (SSOAuthorityMapper)eduApplicationContext.getBean("ssoAuthorityMapper");
		
		String headerUserName = getShibValue(ssoMapper.getSSOUsernameProp(), req);//transform(req.getHeader(authMethodShibboleth.getShibbolethUsername()));
		
		if (req.getRemoteUser() != null && !req.getRemoteUser().trim().isEmpty()) {
			headerUserName = req.getRemoteUser();
		}
		
		AuthenticationToolAPI authTool = new AuthenticationToolAPI();
		HashMap<String,String> validAuthInfo = authTool.validateAuthentication(req.getSession());
		
		if (validAuthInfo != null ) {
			if (validAuthInfo.get(CCConstants.AUTH_USERNAME).equals(headerUserName)) {
				
				logger.info("got valid ticket from session for user:"+headerUserName);
				redirect(resp, req);
				return;
				
			} else {
				
				logger.info("end session for user:" + validAuthInfo.get(CCConstants.AUTH_USERNAME));
				authTool.logout(validAuthInfo.get(CCConstants.AUTH_TICKET));
				
			}
		}
		
		try {
			
			logger.info("no valid authinfo found in session. doing the repository shib auth");
			
			logger.info("req.getCharacterEncoding():"+req.getCharacterEncoding());
			
			if(req.getCharacterEncoding() == null){
				req.setCharacterEncoding("UTF-8");
			}
			
			HashMap<String,String> ssoMap = new HashMap<String,String>();
			for(String ssoKey : ssoMapper.getMappingConfig().getAllSSOAttributes()){
				ssoMap.put(ssoKey, getShibValue(ssoKey,req));
			}
			
			/**
			 * overwrite user name with the remoteUser when set
			 */
			if(req.getRemoteUser() != null && !req.getRemoteUser().trim().isEmpty()) {
				
				logger.info("putting remoteuser:" + ssoMapper.getSSOUsernameProp() + " " +req.getRemoteUser());
				ssoMap.put(ssoMapper.getSSOUsernameProp(),req.getRemoteUser());
				
			}
			
			//put authType Information
			ssoMap.put(SSOAuthorityMapper.PARAM_SSO_TYPE, SSOAuthorityMapper.SSO_TYPE_Shibboleth);
			
			
			EduAuthentication authService =  (EduAuthentication)eduApplicationContext.getBean("authenticationService");
			
			authService.authenticateBySSO(SSOAuthorityMapper.SSO_TYPE_Shibboleth,ssoMap);
			String ticket = authService.getCurrentTicket();
			authTool.storeAuthInfoInSession(headerUserName, ticket,CCConstants.AUTH_TYPE_SHIBBOLETH, req.getSession());
			
			String shibbolethSessionId = getShibValue("Shib-Session-ID", req);
			
			if(shibbolethSessionId != null && !shibbolethSessionId.trim().equals("")){
				ShibbolethSessions.put(shibbolethSessionId, new SessionInfo(ticket, req.getSession()));
				req.getSession().setAttribute(CCConstants.AUTH_SSO_SESSIONID, shibbolethSessionId);
			}
			
			//federated repositoryies arixs(edunex)
			for (Map.Entry<String, ApplicationInfo> entry : ApplicationInfoList.getApplicationInfos().entrySet()) {
				ApplicationInfo appInfo = entry.getValue();
				if(ApplicationInfo.TYPE_REPOSITORY.equals(appInfo.getRepositoryType()) && ApplicationInfo.REPOSITORY_TYPE_EDUNEX.equals(appInfo.getRepositoryType()) ){
					String eduSchoolScopedIdentifierKey = "eduSchoolScopedIdentifier";
					
					Object eduSchoolScopedIdentifier = req.getAttribute(eduSchoolScopedIdentifierKey);
					if(eduSchoolScopedIdentifier == null){
						logger.info(eduSchoolScopedIdentifierKey +" is null");
						continue;
					}
					
					if(!(eduSchoolScopedIdentifier instanceof String)){
						logger.info(eduSchoolScopedIdentifierKey+ " is instance of class" + eduSchoolScopedIdentifier.getClass().getName()+" can only handle Strings");
						continue;
					}
					
					String eduSchoolScopedIdentifierStr = (String)eduSchoolScopedIdentifier;
					
					String[] splittedSemi = eduSchoolScopedIdentifierStr.split(";");
					String contextPath = null;
					for(String val : splittedSemi){
						if(val.contains("@"+appInfo.getAppId())){
							contextPath = val.split("@")[0];
						}
					}
					
					if(contextPath == null){
						logger.info("could not find context path for appId "+appInfo.getAppId()+" eduSchoolScopedIdentifierStr:"+eduSchoolScopedIdentifierStr);
						continue;
					}
					
					req.getSession().setAttribute(CCConstants.SESSION_ARIX_CONTEXT_PREFIX + appInfo.getAppId(), contextPath);
					
				}
			}
			
			redirect(resp,req);
			
		} catch(org.alfresco.repo.security.authentication.AuthenticationException e) {
			logger.error("INVALID ACCESS!",e);
			resp.getOutputStream().println("INVALID ACCESS! "+e.getMessage());
			return;
		}
	}
	
	private void redirect(HttpServletResponse resp, HttpServletRequest req) throws IOException{
		
		String redirectUrl = (String)req.getSession().getAttribute(AuthenticationFilter.LOGIN_SUCCESS_REDIRECT_URL);
		
		if (redirectUrl != null) {
			redirectUrl = resp.encodeURL(redirectUrl);
		} else {
			redirectUrl = req.getContextPath();
			Enumeration paramNames =  req.getParameterNames();
			while(paramNames.hasMoreElements()){
				String paramName = (String)paramNames.nextElement();
				String paramVal = req.getParameter(paramName);
				redirectUrl = UrlTool.setParam(redirectUrl, paramName , paramVal);
			}
		}
		
		logger.info("redirectSuccessUrl:"+redirectUrl);
				
		//so that redirecting to invited trunk works
		if(req.getParameter(CCConstants.WORKSPACE_PARAM_TRUNK) != null && req.getParameter(CCConstants.WORKSPACE_PARAM_TRUNK).equals(CCConstants.WORKSPACE_PARAM_TRUNK_VALUE_INVITED)){
			
			//remove trunk param here cause it's only needed cause of anchor is added here (server side does not get anchors)
			redirectUrl = redirectUrl.replace("&"+CCConstants.WORKSPACE_PARAM_TRUNK+"="+CCConstants.WORKSPACE_PARAM_TRUNK_VALUE_INVITED,"");
			
			if(!redirectUrl.contains(CCConstants.WORKSPACE_INVITED_ANCHOR)){
				redirectUrl += CCConstants.WORKSPACE_INVITED_ANCHOR;
			}
		}
		
		resp.sendRedirect(redirectUrl);
	}
	
	 private String getShibValue(String attName, HttpServletRequest req){
		 
	    	String attValue = null;
	    	
	    	if (useHeaders) {
	    		attValue = req.getHeader(attName);
	    	} else {
	    		attValue  = (String)req.getAttribute(attName);
	    	}
	    	
	    	if (attValue != null){ 
	    		
	    		try {
	    			
	        		// see https://wiki.shibboleth.net/confluence/display/SHIB2/NativeSPAttributeAccess#NativeSPAttributeAccess-Tool-SpecificExamples
				attValue= new String( attValue.getBytes("ISO-8859-1"), "UTF-8");
					
			} catch (UnsupportedEncodingException e) {
				logger.error(e.getMessage(), e);
			}	    		
	    	
	    		attValue = URLDecoder.decode(attValue);
	    	}
	    	
	    	System.out.println("ShibAtt:"+attName+" "+attValue);
	    	
	    	return attValue;
	}
}
