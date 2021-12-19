package org.one.workflow.api.serde;

public interface Serde {

	Serializer serializer();
	
	Deserializer deserializer();
	
}
