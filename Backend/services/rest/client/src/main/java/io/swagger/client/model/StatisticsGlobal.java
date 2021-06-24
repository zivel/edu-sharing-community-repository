/*
 * edu-sharing Repository REST API
 * The public restful API of the edu-sharing repository.
 *
 * OpenAPI spec version: 1.1
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package io.swagger.client.model;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.client.model.StatisticsGroup;
import io.swagger.client.model.StatisticsKeyGroup;
import io.swagger.client.model.StatisticsUser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * StatisticsGlobal
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-09T17:32:21.273+02:00")
public class StatisticsGlobal {
  @SerializedName("overall")
  private StatisticsGroup overall = null;

  @SerializedName("groups")
  private List<StatisticsKeyGroup> groups = null;

  @SerializedName("user")
  private StatisticsUser user = null;

  public StatisticsGlobal overall(StatisticsGroup overall) {
    this.overall = overall;
    return this;
  }

   /**
   * Get overall
   * @return overall
  **/
  @ApiModelProperty(value = "")
  public StatisticsGroup getOverall() {
    return overall;
  }

  public void setOverall(StatisticsGroup overall) {
    this.overall = overall;
  }

  public StatisticsGlobal groups(List<StatisticsKeyGroup> groups) {
    this.groups = groups;
    return this;
  }

  public StatisticsGlobal addGroupsItem(StatisticsKeyGroup groupsItem) {
    if (this.groups == null) {
      this.groups = new ArrayList<StatisticsKeyGroup>();
    }
    this.groups.add(groupsItem);
    return this;
  }

   /**
   * Get groups
   * @return groups
  **/
  @ApiModelProperty(value = "")
  public List<StatisticsKeyGroup> getGroups() {
    return groups;
  }

  public void setGroups(List<StatisticsKeyGroup> groups) {
    this.groups = groups;
  }

  public StatisticsGlobal user(StatisticsUser user) {
    this.user = user;
    return this;
  }

   /**
   * Get user
   * @return user
  **/
  @ApiModelProperty(value = "")
  public StatisticsUser getUser() {
    return user;
  }

  public void setUser(StatisticsUser user) {
    this.user = user;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StatisticsGlobal statisticsGlobal = (StatisticsGlobal) o;
    return Objects.equals(this.overall, statisticsGlobal.overall) &&
        Objects.equals(this.groups, statisticsGlobal.groups) &&
        Objects.equals(this.user, statisticsGlobal.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(overall, groups, user);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class StatisticsGlobal {\n");
    
    sb.append("    overall: ").append(toIndentedString(overall)).append("\n");
    sb.append("    groups: ").append(toIndentedString(groups)).append("\n");
    sb.append("    user: ").append(toIndentedString(user)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

