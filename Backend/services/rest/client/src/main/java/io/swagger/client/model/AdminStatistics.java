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
import io.swagger.client.model.Node;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * AdminStatistics
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-09T17:32:21.273+02:00")
public class AdminStatistics {
  @SerializedName("activeSessions")
  private Integer activeSessions = null;

  @SerializedName("numberOfPreviews")
  private Integer numberOfPreviews = null;

  @SerializedName("maxMemory")
  private Long maxMemory = null;

  @SerializedName("allocatedMemory")
  private Long allocatedMemory = null;

  @SerializedName("previewCacheSize")
  private Long previewCacheSize = null;

  @SerializedName("activeLocks")
  private List<Node> activeLocks = null;

  public AdminStatistics activeSessions(Integer activeSessions) {
    this.activeSessions = activeSessions;
    return this;
  }

   /**
   * Get activeSessions
   * @return activeSessions
  **/
  @ApiModelProperty(value = "")
  public Integer getActiveSessions() {
    return activeSessions;
  }

  public void setActiveSessions(Integer activeSessions) {
    this.activeSessions = activeSessions;
  }

  public AdminStatistics numberOfPreviews(Integer numberOfPreviews) {
    this.numberOfPreviews = numberOfPreviews;
    return this;
  }

   /**
   * Get numberOfPreviews
   * @return numberOfPreviews
  **/
  @ApiModelProperty(value = "")
  public Integer getNumberOfPreviews() {
    return numberOfPreviews;
  }

  public void setNumberOfPreviews(Integer numberOfPreviews) {
    this.numberOfPreviews = numberOfPreviews;
  }

  public AdminStatistics maxMemory(Long maxMemory) {
    this.maxMemory = maxMemory;
    return this;
  }

   /**
   * Get maxMemory
   * @return maxMemory
  **/
  @ApiModelProperty(value = "")
  public Long getMaxMemory() {
    return maxMemory;
  }

  public void setMaxMemory(Long maxMemory) {
    this.maxMemory = maxMemory;
  }

  public AdminStatistics allocatedMemory(Long allocatedMemory) {
    this.allocatedMemory = allocatedMemory;
    return this;
  }

   /**
   * Get allocatedMemory
   * @return allocatedMemory
  **/
  @ApiModelProperty(value = "")
  public Long getAllocatedMemory() {
    return allocatedMemory;
  }

  public void setAllocatedMemory(Long allocatedMemory) {
    this.allocatedMemory = allocatedMemory;
  }

  public AdminStatistics previewCacheSize(Long previewCacheSize) {
    this.previewCacheSize = previewCacheSize;
    return this;
  }

   /**
   * Get previewCacheSize
   * @return previewCacheSize
  **/
  @ApiModelProperty(value = "")
  public Long getPreviewCacheSize() {
    return previewCacheSize;
  }

  public void setPreviewCacheSize(Long previewCacheSize) {
    this.previewCacheSize = previewCacheSize;
  }

  public AdminStatistics activeLocks(List<Node> activeLocks) {
    this.activeLocks = activeLocks;
    return this;
  }

  public AdminStatistics addActiveLocksItem(Node activeLocksItem) {
    if (this.activeLocks == null) {
      this.activeLocks = new ArrayList<Node>();
    }
    this.activeLocks.add(activeLocksItem);
    return this;
  }

   /**
   * Get activeLocks
   * @return activeLocks
  **/
  @ApiModelProperty(value = "")
  public List<Node> getActiveLocks() {
    return activeLocks;
  }

  public void setActiveLocks(List<Node> activeLocks) {
    this.activeLocks = activeLocks;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AdminStatistics adminStatistics = (AdminStatistics) o;
    return Objects.equals(this.activeSessions, adminStatistics.activeSessions) &&
        Objects.equals(this.numberOfPreviews, adminStatistics.numberOfPreviews) &&
        Objects.equals(this.maxMemory, adminStatistics.maxMemory) &&
        Objects.equals(this.allocatedMemory, adminStatistics.allocatedMemory) &&
        Objects.equals(this.previewCacheSize, adminStatistics.previewCacheSize) &&
        Objects.equals(this.activeLocks, adminStatistics.activeLocks);
  }

  @Override
  public int hashCode() {
    return Objects.hash(activeSessions, numberOfPreviews, maxMemory, allocatedMemory, previewCacheSize, activeLocks);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AdminStatistics {\n");
    
    sb.append("    activeSessions: ").append(toIndentedString(activeSessions)).append("\n");
    sb.append("    numberOfPreviews: ").append(toIndentedString(numberOfPreviews)).append("\n");
    sb.append("    maxMemory: ").append(toIndentedString(maxMemory)).append("\n");
    sb.append("    allocatedMemory: ").append(toIndentedString(allocatedMemory)).append("\n");
    sb.append("    previewCacheSize: ").append(toIndentedString(previewCacheSize)).append("\n");
    sb.append("    activeLocks: ").append(toIndentedString(activeLocks)).append("\n");
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

