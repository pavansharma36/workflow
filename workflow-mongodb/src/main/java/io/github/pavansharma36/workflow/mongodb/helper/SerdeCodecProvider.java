package io.github.pavansharma36.workflow.mongodb.helper;

import io.github.pavansharma36.workflow.api.model.ManagerInfo;
import io.github.pavansharma36.workflow.api.serde.Serde;
import java.util.HashMap;
import java.util.Map;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistry;

public class SerdeCodecProvider implements CodecRegistry {

  private final CodecRegistry delegation;
  private final Map<Class<?>, Codec<?>> codecs;

  public SerdeCodecProvider(CodecRegistry delegation, Serde serde) {
    this.delegation = delegation;
    this.codecs = new HashMap<>();
    codecs.put(ManagerInfo.class, new SerdeCodec<>(serde, delegation,
        ManagerInfo.class));
  }

  @Override
  public <T> Codec<T> get(Class<T> aClass, CodecRegistry codecRegistry) {
    if (codecs.containsKey(aClass)) {
      return (Codec<T>) codecs.get(aClass);
    } else {
      return codecRegistry.get(aClass);
    }
  }

  @Override
  public <T> Codec<T> get(Class<T> aClass) {
    return get(aClass, delegation);
  }
}
