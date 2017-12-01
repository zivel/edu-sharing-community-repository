package org.edu_sharing.repository.server.authentication;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.repository.server.AuthenticationTool;
import org.edu_sharing.repository.server.RepoFactory;
import org.edu_sharing.repository.server.tools.ApplicationInfoList;

public class GuestFilter implements javax.servlet.Filter {

	Logger logger = Logger.getLogger(GuestFilter.class);
	
	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		
		logger.debug("starting");
		HttpServletRequest httpRequest = (HttpServletRequest) req;
		
		//prevent caching cause this leads that the index.html would be delivered without going over the search path which goes over the guest filter
		 HttpServletResponse httpresponse = (HttpServletResponse)res ;
         // Set the Cache-Control and Expires header
         httpresponse.setHeader("Cache-Control", "no-cache") ;
         httpresponse.setHeader("Expires", "0") ;
         // Print out the URL we're filtering

		try {
			AuthenticationTool authTool = RepoFactory.getAuthenticationToolInstance(ApplicationInfoList.getHomeRepository().getAppId());
			HttpSession session = httpRequest.getSession(true);
			HashMap<String, String> authentication = authTool.validateAuthentication(session);

			String guestFilterdisabled = req.getParameter(CCConstants.REQUEST_PARAM_DISABLE_GUESTFILTER);
			
			if (authentication == null && !new Boolean(guestFilterdisabled)) {
				String guestLogin = ApplicationInfoList.getHomeRepository().getGuest_username();
				String guestPW = ApplicationInfoList.getHomeRepository().getGuest_password();
				if (guestLogin != null && guestPW != null && !guestLogin.isEmpty() && !guestPW.isEmpty()) {
					logger.info("doing guest login");
					HashMap<String, String> authInfoGuest = authTool.createNewSession(guestLogin, guestPW);
					authTool.storeAuthInfoInSession(authInfoGuest.get(CCConstants.AUTH_USERNAME), authInfoGuest.get(CCConstants.AUTH_TICKET),CCConstants.AUTH_TYPE_DEFAULT, session);
				}else{
					logger.info("no guest defined");
				}
			}
		} catch (Throwable e) {
			logger.warn("guest credentials seem to be wrong");
		}

		chain.doFilter(req, res);

	}

	public void init(javax.servlet.FilterConfig arg0) throws ServletException {

	};

}
