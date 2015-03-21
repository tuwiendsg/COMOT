package at.ac.tuwien.dsg.comot.m.common.events;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlSeeAlso({ StateMessage.class, ExceptionMessage.class })
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public abstract class ComotMessage implements Serializable {

	private static final long serialVersionUID = -8260201306788411309L;

}
