package org.edu_sharing.restservices.iam.v1.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;;

import org.edu_sharing.restservices.shared.Group;

import com.fasterxml.jackson.annotation.JsonProperty;


@Schema(description = "")
public class GroupEntry  {
  
  private Group group = null;

  
  /**
   **/
  @Schema(required = true, description = "")
  @JsonProperty("group")
  public Group getGroup() {
    return group;
  }
  public void setGroup(Group group) {
    this.group = group;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class GroupEntry {\n");
    
    sb.append("  group: ").append(group).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
