package org.edu_sharing.service.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.lang3.StringUtils;
import org.edu_sharing.metadataset.v2.MetadataWidget;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.repository.server.MCAlfrescoAPIClient;
import org.edu_sharing.rest.notification.event.NotificationEventDTO;
import org.edu_sharing.restservices.mds.v1.model.MdsValue;
import org.edu_sharing.service.permission.annotation.Permission;
import org.edu_sharing.service.rating.RatingDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface NotificationService {
	void notifyNodeIssue(String nodeId, String reason, Map<String, Object> properties, String userEmail, String userComment) throws Throwable;

	void notifyWorkflowChanged(String nodeId, Map<String, Object> nodeProperties, String receiver, String comment, String status);

	void notifyPersonStatusChanged(String receiver, String firstname, String lastName, String oldStatus, String newStatus);

    void notifyPermissionChanged(String senderAuthority, String receiverAuthority, String nodeId, Map<String, Object> props, String[] permissions, String[] strings, String mailText) throws Throwable;

	void notifyMetadataSetSuggestion(MdsValue mdsValue, MetadataWidget widgetDefinition, List<String> nodeIds, List<Map<String, Object>> nodeProperties) throws Throwable;

	void notifyComment(String node, String comment, String commentReference, Map<String, Object> nodeProperties, Status status);

	void notifyCollection(String collectionId, String refNodeId, Map<String, Object> collectionProperties, Map<String, Object> nodeProperties, Status status);

	void notifyRatingChanged(String nodeId, Map<String, Object> nodeProps, Double rating, RatingDetails accumulatedRatings, Status removed);

	@Permission(requiresUser = true)
	default NotificationConfig getConfig() throws Exception {
		HashMap<String, String> info = new MCAlfrescoAPIClient().getUserInfo(AuthenticationUtil.getFullyAuthenticatedUser());
		if(!StringUtils.isEmpty(info.get(CCConstants.CCM_PROP_PERSON_NOTIFICATION_PREFERENCES))){
			return new ObjectMapper().readValue(info.get(CCConstants.CCM_PROP_PERSON_NOTIFICATION_PREFERENCES), NotificationConfig.class);
		}
		return new NotificationConfig();
	}
	@Permission(requiresUser = true)
    default void setConfig(NotificationConfig config) throws Exception {
		HashMap<String, Serializable> userInfo = new HashMap<>();
		userInfo.put(CCConstants.PROP_USERNAME, AuthenticationUtil.getFullyAuthenticatedUser());
		userInfo.put(CCConstants.CCM_PROP_PERSON_NOTIFICATION_PREFERENCES, new ObjectMapper().writeValueAsString(config));
		new MCAlfrescoAPIClient().updateUser(userInfo);
	}

	@Permission(requiresUser = true)
	Page<NotificationEventDTO> getNotifications(String receiverId, List<org.edu_sharing.rest.notification.data.Status> status, Pageable pageable)throws IOException;

	@Permission(requiresUser = true)
	NotificationEventDTO setNotificationStatus(String id, org.edu_sharing.rest.notification.data.Status status)throws IOException ;

	@Permission(requiresUser = true)
	void deleteNotification(String id)throws IOException;
}
