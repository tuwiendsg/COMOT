package at.ac.tuwien.dsg.comot.m.common.event.state;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlSeeAlso({ StateMessage.class, ExceptionMessage.class, ExceptionMessageLifeCycle.class })
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ComotMessage implements Serializable {

	private static final long serialVersionUID = -8260201306788411309L;

}
