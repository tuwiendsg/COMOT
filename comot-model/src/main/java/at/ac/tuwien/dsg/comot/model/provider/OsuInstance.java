package at.ac.tuwien.dsg.comot.model.provider;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;

import at.ac.tuwien.dsg.comot.model.HasUniqueId;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.recorder.BusinessId;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@NodeEntity
public class OsuInstance extends Entity implements HasUniqueId, Serializable {

	private static final long serialVersionUID = 4791055570341060145L;

	@Indexed(unique = true)
	@BusinessId
	@XmlID
	@XmlAttribute
	protected String id;

	protected OfferedServiceUnit osu;
	protected CloudService service;

	public OsuInstance(){
		
	}
	
	public OsuInstance(String id, OfferedServiceUnit osu) {
		super();
		this.id = id;
		this.osu = osu;
	}

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public OfferedServiceUnit getOsu() {
		return osu;
	}

	public void setOsu(OfferedServiceUnit osu) {
		this.osu = osu;
	}

	public CloudService getService() {
		return service;
	}

	public void setService(CloudService service) {
		this.service = service;
	}



}
