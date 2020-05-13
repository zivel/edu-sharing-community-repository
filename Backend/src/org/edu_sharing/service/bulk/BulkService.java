package org.edu_sharing.service.bulk;


import org.alfresco.service.cmr.repository.NodeRef;

import java.util.HashMap;
import java.util.List;

public interface BulkService {

    NodeRef sync(String group, List<String> match, String type, List<String> aspects, HashMap<String, String[]> properties) throws Throwable;

    NodeRef find(HashMap<String, String[]> properties) throws Exception;
}
