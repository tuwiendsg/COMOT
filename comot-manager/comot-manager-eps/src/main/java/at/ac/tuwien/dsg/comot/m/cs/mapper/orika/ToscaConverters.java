package at.ac.tuwien.dsg.comot.m.cs.mapper.orika;

import javax.xml.namespace.QName;

import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import at.ac.tuwien.dsg.comot.model.type.DirectiveType;
import at.ac.tuwien.dsg.comot.model.type.RelationshipType;
import at.ac.tuwien.dsg.comot.model.type.ResourceType;

public class ToscaConverters {

	public static final String NS_SALSA = "https://github.com/tuwiendsg/SALSA";
	public static final String PREFIX_SALSA = "salsa";

	public static QName toSalsaQName(String name) {
		return new QName(NS_SALSA, name, PREFIX_SALSA);
	}

	public static class NodeTypeConverter extends BidirectionalConverter<String, QName> {

		@Override
		public QName convertTo(String source, Type<QName> destinationType) {
			return toSalsaQName(source.toString());
		}

		@Override
		public String convertFrom(QName source, Type<String> destinationType) {
			return source.getLocalPart();
		}
	}

	public static class DirectiveTypeConverter extends BidirectionalConverter<DirectiveType, QName> {

		@Override
		public QName convertTo(DirectiveType source, Type<QName> destinationType) {
			return toSalsaQName(source.toString());
		}

		@Override
		public DirectiveType convertFrom(QName source, Type<DirectiveType> destinationType) {
			return DirectiveType.fromString(source.getLocalPart());
		}
	}

	public static class ArtifactTypeConverter extends BidirectionalConverter<ResourceType, QName> {

		@Override
		public QName convertTo(ResourceType source, Type<QName> destinationType) {
			return toSalsaQName(source.toString());
		}

		@Override
		public ResourceType convertFrom(QName source, Type<ResourceType> destinationType) {
			return ResourceType.fromString(source.getLocalPart());
		}
	}

	public static class RelationshipTypeConverter extends BidirectionalConverter<RelationshipType, QName> {

		@Override
		public QName convertTo(RelationshipType source, Type<QName> destinationType) {
			return toSalsaQName(source.toString());
		}

		@Override
		public RelationshipType convertFrom(QName source, Type<RelationshipType> destinationType) {
			return RelationshipType.fromString(source.getLocalPart());
		}
	}
}
