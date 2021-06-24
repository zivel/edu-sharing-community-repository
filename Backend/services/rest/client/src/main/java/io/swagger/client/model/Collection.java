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
import java.io.IOException;

/**
 * Collection
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-09T17:32:21.273+02:00")
public class Collection {
  @SerializedName("scope")
  private String scope = null;

  @SerializedName("authorFreetext")
  private String authorFreetext = null;

  @SerializedName("level0")
  private Boolean level0 = false;

  @SerializedName("title")
  private String title = null;

  @SerializedName("description")
  private String description = null;

  @SerializedName("type")
  private String type = null;

  @SerializedName("viewtype")
  private String viewtype = null;

  @SerializedName("orderMode")
  private String orderMode = null;

  @SerializedName("x")
  private Integer x = null;

  @SerializedName("y")
  private Integer y = null;

  @SerializedName("z")
  private Integer z = null;

  @SerializedName("color")
  private String color = null;

  @SerializedName("fromUser")
  private Boolean fromUser = false;

  @SerializedName("pinned")
  private Boolean pinned = false;

  @SerializedName("childCollectionsCount")
  private Integer childCollectionsCount = null;

  @SerializedName("childReferencesCount")
  private Integer childReferencesCount = null;

  public Collection scope(String scope) {
    this.scope = scope;
    return this;
  }

   /**
   * Get scope
   * @return scope
  **/
  @ApiModelProperty(value = "")
  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public Collection authorFreetext(String authorFreetext) {
    this.authorFreetext = authorFreetext;
    return this;
  }

   /**
   * Get authorFreetext
   * @return authorFreetext
  **/
  @ApiModelProperty(value = "")
  public String getAuthorFreetext() {
    return authorFreetext;
  }

  public void setAuthorFreetext(String authorFreetext) {
    this.authorFreetext = authorFreetext;
  }

  public Collection level0(Boolean level0) {
    this.level0 = level0;
    return this;
  }

   /**
   * false
   * @return level0
  **/
  @ApiModelProperty(required = true, value = "false")
  public Boolean isLevel0() {
    return level0;
  }

  public void setLevel0(Boolean level0) {
    this.level0 = level0;
  }

  public Collection title(String title) {
    this.title = title;
    return this;
  }

   /**
   * Get title
   * @return title
  **/
  @ApiModelProperty(required = true, value = "")
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Collection description(String description) {
    this.description = description;
    return this;
  }

   /**
   * Get description
   * @return description
  **/
  @ApiModelProperty(value = "")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Collection type(String type) {
    this.type = type;
    return this;
  }

   /**
   * Get type
   * @return type
  **/
  @ApiModelProperty(required = true, value = "")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Collection viewtype(String viewtype) {
    this.viewtype = viewtype;
    return this;
  }

   /**
   * Get viewtype
   * @return viewtype
  **/
  @ApiModelProperty(required = true, value = "")
  public String getViewtype() {
    return viewtype;
  }

  public void setViewtype(String viewtype) {
    this.viewtype = viewtype;
  }

  public Collection orderMode(String orderMode) {
    this.orderMode = orderMode;
    return this;
  }

   /**
   * Get orderMode
   * @return orderMode
  **/
  @ApiModelProperty(value = "")
  public String getOrderMode() {
    return orderMode;
  }

  public void setOrderMode(String orderMode) {
    this.orderMode = orderMode;
  }

  public Collection x(Integer x) {
    this.x = x;
    return this;
  }

   /**
   * Get x
   * @return x
  **/
  @ApiModelProperty(value = "")
  public Integer getX() {
    return x;
  }

  public void setX(Integer x) {
    this.x = x;
  }

  public Collection y(Integer y) {
    this.y = y;
    return this;
  }

   /**
   * Get y
   * @return y
  **/
  @ApiModelProperty(value = "")
  public Integer getY() {
    return y;
  }

  public void setY(Integer y) {
    this.y = y;
  }

  public Collection z(Integer z) {
    this.z = z;
    return this;
  }

   /**
   * Get z
   * @return z
  **/
  @ApiModelProperty(value = "")
  public Integer getZ() {
    return z;
  }

  public void setZ(Integer z) {
    this.z = z;
  }

  public Collection color(String color) {
    this.color = color;
    return this;
  }

   /**
   * Get color
   * @return color
  **/
  @ApiModelProperty(value = "")
  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public Collection fromUser(Boolean fromUser) {
    this.fromUser = fromUser;
    return this;
  }

   /**
   * false
   * @return fromUser
  **/
  @ApiModelProperty(required = true, value = "false")
  public Boolean isFromUser() {
    return fromUser;
  }

  public void setFromUser(Boolean fromUser) {
    this.fromUser = fromUser;
  }

  public Collection pinned(Boolean pinned) {
    this.pinned = pinned;
    return this;
  }

   /**
   * Get pinned
   * @return pinned
  **/
  @ApiModelProperty(value = "")
  public Boolean isPinned() {
    return pinned;
  }

  public void setPinned(Boolean pinned) {
    this.pinned = pinned;
  }

  public Collection childCollectionsCount(Integer childCollectionsCount) {
    this.childCollectionsCount = childCollectionsCount;
    return this;
  }

   /**
   * Get childCollectionsCount
   * @return childCollectionsCount
  **/
  @ApiModelProperty(value = "")
  public Integer getChildCollectionsCount() {
    return childCollectionsCount;
  }

  public void setChildCollectionsCount(Integer childCollectionsCount) {
    this.childCollectionsCount = childCollectionsCount;
  }

  public Collection childReferencesCount(Integer childReferencesCount) {
    this.childReferencesCount = childReferencesCount;
    return this;
  }

   /**
   * Get childReferencesCount
   * @return childReferencesCount
  **/
  @ApiModelProperty(value = "")
  public Integer getChildReferencesCount() {
    return childReferencesCount;
  }

  public void setChildReferencesCount(Integer childReferencesCount) {
    this.childReferencesCount = childReferencesCount;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Collection collection = (Collection) o;
    return Objects.equals(this.scope, collection.scope) &&
        Objects.equals(this.authorFreetext, collection.authorFreetext) &&
        Objects.equals(this.level0, collection.level0) &&
        Objects.equals(this.title, collection.title) &&
        Objects.equals(this.description, collection.description) &&
        Objects.equals(this.type, collection.type) &&
        Objects.equals(this.viewtype, collection.viewtype) &&
        Objects.equals(this.orderMode, collection.orderMode) &&
        Objects.equals(this.x, collection.x) &&
        Objects.equals(this.y, collection.y) &&
        Objects.equals(this.z, collection.z) &&
        Objects.equals(this.color, collection.color) &&
        Objects.equals(this.fromUser, collection.fromUser) &&
        Objects.equals(this.pinned, collection.pinned) &&
        Objects.equals(this.childCollectionsCount, collection.childCollectionsCount) &&
        Objects.equals(this.childReferencesCount, collection.childReferencesCount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scope, authorFreetext, level0, title, description, type, viewtype, orderMode, x, y, z, color, fromUser, pinned, childCollectionsCount, childReferencesCount);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Collection {\n");
    
    sb.append("    scope: ").append(toIndentedString(scope)).append("\n");
    sb.append("    authorFreetext: ").append(toIndentedString(authorFreetext)).append("\n");
    sb.append("    level0: ").append(toIndentedString(level0)).append("\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    viewtype: ").append(toIndentedString(viewtype)).append("\n");
    sb.append("    orderMode: ").append(toIndentedString(orderMode)).append("\n");
    sb.append("    x: ").append(toIndentedString(x)).append("\n");
    sb.append("    y: ").append(toIndentedString(y)).append("\n");
    sb.append("    z: ").append(toIndentedString(z)).append("\n");
    sb.append("    color: ").append(toIndentedString(color)).append("\n");
    sb.append("    fromUser: ").append(toIndentedString(fromUser)).append("\n");
    sb.append("    pinned: ").append(toIndentedString(pinned)).append("\n");
    sb.append("    childCollectionsCount: ").append(toIndentedString(childCollectionsCount)).append("\n");
    sb.append("    childReferencesCount: ").append(toIndentedString(childReferencesCount)).append("\n");
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

