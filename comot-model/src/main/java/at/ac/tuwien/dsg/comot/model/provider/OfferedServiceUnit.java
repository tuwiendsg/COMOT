/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.model.provider;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import at.ac.tuwien.dsg.comot.model.HasUniqueId;
import at.ac.tuwien.dsg.comot.model.type.OsuType;
import at.ac.tuwien.dsg.comot.recorder.BusinessId;

/**
 *
 * @author hungld
 *
 *         CloudOfferedService represent a service provided by cloud system and has unique ID The sub element including
 *         Resource, Quality can be duplicated in name but the Metrics are consistent. E.g, a Flavor can have CPU, VCPU,
 *         ECPU but all have cpu_number property
 *
 *         TODO: add Configuration Capability for CloudOfferedService
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@NodeEntity
public class OfferedServiceUnit extends Entity implements HasUniqueId, Serializable {

	private static final long serialVersionUID = -7891748156074018265L;

	@Indexed(unique = true)
	@BusinessId
	@XmlID
	@XmlAttribute
	String id;

	protected String providerID;

	@XmlAttribute(name = "osuType")
	protected OsuType type;

	@Indexed
	private String category = "unknown";
	@Indexed
	private String subcategory = "unknown";

	@RelatedTo(direction = Direction.OUTGOING)
	@Fetch
	Set<Resource> resources;

	@RelatedTo(direction = Direction.OUTGOING)
	@Fetch
	Set<Quality> qualities;

	@RelatedTo(direction = Direction.OUTGOING)
	@Fetch
	Set<CostFunction> costFunctions;

	@RelatedTo(direction = Direction.OUTGOING)
	@Fetch
	@XmlElementWrapper(name = "PrimitiveOperations")
	@XmlElement(name = "Operation")
	Set<PrimitiveOperation> primitiveOperations;

	{
		// id = UUID.randomUUID().toString();
		resources = new HashSet<>();
		qualities = new HashSet<>();
		costFunctions = new HashSet<>();
		primitiveOperations = new HashSet<>();
	}

	public OfferedServiceUnit() {
	}

	public OfferedServiceUnit(String name, String providerID) {
		super(name);
		this.providerID = providerID;
	}

	public OfferedServiceUnit(String name, String providerID, String category, String subCategory) {
		super(name);
		this.providerID = providerID;
		this.category = category;
		this.subcategory = subCategory;
		this.id = providerID + "." + name;
		System.out.println("Constructing OSU. add to provider: " + this.providerID);
	}

	public OfferedServiceUnit hasResource(Resource resource) {
		this.resources.add(resource);
		resource.setId(this.getId() + "." + resource.getName());
		return this;
	}

	public OfferedServiceUnit hasQuality(Quality quality) {
		this.qualities.add(quality);
		return this;
	}

	public OfferedServiceUnit hasCostFunction(CostFunction costFunction) {
		this.costFunctions.add(costFunction);
		return this;
	}

	public OfferedServiceUnit hasPrimitiveOperation(PrimitiveOperation primitive) {
		this.primitiveOperations.add(primitive);
		return this;
	}

	public String getProviderID() {
		return providerID;
	}

	public String getCategory() {
		return category;
	}

	public String getSubcategory() {
		return subcategory;
	}

	public Set<Resource> getResources() {
		return resources;
	}

	public Set<Quality> getQualities() {
		return qualities;
	}

	public Set<CostFunction> getCostFunctions() {
		return costFunctions;
	}

	public Set<PrimitiveOperation> getPrimitiveOperations() {
		return primitiveOperations;
	}

	public OsuType getType() {
		return type;
	}

	public void setType(OsuType type) {
		this.type = type;
	}

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setProviderID(String providerID) {
		this.providerID = providerID;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setSubcategory(String subcategory) {
		this.subcategory = subcategory;
	}

	public void setResources(Set<Resource> resources) {
		this.resources = resources;
	}

	public void setQualities(Set<Quality> qualities) {
		this.qualities = qualities;
	}

	public void setCostFunctions(Set<CostFunction> costFunctions) {
		this.costFunctions = costFunctions;
	}

	public void setPrimitiveOperations(Set<PrimitiveOperation> primitiveOperations) {
		this.primitiveOperations = primitiveOperations;
	}
	
	

}
