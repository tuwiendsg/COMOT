package at.ac.tuwien.dsg.comot.cs.mapper.orika;

import javax.xml.namespace.QName;

import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import at.ac.tuwien.dsg.comot.common.model.type.ArtifactType;
import at.ac.tuwien.dsg.comot.common.model.type.CapabilityType;
import at.ac.tuwien.dsg.comot.common.model.type.DirectiveType;
import at.ac.tuwien.dsg.comot.common.model.type.RelationshipType;
import at.ac.tuwien.dsg.comot.common.model.type.RequirementType;
import at.ac.tuwien.dsg.comot.common.model.type.ServiceUnitType;

public class ToscaConverters {

	public static final String NS_SALSA = "https://github.com/tuwiendsg/SALSA";
	public static final String PREFIX_SALSA = "salsa";

	public static class CapabilityTypeConverter extends BidirectionalConverter<CapabilityType, QName> {

		@Override
		public QName convertTo(CapabilityType source, Type<QName> destinationType) {
			return new QName(NS_SALSA, source.toString(), PREFIX_SALSA);
		}

		@Override
		public CapabilityType convertFrom(QName source, Type<CapabilityType> destinationType) {
			return CapabilityType.fromString(source.getLocalPart());
		}
	}

	public static class ServiceUnitTypeConverter extends BidirectionalConverter<ServiceUnitType, QName> {

		@Override
		public QName convertTo(ServiceUnitType source, Type<QName> destinationType) {
			return new QName(NS_SALSA, source.toString(), PREFIX_SALSA);
		}

		@Override
		public ServiceUnitType convertFrom(QName source, Type<ServiceUnitType> destinationType) {
			return ServiceUnitType.fromString(source.getLocalPart());
		}
	}

	public static class DirectiveTypeConverter extends BidirectionalConverter<DirectiveType, QName> {

		@Override
		public QName convertTo(DirectiveType source, Type<QName> destinationType) {
			return new QName(NS_SALSA, source.toString(), PREFIX_SALSA);
		}

		@Override
		public DirectiveType convertFrom(QName source, Type<DirectiveType> destinationType) {
			return DirectiveType.fromString(source.getLocalPart());
		}
	}

	public static class ArtifactTypeConverter extends BidirectionalConverter<ArtifactType, QName> {

		@Override
		public QName convertTo(ArtifactType source, Type<QName> destinationType) {
			return new QName(NS_SALSA, source.toString(), PREFIX_SALSA);
		}

		@Override
		public ArtifactType convertFrom(QName source, Type<ArtifactType> destinationType) {
			return ArtifactType.fromString(source.getLocalPart());
		}
	}

	public static class RequirementTypeConverter extends BidirectionalConverter<RequirementType, QName> {

		@Override
		public QName convertTo(RequirementType source, Type<QName> destinationType) {
			return new QName(NS_SALSA, source.toString(), PREFIX_SALSA);
		}

		@Override
		public RequirementType convertFrom(QName source, Type<RequirementType> destinationType) {
			return RequirementType.fromString(source.getLocalPart());
		}
	}

	public static class RelationshipTypeConverter extends BidirectionalConverter<RelationshipType, QName> {

		@Override
		public QName convertTo(RelationshipType source, Type<QName> destinationType) {
			return new QName(NS_SALSA, source.toString(), PREFIX_SALSA);
		}

		@Override
		public RelationshipType convertFrom(QName source, Type<RelationshipType> destinationType) {
			return RelationshipType.fromString(source.getLocalPart());
		}
	}
}
