package config.bean;

import java.util.List;

import annotation.ListDesc;
import config.IConfParseBean;

public class Hero_union implements IConfParseBean {
	private int id;

	private String union_name;

	private int type;

	@ListDesc("EAttributeKeyValue")
	private List<EAttributeKeyValue> attr_list;

	@ListDesc("int")
	private List<Integer> actvice_condition;

	@Override
	public boolean parse() {
		return false;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUnion_name() {
		return union_name;
	}

	public void setUnion_name(String union_name) {
		this.union_name = union_name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public List<EAttributeKeyValue> getAttr_list() {
		return attr_list;
	}

	public void setAttr_list(List<EAttributeKeyValue> attr_list) {
		this.attr_list = attr_list;
	}

	public List<Integer> getActvice_condition() {
		return actvice_condition;
	}

	public void setActvice_condition(List<Integer> actvice_condition) {
		this.actvice_condition = actvice_condition;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("id");
		builder.append(":");
		builder.append(id);
		builder.append(",");
		builder.append("union_name");
		builder.append(":");
		builder.append(union_name);
		builder.append(",");
		builder.append("type");
		builder.append(":");
		builder.append(type);
		builder.append(",");
		builder.append("attr_list");
		builder.append(":");
		builder.append(attr_list);
		builder.append(",");
		builder.append("actvice_condition");
		builder.append(":");
		builder.append(actvice_condition);
		return builder.toString();
	}
}