package org.edu_sharing.restservices.mds.v1.model;

import java.util.ArrayList;
import java.util.List;

import org.edu_sharing.metadataset.v2.MetadataKey;
import org.edu_sharing.metadataset.v2.MetadataReaderV2;
import org.edu_sharing.metadataset.v2.MetadataWidget;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
@ApiModel(description = "")
public class WidgetV2 {
		private String id,caption,bottomCaption,icon,type,template,condition;
		private List<ValueV2> values;
		private String placeholder;
		private String unit;
		private Integer min;
		private Integer max;
		private Integer defaultMin;
		private Integer defaultMax;
		private Integer step;
		private boolean isExtended;
		private boolean isRequired;
		private boolean allowempty;
		private String defaultvalue;
		
		public WidgetV2(){}
		public WidgetV2(MetadataWidget widget) {
			this.id=widget.getId();		
			this.caption=widget.getCaption();
			this.bottomCaption=widget.getBottomCaption();
			this.icon=widget.getIcon();
			this.type=widget.getType();
			this.defaultvalue=widget.getDefaultvalue();
			this.placeholder=widget.getPlaceholder();
			this.unit=widget.getUnit();
			this.min=widget.getMin();
			this.max=widget.getMax();
			this.defaultMin=widget.getDefaultMin();
			this.defaultMax=widget.getDefaultMax();
			this.step=widget.getStep();
			this.template=widget.getTemplate();
			this.isExtended=widget.isExtended();
			this.isRequired=widget.isRequired();
			this.allowempty=widget.isAllowempty();
			this.condition=widget.getCondition();
			if(widget.getValues()!=null && widget.isValuespaceClient()){
				values=new ArrayList<ValueV2>();
				for(MetadataKey key : widget.getValues()){
					values.add(new ValueV2(key));
				}
			}
			
		}
		@JsonProperty("condition")
		public String getCondition() {
			return condition;
		}
		public void setCondition(String condition) {
			this.condition = condition;
		}
		@JsonProperty("bottomCaption")
		public String getBottomCaption() {
			return bottomCaption;
		}
		public void setBottomCaption(String bottomCaption) {
			this.bottomCaption = bottomCaption;
		}
		@JsonProperty("defaultvalue")
		public String getDefaultvalue() {
			return defaultvalue;
		}
		public void setDefaultvalue(String defaultvalue) {
			this.defaultvalue = defaultvalue;
		}
		@JsonProperty("template")
		public String getTemplate() {
			return template;
		}
		public void setTemplate(String template) {
			this.template = template;
		}
		@JsonProperty("icon")
		public String getIcon() {
			return icon;
		}
		public void setIcon(String icon) {
			this.icon = icon;
		}
		@JsonProperty("placeholder")
		public String getPlaceholder() {
			return placeholder;
		}
		public void setPlaceholder(String placeholder) {
			this.placeholder = placeholder;
		}
		@JsonProperty("min")
		public Integer getMin() {
			return min;
		}
		public void setMin(Integer min) {
			this.min = min;
		}
		@JsonProperty("max")
		public Integer getMax() {
			return max;
		}
		public void setMax(Integer max) {
			this.max = max;
		}
		@JsonProperty("defaultMin")
		public Integer getDefaultMin() {
			return defaultMin;
		}
		public void setDefaultMin(Integer defaultMin) {
			this.defaultMin = defaultMin;
		}
		@JsonProperty("defaultMax")
		public Integer getDefaultMax() {
			return defaultMax;
		}
		public void setDefaultMax(Integer defaultMax) {
			this.defaultMax = defaultMax;
		}
		@JsonProperty("step")
		public Integer getStep() {
			return step;
		}
		public void setStep(Integer step) {
			this.step = step;
		}
		@JsonProperty("isExtended")
		public boolean isExtended() {
			return isExtended;
		}
		public void setExtended(boolean isExtended) {
			this.isExtended = isExtended;
		}
		@JsonProperty("allowempty")
		public boolean isAllowempty() {
			return allowempty;
		}
		public void setAllowempty(boolean allowempty) {
			this.allowempty = allowempty;
		}
		@JsonProperty("isRequired")
		public boolean isRequired() {
			return isRequired;
		}
		public void setRequired(boolean isRequired) {
			this.isRequired = isRequired;
		}
		@JsonProperty("values")
		public List<ValueV2> getValues() {
			return values;
		}
		public void setValues(List<ValueV2> values) {
			this.values = values;
		}
		@JsonProperty("caption")
		public String getCaption() {
			return caption;
		}
		public void setCaption(String caption) {
			this.caption = caption;
		}
		@JsonProperty("type")
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		@JsonProperty("id")
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		@JsonProperty("unit")
		public String getUnit() {
			return unit;
		}
		public void setUnit(String unit) {
			this.unit = unit;
		}
		
		
	}

