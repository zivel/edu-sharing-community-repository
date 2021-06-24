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
import io.swagger.client.model.Language;
import io.swagger.client.model.Values;
import java.io.IOException;

/**
 * Config
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-09T17:32:21.273+02:00")
public class Config {
  @SerializedName("current")
  private Values current = null;

  @SerializedName("global")
  private Values global = null;

  @SerializedName("language")
  private Language language = null;

  public Config current(Values current) {
    this.current = current;
    return this;
  }

   /**
   * Get current
   * @return current
  **/
  @ApiModelProperty(value = "")
  public Values getCurrent() {
    return current;
  }

  public void setCurrent(Values current) {
    this.current = current;
  }

  public Config global(Values global) {
    this.global = global;
    return this;
  }

   /**
   * Get global
   * @return global
  **/
  @ApiModelProperty(value = "")
  public Values getGlobal() {
    return global;
  }

  public void setGlobal(Values global) {
    this.global = global;
  }

  public Config language(Language language) {
    this.language = language;
    return this;
  }

   /**
   * Get language
   * @return language
  **/
  @ApiModelProperty(value = "")
  public Language getLanguage() {
    return language;
  }

  public void setLanguage(Language language) {
    this.language = language;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Config config = (Config) o;
    return Objects.equals(this.current, config.current) &&
        Objects.equals(this.global, config.global) &&
        Objects.equals(this.language, config.language);
  }

  @Override
  public int hashCode() {
    return Objects.hash(current, global, language);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Config {\n");
    
    sb.append("    current: ").append(toIndentedString(current)).append("\n");
    sb.append("    global: ").append(toIndentedString(global)).append("\n");
    sb.append("    language: ").append(toIndentedString(language)).append("\n");
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

